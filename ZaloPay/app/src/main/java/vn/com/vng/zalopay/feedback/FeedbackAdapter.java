package vn.com.vng.zalopay.feedback;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;

final public class FeedbackAdapter extends AbsRecyclerAdapter<Uri, RecyclerView.ViewHolder> {

    private static final int FOOTER_VIEW = 1;
    private static final int ITEM_VIEW = 0;

    private FeedbackAdapter.OnClickAddListener addListener;
    private FeedbackAdapter.OnClickDeleteListener deleteListener;
    private FeedbackAdapter.OnClickImageListener imageListener;

    private int maxScreenshot = 4;

    public FeedbackAdapter(Context context, FeedbackAdapter.OnClickAddListener addListener,
                           FeedbackAdapter.OnClickDeleteListener deleteListener,
                           FeedbackAdapter.OnClickImageListener imageListener) {
        super(context);
        this.addListener = addListener;
        this.deleteListener = deleteListener;
        this.imageListener = imageListener;
        this.maxScreenshot = context.getResources().getInteger(R.integer.max_length_add_screenshot);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FOOTER_VIEW) {
            return new FeedbackAdapter.FooterViewHolder(mInflater
                    .inflate(R.layout.layout_add_screenshot, parent, false), addListener);
        }

        return new FeedbackAdapter.ViewHolder(mInflater
                .inflate(R.layout.row_card_image_feedback, parent, false), deleteListener, imageListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        addListener = null;
        deleteListener = null;
        imageListener = null;
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            Uri screen = getItem(position);
            if (screen != null) {
                ((ViewHolder) holder).bindView(screen);
            }
        }
    }

    private boolean isHasFooter() {
        return getItems().size() < maxScreenshot;
    }

    @Override
    public int getItemCount() {
        int itemCount = getItems().size();
        if (isHasFooter()) {
            return itemCount + 1;
        }
        return itemCount;

    }

    @Override
    public void insert(Uri object, int index) {
        synchronized (_lock) {
            mItems.add(index, object);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHasFooter() && position == getItems().size()) {
            return FOOTER_VIEW;
        }

        return ITEM_VIEW;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_image)
        SimpleDraweeView mScreenshotView;

        private OnClickDeleteListener mDeleteListener;
        private OnClickImageListener mImageListener;

        public ViewHolder(View itemView, OnClickDeleteListener deleteListener, OnClickImageListener imageListener) {

            super(itemView);
            ButterKnife.bind(this, itemView);
            mDeleteListener = deleteListener;
            mImageListener = imageListener;
        }

        @OnClick(R.id.iv_image)
        public void onClickImage() {
            if (mImageListener != null) {
                mImageListener.onClickImage(getAdapterPosition());
            }
        }

        @OnClick(R.id.iv_delete)
        public void onClickDelete() {
            if (mDeleteListener != null) {
                mDeleteListener.onClickDelete(getAdapterPosition());
            }
        }

        public void bindView(Uri image) {
            if (image != null) {
                mScreenshotView.setImageURI(image);
            } else {
                mScreenshotView.setImageURI("");
            }
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        private OnClickAddListener mListener;

        private FooterViewHolder(View itemView, final OnClickAddListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.mListener = listener;
        }

        @OnClick(R.id.container)
        public void onClickAdd() {
            if (mListener != null) {
                mListener.onClickAdd(getAdapterPosition());
            }
        }
    }

    public interface OnClickAddListener {
        void onClickAdd(int position);
    }

    public interface OnClickDeleteListener {
        void onClickDelete(int position);
    }

    public interface OnClickImageListener {
        void onClickImage(int position);
    }
}
