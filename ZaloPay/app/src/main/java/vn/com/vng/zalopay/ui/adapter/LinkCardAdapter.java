package vn.com.vng.zalopay.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.Enums;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.vng.uicomponent.widget.recyclerview.AbsRecyclerAdapter;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class LinkCardAdapter extends AbsRecyclerAdapter<BankCard, RecyclerView.ViewHolder> {

    public LinkCardAdapter(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new BottomHolder(mInflater.inflate(R.layout.row_bank_card_bottom_layout, parent, false));
        } else {
            return new ViewHolder(mInflater.inflate(R.layout.row_bank_card_layout, parent, false));
        }
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
        @Bind(R.id.iv_logo)
        ImageView mLogo;

        @Bind(R.id.vg_header)
        ViewGroup mHeaderView;

        @Bind(R.id.tv_sub_num_acc)
        TextView mSubAccNumber;

        @Bind(R.id.tv_username)
        TextView mUserName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
        //ef9825 master card

        // 0f7ecd visa

        // 1e3368 jcb

        public void bindView(BankCard bankCard) {
            GradientDrawable bgShape = (GradientDrawable) mHeaderView.getBackground();
            if (bankCard.type.equals(Enums.BankCard.JCB)) {
                mLogo.setImageResource(R.drawable.ic_lc_jcb_card);
                bgShape.setColor(Color.parseColor("#1e3368"));
            } else if (bankCard.type.equals(Enums.BankCard.VISA)) {
                bgShape.setColor(Color.parseColor("#0f7ecd"));
                mLogo.setImageResource(R.drawable.ic_lc_visa_card);
            } else if (bankCard.type.equals(Enums.BankCard.MASTERCARD)) {
                bgShape.setColor(Color.parseColor("#ef9825"));
                mLogo.setImageResource(R.drawable.ic_lc_master_card);
            }
            mUserName.setText(bankCard.userName);
            mSubAccNumber.setText("*** " + bankCard.subAccNumber);
        }
    }

    public class BottomHolder extends RecyclerView.ViewHolder {
        public BottomHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.btn_add_card)
        public void onClickAdd(View v) {

        }
    }
}
