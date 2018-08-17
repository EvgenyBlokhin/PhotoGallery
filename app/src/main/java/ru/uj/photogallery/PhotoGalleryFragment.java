package ru.uj.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Blokhin Evgeny on 07.08.2018.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private boolean mLoading = true;
    private int mPageCount = 1;
    private int mSpanCount = 3;

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private PhotoAdapter mPhotoAdapter = new PhotoAdapter(mItems);


    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

//        GridAutofitLayoutManager layoutManager = new GridAutofitLayoutManager(getActivity(), 220);
        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
//        mPhotoRecyclerView.setLayoutManager(layoutManager);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mSpanCount));
        setupAdapter();
        setupScrollListener();
        final ViewTreeObserver observer = mPhotoRecyclerView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RecyclerView.LayoutManager lm = mPhotoRecyclerView.getLayoutManager();
                int totalSpace;
                if (lm.getPaddingBottom() == LinearLayoutManager.VERTICAL) {
                    totalSpace = lm.getWidth() - lm.getPaddingRight() - lm.getPaddingLeft();
                } else {
                    totalSpace = lm.getHeight() - lm.getPaddingTop() - lm.getPaddingBottom();
                }
                int spanCount = Math.max(1, totalSpace / 220);
                ((GridLayoutManager) mPhotoRecyclerView.getLayoutManager()).setSpanCount(spanCount);
//                mPhotoRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                mPhotoRecyclerWidth = mPhotoRecyclerView.getWidth();
//                mPhotoRecyclerView.getLayoutManager().getColumnCountForAccessibility(mPhotoRecyclerView,);
//                ((GridLayoutManager) mPhotoRecyclerView.getLayoutManager()).setSpanCount(mSpanCount);
            }
        });
        return v;
    }

//    public static class GridAutofitLayoutManager extends GridLayoutManager {
//        private int mColumnWidth;
//        private boolean mColumnWidthChanged = true;
//        public GridAutofitLayoutManager(Context context, int columnWidth) {
//            super(context, 1);
//            setColumnWidth(checkedColumnWidth(context, columnWidth));
//        }
//        public GridAutofitLayoutManager(Context context, int columnWidth, int orientation, boolean reverseLayout) { /* Initially set spanCount to 1, will be changed automatically later. */
//            super(context, 1, orientation, reverseLayout);
//            setColumnWidth(checkedColumnWidth(context, columnWidth));
//        }
//        private int checkedColumnWidth(Context context, int columnWidth) {
//            if (columnWidth <= 0) { /* Set default columnWidth value (48dp here). It is better to move this constant to static constant on top, but we need context to convert it to dp, so can't really do so. */
//                columnWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 220, context.getResources().getDisplayMetrics());
//            }
//            return columnWidth;
//        }
//        public void setColumnWidth(int newColumnWidth) {
//            if (newColumnWidth > 0 && newColumnWidth != mColumnWidth) {
//                mColumnWidth = newColumnWidth;
//                mColumnWidthChanged = true;
//            }
//        }
//        @Override public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
//            if (mColumnWidthChanged && mColumnWidth > 0) {
//                int totalSpace;
//                if (getOrientation() == VERTICAL) {
//                    totalSpace = getWidth() - getPaddingRight() - getPaddingLeft();
//                } else {
//                    totalSpace = getHeight() - getPaddingTop() - getPaddingBottom();
//                }
//                int spanCount = Math.max(1, totalSpace / mColumnWidth);
//                setSpanCount(spanCount);
//                mColumnWidthChanged = false;
//            }
//            super.onLayoutChildren(recycler, state);
//        }
//    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        }
    }

    private void setupScrollListener() {
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mLoading) {
                    if (dy > 0)
                    {
                        int visibleItemCount = mPhotoRecyclerView.getLayoutManager().getChildCount();
                        int totalItemCount = mPhotoRecyclerView.getLayoutManager().getItemCount();
                        int pastVisiblesItems = ((LinearLayoutManager) mPhotoRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            mLoading = false;
                            mPageCount++;
                            new FetchItemsTask().execute();
                        }

                    }
                }
            }
        });
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItem item) {
            mTitleTextView.setText(item.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        public void appendItems(List<GalleryItem> items) {
            mGalleryItems.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            photoHolder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
           return new FlickrFetchr().fetchItems(mPageCount);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mPhotoAdapter.appendItems(items);
            mLoading = true;
        }
    }
}
