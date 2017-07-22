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
import vn.com.zalopay.utility.CurrencyUtil;

/**
 * Created by hieuvm on 6/18/17.
 * *
 */

class HeaderWithdrawModel extends EpoxyModelWithHolder<HeaderWithdrawModel.ViewHolder> {

    private long money;

    HeaderWithdrawModel() {
    }

    void setBalance(long money) {
        this.money = money;
    }

    @Override
    protected ViewHolder createNewHolder() {
        return new HeaderWithdrawModel.ViewHolder();
    }

    @Override
    public void bind(ViewHolder holder) {
        holder.mMoneyView.setText(CurrencyUtil.spanFormatCurrency(money, true));
        holder.mItemLayout.setOnClickListener(viewClickListener);
    }

    @Override
    public int getSpanSize(int totalSpanCount, int position, int itemCount) {
        Timber.d("getSpanSize: %s", totalSpanCount);
        return totalSpanCount;
    }

    private final DebouncingOnClickListener viewClickListener = new DebouncingOnClickListener() {
        @Override
        public void doClick(View v) {
            Timber.d("balance %s", money);
        }
    };

    @Override
    public void unbind(ViewHolder holder) {
        super.unbind(holder);
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.header_withdraw_layout;
    }

    public static final class ViewHolder extends EpoxyHolder {

        @BindView(R.id.tvMoney)
        TextView mMoneyView;

        @BindView(R.id.itemLayout)
        View mItemLayout;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (money ^ (money >>> 32));
        return result;
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

        HeaderWithdrawModel that = (HeaderWithdrawModel) o;

        return money == that.money;

    }
}
