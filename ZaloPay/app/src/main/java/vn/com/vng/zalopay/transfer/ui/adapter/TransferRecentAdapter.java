package vn.com.vng.zalopay.transfer.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.RecentTransaction;

/**
 * Created by AnhHieu on 8/17/16.
 */
public class TransferRecentAdapter extends AbsRecyclerAdapter<RecentTransaction, TransferRecentAdapter.ViewHolder> {

    public interface OnClickTransferRecentListener {
        void onItemRecentClick(RecentTransaction item);
    }

    OnClickTransferRecentListener listener;

    public TransferRecentAdapter(Context context, OnClickTransferRecentListener listener) {
        super(context);
        this.listener = listener;
    }

    public OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {

        }

        @Override
        public boolean onListItemLongClick(View anchor, int position) {
            return false;
        }
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.fragment_trasfer_recent_item, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RecentTransaction item = getItem(position);
        if (item != null) {
            holder.bindView(item);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        OnItemClickListener listener;

        TextView mTvDisplayName;
        TextView mTvPhone;
        ImageView mImgAvatar;
        ImageView mImgTransferType;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.listener = listener;
        }

        public void bindView(RecentTransaction item) {

        }
    }
}
