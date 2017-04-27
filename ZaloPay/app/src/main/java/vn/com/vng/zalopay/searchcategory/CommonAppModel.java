package vn.com.vng.zalopay.searchcategory;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.zalopay.ui.widget.IconFontDrawable;
import com.zalopay.ui.widget.IconFontTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.InsideApp;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by khattn on 3/27/17.
 * Search home item model
 */

class CommonAppModel extends EpoxyModelWithHolder<CommonAppModel.AppItemHolder> {

    interface OnItemClickListener {
        void onAppClick(CommonAppModel app);
    }

    private InsideApp app;

    private OnItemClickListener itemClickListener;

    CommonAppModel(InsideApp app) {
        this.app = app;
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.row_list_app_layout;
    }

    @Override
    public void bind(AppItemHolder holder) {
        super.bind(holder);
        holder.mIconView.setText(app.appName);
        setIconFont(holder.mIconView.getTopIcon(), app);
        holder.itemLayout.setOnClickListener(viewClickListener);
    }

    @Override
    protected AppItemHolder createNewHolder() {
        return new AppItemHolder();
    }

    @Override
    public void unbind(AppItemHolder holder) {
        super.unbind(holder);
    }

    private final View.OnClickListener viewClickListener = v -> {
        if (itemClickListener != null) {
            itemClickListener.onAppClick(this);
        }
    };

    void setClickListener(OnItemClickListener listener) {
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
        @BindView(R.id.tvInsideApp)
        IconFontTextView mIconView;

        @BindView(R.id.itemLayout)
        View itemLayout;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}

