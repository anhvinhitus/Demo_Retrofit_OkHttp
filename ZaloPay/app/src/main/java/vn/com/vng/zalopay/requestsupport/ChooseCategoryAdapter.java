package vn.com.vng.zalopay.requestsupport;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Func2;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.AppResource;

public class ChooseCategoryAdapter extends AbsRecyclerAdapter<AppResource, RecyclerView.ViewHolder> {

    private static final int VIEWTYPE_HEADER = 0;
    private static final int VIEWTYPE_ITEM = 1;

    private ChooseCategoryAdapter.OnClickAppListener listener;

    public ChooseCategoryAdapter(Context context, ChooseCategoryAdapter.OnClickAppListener listener) {
        super(context);
        this.listener = listener;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {

            AppResource app = getItem(position);
            if (listener != null && app != null) {
                listener.onClickAppListener(app);
            }
        }

        @Override
        public boolean onListItemLongClick(View anchor, int position) {
            return false;
        }
    };

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mOnItemClickListener = null;
        listener = null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_HEADER:
                return new ChooseCategoryAdapter.HeaderViewHolder(mInflater
                        .inflate(R.layout.header_choose_category, parent, false));
            case VIEWTYPE_ITEM:
                return new ChooseCategoryAdapter.ViewHolder(mInflater
                        .inflate(R.layout.row_category_app_layout, parent, false), mOnItemClickListener);
            default:
                Timber.w("Unknown viewType: %s", viewType);
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            AppResource item = getItem(position);
            if (item != null) {
                ((ViewHolder) holder).bindView(item, position);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEWTYPE_HEADER;
        }

        return VIEWTYPE_ITEM;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private OnItemClickListener listener;

        @BindView(R.id.tv_name)
        TextView mNameView;

        @BindView(R.id.divider)
        View mDivider;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.itemLayout)
        public void onClickItem(View v) {
            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }

        public void bindView(AppResource appResource, int position) {
            mNameView.setText(appResource.appname);
            if(position == getItemCount() - 1) {
                mDivider.setBackgroundColor(Color.WHITE);
            }
        }
    }

    public interface OnClickAppListener {
        void onClickAppListener(AppResource app);
    }
}