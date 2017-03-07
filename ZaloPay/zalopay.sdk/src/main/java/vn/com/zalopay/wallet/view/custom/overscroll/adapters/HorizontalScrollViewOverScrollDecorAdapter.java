package vn.com.zalopay.wallet.view.custom.overscroll.adapters;

import android.view.View;
import android.widget.HorizontalScrollView;

import vn.com.zalopay.wallet.view.custom.overscroll.HorizontalOverScrollBounceEffectDecorator;
import vn.com.zalopay.wallet.view.custom.overscroll.VerticalOverScrollBounceEffectDecorator;

/**
 * An adapter that enables over-scrolling support over a {@link HorizontalScrollView}.
 * <br/>Seeing that {@link HorizontalScrollView} only supports horizontal scrolling, this adapter
 * should only be used with a {@link HorizontalOverScrollBounceEffectDecorator}.
 *
 * @author amit
 * @see HorizontalOverScrollBounceEffectDecorator
 * @see VerticalOverScrollBounceEffectDecorator
 */
public class HorizontalScrollViewOverScrollDecorAdapter implements vn.com.zalopay.wallet.view.custom.overscroll.adapters.IOverScrollDecoratorAdapter {

    protected final HorizontalScrollView mView;

    public HorizontalScrollViewOverScrollDecorAdapter(HorizontalScrollView view) {
        mView = view;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public boolean isInAbsoluteStart() {
        return !mView.canScrollHorizontally(-1);
    }

    @Override
    public boolean isInAbsoluteEnd() {
        return !mView.canScrollHorizontally(1);
    }
}
