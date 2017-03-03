package vn.com.vng.zalopay.bank.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
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
class LinkCardAdapter extends AbsRecyclerAdapter<BankCard, RecyclerView.ViewHolder> {

    private OnClickBankCardListener listener;

    LinkCardAdapter(Context context, OnClickBankCardListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_bank_card_layout, parent, false), onItemClickListener);
    }


    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {
            if (listener == null) {
                return;
            }

            int id = anchor.getId();

            if (id == R.id.root) {
                BankCard bankCard = getItem(position);
                if (bankCard != null) {
                    listener.onClickMenu(bankCard);
                }
            }

        }

        @Override
        public boolean onListItemLongClick(View anchor, int position) {
            return false;
        }
    };


    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        onItemClickListener = null;
        listener = null;
        Timber.i("Detached");
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

        OnItemClickListener listener;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.root)
        public void onClickMore(View v) {
            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }

        public void bindView(final BankCard bankCard, boolean isLastItem) {
            Timber.d("bindView bankCard.type:%s", bankCard.type);
            bindBankCard(mRoot, imgLogo, bankCard, true);
            String bankCardNumber = BankUtils.formatBankCardNumber(bankCard.first6cardno, bankCard.last4cardno);
            mCardNumber.setText(Html.fromHtml(bankCardNumber));
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
        colors[0] = getColorFromResource(bankCardStyle.backgroundGradientStart);
        colors[1] = getColorFromResource(bankCardStyle.backgroundGradientEnd);
        colors[2] = getColorFromResource(bankCardStyle.backgroundGradientStart);

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

    interface OnClickBankCardListener {
        void onClickMenu(BankCard bankCard);
    }
}
