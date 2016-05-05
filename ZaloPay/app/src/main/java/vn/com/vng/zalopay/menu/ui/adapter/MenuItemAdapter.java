package vn.com.vng.zalopay.menu.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.menu.listener.MenuItemClickListener;
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.model.MenuItemType;

/**
 * Created by longlv on 04/05/2016.
 */
public class MenuItemAdapter extends ArrayAdapter<MenuItem> {

    private final MenuItemClickListener mListener;
    private final LayoutInflater mLayoutInflater;

    public MenuItemAdapter(Context context, List<MenuItem> items, MenuItemClickListener listener) {
        super(context, R.layout.layout_item_drawer, items);
        mListener = listener;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemViewType(int position) {
        MenuItem menuItem = getItem(position);
        if (menuItem.getItemType() == MenuItemType.HEADER) {
            return MenuItemType.HEADER.getValue();
        }
        return MenuItemType.ITEM.getValue();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        if (viewType == MenuItemType.HEADER.getValue()) {
            ViewHolder viewHolder = null;
            if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.layout_item_drawer_header, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            bindHeaderView(viewHolder, getItem(position));
        } else if (viewType == MenuItemType.ITEM.getValue()) {
            ItemViewHolder viewHolder = null;
            if (convertView == null || !(convertView.getTag() instanceof ItemViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.layout_item_drawer, null);
                viewHolder = new ItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ItemViewHolder) convertView.getTag();
            }
            bindMenuItemView(viewHolder, getItem(position));
        }

        return convertView;
    }

    private void onItemClick(View view, MenuItem menuItem) {

    }

    private void bindMenuItemView(ViewHolder holder, final MenuItem menuItem) {
        ((ItemViewHolder)holder).mTvTitle.setText(menuItem.getTitle());
        ((ItemViewHolder)holder).mImageView.setImageResource(menuItem.getResource());
        ((ItemViewHolder)holder).mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, menuItem);
            }
        });
    }

    private void bindHeaderView(ViewHolder holder, final MenuItem menuItem) {
        ((ViewHolder)holder).mTvTitle.setText(menuItem.getTitle());
        ((ViewHolder)holder).mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, menuItem);
            }
        });
    }

    public class ViewHolder {
        public final View mView;
        public final TextView mTvTitle;
        public MenuItem mItem;

        public ViewHolder(View view) {
            mView = view;
            mTvTitle = (TextView) view.findViewById(R.id.tvTitle);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTvTitle.getText() + "'";
        }
    }

    public class ItemViewHolder extends ViewHolder{
        public final ImageView mImageView;

        public ItemViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.imgIcon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTvTitle.getText() + "'";
        }
    }
}