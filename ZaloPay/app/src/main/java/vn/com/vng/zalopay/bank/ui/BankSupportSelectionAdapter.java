package vn.com.vng.zalopay.bank.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by datnt10 on 5/25/17.
 */

public class BankSupportSelectionAdapter extends AbsRecyclerAdapter<ZPCard, BankSupportSelectionAdapter.ViewHolder> {
    private OnClickBankSupportListener listener;

    BankSupportSelectionAdapter(Context context, OnClickBankSupportListener listener) {
        super(context);
        this.listener = listener;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {

            ZPCard card = getItem(position);
            if (listener != null && card != null) {
                listener.onClickBankSupportListener(card, position);
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
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_bank_support_selection, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ZPCard item = getItem(position);
        holder.bindView(item, position);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void insertItems(List<ZPCard> items) {
        if (items == null || items.isEmpty()) return;
        synchronized (_lock) {
            for (ZPCard item : items) {
                if (!exist(item)) {
                    insert(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean exist(ZPCard item) {
        List<ZPCard> list = getItems();
        return list.indexOf(item) >= 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private OnItemClickListener listener;

        @BindView(R.id.row_bank_support_selection_iv_logo)
        ImageView mLogoView;

        @BindView(R.id.row_bank_support_selection_iv_next)
        ImageView ivNext;

        @BindView(R.id.row_bank_support_selection_tv_bank_name)
        TextView tvBankName;

        @BindView(R.id.row_bank_support_selection_dash_line)
        View mDashLine;

        @OnClick(R.id.row_bank_support_selection)
        void onBankSupportClickItem(View v) {
            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
        }

        void bindView(ZPCard card, int position) {
            if (card == null) {
                mLogoView.setVisibility(View.GONE);
                return;
            }
            if (getItemCount() == (position + 1) ) {
                mDashLine.setVisibility(View.GONE);
            }
            mLogoView.setImageBitmap(ResourceManager.getImage(card.getCardLogoName()));
            mLogoView.setVisibility(View.VISIBLE);
            tvBankName.setText(card.getCardName());
            ivNext.setImageBitmap(ResourceManager.getImage("ic_next.png"));
        }
    }

    public interface OnClickBankSupportListener {
        void onClickBankSupportListener(ZPCard card, int position);
    }
}