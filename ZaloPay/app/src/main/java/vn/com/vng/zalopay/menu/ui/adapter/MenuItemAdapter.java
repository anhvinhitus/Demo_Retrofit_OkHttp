package vn.com.vng.zalopay.menu.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.AndroidApplication;
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
        super(context, R.layout.row_left_menu, items);

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemViewType(int position) {
        MenuItem menuItem = getItem(position);
        if (menuItem != null && menuItem.itemType == MenuItemType.HEADER) {
            return MenuItemType.HEADER.getValue();
        }
        return MenuItemType.ITEM.getValue();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        int viewType = getItemViewType(position);

        if (viewType == MenuItemType.HEADER.getValue()) {
            ViewHeaderHolder viewHolder;
            if (convertView == null || convertView.getTag() instanceof ItemViewHolder) {
                convertView = mLayoutInflater.inflate(R.layout.row_section_left_menu, parent, false);
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
                convertView = mLayoutInflater.inflate(R.layout.row_left_menu, parent, false);
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
            mTvTitle.setText(menuItem.title);
        }
    }

    static class ItemViewHolder {
        @BindView(R.id.imgIcon)
        IconFont mIconFont;

        @BindView(R.id.viewSeparate)
        View viewSeparate;

        @BindView(R.id.tvTitle)
        TextView mTvTitle;


        ItemViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bindView(MenuItem menuItem) {
            mTvTitle.setText(menuItem.title);
            mIconFont.setIcon(menuItem.iconResource);
            mIconFont.setIconColor(menuItem.iconColor);
            if (menuItem.showDivider) {
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