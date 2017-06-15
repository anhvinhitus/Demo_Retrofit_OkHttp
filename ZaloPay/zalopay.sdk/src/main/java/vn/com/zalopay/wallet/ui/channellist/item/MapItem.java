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
        String fee_desc = mContext.getString(R.string.zpw_string_fee_free);
        if (channel.hasFee()) {
            fee_desc = StringUtil.formatVnCurrence(String.valueOf(channel.totalfee));
        }
        if (channel.isMapCardChannel() && user_level < BuildConfig.level_allow_cardmap) {
            fee_desc = mContext.getString(R.string.zpw_string_fee_upgrade_level);
        }
        if (!TextUtils.isEmpty(fee_desc)
                && !fee_desc.equals(mContext.getString(R.string.zpw_string_fee_free))
                && !fee_desc.equals(mContext.getString(R.string.zpw_string_fee_upgrade_level))) {
            fee_desc = String.format(GlobalData.getStringResource(RS.string.zpw_string_fee_format), fee_desc);
        }
        holder.fee_textview.setText(fee_desc);
    }

    static class ViewHolder extends AbstractItem.ViewHolder {
        View line;
        public ViewHolder(View view) {
            super(view);
            line = view.findViewById(R.id.line);
        }
    }
}
