package vn.com.vng.zalopay.searchcategory;

import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.zalopay.ui.widget.IconFontDrawable;
import com.zalopay.ui.widget.IconFontTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.InsideApp;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by khattn on 3/27/17.
 * App result item model
 */

public class SearchAppResultModel extends EpoxyModelWithHolder<SearchAppResultModel.AppItemHolder> {

    interface OnItemClickListener {
        void onAppClick(SearchAppResultModel app);
    }

    private InsideApp app;
    private boolean isLastPos;

    private SearchAppResultModel.OnItemClickListener itemClickListener;

    SearchAppResultModel(InsideApp app, boolean isLastPos) {
        this.app = app;
        this.isLastPos = isLastPos;
    }

    @Override
    protected SearchAppResultModel.AppItemHolder createNewHolder() {
        return new SearchAppResultModel.AppItemHolder();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.row_search_list_app;
    }

    @Override
    public void bind(SearchAppResultModel.AppItemHolder holder) {
        super.bind(holder);
        holder.itemLayout.setOnClickListener(viewClickListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.mIcon.setText(Html.fromHtml(app.appName, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.mIcon.setText(Html.fromHtml(app.appName));
        }

        setIconFont(holder.mIcon.getLeftIcon(), app);

        if (isLastPos) {
            holder.viewSeparate.setVisibility(View.GONE);
        } else {
            holder.viewSeparate.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void unbind(SearchAppResultModel.AppItemHolder holder) {
        super.unbind(holder);
    }

    private final View.OnClickListener viewClickListener = v -> {
        if (itemClickListener != null) {
            itemClickListener.onAppClick(this);
        }
    };

    void setClickListener(SearchAppResultModel.OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public InsideApp getApp() {
        return app;
    }

    private void setIconFont(IconFontDrawable iconInsideApp, InsideApp appResource) {
        if (iconInsideApp == null || appResource == null) {
            return;
        }

        try {
            loadIconFont(iconInsideApp,
                    appResource.iconName,
                    appResource.iconColor);
        } catch (Exception e) {
            Timber.w(e, "set IconFont for inside app exception.");
            loadIconFontDefault(iconInsideApp);
        }
    }

    private void loadIconFontDefault(IconFontDrawable iconInsideApp) {
        loadIconFont(iconInsideApp,
                R.string.general_icondefault,
                AndroidUtils.getColorFromResource(R.color.home_font_inside_app));
    }

    private void loadIconFont(IconFontDrawable iconInsideApp, String iconName, String iconColor)
            throws Resources.NotFoundException {
        iconInsideApp.setIcon(iconName);
        if (iconInsideApp.hasIcon()) {
            setColorIconFont(iconInsideApp, iconColor);
        } else {
            loadIconFontDefault(iconInsideApp);
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

    static class AppItemHolder extends EpoxyHolder {
        @BindView(R.id.icon)
        IconFontTextView mIcon;

        @BindView(R.id.viewSeparate)
        View viewSeparate;

        @BindView(R.id.itemLayout)
        View itemLayout;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}