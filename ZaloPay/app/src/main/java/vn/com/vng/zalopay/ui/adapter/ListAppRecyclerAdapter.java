package vn.com.vng.zalopay.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Func2;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.utils.AndroidUtils;
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

        @BindView(R.id.tv_name)
        TextView mNameView;

        @BindView(R.id.iconInsideApp)
        IconFont iconInsideApp;

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
            setIconFont(iconInsideApp, appResource);
        }

        private void setIconFont(IconFont iconInsideApp, AppResource appResource) {
            //  Timber.d("set image appType [%s] url: [%s]", appResource.appType, appResource.iconUrl);
            if (iconInsideApp == null || appResource == null) {
                return;
            }

            try {
                if (appResource.appid == PaymentAppConfig.Constants.TRANSFER_MONEY
                        || appResource.appid == PaymentAppConfig.Constants.RECEIVE_MONEY) {
                    loadIconFontFromAssert(iconInsideApp,
                            Integer.parseInt(appResource.iconName),
                            appResource.iconColor);
                } else {
                    loadIconFontFromFile(iconInsideApp,
                            appResource.iconName,
                            appResource.iconColor);
                }
            } catch (Exception e) {
                Timber.w(e, "set IconFont for inside app exception.");
                loadIconFontDefault();
            }
        }

        private void setColorIconFont(IconFont iconInsideApp, String color) {
            if (!TextUtils.isEmpty(color)) {
                iconInsideApp.setTextColor(Color.parseColor(color));
            }
        }

        private void loadIconFontFromAssert(IconFont iconInsideApp, int resourceId, String iconColor)
                throws Resources.NotFoundException {
            iconInsideApp.setTypefaceFromAsset(getContext().getString(R.string.font_name));
            iconInsideApp.setText(resourceId);
            setColorIconFont(iconInsideApp, iconColor);
        }

        private void loadIconFontFromFile(IconFont iconInsideApp, String code, String iconColor) {
            if (TextUtils.isEmpty(code)) {
                loadIconFontDefault();
            }
            String filePath = ResourceHelper.getFontPath(BuildConfig.ZALOPAY_APP_ID,
                    getContext().getString(R.string.font_name_dynamic));
            iconInsideApp.setTypefaceFromFile(filePath);
            iconInsideApp.setText(fromHtml(String.format("&#%s;", code)));
            setColorIconFont(iconInsideApp, iconColor);
        }

        private void loadIconFontDefault() {
            loadIconFontFromAssert(iconInsideApp,
                    R.string.general_icondefault,
                    AndroidUtils.getColorFromResource(R.color.home_font_inside_app));
        }
    }

    @SuppressWarnings("deprecation")
    private static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public interface OnClickAppListener {
        void onClickAppListener(AppResource app, int position);
    }
}
