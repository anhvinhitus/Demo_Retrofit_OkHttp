package vn.com.vng.zalopay.withdraw.ui.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.withdraw.models.BankSupportWithdraw;

/**
 * Created by longlv on 1/04/17.
 * *
 */
class AccountSupportWithdrawAdapter extends AbsRecyclerAdapter<BankSupportWithdraw, AccountSupportWithdrawAdapter.ViewHolder> {

    AccountSupportWithdrawAdapter(Context context) {
        super(context);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_card_support_withdraw_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BankSupportWithdraw item = getItem(position);
        holder.bindView(item);
    }

    @Override
    public int getItemCount() {
        // Lấy số nhỏ nhất chia hết cho 2, lớn hơn itemCount.
        // Để đảm bảo item empty cuối cùng trong grid có màu khác với màu nền.
        return ((super.getItemCount() + 1) / 2) * 2;
    }

    @Override
    public void insertItems(List<BankSupportWithdraw> items) {
        if (items == null || items.isEmpty()) return;
        synchronized (_lock) {
            for (BankSupportWithdraw item : items) {
                if (!exist(item)) {
                    insert(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean exist(BankSupportWithdraw item) {
        List<BankSupportWithdraw> list = getItems();
        return list.indexOf(item) >= 0;
    }

    public void updateBankValid(String bankCode) {
        if (TextUtils.isEmpty(bankCode) || getItems() == null) {
            return;
        }
        for (int i = 0; i < getItems().size(); i++) {
            BankSupportWithdraw card = getItem(i);
            if (card == null || card.mBankConfig == null) {
                continue;
            }
            if (bankCode.equals(card.mBankConfig.name)) {
                card.mIsMapped = true;
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.checkBox)
        CheckBox mCheckBox;

        @BindView(R.id.txtBankName)
        TextView mTxtBankName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindView(BankSupportWithdraw card) {
            if (card == null || card.mBankConfig == null) {
                mTxtBankName.setVisibility(View.INVISIBLE);
                mCheckBox.setVisibility(View.INVISIBLE);
                return;
            } else {
                mTxtBankName.setVisibility(View.VISIBLE);
                mCheckBox.setVisibility(View.VISIBLE);
            }
            mTxtBankName.setText(card.mBankConfig.name);
            mCheckBox.setChecked(card.mIsMapped);
        }
    }
}
