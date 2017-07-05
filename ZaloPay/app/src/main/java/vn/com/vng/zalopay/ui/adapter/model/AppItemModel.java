package vn.com.vng.zalopay.ui.adapter.model;

import android.view.View;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.zalopay.ui.widget.IconFontTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.DebouncingOnClickListener;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;

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
        holder.mIconView.setTopIcon(app.iconName, app.iconColor);
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

    static class AppItemHolder extends EpoxyHolder {

        @BindView(R.id.tvInsideApp)
        IconFontTextView mIconView;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
