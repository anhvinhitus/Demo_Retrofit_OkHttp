package vn.com.zalopay.wallet.ui.channellist.item;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zalopay.ui.widget.mutilview.recyclerview.DataBindAdapter;
import com.zalopay.ui.widget.mutilview.recyclerview.DataBinder;

import vn.com.zalopay.wallet.R;

/**
 * Created by chucvv on 6/14/17.
 */

public class TitleItem extends DataBinder<TitleItem.ViewHolder> {
    private String title;

    public TitleItem(DataBindAdapter dataBindAdapter) {
        super(dataBindAdapter);
    }

    public void setTitle(String title) {
        this.title = title;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder newViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.title_item, parent, false);
        return new TitleItem.ViewHolder(view);
    }

    @Override
    public void bindViewHolder(ViewHolder holder, int position) {
        holder.title_textview.setText(title);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title_textview;

        public ViewHolder(View view) {
            super(view);
            title_textview = (TextView) view.findViewById(R.id.title_textview);
        }
    }
}
