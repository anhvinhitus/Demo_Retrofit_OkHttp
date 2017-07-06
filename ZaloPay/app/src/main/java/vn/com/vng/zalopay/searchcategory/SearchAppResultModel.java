package vn.com.vng.zalopay.searchcategory;

import android.os.Build;
import android.text.Html;
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
 * App result item model
 */

class SearchAppResultModel extends EpoxyModelWithHolder<SearchAppResultModel.AppItemHolder> {

    interface OnItemClickListener {
        void onAppClick(SearchAppResultModel app);
    }

    private InsideApp app;

    private SearchAppResultModel.OnItemClickListener itemClickListener;

    SearchAppResultModel(InsideApp app, boolean isLastPos) {
        this.app = app;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.mAppView.setText(Html.fromHtml(app.appName, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.mAppView.setText(Html.fromHtml(app.appName));
        }

        holder.mAppView.setLeftIcon(app.iconName, app.iconColor);
        holder.mAppView.setOnClickListener(viewClickListener);
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

    static class AppItemHolder extends EpoxyHolder {
        @BindView(R.id.icon)
        IconFontTextView mAppView;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}