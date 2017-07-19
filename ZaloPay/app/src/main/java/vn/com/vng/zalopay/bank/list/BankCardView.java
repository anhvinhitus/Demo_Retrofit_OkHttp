package vn.com.vng.zalopay.bank.list;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankCardStyle;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.FrescoUtil;

/**
 * Created by hieuvm on 7/11/17.
 * *
 */

public class BankCardView extends SwipeLayout {

    @BindView(R.id.background)
    View mBackgroundView;

    @BindView(R.id.foreground)
    View mForegroundView;

    @BindView(R.id.card)
    View mCardView;

    @BindView(R.id.iv_logo)
    SimpleDraweeView mLogoView;

    @BindView(R.id.tv_num_acc)
    TextView mNumberCardView;
    float border = 0;

    public BankCardView(Context context) {
        this(context, null);
    }

    public BankCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BankCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        border = getContext().getResources().getDimension(R.dimen.border_link_card);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void layoutPullOut() {
        View surfaceView = getSurfaceView();
        Rect surfaceRect = mViewBoundCache.get(surfaceView);
        if (surfaceRect == null) {
            surfaceRect = computeSurfaceLayoutArea(false);
        }

        if (surfaceView != null) {
            surfaceView.layout(surfaceRect.left, surfaceRect.top, surfaceRect.right, surfaceRect.bottom);
            bringChildToFront(surfaceView);
        }

        View currentBottomView = getCurrentBottomView();
        Rect bottomViewRect = mViewBoundCache.get(currentBottomView);
        if (bottomViewRect == null) {
            bottomViewRect = computeBottomLayoutAreaViaSurface(ShowMode.PullOut, surfaceRect);
        }

        if (mForegroundView != null && mCardView != null) {
            //  Timber.d("layoutPullOut: %s %s", mForegroundView.getWidth(), mForegroundView.getPaddingLeft());
            bottomViewRect.left = mForegroundView.getWidth() - mForegroundView.getPaddingLeft();
        }

        if (currentBottomView != null) {
            currentBottomView.layout(bottomViewRect.left, bottomViewRect.top, bottomViewRect.right, bottomViewRect.bottom);
        }

        Timber.d("layoutPullOut: [surfaceRect: %s bottomViewRect: %s] ", surfaceRect.toShortString(), bottomViewRect.toShortString());
    }

    void bindView(BankData data) {
        mNumberCardView.setText(Html.fromHtml(data.mBankInfo));
        setBackground(mCardView, data.mBankCardStyle);
        String logo = ResourceHelper
                .getResource(mLogoView.getContext(), BuildConfig.ZALOPAY_APP_ID, data.mBankCardStyle.bankIcon);
        FrescoUtil.loadWrapContent(mLogoView, logo);
    }

    private void setBackground(View foreground, BankCardStyle bankCardStyle) {
        Drawable drawable = foreground.getBackground();
        if (!(drawable instanceof GradientDrawable)) {
            return;
        }

        GradientDrawable gradient = (GradientDrawable) drawable;
        int[] colors = new int[]{bankCardStyle.backgroundGradientStart, bankCardStyle.backgroundGradientEnd, bankCardStyle.backgroundGradientStart};
        gradient.setCornerRadii(new float[]{border, border, border, border, 0, 0, 0, 0});
        gradient.setColors(colors);
    }
}
