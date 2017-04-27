package vn.com.vng.zalopay.searchcategory;

import android.view.View;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;

/**
 * Created by khattn on 3/27/17.
 */

class SectionSeeMoreModel extends EpoxyModelWithHolder<SectionSeeMoreModel.ItemHolder> {

    interface OnItemClickListener {
        void onAppClick(SectionSeeMoreModel app);
    }

    private SectionSeeMoreModel.OnItemClickListener itemClickListener;

    @Override
    protected SectionSeeMoreModel.ItemHolder createNewHolder() {
        return new SectionSeeMoreModel.ItemHolder();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.layout_see_more;
    }

    @Override
    public void bind(SectionSeeMoreModel.ItemHolder holder) {
        super.bind(holder);
        holder.itemLayout.setOnClickListener(viewClickListener);
    }

    @Override
    public void unbind(SectionSeeMoreModel.ItemHolder holder) {
        super.unbind(holder);
    }

    private final View.OnClickListener viewClickListener = v -> {
        if (itemClickListener != null) {
            itemClickListener.onAppClick(this);
        }
    };

    void setClickListener(SectionSeeMoreModel.OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    static class ItemHolder extends EpoxyHolder {
        @BindView(R.id.layoutSeeMore)
        View itemLayout;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}