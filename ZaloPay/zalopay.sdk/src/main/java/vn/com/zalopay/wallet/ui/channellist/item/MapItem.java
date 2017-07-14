package vn.com.zalopay.wallet.ui.channellist.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zalopay.ui.widget.mutilview.recyclerview.DataBindAdapter;

import vn.com.zalopay.wallet.R;

/**
 * Created by chucvv on 6/14/17.
 */

public class MapItem extends AbstractItem<MapItem.ViewHolder> {
    public MapItem(Context context, long amount, DataBindAdapter dataBindAdapter) {
        super(context, amount, dataBindAdapter);
    }

    @Override
    public MapItem.ViewHolder onNewBindHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item, parent, false);
        return new MapItem.ViewHolder(view);
    }

    static class ViewHolder extends AbstractItem.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}
