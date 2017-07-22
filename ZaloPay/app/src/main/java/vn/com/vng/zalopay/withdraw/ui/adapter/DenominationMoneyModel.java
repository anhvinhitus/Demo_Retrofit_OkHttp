package vn.com.vng.zalopay.withdraw.ui.adapter;

import android.view.View;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.ConfigLoader;
import vn.com.vng.zalopay.utils.CurrencyUtil;

/**
 * Created by hieuvm on 6/18/17.
 * *
 */

class DenominationMoneyModel extends EpoxyModelWithHolder<DenominationMoneyModel.ViewHolder> {

    interface OnItemClickListener {
        void onClickDenominationMoney(long money);
    }

    private long money;
    private long balance;

    private OnItemClickListener mOnItemClickListener;

    DenominationMoneyModel() {
    }

    void setDenominationMoney(long money) {
        this.money = money;
    }

    void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    protected ViewHolder createNewHolder() {
        return new ViewHolder();
    }

    @Override
    public void bind(ViewHolder holder) {
        boolean isValid = balance >= money;
        if (money == 0) {
            holder.mMoneyView.setText(R.string.input_money_text);
            isValid = balance >= ConfigLoader.getMinMoneyWithdraw();

        } else {
            holder.mMoneyView.setText(CurrencyUtil.formatCurrency(money, false));
        }
        holder.mItemLayout.setOnClickListener(viewClickListener);

        holder.mItemLayout.setEnabled(isValid);
        holder.mMoneyView.setEnabled(isValid);
        holder.mMaskView.setVisibility(isValid ? View.GONE : View.VISIBLE);
    }

    private final DebouncingOnClickListener viewClickListener = new DebouncingOnClickListener() {
        @Override
        public void doClick(View v) {
            Timber.d("denomination money %s", money);
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onClickDenominationMoney(money);
            }
        }
    };

    void setClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public void unbind(ViewHolder holder) {
        super.unbind(holder);
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.row_withdraw_layout;
    }

    public static final class ViewHolder extends EpoxyHolder {

        @BindView(R.id.tvMoney)
        TextView mMoneyView;

        @BindView(R.id.itemLayout)
        View mItemLayout;

        @BindView(R.id.mask)
        View mMaskView;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        DenominationMoneyModel model = (DenominationMoneyModel) o;

        return money == model.money && balance == model.balance;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (money ^ (money >>> 32));
        result = 31 * result + (int) (balance ^ (balance >>> 32));
        return result;
    }
}
