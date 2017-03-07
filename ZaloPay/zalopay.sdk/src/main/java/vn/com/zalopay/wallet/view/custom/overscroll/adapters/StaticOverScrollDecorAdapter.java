package vn.com.zalopay.wallet.view.custom.overscroll.adapters;

import android.view.View;

import vn.com.zalopay.wallet.view.custom.overscroll.HorizontalOverScrollBounceEffectDecorator;
import vn.com.zalopay.wallet.view.custom.overscroll.VerticalOverScrollBounceEffectDecorator;

/**
 * A static adapter for views that are ALWAYS over-scroll-able (e.g. image view).
 *
 * @author amit
 * @see HorizontalOverScrollBounceEffectDecorator
 * @see VerticalOverScrollBounceEffectDecorator
 */
public class StaticOverScrollDecorAdapter implements vn.com.zalopay.wallet.view.custom.overscroll.adapters.IOverScrollDecoratorAdapter {

    protected final View mView;

    public StaticOverScrollDecorAdapter(View view) {
        mView = view;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public boolean isInAbsoluteStart() {
        return true;
    }

    @Override
    public boolean isInAbsoluteEnd() {
        return true;
    }
}
