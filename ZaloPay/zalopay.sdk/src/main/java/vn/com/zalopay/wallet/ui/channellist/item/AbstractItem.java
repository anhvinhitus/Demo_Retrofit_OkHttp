package vn.com.zalopay.wallet.ui.channellist.item;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zalopay.ui.widget.mutilview.recyclerview.DataBindAdapter;
import com.zalopay.ui.widget.mutilview.recyclerview.DataBinder;

import java.util.ArrayList;
import java.util.List;

import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.helper.RenderHelper;

/**
 * Created by chucvv on 6/14/17.
 */

public abstract class AbstractItem<T extends AbstractItem.ViewHolder> extends DataBinder<T> {
    protected List<PaymentChannel> mDataSet = new ArrayList<>();
    protected long amount;
    protected Context mContext;
    private int bankLogoSize = 0;
    private int marginLeft = 0;

    public AbstractItem(Context context, long amount, DataBindAdapter dataBindAdapter) {
        super(dataBindAdapter);
        this.mContext = context;
        this.amount = amount;
        this.bankLogoSize = (int) mContext.getResources().getDimension(R.dimen.sdk_ic_channel_size);
        this.marginLeft = (int) mContext.getResources().getDimension(R.dimen.zpw_item_listview_padding_left_right);
    }

    public abstract T onNewBindHolder(ViewGroup parent);

    public abstract void onBindViewHolder(T holder, int position);

    private void adjustLine(View pLine) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) pLine.getLayoutParams();
        params.leftMargin = bankLogoSize * 2 + marginLeft * 2;
        pLine.requestLayout();
    }

    protected String getFeeDesc(PaymentChannel pChannel) {
        String fee_desc = mContext.getString(R.string.zpw_string_fee_free);
        if (pChannel.hasFee()) {
            fee_desc = StringUtil.formatVnCurrence(String.valueOf(pChannel.totalfee));
        }
        return fee_desc;
    }

    protected String formatFeeDesc(String fee_desc) {
        if (!TextUtils.isEmpty(fee_desc)
                && !fee_desc.equals(mContext.getString(R.string.default_message_pmc_fee))
                && !fee_desc.equals(mContext.getString(R.string.zpw_string_fee_free))
                && !fee_desc.equals(mContext.getString(R.string.zpw_string_fee_upgrade_level))) {
            fee_desc = String.format(mContext.getString(R.string.zpw_string_fee_format), fee_desc);
        }
        return fee_desc;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void add(PaymentChannel pChannel) {
        mDataSet.add(pChannel);
        notifyBinderDataSetChanged();
    }

    public void addAll(List<PaymentChannel> channelList) {
        mDataSet.addAll(channelList);
        notifyBinderDataSetChanged();
    }

    public void clear() {
        mDataSet.clear();
        notifyBinderDataSetChanged();
    }

    public List<PaymentChannel> getDataSet() {
        return mDataSet;
    }

    @Override
    public T newViewHolder(ViewGroup parent) {
        return onNewBindHolder(parent);
    }
    /* @Override
    public T newViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), parent, false);
        try {
            Constructor constructor = clazz.getConstructor(View.class);
            return (T) constructor.newInstance(view);
        } catch (Exception e) {
            Log.e(this, e);
        }
        return null;
    }*/

    private String getWarningDesc(PaymentChannel pChannel) {
        String mess;
        if (!pChannel.isAllowPmcQuota()) {
            mess = mContext.getString(R.string.zpw_string_channel_not_allow_by_amount);
            if ((amount + pChannel.totalfee) < pChannel.minvalue) {
                mess = mContext.getString(R.string.zpw_string_channel_not_allow_by_amount_small);
            }
        } else if (pChannel.isMaintenance() && pChannel.isMapCardChannel()) {
            mess = mContext.getString(R.string.zpw_string_bank_maintenance);
        } else if (pChannel.isMaintenance()) {
            mess = mContext.getString(R.string.zpw_string_channel_maintenance);
        } else if (pChannel.isZaloPayChannel() && !pChannel.isAllowOrderAmount()) {
            mess = mContext.getString(R.string.sdk_warning_balance_error);
        } else if (!pChannel.isAllowOrderAmount()) {
            StringBuilder builder = new StringBuilder();
            if (pChannel.hasFee()) {
                builder.append(String.format(mContext.getString(R.string.zpw_string_fee_format), StringUtil.formatVnCurrence(String.valueOf(pChannel.totalfee))));
            }
            builder.append(".").append(mContext.getString(R.string.zpw_string_channel_not_allow_by_fee));
            mess = builder.toString();
        } else {
            mess = mContext.getString(R.string.zpw_string_channel_not_allow);
        }
        return mess;
    }

    private void renderWarningDesc(T holder, PaymentChannel channel) {
        String desc = getWarningDesc(channel);
        boolean zaloPay = this instanceof ZaloPayItem;
        if (zaloPay && !channel.isAllowOrderAmount()) {
            ZaloPayItem zaloPayItem = (ZaloPayItem) this;
            zaloPayItem.renderBalanceError((ZaloPayItem.ViewHolder) holder, desc);
        } else {
            holder.fee_textview.setText(RenderHelper.getHtml(desc));
        }
        holder.name_textview.setTextColor(ContextCompat.getColor(mContext, (R.color.text_color)));
        holder.fee_textview.setTextColor(ContextCompat.getColor(mContext, (R.color.text_color_red_blur)));
        holder.icon_imageview.setImageAlpha(100);
    }

    protected void renderDesc(T holder, String desc) {
        holder.fee_textview.setText(desc);
    }

    @Override
    public void bindViewHolder(T holder, int position) {
        PaymentChannel channel = mDataSet.get(position);
        ResourceManager.loadImageIntoView(holder.icon_imageview, channel.channel_icon);
        holder.name_textview.setText(channel.pmcname);
        holder.select_imageview.setVisibility(channel.select ? View.VISIBLE : View.INVISIBLE);
        renderGeneric(holder);
        //show not support channel
        if (!channel.meetPaymentCondition()) {
            renderWarningDesc(holder, channel);
        } else {
            onBindViewHolder(holder, position);
        }
        //indent line bottom
        if (this instanceof ZaloPayItem) {
            adjustLine(holder.line);
        } else if (position + 1 < getItemCount()) {
            adjustLine(holder.line);
        }
    }

    protected void renderGeneric(T holder) {
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name_textview;
        ImageView icon_imageview, select_imageview;
        TextView fee_textview;
        View line;

        public ViewHolder(View view) {
            super(view);
            name_textview = (TextView) view.findViewById(R.id.name_textview);
            icon_imageview = (ImageView) view.findViewById(R.id.icon_imageview);
            select_imageview = (ImageView) view.findViewById(R.id.select_imageview);
            fee_textview = (TextView) view.findViewById(R.id.fee_textview);
            line = view.findViewById(R.id.line);
        }
    }
}
