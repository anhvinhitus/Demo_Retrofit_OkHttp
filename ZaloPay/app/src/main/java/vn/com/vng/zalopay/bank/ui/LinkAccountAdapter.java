package vn.com.vng.zalopay.bank.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
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
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.bank.models.BankAccountStyle;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.utils.FrescoHelper;

/**
 * Created by AnhHieu on 5/10/16.
 * *
 */
class LinkAccountAdapter extends AbstractSwipeMenuRecyclerAdapter<BankAccount, RecyclerView.ViewHolder> {

    LinkAccountAdapter(Context context) {
        super(context);
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        return mInflater.inflate(R.layout.row_bank_account_layout, parent, false);
    }

    @Override
    public RecyclerView.ViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        return new ViewHolder(realContentView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            BankAccount bankAccount = getItem(position);
            boolean isLastItem = (position == getItemCount() - 1);
            if (bankAccount != null) {
                ((ViewHolder) holder).bindView(bankAccount, isLastItem);
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

        @BindView(R.id.line)
        View mLineVertical;

        @BindView(R.id.iv_logo)
        ImageView mImgLogo;

        @BindView(R.id.tvAccountName)
        TextView mTvAccountName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindView(final BankAccount bankAccount, boolean isLastItem) {
            bindBankAccountInfo(mTvAccountName, bankAccount);
            bindBankImage(mLineVertical, mImgLogo, bankAccount);
            setMargin(isLastItem);
        }

        private void bindBankAccountInfo(TextView tvAccountInfo, BankAccount bankAccount) {
            tvAccountInfo.setText(bankAccount.getAccountInfo());
        }

        private void setMargin(boolean isLastItem) {
            FrameLayout.LayoutParams params = new FrameLayout
                    .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
            String fileName = getContext().getString(bankIcon);
            FrescoHelper.setImage(imgLogo, ResourceHelper.getResource(getContext(), BuildConfig.ZALOPAY_APP_ID, fileName));
        }
    }

    void bindBankImage(View lineVertical, ImageView imgLogo, BankAccount bankAccount) {
        BankAccountStyle bankAccountStyle = BankUtils.getBankAccountStyle(bankAccount);
        setLineStyle(lineVertical, bankAccountStyle.mLineColor);
        setBankIcon(imgLogo, bankAccountStyle.mBankIcon);
    }

    private void setLineStyle(View lineVertical, int lineColor) {
        GradientDrawable bgShape = (GradientDrawable) lineVertical.getBackground();
        bgShape.setColor(ContextCompat.getColor(getContext(), lineColor));
    }
}
