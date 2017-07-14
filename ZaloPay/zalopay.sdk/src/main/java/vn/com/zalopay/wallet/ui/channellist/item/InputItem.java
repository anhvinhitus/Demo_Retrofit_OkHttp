package vn.com.zalopay.wallet.ui.channellist.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zalopay.ui.widget.mutilview.recyclerview.DataBindAdapter;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.constants.TransactionType;

/**
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item, parent, false);
        return new InputItem.ViewHolder(view);
    }

    static class ViewHolder extends AbstractItem.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}
