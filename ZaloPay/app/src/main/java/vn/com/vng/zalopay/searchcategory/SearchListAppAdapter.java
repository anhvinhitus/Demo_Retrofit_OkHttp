package vn.com.vng.zalopay.searchcategory;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.zalopay.ui.widget.IconFontDrawable;
import com.zalopay.ui.widget.IconFontTextView;
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
import vn.com.vng.zalopay.domain.model.InsideApp;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.ImageLoader;

/**
 * Created by khattn on 3/14/17.
 * Adapter for list search app result
 */

public class SearchListAppAdapter extends AbsRecyclerAdapter<InsideApp, RecyclerView.ViewHolder> {
    private static final int FOOTER_VIEW = 1;
    private static final int ITEM_VIEW = 0;

    private SearchListAppAdapter.OnClickAppListener appListener;
    private SearchListAppAdapter.OnClickSeeMoreAppListener seeMoreListener;

    private boolean isLoadMore = false;

    public SearchListAppAdapter(Context context,
                                SearchListAppAdapter.OnClickAppListener appListener,
                                SearchListAppAdapter.OnClickSeeMoreAppListener seeMoreListener) {
        super(context);
        this.appListener = appListener;
        this.seeMoreListener = seeMoreListener;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {

            InsideApp app = getItem(position);
            if (appListener != null && app != null) {
                appListener.handleClickApp(app, position);
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
        appListener = null;
        seeMoreListener = null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FOOTER_VIEW) {
            return new SearchListAppAdapter.FooterViewHolder(mInflater
                    .inflate(R.layout.layout_see_more, parent, false), seeMoreListener);
        }

        return new SearchListAppAdapter.ViewHolder(mInflater
                .inflate(R.layout.row_search_list_app, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            InsideApp item = getItem(position);
            if (item != null) {
                ((ViewHolder) holder).bindView(item, position);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (isLoadMore) {
            return getItems().size() + 1;
        }

        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadMore && position > 2) {
            return FOOTER_VIEW;
        }

        return ITEM_VIEW;
    }

    @Override
    public void setData(List<InsideApp> items) {
        if (Lists.elementsEqual(items, getItems(), Object::equals)) {
            Timber.d("application data not change");
            return;
        }
        super.setData(items);
    }

    void setLoadMore(boolean isLoadMore) {
        this.isLoadMore = isLoadMore;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private OnItemClickListener listener;

        @BindView(R.id.icon)
        IconFontTextView mIcon;

        @BindView(R.id.viewSeparate)
        View viewSeparate;

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

        public void bindView(InsideApp insideApp, int position) {
            mIcon.setText(Html.fromHtml(insideApp.appName));
            setIconFont(mIcon.getLeftIcon(), insideApp);

            if (position == getItemCount() - 1) {
                viewSeparate.setVisibility(View.GONE);
            } else {
                viewSeparate.setVisibility(View.VISIBLE);
            }
        }

        private void setIconFont(IconFontDrawable iconInsideApp, InsideApp insideApp) {
            if (iconInsideApp == null || insideApp == null) {
                return;
            }

            try {
                loadIconFont(iconInsideApp,
                        insideApp.iconName,
                        insideApp.iconColor);
            } catch (Exception e) {
                Timber.w(e, "set IconFont for inside app exception.");
                loadIconFontDefault();
            }
        }

        private void loadIconFontDefault() {
            loadIconFont(mIcon.getLeftIcon(),
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

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        private OnClickSeeMoreAppListener mListener;

        private FooterViewHolder(View itemView, final OnClickSeeMoreAppListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.mListener = listener;
        }

        @OnClick(R.id.layoutSeeMore)
        public void handleSeeMore() {
            if (mListener != null) {
                mListener.handleClickSeeMoreApp();
            }
        }
    }

    public interface OnClickAppListener {
        void handleClickApp(InsideApp app, int position);
    }

    public interface OnClickSeeMoreAppListener {
        void handleClickSeeMoreApp();
    }
}
