package vn.com.zalopay.wallet.ui.channellist;


import android.content.Context;

import com.zalopay.ui.widget.mutilview.recyclerview.EnumListBindAdapter;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.ui.channellist.item.AbstractItem;
import vn.com.zalopay.wallet.ui.channellist.item.InputItem;
import vn.com.zalopay.wallet.ui.channellist.item.MapItem;
import vn.com.zalopay.wallet.ui.channellist.item.TitleItem;
import vn.com.zalopay.wallet.ui.channellist.item.ZaloPayItem;

/**
 * Created by chucvv on 6/13/17.
 */
public class ChannelListAdapter extends EnumListBindAdapter<ChannelListAdapter.ItemType> {

    public ChannelListAdapter() {
    }

    public void addZaloPayBinder(Context context, long amount, UserInfo userInfo, @TransactionType int transtype) {
        addBinder(new ZaloPayItem(context, amount, userInfo, transtype, this));
    }

    public void addMapBinder(Context context, long amount, int user_level) {
        addBinder(new MapItem(context, amount, user_level, this));
    }

    public void addInputBinder(Context context, long amount, UserInfo userInfo, @TransactionType int transtype) {
        addBinder(new InputItem(context, amount, userInfo, transtype, this));
    }

    public void addTitle() {
        addBinder(new TitleItem(this));
    }

    public void add(ItemType pItemType, PaymentChannel pChannel) {
        AbstractItem abstractItem = getDataBinder(pItemType);
        if (abstractItem != null) {
            abstractItem.add(pChannel);
        }
    }

    public void addAll(ItemType pItemType, List<PaymentChannel> channelList) {
        AbstractItem abstractItem = getDataBinder(pItemType);
        if (abstractItem != null) {
            abstractItem.addAll(channelList);
        }
    }

    public List<PaymentChannel> getDataSet(ItemType pItemType) {
        AbstractItem abstractItem = getDataBinder(pItemType);
        return abstractItem != null ? abstractItem.getDataSet() : null;
    }

    public boolean hasTitle() {
        return getDataBinder(ItemType.TITLE) instanceof TitleItem;
    }

    public void setTitle(String title) {
        TitleItem titleItem = getDataBinder(ItemType.TITLE);
        titleItem.setTitle(title);
        titleItem.notifyBinderDataSetChanged();
    }

    public void notifyBinderItemChanged(int position) {
        notifyItemChanged(position);
        //notifyDataSetChanged();//need to optimize later
    }


    public enum ItemType {
        ZALOPAY, MAP, TITLE, INPUT
    }
}
