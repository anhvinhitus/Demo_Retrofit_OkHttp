package vn.com.zalopay.wallet.ui.channellist.item;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.zalopay.ui.widget.mutilview.recyclerview.DataBindAdapter;

import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkInvalidDataMessage;

/**
 * Created by chucvv on 6/14/17.
 */

public class ZaloPayItem extends AbstractItem<ZaloPayItem.ViewHolder> {
    @TransactionType
    int transtype;
    private UserInfo userInfo;

    public ZaloPayItem(Context context, long amount, UserInfo userInfo, @TransactionType int transtype, DataBindAdapter dataBindAdapter) {
        super(context, amount, dataBindAdapter, ViewHolder.class);
        this.userInfo = userInfo;
        this.transtype = transtype;
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
        if (userInfo.level < BuildConfig.level_allow_use_zalopay) {
            //check map table for allow
            int iCheck = userInfo.getPermissionByChannelMap(channel.pmcid, transtype);
            //error map table from server, show dialog alert and quit sdk
            if (iCheck == Constants.LEVELMAP_INVALID) {
                SDKApplication.getApplicationComponent().eventBus()
                        .post(new SdkInvalidDataMessage(mContext.getString(R.string.zingpaysdk_alert_input_error)));
            }
            else if (iCheck == Constants.LEVELMAP_BAN) {
                fee_desc = GlobalData.getStringResource(RS.string.zpw_string_fee_upgrade_level);
            }
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
