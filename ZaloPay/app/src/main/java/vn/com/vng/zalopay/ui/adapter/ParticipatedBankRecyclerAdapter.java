package vn.com.vng.zalopay.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.vng.uicomponent.widget.recyclerview.AbsRecyclerAdapter;

/**
 * Created by AnhHieu on 5/25/16.
 */
public class ParticipatedBankRecyclerAdapter extends AbsRecyclerAdapter<Integer, ParticipatedBankRecyclerAdapter.ViewHolder> {

    public ParticipatedBankRecyclerAdapter(Context context, List<Integer> bankIcons) {
        super(context);
        this.mItems.addAll(bankIcons);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.fragment_participated_bank_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Integer item = getItem(position);
        if (item != null) {
            holder.bindView(item);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_logo)
        ImageView mLogoView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindView(Integer imgResource) {
            if (imgResource == null) {
                return;
            }
            mLogoView.setImageResource(imgResource);
        }

        private final void loadImage(ImageView image, String url) {
            Glide.with(context).load(url).centerCrop().placeholder(R.color.silver).into(image);
        }
    }
}
