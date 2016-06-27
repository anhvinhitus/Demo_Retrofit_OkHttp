package vn.com.vng.zalopay.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.utils.BankCardUtil;
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
            if (bankCard != null) {
                ((ViewHolder) holder).bindView(bankCard);
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

        public void bindView(BankCard bankCard) {
            Timber.d("bindView bankCard.type:%s", bankCard.type);
            if (bankCard.type == null) {
                mRoot.setBackgroundResource(R.color.transparent);
            } else if (bankCard.type.equals(ECardType.JCB.toString())) {
                mRoot.setBackgroundResource(R.drawable.ic_jcb);
            } else if (bankCard.type.equals(ECardType.VISA.toString())) {
                mRoot.setBackgroundResource(R.drawable.ic_visa);
            } else if (bankCard.type.equals(ECardType.MASTER.toString())) {
                mRoot.setBackgroundResource(R.drawable.ic_mastercard);
            } else if (bankCard.type.equals(ECardType.PVTB.toString())) {
                mRoot.setBackgroundResource(R.drawable.ic_vietinbank);
            } else if (bankCard.type.equals(ECardType.PBIDV.toString())) {
                mRoot.setBackgroundResource(R.drawable.ic_bidv);
            } else if (bankCard.type.equals(ECardType.PVCB.toString())) {
                mRoot.setBackgroundResource(R.drawable.ic_vietcombank);
            } else if (bankCard.type.equals(ECardType.PEIB.toString())) {
                mRoot.setBackgroundResource(R.drawable.ic_eximbank);
            } else if (bankCard.type.equals(ECardType.PSCB.toString())) {
                mRoot.setBackgroundResource(R.drawable.ic_sacombank);
            } else if (bankCard.type.equals(ECardType.PAGB.toString())) {
                mRoot.setBackgroundResource(R.drawable.ic_agribank);
            } else if (bankCard.type.equals(ECardType.PTPB.toString())) {
                mRoot.setBackgroundResource(R.drawable.ic_tpbank);
            } else if (bankCard.type.equals(ECardType.UNDEFINE.toString())) {
                mRoot.setBackgroundResource(R.color.transparent);
            }
            mCardNumber.setText(BankCardUtil.formatBankCardNumber(bankCard.first6cardno, bankCard.last4cardno));
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
        void onClickMenu(BankCard bankCard);
    }
}
