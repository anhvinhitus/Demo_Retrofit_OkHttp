package vn.com.vng.zalopay.linkcard.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by longlv on 10/13/16.
 * *
 */
class AssociatedBankAdapter extends AbsRecyclerAdapter<AppResource, AssociatedBankAdapter.ViewHolder> {

    AssociatedBankAdapter(Context context) {
        super(context);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_associated_bank_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppResource item = getItem(position);
        if (item != null) {
            holder.bindView(item);
        }
    }

    @Override
    public void insertItems(Collection<AppResource> items) {
        if (items == null || items.isEmpty()) return;
        synchronized (_lock) {
            for (AppResource item : items) {
                if (!exist(item)) {
                    insert(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean exist(AppResource item) {
        List<AppResource> list = getItems();
        return list.indexOf(item) >= 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_logo)
        ImageView mLogoView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindView(AppResource appResource) {
            loadImage(mLogoView, Integer.parseInt(appResource.iconUrl));
        }

        private void loadImage(ImageView image, int resourceId) {
            Glide.with(context).load(resourceId).centerCrop().placeholder(R.color.white).into(image);
        }
    }
}
