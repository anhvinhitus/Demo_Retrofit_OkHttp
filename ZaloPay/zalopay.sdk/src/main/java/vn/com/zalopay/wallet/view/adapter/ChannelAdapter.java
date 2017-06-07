package vn.com.zalopay.wallet.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentGatewayActivity;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {
    protected static final int mAlphaColor = 100;
    protected Context mContext;
    protected List<PaymentChannel> mChannelList;
    protected int mLayoutId;
    protected PaymentInfoHelper mPaymentInfoHelper;

    public ChannelAdapter(Context pContext, List<PaymentChannel> pChannelList, int pLayoutId, PaymentInfoHelper paymentInfoHelper) {
        mContext = pContext;
        mChannelList = pChannelList;
        mLayoutId = pLayoutId;
        mPaymentInfoHelper = paymentInfoHelper;
    }

    protected String getChannelSubTitle(PaymentChannel pChannel) {
        String mess = null;
        if (!pChannel.isAllowByAmount()) {
            mess = GlobalData.getStringResource(RS.string.zpw_string_channel_not_allow_by_amount);
            if ((mPaymentInfoHelper.getAmount() + pChannel.totalfee) < pChannel.minvalue) {
                mess = GlobalData.getStringResource(RS.string.zpw_string_channel_not_allow_by_amount_small);
            }
        } else if (pChannel.isMaintenance() && pChannel.isMapCardChannel()) {
            mess = GlobalData.getStringResource(RS.string.zpw_string_bank_maintenance);
        } else if (pChannel.isMaintenance()) {
            mess = GlobalData.getStringResource(RS.string.zpw_string_channel_maintenance);
        } else if (!pChannel.isAllowByAmountAndFee()) {
            if (pChannel.hasFee()) {
                mess = String.format(GlobalData.getStringResource(RS.string.zpw_string_fee_format), StringUtil.formatVnCurrence(String.valueOf(pChannel.totalfee)));
            }
            mess += ". " + GlobalData.getStringResource(RS.string.zpw_string_channel_not_allow_by_fee);
        } else {
            mess = GlobalData.getStringResource(RS.string.zpw_string_channel_not_allow);
        }

        return mess;
    }

    @Override
    public ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ChannelViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChannelViewHolder holder, int position) {
        try {
            PaymentChannel channel = mChannelList.get(position);
            //icon channel
            ResourceManager.loadImageIntoView(holder.channelIconImageView, channel.channel_icon);

            //indent line view to the right
            int bitmapSize = (int) (2 * mContext.getResources().getDimension(R.dimen.zpw_item_listview_padding_left_right));
            bitmapSize += mContext.getResources().getDimension(R.dimen.sdk_ic_channal_size);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) SdkUtils.convertDpToPixel(0.5f, mContext));
            params.setMargins(bitmapSize, 0, 0, 0);
            holder.lineView.setLayoutParams(params);

            //next icon view
            ResourceManager.loadImageIntoView(holder.nextIconImageView, channel.channel_next_icon);

            //channel name
            holder.channelNameTextView.setText(channel.pmcname);

            String feeDescription = GlobalData.getStringResource(RS.string.zpw_string_fee_free);
            //show not support channel
            if (!channel.isEnable() || !channel.isAllowByAmount() || channel.isMaintenance() || !channel.isAllowByAmountAndFee()) {
                feeDescription = getChannelSubTitle(channel);
                holder.channelFeeTextView.setText(Html.fromHtml(feeDescription));

                holder.channelNameTextView.setTextColor(mContext.getResources().getColor(R.color.text_color));
                holder.channelFeeTextView.setTextColor(mContext.getResources().getColor(R.color.text_color_red_blur));

                holder.nextIconImageView.setVisibility(View.GONE);
                holder.channelIconImageView.setAlpha(mAlphaColor);
                return;
            }

            //calculate fee
            if (channel.hasFee()) {
                feeDescription = StringUtil.formatVnCurrence(String.valueOf(channel.totalfee));
            }

            //show warning if user need to upgrade
            //zalopay channel
            int user_level = mPaymentInfoHelper.getLevel();
            if ((channel.isZaloPayChannel() && user_level < BuildConfig.level_allow_use_zalopay) ||
                    ((channel.isBankAccount() && user_level < BuildConfig.level_allow_bankaccount))) {
                //check map table for allow
                int iCheck = mPaymentInfoHelper.getUserInfo().getPermissionByChannelMap(channel.pmcid, mPaymentInfoHelper.getTranstype());
                //error map table from server, show dialog alert and quit sdk
                if (iCheck == Constants.LEVELMAP_INVALID) {
                    ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).showErrorDialog(() -> {
                        mPaymentInfoHelper.setResult(PaymentStatus.INVALID_DATA);
                        ((PaymentGatewayActivity) BasePaymentActivity.getCurrentActivity()).recycleActivity();
                    }, GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error));
                }
                //not allow zalopay
                else if (iCheck == Constants.LEVELMAP_BAN) {
                    feeDescription = GlobalData.getStringResource(RS.string.zpw_string_fee_upgrade_level);
                    holder.nextIconImageView.setVisibility(View.GONE);
                }
            }
            //map card channel
            if (channel.isMapCardChannel() && user_level < BuildConfig.level_allow_cardmap) {
                feeDescription = GlobalData.getStringResource(RS.string.zpw_string_fee_upgrade_level);
                holder.nextIconImageView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(feeDescription) && !feeDescription.equals(GlobalData.getStringResource(RS.string.zpw_string_fee_free))
                    && !feeDescription.equals(GlobalData.getStringResource(RS.string.zpw_string_fee_upgrade_level))) {
                feeDescription = String.format(GlobalData.getStringResource(RS.string.zpw_string_fee_format), feeDescription);
            }

            holder.channelFeeTextView.setText(feeDescription);

        } catch (Exception ex) {
            Log.e(this, ex);
            holder.channelFeeTextView.setText(GlobalData.getStringResource(RS.string.zpw_string_fee_free));
        }
    }

    @Override
    public int getItemCount() {
        return mChannelList.size();
    }

    public class ChannelViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView channelIconImageView, nextIconImageView;
        public TextView channelNameTextView, channelFeeTextView;
        public View lineView;

        public ChannelViewHolder(View view) {
            super(view);
            channelIconImageView = (SimpleDraweeView) view.findViewById(R.id.zpw_channel_icon);
            nextIconImageView = (SimpleDraweeView) view.findViewById(R.id.zpw_channel_next_icon);
            channelNameTextView = (TextView) view.findViewById(R.id.zpw_channel_name_textview);
            channelFeeTextView = (TextView) view.findViewById(R.id.zpw_channel_fee);
            lineView = view.findViewById(R.id.zpw_line_view);
        }
    }
}
