package vn.com.vng.zalopay.menu.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class MenuItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MenuItem> mValues;
    private final MenuItemClickListener mListener;
    private Context mContext;

    public MenuItemAdapter(Context context, List<MenuItem> items, MenuItemClickListener listener) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        MenuItem menuItem = mValues.get(position);
        if (menuItem.getItemType() == MenuItemType.HEADER) {
            return MenuItemType.HEADER.getValue();
        }
        return MenuItemType.ITEM.getValue();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == MenuItemType.HEADER.getValue()) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_item_drawer_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == MenuItemType.ITEM.getValue()) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_item_drawer, parent, false);
            return new ItemViewHolder(view);
        }
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MenuItem menuItem = mValues.get(position);
        if (menuItem == null) {
            return;
        }
        if (menuItem.getItemType() == MenuItemType.HEADER) {
            bindHeaderView(holder, menuItem);
        } else if (menuItem.getItemType() == MenuItemType.ITEM) {
            bindMenuItemView(holder, menuItem);
        }
    }

    private void onItemClick(View view, MenuItem menuItem) {

    }

    private void bindMenuItemView(RecyclerView.ViewHolder holder, final MenuItem menuItem) {
        ((ItemViewHolder)holder).mContentView.setText(menuItem.getTitle());
        ((ItemViewHolder)holder).mImageView.setImageResource(menuItem.getResource());
        ((ItemViewHolder)holder).mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, menuItem);
            }
        });
    }

    private void bindHeaderView(RecyclerView.ViewHolder holder, final MenuItem menuItem) {
        ((HeaderViewHolder)holder).mTvHeader.setText(menuItem.getTitle());
        ((HeaderViewHolder)holder).mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(v, menuItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mValues == null) return 0;
        return mValues.size();
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTvHeader;
        public MenuItem mItem;

        public HeaderViewHolder(View view) {
            super(view);
            mView = view;
            mTvHeader = (TextView) view.findViewById(R.id.tvHeader);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTvHeader.getText() + "'";
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImageView;
        public final TextView mContentView;
        public MenuItem mItem;

        public ItemViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.imgIcon);
            mContentView = (TextView) view.findViewById(R.id.tvHeader);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

//    public void setData(List<MenuItem> data){
//        mValues = new ArrayList<>();
//        for (MenuItem bank : data){
//            if (mType == BankListFragment.TYPE_GLOBAL_CARD && bank.isCreditCard()){
//                mValues.add(bank);
//            } else if (mType == BankListFragment.TYPE_BANK_ATM && (bank.isATM() || bank.isInternetBanking())){
//                mValues.add(bank);
//            }
//        }
//        notifyDataSetChanged();
//    }
}