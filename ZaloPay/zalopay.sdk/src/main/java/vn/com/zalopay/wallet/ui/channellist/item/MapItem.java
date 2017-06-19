package vn.com.zalopay.wallet.ui.channellist.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zalopay.ui.widget.mutilview.recyclerview.DataBindAdapter;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;

/**
 * Created by chucvv on 6/14/17.
 */

public class MapItem extends AbstractItem<MapItem.ViewHolder> {

    private int user_level = 1;

    public MapItem(Context context, long amount, int user_level, DataBindAdapter dataBindAdapter) {
        super(context, amount, dataBindAdapter);
        this.user_level = user_level;
    }

    @Override
    public MapItem.ViewHolder onNewBindHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item, parent, false);
        return new MapItem.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PaymentChannel channel = mDataSet.get(position);
        String fee_desc = getFeeDesc(channel);
        if (channel.isMapCardChannel() && user_level < BuildConfig.level_allow_cardmap) {
            fee_desc = mContext.getString(R.string.zpw_string_fee_upgrade_level);
        }
        fee_desc = formatFeeDesc(fee_desc);
        renderDesc(holder, fee_desc);
    }

    static class ViewHolder extends AbstractItem.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}
