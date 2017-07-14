package vn.com.zalopay.wallet.ui.channellist.item;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zalopay.ui.widget.mutilview.recyclerview.DataBindAdapter;

import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.TransactionType;

/**
 * Created by chucvv on 6/14/17.
 */

public class ZaloPayItem extends AbstractItem<ZaloPayItem.ViewHolder> {
    @TransactionType
    int transtype;
    private UserInfo userInfo;

    public ZaloPayItem(Context context, long amount, UserInfo userInfo, @TransactionType int transtype, DataBindAdapter dataBindAdapter) {
        super(context, amount, dataBindAdapter);
        this.userInfo = userInfo;
        this.transtype = transtype;
    }

    @Override
    public ViewHolder onNewBindHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.zalopay_channel_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PaymentChannel channel = mDataSet.get(position);
        String fee_desc = getFeeDesc(channel);
        if (!TextUtils.isEmpty(fee_desc)) {
            fee_desc = formatFeeDesc(fee_desc);
        }
        renderDesc(holder, fee_desc);
        renderBalance(holder);
    }

    @Override
    protected void renderGeneric(ViewHolder holder) {
        super.renderGeneric(holder);
        renderBalance(holder);
    }

    public void renderBalance(ViewHolder holder) {
        long balance = userInfo.balance;
        balance = balance > 0 ? balance : 0;
        holder.balance_textview.setText(StringUtil.formatVnCurrence(String.valueOf(balance)));
    }

    public void renderBalanceError(ZaloPayItem.ViewHolder holder, String warningDesc) {
        holder.balance_error_textview.setText(warningDesc);
        holder.balance_error_textview.setVisibility(View.VISIBLE);
        holder.fee_textview.setVisibility(View.GONE);
    }

    static class ViewHolder extends AbstractItem.ViewHolder {
        TextView balance_textview, balance_error_textview;
        View balance_linearlayout;

        public ViewHolder(View view) {
            super(view);
            balance_textview = (TextView) view.findViewById(R.id.balance_textview);
            balance_error_textview = (TextView) view.findViewById(R.id.balance_error_textview);
            balance_linearlayout = view.findViewById(R.id.balance_linearlayout);
        }
    }
}
