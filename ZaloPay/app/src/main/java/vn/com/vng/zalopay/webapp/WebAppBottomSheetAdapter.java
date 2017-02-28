package vn.com.vng.zalopay.webapp;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;

/**
 * Created by khattn on 2/21/17.
 */

public class WebAppBottomSheetAdapter extends AbsRecyclerAdapter<WebAppBottomSheetItem, WebAppBottomSheetAdapter.ViewHolder> {

    private WebAppBottomSheetAdapter.OnClickItemListener itemListener;

    WebAppBottomSheetAdapter(Context context, WebAppBottomSheetAdapter.OnClickItemListener itemListener) {
        super(context);
        this.itemListener = itemListener;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        itemListener = null;
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public WebAppBottomSheetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WebAppBottomSheetAdapter.ViewHolder(mInflater
                .inflate(R.layout.row_card_webapp_item, parent, false), itemListener);
    }

    @Override
    public void onBindViewHolder(WebAppBottomSheetAdapter.ViewHolder holder, int position) {
        WebAppBottomSheetItem item = getItem(position);
        holder.bindView(item);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void insertItems(List<WebAppBottomSheetItem> items) {
        if (items == null || items.isEmpty()) return;
        synchronized (_lock) {
            for (WebAppBottomSheetItem item : items) {
                insert(item);
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_icon)
        ImageView mShareZaloIcon;
        @BindView(R.id.if_icon)
        IconFont mIcon;
        @BindView(R.id.tv_name)
        TextView mName;

        private OnClickItemListener mItemListener;

        public ViewHolder(View itemView, OnClickItemListener itemListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mItemListener = itemListener;
        }

        @OnClick(R.id.iv_icon)
        public void OnShareZaloClick() {
            if (mItemListener != null) {
                mItemListener.onClickItem(getAdapterPosition());
            }
        }

        @OnClick(R.id.if_icon)
        public void OnClick() {
            if (mItemListener != null) {
                mItemListener.onClickItem(getAdapterPosition());
            }
        }

        void bindView(WebAppBottomSheetItem item) {
            mName.setText(getContext().getResources().getText(item.resStrId));
            if(item.iconResource != null) {
                mIcon.setIcon(item.iconResource);
                mIcon.setIconColor(item.iconColor);
                mIcon.setVisibility(View.VISIBLE);
            } else {
                mShareZaloIcon.setImageResource(item.resImgId);
                mShareZaloIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnClickItemListener {
        void onClickItem(int position);
    }
}
