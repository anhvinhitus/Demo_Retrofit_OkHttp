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
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.model.MenuItemType;

/**
 * Created by longlv on 04/05/2016.
 */
public class MenuItemAdapter extends ArrayAdapter<MenuItem> {

    private final LayoutInflater mLayoutInflater;

    public MenuItemAdapter(Context context, List<MenuItem> items) {
        super(context, R.layout.layout_item_drawer, items);

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
            if (convertView == null || convertView.getTag() instanceof ItemViewHolder) {
                convertView = mLayoutInflater.inflate(R.layout.layout_item_drawer_header, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            bindHeaderView(viewHolder, getItem(position));
        } else if (viewType == MenuItemType.ITEM.getValue()) {
            ItemViewHolder viewHolder = null;
            if (convertView == null || !(convertView.getTag() instanceof ItemViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.layout_item_drawer, parent, false);
                viewHolder = new ItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ItemViewHolder) convertView.getTag();
            }
            bindMenuItemView(viewHolder, getItem(position));
        }

        return convertView;
    }

    private void bindMenuItemView(ViewHolder holder, final MenuItem menuItem) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.mTvTitle.setText(menuItem.getTitle());
        viewHolder.mImageView.setImageResource(menuItem.getIconResource());
        if (menuItem.isShowDivider()) {
            viewHolder.viewSeparate.setVisibility(View.VISIBLE);
        } else {
            viewHolder.viewSeparate.setVisibility(View.INVISIBLE);
        }
        if (menuItem.getSubIconResource() != null) {
            viewHolder.mImageSubIcon.setImageResource(menuItem.getSubIconResource());
            viewHolder.mImageSubIcon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mImageSubIcon.setVisibility(View.GONE);
        }
    }

    private void bindHeaderView(ViewHolder holder, final MenuItem menuItem) {
        ViewHolder viewHolder = holder;
        viewHolder.mTvTitle.setText(menuItem.getTitle());
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

    public class ItemViewHolder extends ViewHolder {
        public final ImageView mImageView;
        public final ImageView mImageSubIcon;
        public final View viewSeparate;

        public ItemViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.imgIcon);
            mImageSubIcon = (ImageView) view.findViewById(R.id.imgArrowRight);
            viewSeparate = view.findViewById(R.id.viewSeparate);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTvTitle.getText() + "'";
        }
    }
}