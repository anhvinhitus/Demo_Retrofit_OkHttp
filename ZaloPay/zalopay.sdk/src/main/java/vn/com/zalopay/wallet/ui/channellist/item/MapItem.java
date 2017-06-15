package vn.com.zalopay.wallet.ui.channellist.item;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.zalopay.ui.widget.mutilview.recyclerview.DataBindAdapter;

import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;

/**
 * Created by chucvv on 6/14/17.
 */

public class MapItem extends AbstractItem<MapItem.ViewHolder> {

    private int user_level = 1;

    public MapItem(Context context, long amount, int user_level, DataBindAdapter dataBindAdapter) {
        super(context, amount, dataBindAdapter, ViewHolder.class);
        this.user_level = user_level;
    }

    @Override
    public int getLayoutId() {
        return R.layout.channel_item;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PaymentChannel channel = mDataSet.get(position);
        String fee_desc = getFeeDesc(channel);
        if (channel.isMapCardChannel() && user_level < BuildConfig.level_allow_cardmap) {
            fee_desc = mContext.getString(R.string.zpw_string_fee_upgrade_level);
        }
        fee_desc = formatFeeDesc(fee_desc);
        holder.fee_textview.setText(fee_desc);
    }

    static class ViewHolder extends AbstractItem.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}
