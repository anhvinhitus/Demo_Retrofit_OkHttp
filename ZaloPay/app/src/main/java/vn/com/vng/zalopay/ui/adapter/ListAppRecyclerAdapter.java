package vn.com.vng.zalopay.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Func2;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.utils.AndroidUtils;
import com.zalopay.ui.widget.IconFontDrawable;
import com.zalopay.ui.widget.IconFontTextView;
import vn.com.vng.zalopay.utils.ImageLoader;

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

        @BindView(R.id.tvInsideApp)
        IconFontTextView tvInsideApp;

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
            tvInsideApp.setText(appResource.appname);
            setIconFont(tvInsideApp.getTopIcon(), appResource);
        }

        private void setIconFont(IconFontDrawable iconInsideApp, AppResource appResource) {
            //  Timber.d("set image appType [%s] url: [%s]", appResource.appType, appResource.iconUrl);
            if (iconInsideApp == null || appResource == null) {
                return;
            }

            try {
                loadIconFont(iconInsideApp,
                        appResource.iconName,
                        appResource.iconColor);
            } catch (Exception e) {
                Timber.w(e, "set IconFont for inside app exception.");
                loadIconFontDefault();
            }
        }

        private void loadIconFontDefault() {
            loadIconFont(tvInsideApp.getLeftIcon(),
                    R.string.general_icondefault,
                    AndroidUtils.getColorFromResource(R.color.home_font_inside_app));
        }

        private void loadIconFont(IconFontDrawable iconInsideApp, String iconName, String iconColor)
                throws Resources.NotFoundException {
            iconInsideApp.setIcon(iconName);
            if (iconInsideApp.hasIcon()) {
                setColorIconFont(iconInsideApp, iconColor);
            } else {
                loadIconFontDefault();
            }
        }

        private void loadIconFont(IconFontDrawable iconInsideApp, int resourceId, String iconColor)
                throws Resources.NotFoundException {
            iconInsideApp.setIcon(resourceId);
            setColorIconFont(iconInsideApp, iconColor);
        }

        private void setColorIconFont(IconFontDrawable iconInsideApp, String color) {
            if (!TextUtils.isEmpty(color)) {
                iconInsideApp.setColor(color);
            }
        }
    }

    public interface OnClickAppListener {
        void onClickAppListener(AppResource app, int position);
    }
}
