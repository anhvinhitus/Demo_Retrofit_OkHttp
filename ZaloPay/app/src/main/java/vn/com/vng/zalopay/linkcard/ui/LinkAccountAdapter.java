package vn.com.vng.zalopay.linkcard.ui;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
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
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.linkcard.models.BankAccount;

/**
 * Created by AnhHieu on 5/10/16.
 * *
 */
class LinkAccountAdapter extends AbsRecyclerAdapter<BankAccount, RecyclerView.ViewHolder> {

    private OnClickBankAccountListener listener;

    public LinkAccountAdapter(Context context, OnClickBankAccountListener listener) {
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
                BankAccount bankAccount = getItem(position);
                if (bankAccount != null) {
                    listener.onClickMenu(bankAccount);
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

        public void bindView(final BankAccount bankAccount, boolean isLastItem) {
            Timber.d("bindView type:%s", bankAccount.type);
            bindBankAccount(mRoot, imgLogo, bankAccount, true);
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

    public void bindBankAccount(View mRoot, ImageView imgLogo, BankAccount bankAccount, boolean borderTopOnly) {
        /*BankImageSetting bankImageSetting = getBankCardStyle(bankAccount);
        setBankIcon(imgLogo, bankImageSetting.bankIcon);
        setBankBackground(mRoot, bankImageSetting, borderTopOnly);*/
    }

    interface OnClickBankAccountListener {
        void onClickMenu(BankAccount bankAccount);
    }
}
