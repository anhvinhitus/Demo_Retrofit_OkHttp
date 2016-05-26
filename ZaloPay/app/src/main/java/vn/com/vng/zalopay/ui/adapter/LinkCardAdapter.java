package vn.com.vng.zalopay.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.zalopay.wallet.entity.enumeration.ECardType;
import vn.vng.uicomponent.widget.recyclerview.AbsRecyclerAdapter;
import vn.vng.uicomponent.widget.recyclerview.OnItemClickListener;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class LinkCardAdapter extends AbsRecyclerAdapter<BankCard, RecyclerView.ViewHolder> {

    private OnClickBankCardListener listener;

    public LinkCardAdapter(Context context, OnClickBankCardListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new BottomHolder(mInflater.inflate(R.layout.row_bank_card_bottom_layout, parent, false), onItemClickListener);
        } else {
            return new ViewHolder(mInflater.inflate(R.layout.row_bank_card_layout, parent, false), onItemClickListener);
        }
    }


    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {
            if (listener == null) return;
            int id = anchor.getId();

            if (id == R.id.btn_add_card) {
                listener.onClickAddBankCard();
            } else if (id == R.id.btn_more) {
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
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            BankCard bankCard = getItem(position);
            if (bankCard != null) {
                ((ViewHolder) holder).bindView(bankCard);
            }
        } else {

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size() + 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_logo)
        ImageView mLogo;

        @BindView(R.id.vg_header)
        ViewGroup mHeaderView;

        @BindView(R.id.tv_sub_num_acc)
        TextView mSubAccNumber;

        @BindView(R.id.tv_username)
        TextView mUserName;

        OnItemClickListener listener;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.btn_more)
        public void onClickMore(View v) {
            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }

        //ef9825 master card

        // 0f7ecd visa

        // 1e3368 jcb

        public void bindView(BankCard bankCard) {
            GradientDrawable bgShape = (GradientDrawable) mHeaderView.getBackground();
            if (bankCard.type == null) {
                mLogo.setImageResource(R.color.transparent);
            } else if (bankCard.type.equals(ECardType.JCB.toString())) {
                mLogo.setImageResource(R.drawable.ic_lc_jcb_card);
                bgShape.setColor(Color.parseColor("#1e3368"));
            } else if (bankCard.type.equals(ECardType.VISA.toString())) {
                bgShape.setColor(Color.parseColor("#0f7ecd"));
                mLogo.setImageResource(R.drawable.ic_lc_visa_card);
            } else if (bankCard.type.equals(ECardType.MASTER.toString())) {
                bgShape.setColor(Color.parseColor("#ef9825"));
                mLogo.setImageResource(R.drawable.ic_lc_master_card);
            } else if (bankCard.type.equals(ECardType.UNDEFINE.toString())) {
                mLogo.setImageResource(R.color.transparent);
            }
            mUserName.setText(bankCard.cardname);
            mSubAccNumber.setText("*** " + bankCard.last4cardno);
        }
    }

    public class BottomHolder extends RecyclerView.ViewHolder {

        private OnItemClickListener listener;

        public BottomHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.listener = listener;
        }

        @OnClick(R.id.btn_add_card)
        public void onClickAdd(View v) {
            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }
    }

    public interface OnClickBankCardListener {
        void onClickAddBankCard();

        void onClickMenu(BankCard bankCard);
    }
}
