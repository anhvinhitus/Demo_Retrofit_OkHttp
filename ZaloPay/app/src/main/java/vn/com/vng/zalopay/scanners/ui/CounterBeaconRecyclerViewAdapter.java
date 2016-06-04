package vn.com.vng.zalopay.scanners.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.scanners.ui.CounterBeaconFragment.OnListFragmentInteractionListener;
import vn.com.vng.zalopay.scanners.ui.beacon.BeaconDevice;
import vn.com.vng.zalopay.scanners.ui.dummy.DummyContent.DummyItem;
import vn.com.vng.zalopay.utils.CurrencyUtil;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class CounterBeaconRecyclerViewAdapter extends RecyclerView.Adapter<CounterBeaconRecyclerViewAdapter.ViewHolder> {

    private final List<BeaconDevice> mValues;
    private final OnListFragmentInteractionListener mListener;

    public CounterBeaconRecyclerViewAdapter(List<BeaconDevice> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_counterbeacon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        String amount = "";
        if (holder.mItem.order != null) {
            amount = CurrencyUtil.formatCurrency(holder.mItem.order.getAmount());
            holder.mDescriptionView.setText(holder.mItem.order.getDescription());
        } else if (holder.mItem.paymentRecord != null) {
            amount = CurrencyUtil.formatCurrency(holder.mItem.paymentRecord.amount);
            holder.mDescriptionView.setText("Hoá đơn: ");
        } else {
            holder.mDescriptionView.setText(mValues.get(position).id);
        }
        holder.mAmountView.setText(amount);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mDescriptionView;
        public final TextView mAmountView;
        public BeaconDevice mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mDescriptionView = (TextView) view.findViewById(R.id.counter_description);
            mAmountView = (TextView) view.findViewById(R.id.counter_order_amount);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mAmountView.getText() + "'";
        }
    }
}
