package vn.com.zalopay.wallet.ui.channellist.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zalopay.ui.widget.mutilview.recyclerview.DataBindAdapter;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkInvalidDataMessage;

/**
 * Created by chucvv on 6/14/17.
 */

public class InputItem extends AbstractItem<InputItem.ViewHolder> {
    @TransactionType
    int transtype;
    private UserInfo userInfo;

    public InputItem(Context context, long amount, UserInfo userInfo, @TransactionType int transtype, DataBindAdapter dataBindAdapter) {
        super(context, amount, dataBindAdapter);
        this.userInfo = userInfo;
        this.transtype = transtype;
    }

    @Override
    public InputItem.ViewHolder onNewBindHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item, parent, false);
        return new InputItem.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PaymentChannel channel = mDataSet.get(position);
        String fee_desc = getFeeDesc(channel);
        if (channel.isBankAccount() && userInfo.level < BuildConfig.level_allow_bankaccount) {
            //check map table for allow
            int iCheck = userInfo.getPermissionByChannelMap(channel.pmcid, transtype);
            //error map table from server, show dialog alert and quit sdk
            if (iCheck == Constants.LEVELMAP_INVALID) {
                SDKApplication.getApplicationComponent().eventBus()
                        .post(new SdkInvalidDataMessage(mContext.getString(R.string.zingpaysdk_alert_input_error)));
            } else if (iCheck == Constants.LEVELMAP_BAN) {
                fee_desc = GlobalData.getStringResource(RS.string.zpw_string_fee_upgrade_level);
            }
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
