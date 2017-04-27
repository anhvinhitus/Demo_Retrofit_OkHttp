package vn.com.vng.zalopay.searchcategory;

import android.view.View;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;

/**
 * Created by khattn on 3/28/17.
 */

class SectionTitleModel extends EpoxyModelWithHolder<SectionTitleModel.ItemHolder> {

    private String mTitle;

    public SectionTitleModel(String title) {
        this.mTitle = title;
    }

    @Override
    protected SectionTitleModel.ItemHolder createNewHolder() {
        return new SectionTitleModel.ItemHolder();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.layout_title_search_result;
    }

    @Override
    public void bind(SectionTitleModel.ItemHolder holder) {
        super.bind(holder);
        holder.tvTitle.setText(mTitle);
    }

    @Override
    public void unbind(SectionTitleModel.ItemHolder holder) {
        super.unbind(holder);
    }

    static class ItemHolder extends EpoxyHolder {
        @BindView(R.id.tvTitle)
        TextView tvTitle;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}