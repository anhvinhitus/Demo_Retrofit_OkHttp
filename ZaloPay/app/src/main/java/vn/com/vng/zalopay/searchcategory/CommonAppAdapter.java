package vn.com.vng.zalopay.searchcategory;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.airbnb.epoxy.EpoxyAdapter;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.InsideApp;

/**
 * Created by khattn on 3/27/17.
 * Adapter list application in search home view
 */

class CommonAppAdapter extends EpoxyAdapter {

    interface OnModelClickListener {
        void onClickAppItem(InsideApp app, int position);
    }

    private OnModelClickListener clickListener;

    CommonAppAdapter() {
        super();
        enableDiffing();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        clickListener = null;
        super.onDetachedFromRecyclerView(recyclerView);
    }

    void setClickListener(OnModelClickListener listener) {
        clickListener = listener;
    }

    void setAppItem(List<InsideApp> list) {
        Timber.d("set app items size [%s]", list.size());
        removeAllModels();
        addModel(new CommonAppTitleModel());
        addModels(appTransform(list));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            ((LinearLayoutManager) recyclerView.getLayoutManager()).setRecycleChildrenOnDetach(true);
        }
    }

    private final CommonAppModel.OnItemClickListener appClickListener = appModel -> {
        if (clickListener != null) {
            clickListener.onClickAppItem(appModel.getApp(), getModelPosition(appModel));
        }
    };

    private CommonAppModel appTransform(InsideApp resource) {
        CommonAppModel appModel = new CommonAppModel(resource);
        appModel.setClickListener(appClickListener);
        return appModel;
    }

    private List<CommonAppModel> appTransform(List<InsideApp> resources) {
        return Lists.transform(resources, this::appTransform);
    }

}

