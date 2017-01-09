package vn.com.vng.zalopay.requestsupport;

import android.content.Context;
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

public class RequestSupportAdapter extends AbsRecyclerAdapter<AppResource, RequestSupportAdapter.ViewHolder> {

    private RequestSupportAdapter.OnClickAppListener listener;

    public RequestSupportAdapter(Context context, RequestSupportAdapter.OnClickAppListener listener) {
        super(context);
        this.listener = listener;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {

            AppResource app = getItem(position);
            if (listener != null && app != null) {
                listener.onClickAppListener(app, position);
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
    public RequestSupportAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RequestSupportAdapter.ViewHolder(mInflater.inflate(R.layout.row_category_app_layout, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppResource item = getItem(position);
        if (item != null) {
            holder.bindView(item);
        }
    }

    @Override
    public void insertItems(List<AppResource> items) {
        if (items == null || items.isEmpty()) return;
        synchronized (_lock) {
            for (AppResource item : items) {
                if (!exist(item)) {
                    insert(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void setData(List<AppResource> items) {
        if (Lists.elementsEqual(items, getItems(), new Func2<AppResource, AppResource, Boolean>() {
            @Override
            public Boolean call(AppResource app, AppResource app2) {
                return app.equals(app2);
            }
        })) {
            Timber.d("application data not change");
            return;
        }
        super.setData(items);
    }

    private boolean exist(AppResource item) {
        List<AppResource> list = getItems();
        return list.indexOf(item) >= 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private OnItemClickListener listener;

        @BindView(R.id.tv_name)
        TextView mNameView;

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

        public void bindView(AppResource appResource) {
            mNameView.setText(appResource.appname);
        }
    }

    public interface OnClickAppListener {
        void onClickAppListener(AppResource app, int position);
    }
}