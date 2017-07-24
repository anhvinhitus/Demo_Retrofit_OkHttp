package vn.com.vng.zalopay.bank.list;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.BankCardStyle;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
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

    @OnClick(R.id.card)
    public void onClickCard() {
        toggle();
    }

    @Override
    protected Rect computeBottomLayoutAreaViaSurface(ShowMode mode, Rect surfaceArea) {
        Rect rect = super.computeBottomLayoutAreaViaSurface(mode, surfaceArea);
        View foregroundView = getSurfaceView();
        int paddingRight = 0;
        if (foregroundView != null) {
            paddingRight = foregroundView.getPaddingRight();
        }

        rect.left -= paddingRight;
        return rect;
    }

    @Override
    protected Rect computeSurfaceLayoutArea(boolean open) {
        Rect rect = super.computeSurfaceLayoutArea(open);
        View foregroundView = getSurfaceView();
        int paddingRight = 0;
        if (foregroundView != null) {
            paddingRight = foregroundView.getPaddingRight();
        }

        if (!open) {
            rect.left = 0;
            rect.right = getMeasuredWidth();
        } else {
            int offset = (int) getCurrentOffset() - paddingRight;
            rect.left = -offset;
            rect.right = getMeasuredWidth() - offset;
        }
        return rect;
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

        int[] colors = new int[]{bankCardStyle.backgroundGradientStart, bankCardStyle.backgroundGradientEnd, bankCardStyle.backgroundGradientStart};
        GradientDrawable gradient = (GradientDrawable) drawable;
        gradient.mutate();
        gradient.setCornerRadii(new float[]{border, border, border, border, 0, 0, 0, 0});
        gradient.setColors(colors);
    }
}
