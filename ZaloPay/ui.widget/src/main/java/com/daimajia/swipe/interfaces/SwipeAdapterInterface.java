package com.daimajia.swipe.interfaces;

public interface SwipeAdapterInterface {

    int getSwipeLayoutResourceId(int position);

    void notifyDataSetChanged();

    void notifyItemChanged(int position);
}
