package vn.com.vng.zalopay.menu.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.model.MenuItemType;

/**
 * Created by longlv on 04/05/2016.
 * *
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
            ViewHeaderHolder viewHolder;
            if (convertView == null || convertView.getTag() instanceof ItemViewHolder) {
                convertView = mLayoutInflater.inflate(R.layout.layout_item_drawer_header, parent, false);
                viewHolder = new ViewHeaderHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHeaderHolder) convertView.getTag();
            }

            MenuItem item = getItem(position);

            if (item != null) {
                viewHolder.bindView(item);
            }

        } else if (viewType == MenuItemType.ITEM.getValue()) {
            ItemViewHolder viewHolder;
            if (convertView == null || !(convertView.getTag() instanceof ItemViewHolder)) {
                convertView = mLayoutInflater.inflate(R.layout.layout_item_drawer, parent, false);
                viewHolder = new ItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ItemViewHolder) convertView.getTag();
            }

            MenuItem item = getItem(position);

            if (item != null) {
                viewHolder.bindView(item);
            }
        }

        return convertView;
    }

    static class ViewHeaderHolder {
        @BindView(R.id.tvTitle)
        TextView mTvTitle;

        ViewHeaderHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bindView(MenuItem menuItem) {
            mTvTitle.setText(menuItem.getTitle());
        }
    }

    static class ItemViewHolder {
        @BindView(R.id.imgIcon)
        ImageView mImageView;
        @BindView(R.id.imgArrowRight)
        ImageView mImageSubIcon;

        @BindView(R.id.viewSeparate)
        View viewSeparate;

        @BindView(R.id.tvTitle)
        TextView mTvTitle;


        ItemViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bindView(MenuItem menuItem) {
            mTvTitle.setText(menuItem.getTitle());
            mImageView.setImageResource(menuItem.getIconResource());
            if (menuItem.isShowDivider()) {
                viewSeparate.setVisibility(View.VISIBLE);
            } else {
                viewSeparate.setVisibility(View.INVISIBLE);
            }

          /*  if (menuItem.getSubIconResource() != null) {
                mImageSubIcon.setImageResource(menuItem.getSubIconResource());
                mImageSubIcon.setVisibility(View.VISIBLE);
            } else {
                mImageSubIcon.setVisibility(View.GONE);
            }*/
        }
    }
}