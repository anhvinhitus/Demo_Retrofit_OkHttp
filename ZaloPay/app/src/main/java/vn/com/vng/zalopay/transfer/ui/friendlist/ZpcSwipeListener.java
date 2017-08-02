package vn.com.vng.zalopay.transfer.ui.friendlist;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;

/**
 * Created by hieuvm on 8/2/17.
 * *
 */

public class ZpcSwipeListener extends SimpleSwipeListener {

    @Override
    public void onStartOpen(SwipeLayout layout) {
        layout.setSelected(true);
    }

    @Override
    public void onStartClose(SwipeLayout layout) {
        layout.setSelected(false);
    }

    @Override
    public void onClose(SwipeLayout layout) {
        layout.setSelected(false);
    }
}
