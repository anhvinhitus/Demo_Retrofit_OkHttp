package vn.com.vng.zalopay.scanners.beacons;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;


public class CounterBeaconRecyclerViewAdapter extends AbsRecyclerAdapter<BeaconDevice, CounterBeaconRecyclerViewAdapter.ViewHolder> {


    public interface OnClickBeaconDeviceListener {
        void onClickBeaconListener(BeaconDevice beacon);
    }

    OnClickBeaconDeviceListener listener;

    public CounterBeaconRecyclerViewAdapter(Context context, OnClickBeaconDeviceListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_counter_beacon, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BeaconDevice item = getItem(position);
        if (item != null) {
            holder.bindView(item);
        }
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {
            Timber.d("onListItemClick: position %s", position);
            BeaconDevice item = getItem(position);
            if (item != null && listener != null) {
                listener.onClickBeaconListener(item);
            }
        }

        @Override
        public boolean onListItemLongClick(View anchor, int position) {
            return false;
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.counter_description)
        TextView mDescriptionView;

        @BindView(R.id.counter_order_amount)
        TextView mAmountView;

        private OnItemClickListener listener;

        public ViewHolder(View view, OnItemClickListener listener) {
            super(view);
            ButterKnife.bind(this, view);
            this.listener = listener;
        }

        public void bindView(BeaconDevice mItem) {
            String amount = "";
            if (mItem.order != null) {
                amount = CurrencyUtil.formatCurrency(mItem.order.amount);
                String description = mItem.order.description;
                if (TextUtils.isEmpty(description)) {
                    description = "Thanh toán cho hoá đơn";
                }
                mDescriptionView.setText(description);
            } else if (mItem.paymentRecord != null) {
                amount = CurrencyUtil.formatCurrency(mItem.paymentRecord.amount);
                mDescriptionView.setText("Hoá đơn: ");
            } else {
                mDescriptionView.setText(mItem.id);
            }
            mAmountView.setText(amount);

        }

        @OnClick(R.id.beacon_layout)
        public void onClickItemLayout(View v) {
            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAmountView.getText() + "'";
        }
    }
}
