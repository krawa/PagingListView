package com.paging.listview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.List;


public class PagingListView extends ListView {


    private static final int PAGING_BOTTOM = 0;
    private static final int PAGING_TOP = 1;

    private boolean isLoading;
    private boolean hasMoreItems;
    private Pagingable pagingableListener;
    private LoadingView loadinView;
    private int mPagingPos;
    private OnScroolEventListener onScroolEventListener;
    private boolean isTouchScrool;

    public PagingListView(Context context) {
        super(context);
        init();
    }

    public PagingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PagingListView,
                0, 0);

        try {
            mPagingPos = a.getInteger(R.styleable.PagingListView_pagingPosition, PAGING_BOTTOM);
        } finally {
            a.recycle();
        }

        init();
    }

    public PagingListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public boolean isLoading() {
        return this.isLoading;
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public void setPagingableListener(Pagingable pagingableListener) {
        this.pagingableListener = pagingableListener;
    }

    public void setOnScroolEventListener(OnScroolEventListener onScroolEventListener) {
        this.onScroolEventListener = onScroolEventListener;
    }

    public void setHasMoreItems(boolean hasMoreItems) {
        this.hasMoreItems = hasMoreItems;
        if(!this.hasMoreItems) {
            switch(mPagingPos){
                case PAGING_BOTTOM:
                    removeFooterView(loadinView);
                    break;
                case PAGING_TOP:
                    removeHeaderView(loadinView);
                    break;
            }
        }else{
            switch(mPagingPos){
                case PAGING_BOTTOM:
                    if(getFooterViewsCount() == 0)
                        addFooterView(loadinView);
                    break;
                case PAGING_TOP:
                    if(getHeaderViewsCount() == 0)
                        addHeaderView(loadinView);
                    break;
            }
        }
    }

    public boolean hasMoreItems() {
        return this.hasMoreItems;
    }


    public void onFinishLoading(boolean hasMoreItems, List<? extends Object> newItems) {
        setHasMoreItems(hasMoreItems);
        setIsLoading(false);
        if(newItems != null && newItems.size() > 0) {
            ListAdapter adapter = ((HeaderViewListAdapter)getAdapter()).getWrappedAdapter();
            if(adapter instanceof PagingBaseAdapter ) {
                ((PagingBaseAdapter)adapter).addMoreItems(newItems);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                isTouchScrool = true;
                break;
            case MotionEvent.ACTION_MOVE:
                isTouchScrool = true;
                break;
            case MotionEvent.ACTION_UP:
                isTouchScrool = false;
                break;
        }



        return super.onTouchEvent(ev);
    }


    private void init() {
        isLoading = false;
        loadinView = new LoadingView(getContext());

        switch(mPagingPos){
            case PAGING_BOTTOM:
                addFooterView(loadinView);
                break;
            case PAGING_TOP:
                addHeaderView(loadinView);
                break;
        }

        setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //DO NOTHING...
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount-1 < totalItemCount) {

                    int lastVisibleItem = firstVisibleItem + visibleItemCount;
                    if (!isLoading && hasMoreItems && mPagingPos == PAGING_BOTTOM
                            && lastVisibleItem == totalItemCount
                            && view.getChildAt(view.getChildCount() - 1).getBottom() <= view.getHeight()){
                        if(pagingableListener != null) {
                            isLoading = true;
                            pagingableListener.onLoadMoreItems();
                        }
                    }


                    if (!isLoading && hasMoreItems && mPagingPos == PAGING_TOP
                            && firstVisibleItem == 0
                            && view.getChildAt(0).getTop() >= 0){
                        if(pagingableListener != null) {
                            isLoading = true;
                            pagingableListener.onLoadMoreItems();
                        }
                    }

                    if(onScroolEventListener != null && isTouchScrool){
                        onDetectedListScroll(view, firstVisibleItem);
                    }
                }
            }
        });
    }


    private int oldTop;
    private int oldFirstVisibleItem;

    private void onDetectedListScroll(AbsListView absListView, int firstVisibleItem) {
        View view = absListView.getChildAt(0);
        int top = (view == null) ? 0 : view.getTop();
        if (firstVisibleItem == oldFirstVisibleItem) {
            if (top > oldTop) {
                onScroolEventListener.onUpScrolling();
            } else if (top < oldTop) {
                onScroolEventListener.onDownScrolling();
            }
        } else {
            if (firstVisibleItem < oldFirstVisibleItem) {
                onScroolEventListener.onUpScrolling();
            } else {
                if(oldFirstVisibleItem != 0){
                    onScroolEventListener.onDownScrolling();
                }

            }
        }

        oldTop = top;
        oldFirstVisibleItem = firstVisibleItem;
    }

    public interface Pagingable {
        void onLoadMoreItems();
    }

    public interface OnScroolEventListener {
        void onDownScrolling();
        void onUpScrolling();
    }


}
