package vn.com.vng.zalopay.searchcategory;

import android.view.View;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.zalopay.ui.widget.IconFontTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.InsideApp;

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
        holder.mIconView.setTopIcon(app.iconName, app.iconColor);
        holder.mIconView.setOnClickListener(viewClickListener);
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

    static class AppItemHolder extends EpoxyHolder {
        @BindView(R.id.tvInsideApp)
        IconFontTextView mIconView;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}

