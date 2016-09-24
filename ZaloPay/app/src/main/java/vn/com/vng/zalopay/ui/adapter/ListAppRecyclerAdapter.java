package vn.com.vng.zalopay.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.paymentapps.PaymentAppTypeEnum;
import vn.com.vng.zalopay.utils.ImageLoader;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

/**
 * Created by AnhHieu on 5/25/16.
 * *
 */
public class ListAppRecyclerAdapter extends AbsRecyclerAdapter<AppResource, ListAppRecyclerAdapter.ViewHolder> {

    private OnClickAppListener listener;

    public ListAppRecyclerAdapter(Context context, OnClickAppListener listener) {
        super(context);
        this.listener = listener;
    }


    public OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_list_app_layout, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppResource item = getItem(position);
        if (item != null) {
            holder.bindView(item);
        }
    }

    @Override
    public void insertItems(Collection<AppResource> items) {
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

    private boolean exist(AppResource item) {
        List<AppResource> list = getItems();
        return list.indexOf(item) >= 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private OnItemClickListener listener;

        @BindView(R.id.iv_logo)
        SimpleDraweeView mLogoView;

        @BindView(R.id.tv_name)
        TextView mNameView;

        ImageLoader mImageLoader;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
            mImageLoader = AndroidApplication.instance().getAppComponent().imageLoader();
        }

        @OnClick(R.id.itemLayout)
        public void onClickItem(View v) {
            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }

        public void bindView(AppResource appResource) {
            mNameView.setText(appResource.appname);
            setImage(mLogoView, appResource);
        }

        private void setImage(SimpleDraweeView image, AppResource appResource) {
            Timber.d("set image appType [%s] url: [%s]", appResource.appType, appResource.iconUrl);
            if (TextUtils.isEmpty(appResource.iconUrl) &&
                    appResource.appType == PaymentAppTypeEnum.NATIVE.getValue() &&
                    PaymentAppConfig.getAppResource(appResource.appid) != null) {
                appResource.iconUrl = PaymentAppConfig.getAppResource(appResource.appid).iconUrl;
            }
            if (!TextUtils.isEmpty(appResource.iconUrl)) {
                try {
                    loadImage(image, Integer.parseInt(appResource.iconUrl));
                } catch (NumberFormatException ex) {
                    loadImage(image, appResource.iconUrl);
                }
            } else {
                loadImage(image,R.drawable.ic_imagedefault);
            }
        }

        private void loadImage(SimpleDraweeView image, int resourceId) {
            mImageLoader.loadImage(image, resourceId);
        }

        private void loadImage(SimpleDraweeView image, String url) {
            mImageLoader.loadImage(image, url);
        }

    }

    public interface OnClickAppListener {
        void onClickAppListener(AppResource app, int position);
    }
}
