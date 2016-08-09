package com.zalopay.ui.widget.recyclerview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by AnhHieu on 9/29/15.
 */

public abstract class EndlessRecyclerViewListener extends RecyclerView.OnScrollListener {

    protected int visibleThreshold = 5;
    // The current offset index of data you have loaded
    protected int currentPage = 0;
    // The total number of items in the dataset after the last load
    protected int previousTotalItemCount = 0;
    // True if we are still waiting for the last set of data to load.
    protected boolean loading = true;
    // Sets the starting page index
    protected int startingPageIndex = 0;

    protected int MIN_TOTALCOUNT = 0;

    protected LAYOUT_MANAGER_TYPE layoutManagerType;
    protected int[] lastPositions;

    public EndlessRecyclerViewListener() {
    }

    public EndlessRecyclerViewListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    public EndlessRecyclerViewListener(int visibleThreshold, int startPage) {
        this.visibleThreshold = visibleThreshold;
        this.startingPageIndex = startPage;
        this.currentPage = startPage;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        switch (newState) {
            case RecyclerView.SCROLL_STATE_DRAGGING:
                //  Glide.with(CApplication.Instance()).pauseRequests();
                break;

            case RecyclerView.SCROLL_STATE_IDLE:
                // Glide.with(CApplication.Instance()).resumeRequests();
                break;
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        loadMoreItemsIfNecessary(recyclerView);
    }

    protected void loadMoreItemsIfNecessary(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();

        int lastVisibleItemPosition = -1;
        if (layoutManagerType == null) {
            if (layoutManager instanceof LinearLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.LINEAR;
            } else if (layoutManager instanceof GridLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.GRID;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.STAGGERED_GRID;
            } else {
                throw new RuntimeException("Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
            }
        }

        switch (layoutManagerType) {
            case LINEAR:
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

                break;
            case GRID:
                GridLayoutManager gridLayoutManager = ((GridLayoutManager) layoutManager);
                lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition();
                if (gridLayoutManager.findFirstVisibleItemPosition() == 0) {
                    return;
                }
                break;
            case STAGGERED_GRID:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                if (lastPositions == null)
                    lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];

                staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                lastVisibleItemPosition = findMax(lastPositions);
                break;
        }

        if (MIN_TOTALCOUNT > totalItemCount)
            return;
        if (isPause)
            return;

        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            } else {
                loading = false;
                return;
            }
        }

        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
            currentPage++;
        }

        if (((totalItemCount - lastVisibleItemPosition) <= visibleThreshold ||
                (totalItemCount - lastVisibleItemPosition) == 0 && totalItemCount > visibleItemCount)
                && !loading) {

            loading = true;
            onLoadMore(currentPage + 1, totalItemCount);
        }
    }


    protected boolean isPause = false;

    public abstract void onLoadMore(int page, int totalItemsCount);


    public void onPause() {
        isPause = true;
    }

    public void onResume() {
        isPause = false;
    }

    public void setStartingPageIndex(int startingPageIndex) {
        this.startingPageIndex = startingPageIndex;
    }

    protected int findMax(int[] lastPositions) {
        int max = Integer.MIN_VALUE;
        for (int value : lastPositions) {
            if (value > max)
                max = value;
        }
        return max;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public enum LAYOUT_MANAGER_TYPE {
        LINEAR,
        GRID,
        STAGGERED_GRID
    }
}