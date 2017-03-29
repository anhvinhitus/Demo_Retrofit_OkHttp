package vn.com.vng.zalopay.ui.adapter.model;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.zalopay.ui.widget.IconFontDrawable;
import com.zalopay.ui.widget.IconFontTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by hieuvm on 3/21/17.
 * AppItemModel
 */
public class AppItemModel extends EpoxyModelWithHolder<AppItemModel.AppItemHolder> {

    public interface OnAppModelClickListener {
        void onAppClick(AppItemModel app);
    }

    public AppResource app;

    private OnAppModelClickListener clickListener;

    public AppItemModel(AppResource app, OnAppModelClickListener listener) {
        this.app = app;
        this.clickListener = listener;
    }

    @Override
    protected AppItemHolder createNewHolder() {
        return new AppItemHolder();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.row_list_app_layout;
    }

    @Override
    public void bind(AppItemHolder holder) {
        super.bind(holder);
        holder.mIconView.setText(app.appname);
        holder.mIconView.setOnClickListener(listener);
        setIconFont(holder.mIconView.getTopIcon(), app);
    }

    @Override
    public void unbind(AppItemHolder holder) {
        super.unbind(holder);
        holder.mIconView.setOnClickListener(null);
    }

    public AppItemModel setApp(AppResource app) {
        if (app != null) {
            this.app = app;
        }
        return this;
    }

    private final DebouncingOnClickListener listener = new DebouncingOnClickListener() {
        @Override
        public void doClick(View v) {
            if (clickListener != null) {
                clickListener.onAppClick(AppItemModel.this);
            }
        }
    };

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + app.hashCode();
        return result;
    }

    private void setIconFont(IconFontDrawable iconInsideApp, AppResource appResource) {
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

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
