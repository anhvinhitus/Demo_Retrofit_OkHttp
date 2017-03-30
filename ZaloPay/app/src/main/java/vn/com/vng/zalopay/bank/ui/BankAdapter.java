package vn.com.vng.zalopay.bank.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.listener.OnClickBankListener;
import vn.com.vng.zalopay.bank.models.BankAccount;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by longlv on 10/13/16.
 * *
 */
class BankAdapter extends AbsRecyclerAdapter<ZPCard, BankAdapter.ViewHolder> {

    private OnClickBankListener mListener;

    BankAdapter(Context context, List<ZPCard> cards, OnClickBankListener listener) {
        super(context);
        mListener = listener;
        insertItems(cards);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_bank_layout, parent, false), onItemClickListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        onItemClickListener = null;
        mListener = null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ZPCard item = getItem(position);
        holder.bindView(item);
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

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {
            if (mListener == null) {
                return;
            }

            int id = anchor.getId();
            if (id == R.id.itemLayout) {
                ZPCard zpCard = getItem(position);
                if (zpCard != null) {
                    mListener.onClickBankItem(zpCard);
                }
            }
        }

        @Override
        public boolean onListItemLongClick(View anchor, int position) {
            return false;
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_logo)
        ImageView mLogoView;

        @OnClick(R.id.itemLayout)
        public void onClickRootView(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onListItemClick(v, getAdapterPosition());
            }
        }

        OnItemClickListener mOnItemClickListener;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mOnItemClickListener = listener;
            ButterKnife.bind(this, itemView);
        }

        void bindView(ZPCard card) {
            if (card == null) {
                mLogoView.setVisibility(View.GONE);
                return;
            }
            mLogoView.setImageBitmap(ResourceManager.getImage(card.getCardLogoName()));
            mLogoView.setVisibility(View.VISIBLE);
        }
    }

}