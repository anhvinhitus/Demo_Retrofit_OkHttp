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

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.utils.BankCardUtil;
import vn.com.zalopay.wallet.entity.enumeration.ECardType;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

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
            bindBankCard(mRoot, imgLogo, bankCard, true);
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

    private BankImageSetting findBank(BankCard bankCard) {
        if (bankCard == null || TextUtils.isEmpty(bankCard.type)) {
            return BankImageSetting.DEFAULT;
        }

        if (mBankSettings.containsKey(bankCard.type)) {
            return mBankSettings.get(bankCard.type);
        } else {
            return BankImageSetting.DEFAULT;
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

    private void setBankBackground(View mRoot, BankImageSetting bankInfos, boolean borderTopOnly) {
        if (mRoot == null || bankInfos == null) {
            return;
        }

        int[] colors = new int[3];
        colors[0] = getColorFromResource(bankInfos.backgroundGradientStart);
        colors[1] = getColorFromResource(bankInfos.backgroundGradientEnd);
        colors[2] = getColorFromResource(bankInfos.backgroundGradientStart);

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        float radius = getContext().getResources().getDimension(R.dimen.border);
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

    public void bindBankCard(View mRoot, ImageView imgLogo, BankCard bankCard, boolean borderTopOnly) {
        BankImageSetting bankImageSetting = findBank(bankCard);
        setBankIcon(imgLogo, bankImageSetting.bankIcon);
        setBankBackground(mRoot, bankImageSetting, borderTopOnly);
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

    private final static HashMap<String, BankImageSetting> mBankSettings = new HashMap<>();

    static {
        mBankSettings.put(ECardType.JCB.toString(), new BankImageSetting(R.drawable.ic_jcb, R.color.bg_jcb_start, R.color.bg_jcb_end));
        mBankSettings.put(ECardType.VISA.toString(), new BankImageSetting(R.drawable.ic_visa, R.color.bg_visa_start, R.color.bg_visa_end));
        mBankSettings.put(ECardType.MASTER.toString(), new BankImageSetting(R.drawable.ic_mastercard, R.color.bg_master_card_start, R.color.bg_master_card_end));
        mBankSettings.put(ECardType.PVTB.toString(), new BankImageSetting(R.drawable.ic_vietinbank, R.color.bg_vietinbank_start, R.color.bg_vietinbank_end));
        mBankSettings.put(ECardType.PBIDV.toString(), new BankImageSetting(R.drawable.ic_bidv, R.color.bg_bidv_start, R.color.bg_bidv_end));
        mBankSettings.put(ECardType.PVCB.toString(), new BankImageSetting(R.drawable.ic_vietcombank, R.color.bg_vietcombank_start, R.color.bg_vietcombank_end));
        mBankSettings.put(ECardType.PEIB.toString(), new BankImageSetting(R.drawable.ic_eximbank, R.color.bg_eximbank_start, R.color.bg_eximbank_end));
        mBankSettings.put(ECardType.PSCB.toString(), new BankImageSetting(R.drawable.ic_sacombank, R.color.bg_sacombank_start, R.color.bg_sacombank_end));
        mBankSettings.put(ECardType.PAGB.toString(), new BankImageSetting(R.drawable.ic_agribank, R.color.bg_agribank_start, R.color.bg_agribank_end));
        mBankSettings.put(ECardType.PTPB.toString(), new BankImageSetting(R.drawable.ic_tpbank, R.color.bg_tpbank_start, R.color.bg_tpbank_end));
        mBankSettings.put(ECardType.UNDEFINE.toString(), BankImageSetting.DEFAULT);
    }

    static class BankImageSetting {
        final int bankIcon;
        final int backgroundGradientStart;
        final int backgroundGradientEnd;

        BankImageSetting(int bankIcon, int backgroundGradientStart, int backgroundGradientEnd) {
            this.bankIcon = bankIcon;
            this.backgroundGradientStart = backgroundGradientStart;
            this.backgroundGradientEnd = backgroundGradientEnd;
        }

        final static BankImageSetting DEFAULT = new BankImageSetting(0, R.color.bg_vietinbank_start, R.color.bg_vietinbank_end);
    }
}
