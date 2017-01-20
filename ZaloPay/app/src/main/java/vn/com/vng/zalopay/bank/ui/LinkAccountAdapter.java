package vn.com.vng.zalopay.bank.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.vng.zalopay.bank.models.BankAccountStyle;

/**
 * Created by AnhHieu on 5/10/16.
 * *
 */
class LinkAccountAdapter extends AbsRecyclerAdapter<BankAccount, RecyclerView.ViewHolder> {

    LinkAccountAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_bank_account_layout, parent, false));
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        Timber.i("Detached");
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
            mTvAccountName.setText(bankAccount.getAccountNo());
            bindBankAccount(mLineVertical, mImgLogo, bankAccount);
            setMargin(isLastItem);
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
            params.setMargins(margin, margin, margin, marginBottom);
            mRoot.setLayoutParams(params);
        }
    }

    private int getColorFromResource(int resource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getContext().getColor(resource);
        } else {
            return getContext().getResources().getColor(resource);
        }
    }

    private void setBankIcon(ImageView imgLogo, int bankIcon) {
        if (imgLogo == null) {
            return;
        }
        if (bankIcon == 0) {
            imgLogo.setImageDrawable(null);
        } else {
            imgLogo.setImageResource(bankIcon);
        }
    }

    private void bindBankAccount(View lineVertical, ImageView imgLogo, BankAccount bankAccount) {
        BankAccountStyle bankAccountStyle = BankUtils.getBankAccountStyle(bankAccount);
        setLineStyle(lineVertical, bankAccountStyle.mLineColor);
        setBankIcon(imgLogo, bankAccountStyle.mBankIcon);
    }

    private void setLineStyle(View lineVertical, int lineColor) {
        GradientDrawable bgShape = (GradientDrawable) lineVertical.getBackground();
        bgShape.setColor(getColorFromResource(lineColor));
    }
}
