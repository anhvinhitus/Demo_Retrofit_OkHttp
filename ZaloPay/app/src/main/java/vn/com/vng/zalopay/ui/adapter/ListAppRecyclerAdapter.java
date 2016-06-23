package vn.com.vng.zalopay.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.vng.uicomponent.widget.recyclerview.AbsRecyclerAdapter;
import vn.vng.uicomponent.widget.recyclerview.OnItemClickListener;

/**
 * Created by AnhHieu on 5/25/16.
 */
public class ListAppRecyclerAdapter extends AbsRecyclerAdapter<AppResource, ListAppRecyclerAdapter.ViewHolder> {

    private OnClickAppListener listener;

    public ListAppRecyclerAdapter(Context context, OnClickAppListener listener) {
        super(context);
        this.listener = listener;
    }


    public OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {
            if (listener != null) {
                listener.onClickAppListener(getItem(position));
            }
        }

        @Override
        public boolean onListItemLongClick(View anchor, int position) {
            return false;
        }
    };

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mOnItemClickListener = null;
        listener = null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_list_app_layout, parent, false), mOnItemClickListener);
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
        for (AppResource item : items) {
            if (!exist(item)) {
                insert(item);
            }
        }
    }

    private boolean exist(AppResource item) {
        List<AppResource> list = getItems();
        return list.indexOf(item) >= 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private OnItemClickListener listener;

        @BindView(R.id.iv_logo)
        ImageView mLogoView;

        @BindView(R.id.tv_name)
        TextView mNameView;


        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.itemLayout)
        public void onClickItem(View v) {


            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }

        public void bindView(AppResource appResource) {

            mNameView.setText(appResource.appname);
            setImage(mLogoView, appResource.urlImage);
         /*   if (appResource.status == 0) {
                itemView.setSelected(false);
            } else {
                itemView.setSelected(true);
            }*/

        }

        private final void setImage(ImageView image, String url) {
            image.setVisibility(View.VISIBLE);

            try {
                int resId = Integer.parseInt(url);
                image.setImageResource(resId);
            } catch (NumberFormatException ex) {

                if (TextUtils.isEmpty(url)) {
                    image.setVisibility(View.INVISIBLE);
                } else {
                    loadImage(image, url);
                }

            }
        }

        private final void loadImage(ImageView image, String url) {
            Glide.with(context).load(url).centerCrop().placeholder(R.color.silver).into(image);
        }

    }

    public interface OnClickAppListener {
        void onClickAppListener(AppResource app);
    }
}
