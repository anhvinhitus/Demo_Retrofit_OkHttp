/*
package vn.com.vng.zalopay.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.Video;
import vn.vng.uicomponent.widget.recyclerview.OnItemClickListener;

*/
/**
 * Created by AnhHieu on 3/31/16.
 *//*

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder>
*/
/*        implements ListPreloader.PreloadModelProvider<Video> *//*
 {
    private static final int PRELOAD_AHEAD_ITEMS = 5;

    private final Context mContext;
    private OnItemClickListener listener;
 */
/*   private RequestBuilder fullRequest;
    private RequestBuilder thumbRequest;*//*


    final RequestManager requestManager;

    private ViewPreloadSizeProvider<Video> preloadSizeProvider;

    private List<Video> mItems;

    private LayoutInflater mInflater;
    private final Object _lock = new Object();

    public TimelineAdapter(Context context, OnItemClickListener listener) {
        this.mContext = context;
        this.listener = listener;
        this.mInflater = LayoutInflater.from(context);
        this.mItems = new ArrayList<>();

        preloadSizeProvider = new ViewPreloadSizeProvider<>();

        requestManager = Glide.with(context);
      */
/*  fullRequest = requestManager
                .asDrawable()
                .apply(centerCropTransform(context)
                        .placeholder(new ColorDrawable(Color.GRAY)));

        thumbRequest = requestManager
                .asDrawable()
                .apply(diskCacheStrategyOf(DiskCacheStrategy.DATA)
                        .override(75))
                .transition(withCrossFade());*//*

    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        //  requestManager.clear(holder.imageView);
    }


    public RecyclerView.OnScrollListener getOnScrollListener() {
       */
/* return new RecyclerViewPreloader<>(Glide.with(mContext), this,
                preloadSizeProvider, PRELOAD_AHEAD_ITEMS);*//*


        return null;
    }


    @Override
    public long getItemId(int i) {
        return RecyclerView.NO_ID;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh = new ViewHolder(mInflater.inflate(R.layout.row_timeline_layout, parent, false), listener);
        preloadSizeProvider.setView(vh.imageView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Video item = getItem(position);
        if (item != null) {
            holder.bindView(item, position);
            loadImage(holder.imageView, "https://sohanews2.vcmedia.vn/k:thumb_w/640/2016/999-1459583108638/phong-tap-ruc-lua-vi-nhung-co-nang-xinh-dep.jpg");
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public boolean isEmpty() {
        return mItems.isEmpty();
    }

    public Video getItem(int position) {
        if (position >= 0 && position < mItems.size())
            return this.mItems.get(position);

        return null;
    }

    public void remove(Video values) {
        synchronized (_lock) {
            mItems.remove(values);
        }
        notifyDataSetChanged();
    }

    public void insertItems(Collection<Video> items) {
        if (Lists.isEmptyOrNull(items)) return;
        int before = mItems.size();
        synchronized (_lock) {
            mItems.addAll(items);
        }
        notifyItemRangeInserted(before, getItemCount());
    }


    public void setData(Collection<Video> items) {
        if (items == null) return;

        synchronized (_lock) {
            mItems.clear();
            mItems.addAll(items);
        }

        notifyDataSetChanged();
    }


    protected final void loadImage(ImageView view, String url) {
       */
/* fullRequest.load(url)
                .thumbnail(thumbRequest.load(url))
                .into(view);*//*

    }

    */
/*
      @Override
      public List<Video> getPreloadItems(int position) {
          return mItems.subList(position, position + 1);
      }

   @Override
      public RequestBuilder getPreloadRequestBuilder(Video item) {
          return fullRequest.thumbnail(thumbRequest.load(item)).load(item);
      }
  *//*

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private OnItemClickListener listener;

        @Bind(R.id.image)
        public ImageView imageView;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
        }


        public void bindView(Video item, int position) {

        }
    }
}
*/
