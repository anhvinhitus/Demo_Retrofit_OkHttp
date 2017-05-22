/*
 * Copyright 2016 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.recyclerview.swipe;

import android.view.View;
import android.widget.OverScroller;

import com.zalopay.ui.widget.R;

import timber.log.Timber;

/**
 * Created by Yan Zhenjie on 2016/7/22.
 */
class SwipeRightHorizontal extends SwipeHorizontal {

    private ISwipeRightMenuListener mSwipeRightMenuListener;

    public SwipeRightHorizontal(View menuView) {
        super(SwipeMenuRecyclerView.RIGHT_DIRECTION, menuView);
    }

    @Override
    public boolean isCompleteClose(int scrollX) {
        int i = -getWidth() * getDirection();
        return scrollX == 0 && i != 0;
    }

    @Override
    public boolean isMenuOpen(int scrollX) {
        int i = -getWidth() * getDirection();
        return scrollX >= i && i != 0;
    }

    @Override
    public boolean isMenuOpenNotEqual(int scrollX) {
        return scrollX > -getWidth() * getDirection();
    }

    @Override
    public void autoOpenMenu(OverScroller scroller, int scrollX, int duration) {
        scroller.startScroll(Math.abs(scrollX), 0, getWidth() - Math.abs(scrollX), 0, duration);
    }

    @Override
    public void autoCloseMenu(OverScroller scroller, int scrollX, int duration) {
        scroller.startScroll(-Math.abs(scrollX), 0, Math.abs(scrollX), 0, duration);
    }

    @Override
    public Checker checkXY(int x, int y) {
        float alpha = (float) x / getWidth();
        menuView.setAlpha(alpha);
        mChecker.x = x;
        mChecker.y = y;
        mChecker.shouldResetSwipe = mChecker.x == 0;
        if (mChecker.x < 0) {
            mChecker.x = 0;
        }
        if (mChecker.x > getWidth()) {
            mChecker.x = getWidth();
        }
        if (mSwipeRightMenuListener != null) {
            if (alpha <= 0.1) {
                mSwipeRightMenuListener.onHideRightMenu();
            } else {
                mSwipeRightMenuListener.onShowRightMenu();
            }
        }
        return mChecker;
    }

    @Override
    public boolean isClickOnContentView(int contentViewWidth, float x) {
        return x < (contentViewWidth - getWidth());
    }

    private int getWidth() {
        int offset = (int) menuView.getContext().getResources().getDimension(R.dimen.swipe_menu_margin_offset);
        return getMenuView().getWidth() - offset;
    }

    public void setSwipeRightMenuListener(ISwipeRightMenuListener listener) {
        mSwipeRightMenuListener = listener;
    }

    interface ISwipeRightMenuListener {
        void onShowRightMenu();

        void onHideRightMenu();
    }
}
