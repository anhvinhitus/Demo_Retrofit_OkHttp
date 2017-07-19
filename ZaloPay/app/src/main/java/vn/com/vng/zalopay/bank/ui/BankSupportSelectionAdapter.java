package vn.com.vng.zalopay.bank.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.constants.BankStatus;
import vn.com.zalopay.wallet.merchant.entities.ZPBank;

/**
 * Created by datnt10 on 5/25/17.
 * adapter list bank support
 */

final class BankSupportSelectionAdapter extends AbsRecyclerAdapter<ZPBank, BankSupportSelectionAdapter.ViewHolder> {

    interface OnClickBankSupportListener {
        void onClickBankSupport(ZPBank card);
    }

    OnClickBankSupportListener listener;

    BankSupportSelectionAdapter(Context context, OnClickBankSupportListener listener) {
        super(context);
        this.listener = listener;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {

            ZPBank card = getItem(position);

            if (listener != null && card != null) {
                listener.onClickBankSupport(card);
            }
        }

        @Override
        public boolean onListItemLongClick(View anchor, int position) {
            return false;
        }
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_bank_support_selection, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ZPBank item = getItem(position);
        if (item != null) {
            holder.bindView(item);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.row_bank_support_selection_iv_logo)
        SimpleDraweeView mLogoView;
        @BindView(R.id.row_bank_support_selection_iv_next)
        View ivNext;
        @BindView(R.id.row_bank_support_selection_tv_bank_name)
        TextView tvBankName;
        @BindView(R.id.row_bank_support_selection_tv_bank_maintain)
        TextView tvBankMaintain;

        private OnItemClickListener listener;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.row_bank_support_selection)
        void onBankSupportClickItem(View v) {
            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }

        void bindView(ZPBank card) {
            mLogoView.setImageURI(ResourceManager.getAbsoluteImagePath(card.bankLogo));
            mLogoView.setVisibility(View.VISIBLE);
            tvBankName.setText(card.bankName);
            setBankStatus(card.bankStatus);
        }

        private void setBankStatus(@BankStatus int status) {
            if (status == BankStatus.MAINTENANCE || status == BankStatus.UPVERSION) {
                tvBankMaintain.setVisibility(View.VISIBLE);
                mLogoView.setAlpha(0.5f);
                tvBankName.setAlpha(0.5f);
                tvBankMaintain.setAlpha(0.5f);
                ivNext.setAlpha(0.5f);
                tvBankMaintain.setText(getDisableMessage(status));
            } else {
                tvBankMaintain.setVisibility(View.GONE);
                mLogoView.setAlpha(1f);
                tvBankName.setAlpha(1f);
                tvBankMaintain.setAlpha(1f);
                ivNext.setAlpha(1f);
            }
        }

        private String getDisableMessage(@BankStatus int status) {
            switch (status) {
                case BankStatus.MAINTENANCE:
                    return getContext().getString(R.string.bank_maintenance_message);
                case BankStatus.UPVERSION:
                    return getContext().getString(R.string.bank_upversion_message);
                default:
                    return null;
            }
        }
    }
}