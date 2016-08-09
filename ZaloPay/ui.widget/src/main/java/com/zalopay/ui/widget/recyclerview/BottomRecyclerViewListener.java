package com.zalopay.ui.widget.recyclerview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by trung on 01/02/2016.
 */
public abstract class BottomRecyclerViewListener extends EndlessRecyclerViewListener {

    @Override
    protected void loadMoreItemsIfNecessary(RecyclerView recyclerView) {
//        super.loadMoreItemsIfNecessary(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();

        int firstVisibleItemPosition = -1;
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
                firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

                break;
            case GRID:
                GridLayoutManager gridLayoutManager = ((GridLayoutManager) layoutManager);
                lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition();
                firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition();
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

        onScrollTo(lastVisibleItemPosition, totalItemCount);

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

        if ((firstVisibleItemPosition <= 0  && totalItemCount > visibleItemCount)
                && !loading) {

            loading = true;
            onLoadMore(currentPage + 1, totalItemCount);
        }
    }

    public abstract void onScrollTo(int lastPosition, int totalItemsCount);
}
