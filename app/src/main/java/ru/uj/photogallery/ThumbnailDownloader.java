package ru.uj.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Blokhin Evgeny on 20.08.2018.
 */
public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MASSAGE_DOWNLOAD = 0;
    private static final int PREVENT_DOWNLOAD = 1;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private LruCache mLruCache = new LruCache(100);
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MASSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    handleRequest(target);
                }
                else if (msg.what == PREVENT_DOWNLOAD) {
                    loadPrevent((String) msg.obj);
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {
        if (url == null) {
            mRequestMap.remove(target);
            Log.i("HUI", "QueueThumbnail - URL of image IS NULL");
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MASSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MASSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);

            if (url == null) {
                Log.i("HUI", "URL of image IS NULL");
                return;
            }

            synchronized (mLruCache) {
                if (mLruCache.get(url) == null) {
                    byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                    mLruCache.put(url, bitmap);
                    Log.i("HUI", "!!!ЗАГРУЗКА ИЗ СЕТИ: " + url);
                }
                else {
                    Log.i("HUI", "Загрузка из кэша: " + url);
                }
            }
            Log.i(TAG, "Bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url || mHasQuit) {
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target,(Bitmap) mLruCache.get(url));

                }
            });

        } catch (IOException ioe) {
            Log.i(TAG, "Error downloading image", ioe);
        }
    }

    private void loadPrevent(String url) {
        if (mLruCache.get(url) == null) {
            byte[] bitmapBytes = new byte[0];
            try {
                bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            } catch (IOException e) {
                Log.e("HUI", e.toString());
            }
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            mLruCache.put(url, bitmap);
        }
    }

    public void preventFetch(final String url) {
        mRequestHandler.obtainMessage(PREVENT_DOWNLOAD, url).sendToTarget();
    }
}
