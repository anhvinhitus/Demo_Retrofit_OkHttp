package vn.com.zalopay.wallet.ui.channellist.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.mutilview.recyclerview.DataBindAdapter;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.RS;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.repository.ResourceManager;

/*
 * Created by chucvv on 6/14/17.
 */

public class InputItem extends AbstractItem<InputItem.ViewHolder> {
    @TransactionType
    int transtype;

    public InputItem(Context context, long amount, @TransactionType int transtype, DataBindAdapter dataBindAdapter) {
        super(context, amount, dataBindAdapter);
        this.transtype = transtype;
    }

    @Override
    public InputItem.ViewHolder onNewBindHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.link_channel_item, parent, false);
        return new InputItem.ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ResourceManager.loadLocalSDKImage(holder.next_img, RS.drawable.ic_next);
        PaymentChannel channel = mDataSet.get(position);
        boolean hasSubText = channel != null && channel.hasOneChannel;
        if (hasSubText) {
            holder.fee_textview.setText(getSubText());
        }
        holder.fee_textview.setVisibility(hasSubText ? View.VISIBLE : View.GONE);
    }

    private String getSubText() {
        if (transtype == TransactionType.TOPUP) {
            return mContext.getResources().getString(R.string.sdk_link_channel_subtext_topup);
        } else if (transtype == TransactionType.MONEY_TRANSFER) {
            return mContext.getResources().getString(R.string.sdk_link_channel_subtext_transfer);
        } else {
            return mContext.getResources().getString(R.string.sdk_link_channel_subtext_pay);
        }
    }

    static class ViewHolder extends AbstractItem.ViewHolder {
        SimpleDraweeView next_img;

        public ViewHolder(View view) {
            super(view);
            next_img = (SimpleDraweeView) view.findViewById(R.id.next_img);
        }
    }
}
