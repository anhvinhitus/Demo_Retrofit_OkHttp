package vn.com.vng.zalopay.ui.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        public void bindView(final BankCard bankCard) {
            Timber.d("bindView bankCard.type:%s", bankCard.type);
            bindBankCard(mRoot, imgLogo, bankCard);
            mCardNumber.setText(BankCardUtil.formatBankCardNumber(bankCard.first6cardno, bankCard.last4cardno));
        }
    }

    public int getColorFromResource(int resource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getContext().getColor(resource);
        } else {
            return getContext().getResources().getColor(resource);
        }
    }

    public void bindBankCard(View mRoot, ImageView imgLogo, BankCard bankCard) {
        GradientDrawable bgShape = (GradientDrawable) mRoot.getBackground();
        int[] colors = new int[3];
        if (bankCard == null || TextUtils.isEmpty(bankCard.type)) {
            imgLogo.setImageDrawable(null);
            colors = new int[]{getColorFromResource(R.color.bg_vietinbank_start), getColorFromResource(R.color.bg_vietinbank_end), getColorFromResource(R.color.bg_vietinbank_start)};
        } else if (bankCard.type.equals(ECardType.JCB.toString())) {
            imgLogo.setImageResource(R.drawable.ic_jcb);
            colors = new int[]{getColorFromResource(R.color.bg_jcb_start), getColorFromResource(R.color.bg_jcb_end), getColorFromResource(R.color.bg_jcb_start)};
        } else if (bankCard.type.equals(ECardType.VISA.toString())) {
            imgLogo.setImageResource(R.drawable.ic_visa);
            colors = new int[]{getColorFromResource(R.color.bg_visa_start), getColorFromResource(R.color.bg_visa_end), getColorFromResource(R.color.bg_visa_start)};
        } else if (bankCard.type.equals(ECardType.MASTER.toString())) {
            imgLogo.setImageResource(R.drawable.ic_mastercard);
            colors = new int[]{getColorFromResource(R.color.bg_master_card_start), getColorFromResource(R.color.bg_master_card_end), getColorFromResource(R.color.bg_master_card_start)};
        } else if (bankCard.type.equals(ECardType.PVTB.toString())) {
            imgLogo.setImageResource(R.drawable.ic_vietinbank);
            colors = new int[]{getColorFromResource(R.color.bg_vietinbank_start), getColorFromResource(R.color.bg_vietinbank_end), getColorFromResource(R.color.bg_vietinbank_start)};
        } else if (bankCard.type.equals(ECardType.PBIDV.toString())) {
            imgLogo.setImageResource(R.drawable.ic_bidv);
            colors = new int[]{getColorFromResource(R.color.bg_bidv_start), getColorFromResource(R.color.bg_bidv_end), getColorFromResource(R.color.bg_bidv_start)};
        } else if (bankCard.type.equals(ECardType.PVCB.toString())) {
            imgLogo.setImageResource(R.drawable.ic_vietcombank);
            colors = new int[]{getColorFromResource(R.color.bg_vietcombank_start), getColorFromResource(R.color.bg_vietcombank_end), getColorFromResource(R.color.bg_vietcombank_start)};
        } else if (bankCard.type.equals(ECardType.PEIB.toString())) {
            imgLogo.setImageResource(R.drawable.ic_eximbank);
            colors = new int[]{getColorFromResource(R.color.bg_eximbank_start), getColorFromResource(R.color.bg_eximbank_end), getColorFromResource(R.color.bg_eximbank_start)};
        } else if (bankCard.type.equals(ECardType.PSCB.toString())) {
            imgLogo.setImageResource(R.drawable.ic_sacombank);
            colors = new int[]{getColorFromResource(R.color.bg_sacombank_start), getColorFromResource(R.color.bg_sacombank_end), getColorFromResource(R.color.bg_sacombank_start)};
        } else if (bankCard.type.equals(ECardType.PAGB.toString())) {
            imgLogo.setImageResource(R.drawable.ic_agribank);
            colors = new int[]{getColorFromResource(R.color.bg_agribank_start), getColorFromResource(R.color.bg_agribank_end), getColorFromResource(R.color.bg_agribank_start)};
        } else if (bankCard.type.equals(ECardType.PTPB.toString())) {
            imgLogo.setImageResource(R.drawable.ic_tpbank);
            colors = new int[]{getColorFromResource(R.color.bg_tpbank_start), getColorFromResource(R.color.bg_tpbank_end), getColorFromResource(R.color.bg_tpbank_start)};
        } else if (bankCard.type.equals(ECardType.UNDEFINE.toString())) {
            imgLogo.setImageDrawable(null);
            colors = new int[]{getColorFromResource(R.color.bg_vietinbank_start), getColorFromResource(R.color.bg_vietinbank_end), getColorFromResource(R.color.bg_vietinbank_start)};
        }
        bgShape.setColors(colors);
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
