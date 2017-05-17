package vn.com.vng.zalopay.bank.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuView;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.bank.models.BankCardStyle;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.domain.model.BankCard;

/**
 * Created by AnhHieu on 5/10/16.
 * *
 */
class LinkCardAdapter extends AbstractSwipeMenuRecyclerAdapter<BankCard, RecyclerView.ViewHolder> {

    LinkCardAdapter(Context context) {
        super(context);
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        return mInflater.inflate(R.layout.row_bank_card_layout, parent, false);
    }

    @Override
    public RecyclerView.ViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        return new ViewHolder(realContentView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            BankCard bankCard = getItem(position);
            boolean isLastItem = (position == getItemCount() - 1);
            if (bankCard != null) {
                ((ViewHolder) holder).bindView(bankCard, isLastItem);
            }
        }
    }

    @Override
    public void onBindSwipeMenuViewHolder(SwipeMenuView swipeLeftMenuView, SwipeMenuView swipeRightMenuView, int position) {
        boolean isLastItem = (position == getItemCount() - 1);
        FrameLayout.LayoutParams params = new FrameLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = getContext().getResources().getDimensionPixelSize(R.dimen.spacing_medium_s);
        int marginBottom = 0;
        if (isLastItem) {
            marginBottom = getContext().getResources()
                    .getDimensionPixelSize(R.dimen.linkcard_margin_bottom_lastitem);
        }
        params.setMargins(0, margin, 0, marginBottom);
        swipeRightMenuView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root)
        View mRoot;

        @BindView(R.id.iv_logo)
        ImageView imgLogo;

        @BindView(R.id.tv_num_acc)
        TextView mCardNumber;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindView(final BankCard bankCard, boolean isLastItem) {
            bindBankCard(mRoot, imgLogo, bankCard, true);
            String bankCardNumber = BankUtils.formatBankCardNumber(bankCard.first6cardno, bankCard.last4cardno);
            mCardNumber.setText(Html.fromHtml(bankCardNumber));
            setMargin(isLastItem);
        }

        private void setMargin(boolean isLastItem) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mRoot.getLayoutParams();
            int margin = getContext().getResources().getDimensionPixelSize(R.dimen.spacing_medium_s);
            int marginBottom = 0;
            if (isLastItem) {
                marginBottom = getContext().getResources()
                        .getDimensionPixelSize(R.dimen.linkcard_margin_bottom_lastitem);
            }
            params.setMargins(0, margin, 0, marginBottom);
            mRoot.setLayoutParams(params);
        }
    }

    private void setBankIcon(ImageView imgLogo, @StringRes int bankIcon) {
        if (imgLogo == null) {
            return;
        }
        if (bankIcon == 0) {
            imgLogo.setImageDrawable(null);
        } else {
            String iconName = getContext().getString(bankIcon);
            imgLogo.setImageBitmap(ResourceHelper
                    .getBitmap(getContext(), BuildConfig.ZALOPAY_APP_ID, iconName));
        }
    }

    private void setBankBackground(View mRoot, BankCardStyle bankCardStyle, boolean borderTopOnly) {
        if (mRoot == null || bankCardStyle == null) {
            return;
        }

        int[] colors = new int[3];
        colors[0] = ContextCompat.getColor(getContext(), bankCardStyle.backgroundGradientStart);
        colors[1] = ContextCompat.getColor(getContext(), bankCardStyle.backgroundGradientEnd);
        colors[2] = ContextCompat.getColor(getContext(), bankCardStyle.backgroundGradientStart);

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        float radius = getContext().getResources().getDimension(R.dimen.border_link_card);
        if (borderTopOnly) {
            gradientDrawable.setCornerRadii(new float[]{radius, radius,
                    radius, radius,
                    0, 0,
                    0, 0});
        } else {
            gradientDrawable.setCornerRadius(radius);
        }
        mRoot.setBackground(gradientDrawable);
    }

    void bindBankCard(View mRoot, ImageView imgLogo, BankCard bankCard, boolean borderTopOnly) {
        BankCardStyle bankCardStyle = BankUtils.getBankCardStyle(bankCard);
        setBankIcon(imgLogo, bankCardStyle.bankIcon);
        setBankBackground(mRoot, bankCardStyle, borderTopOnly);
    }
}
