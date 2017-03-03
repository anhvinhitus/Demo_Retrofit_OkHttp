package vn.com.zalopay.wallet.view.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannelView;
import vn.com.zalopay.wallet.datasource.request.SDKReport;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.StringUtil;
import vn.com.zalopay.wallet.utils.ZPWUtils;
import vn.com.zalopay.wallet.view.adapter.holder.ZPWItemChannelHolder;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentGatewayActivity;

/***
 * show channels
 */
public class GatewayChannelListViewAdapter extends ArrayAdapter<DPaymentChannelView> {
    protected int mAlphaColor = 100;
    private Activity mActivity;
    private ArrayList<DPaymentChannelView> mChannelList = null;
    private int mLayoutId;

    public GatewayChannelListViewAdapter(Activity pActivity, int pLayoutId, ArrayList<DPaymentChannelView> pChannelList) {
        super(pActivity, pLayoutId, pChannelList);

        this.mActivity = pActivity;
        this.mLayoutId = pLayoutId;
        this.mChannelList = pChannelList;

    }

    @Override
    public int getCount() {
        return mChannelList.size();
    }

    protected String getMessage(DPaymentChannelView pChannel) {
        String mess = null;
        if (!pChannel.isAllowByAmount()) {
            mess = GlobalData.getStringResource(RS.string.zpw_string_channel_not_allow_by_amount);

            if ((GlobalData.getOrderAmount() + pChannel.totalfee) < pChannel.minvalue) {
                mess = GlobalData.getStringResource(RS.string.zpw_string_channel_not_allow_by_amount_small);
            }
        } else if (pChannel.isMaintenance() && pChannel.isMapCardChannel()) {
            mess = GlobalData.getStringResource(RS.string.zpw_string_bank_maintenance);
        } else if (pChannel.isMaintenance()) {
            mess = GlobalData.getStringResource(RS.string.zpw_string_channel_maintenance);
        } else if (!pChannel.isAllowByAmountAndFee()) {
            if (pChannel.hasFee()) {
                mess = GlobalData.getStringResource(RS.string.zpw_string_fee_label)
                        + " " + StringUtil.formatVnCurrence(String.valueOf(pChannel.totalfee))
                        + " " + GlobalData.getStringResource(RS.string.zpw_string_vnd);
            }

            mess += ". " + GlobalData.getStringResource(RS.string.zpw_string_channel_not_allow_by_fee);
        } else {
            mess = GlobalData.getStringResource(RS.string.zpw_string_channel_not_allow);
        }

        return mess;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView channelIconImageView, nextIconImageView;
        TextView channelNameTextView, channelFeeTextview, currencyUnitTextView;
        View lineView;

        ZPWItemChannelHolder holder;
        //the first load
        if (convertView == null) {
            LayoutInflater inflater = this.mActivity.getLayoutInflater();
            convertView = inflater.inflate(mLayoutId, null);
            holder = new ZPWItemChannelHolder();

            channelIconImageView = (ImageView) convertView.findViewById(R.id.zpw_channel_icon);
            nextIconImageView = (ImageView) convertView.findViewById(R.id.zpw_channel_next_icon);
            channelNameTextView = (TextView) convertView.findViewById(R.id.zpw_channel_name_textview);
            channelFeeTextview = (TextView) convertView.findViewById(R.id.zpw_channel_fee);
            lineView = convertView.findViewById(R.id.zpw_line_view);
            currencyUnitTextView = (TextView) convertView.findViewById(R.id.zpw_title_vnd);
            //save to holder for using later
            holder.channelIconImageView = channelIconImageView;
            holder.nextIconImageView = nextIconImageView;
            holder.channelNameTextView = channelNameTextView;
            holder.channelFeeTextView = channelFeeTextview;
            holder.lineView = lineView;
            holder.currencyUnitTextView = currencyUnitTextView;
            convertView.setTag(holder);
        } else {
            holder = (ZPWItemChannelHolder) convertView.getTag();
            channelIconImageView = holder.channelIconImageView;
            nextIconImageView = holder.nextIconImageView;
            channelNameTextView = holder.channelNameTextView;
            channelFeeTextview = holder.channelFeeTextView;
            lineView = holder.lineView;
            currencyUnitTextView = holder.currencyUnitTextView;
        }

        final DPaymentChannelView channel = this.mChannelList.get(position);
        if (channel != null) {
            try {

                //icon channel
                Bitmap iconChannel = ResourceManager.getImage(channel.channel_icon);
                channelIconImageView.setImageBitmap(iconChannel);
                channelIconImageView.setAlpha(255);

                //remargin line view
                int bitmapSize = (int) (2 * getContext().getResources().getDimension(R.dimen.zpw_item_listview_padding_left_right));
                if (iconChannel != null) {
                    bitmapSize += iconChannel.getWidth();
                }

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) ZPWUtils.convertDpToPixel(0.5f, getContext()));
                params.setMargins(bitmapSize, 0, 0, 0);
                lineView.setLayoutParams(params);

                //next icon view
                nextIconImageView.setImageBitmap(ResourceManager.getImage(channel.channel_next_icon));
                nextIconImageView.setVisibility(View.VISIBLE);

                //channel name
                channelNameTextView.setText(channel.pmcname);
                channelNameTextView.setTextColor(mActivity.getResources().getColor(R.color.text_color_bold));

                //channel fee
                channelFeeTextview.setTextColor(mActivity.getResources().getColor(R.color.text_color_grey));

                //vnd label
                currencyUnitTextView.setVisibility(View.GONE);

                String feeDescription = GlobalData.getStringResource(RS.string.zpw_string_fee_free);

                try {
                    //show not support channel
                    if (!channel.isEnable() || !channel.isAllowByAmount() || channel.isMaintenance() || !channel.isAllowByAmountAndFee()) {
                        feeDescription = getMessage(channel);

                        channelFeeTextview.setText(Html.fromHtml(feeDescription));

                        channelNameTextView.setTextColor(mActivity.getResources().getColor(R.color.text_color));
                        channelFeeTextview.setTextColor(mActivity.getResources().getColor(R.color.text_color_red_blur));

                        nextIconImageView.setVisibility(View.GONE);
                        channelIconImageView.setAlpha(mAlphaColor);

                        return convertView;
                    }

                    //calculate fee
                    if (channel.hasFee()) {
                        feeDescription = StringUtil.formatVnCurrence(String.valueOf(channel.totalfee));
                    }

                    //show warning if user need to upgrade
                    //zalopay channel
                    if (channel.isZaloPayChannel() && GlobalData.getLevel() < Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_level_allow_use_zalopay))) {
                        //check map table for allow
                        int iCheck = GlobalData.checkPermissionByChannelMap(channel.pmcid);

                        //error map table from server, show dialog alert and quit sdk
                        if (iCheck == Constants.LEVELMAP_INVALID) {
                            ((BasePaymentActivity) mActivity).showErrorDialog(new ZPWOnEventDialogListener() {
                                @Override
                                public void onOKevent() {
                                    GlobalData.setResultInvalidInput();
                                    ((PaymentGatewayActivity) mActivity).recycleActivity();
                                }
                            }, GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error));

                            SDKReport.makeReportError(SDKReport.INVALID_USERPROFILE, GsonUtils.toJsonString(GlobalData.getPaymentInfo()));

                        }
                        //not allow zalopay
                        else if (iCheck == Constants.LEVELMAP_BAN) {
                            feeDescription = GlobalData.getStringResource(RS.string.zpw_string_fee_upgrade_level);
                            nextIconImageView.setVisibility(View.GONE);
                        }
                    }
                    //bank account channel
                    if ((channel.isBankAccount() && GlobalData.getLevel() < Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_level_allow_bankaccount)))) {
                        //check map table for allow
                        int iCheck = GlobalData.checkPermissionByChannelMap(channel.pmcid);

                        //error map table from server, show dialog alert and quit sdk
                        if (iCheck == Constants.LEVELMAP_INVALID) {
                            ((BasePaymentActivity) mActivity).showErrorDialog(new ZPWOnEventDialogListener() {
                                @Override
                                public void onOKevent() {
                                    GlobalData.setResultInvalidInput();
                                    ((PaymentGatewayActivity) mActivity).recycleActivity();
                                }
                            }, GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error));

                            SDKReport.makeReportError(SDKReport.INVALID_USERPROFILE, GsonUtils.toJsonString(GlobalData.getPaymentInfo()));

                        }
                        //not allow bank account
                        else if (iCheck == Constants.LEVELMAP_BAN) {
                            feeDescription = GlobalData.getStringResource(RS.string.zpw_string_fee_upgrade_level);
                            nextIconImageView.setVisibility(View.GONE);
                        }
                    }

                    //map card channel
                    if (channel.isMapCardChannel() && GlobalData.getLevel() < Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_level_allow_mapcard))) {
                        feeDescription = GlobalData.getStringResource(RS.string.zpw_string_fee_upgrade_level);
                        nextIconImageView.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    Log.e(this, e);
                    feeDescription = GlobalData.getStringResource(RS.string.zpw_string_fee_free);
                }

                if (!TextUtils.isEmpty(feeDescription) && !feeDescription.equals(GlobalData.getStringResource(RS.string.zpw_string_fee_free))
                        && !feeDescription.equals(GlobalData.getStringResource(RS.string.zpw_string_fee_upgrade_level))) {
                    currencyUnitTextView.setVisibility(View.VISIBLE);
                    feeDescription = GlobalData.getStringResource(RS.string.zpw_string_fee_label) + " " + feeDescription;
                }

                channelFeeTextview.setText(feeDescription);

            } catch (Exception ex) {
                Log.e(this, ex);

                channelFeeTextview.setText(GlobalData.getStringResource(RS.string.zpw_string_fee_free));
                currencyUnitTextView.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}

