package vn.com.vng.zalopay.bank.list;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.daimajia.swipe.SwipeLayout;
import com.zalopay.ui.widget.recyclerview.AbstractSwipeAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.bank.models.BankCardStyle;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;

/**
 * Created by hieuvm on 7/10/17.
 * *
 */

final class BankListAdapter extends AbstractSwipeAdapter<BankData, RecyclerView.ViewHolder> {


    interface OnBankListClickListener {
        void onClickAddCard();

        void onClickRemoveCard(BankData card, int position);
    }

    private final SwipeLayout.SwipeListener mSwipeListener;
    protected final OnBankListClickListener mListener;

    BankListAdapter(Context context, OnBankListClickListener listener) {
        super(context);
        mSwipeListener = new BankSwipeListener(context);
        mListener = listener;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {
            if (mListener == null) {
                return;
            }

            int id = anchor.getId();
            switch (id) {
                case R.id.btn_add_more:
                    mListener.onClickAddCard();
                    break;
                case R.id.background:
                    BankData data = getItem(position);
                    if (data != null) {
                        mListener.onClickRemoveCard(data, position);
                    }

                    break;
            }
        }

        @Override
        public boolean onListItemLongClick(View anchor, int position) {
            return false;
        }
    };

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == 0) {
            return new FooterHolder(mInflater.inflate(R.layout.layout_footer_list_bank, parent, false), mOnItemClickListener);
        }

        return new ViewHolder(mInflater.inflate(R.layout.row_bank_list_v2, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (!(viewHolder instanceof ViewHolder)) {
            return;
        }

        ViewHolder holder = (ViewHolder) viewHolder;

        BankData data = getItem(position);
        if (data != null) {
            holder.mSwipeView.bindView(data);
        }

        mItemManger.bind(holder.mSwipeView, position);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).mSwipeView.removeSwipeListener(mSwipeListener);
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).mSwipeView.addSwipeListener(mSwipeListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return isFooter(position) ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return mItems.size() + 1;
    }

    private boolean isFooter(int position) {
        return position == getItemCount() - 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.swipe)
        BankCardView mSwipeView;

        private OnItemClickListener mOnItemClickListener;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mOnItemClickListener = listener;
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.background)
        public void onClickDelete(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onListItemClick(v, getAdapterPosition());
            }
        }
    }

    static class FooterHolder extends RecyclerView.ViewHolder {

        @OnClick(R.id.btn_add_more)
        public void onClickAddMore(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onListItemClick(v, getAdapterPosition());
            }
        }

        private OnItemClickListener mOnItemClickListener;

        FooterHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mOnItemClickListener = listener;
            ButterKnife.bind(this, itemView);
        }
    }
}
