package vn.com.vng.zalopay.feedback;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;

/**
 * Created by cpu11759-local on 03/01/2017.
 */

public class FeedbackAdapter extends AbsRecyclerAdapter<ScreenshotData, RecyclerView.ViewHolder> {
    private static final int FOOTER_VIEW = 1;
    private static final int ITEM_VIEW = 0;

    private FeedbackAdapter.OnClickAddListener addListener;
    private FeedbackAdapter.OnClickDeleteListener deleteListener;

    private int maxScreenshot = 4;

    public FeedbackAdapter(Context context, FeedbackAdapter.OnClickAddListener addListener,
                           FeedbackAdapter.OnClickDeleteListener deleteListener) {
        super(context);
        this.addListener = addListener;
        this.deleteListener = deleteListener;
        this.maxScreenshot = context.getResources().getInteger(R.integer.max_length_add_screenshot);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FOOTER_VIEW) {
            return new FeedbackAdapter.FooterViewHolder(mInflater
                    .inflate(R.layout.layout_add_screenshot, parent, false), addListener);
        }

        return new FeedbackAdapter.ViewHolder(mInflater
                .inflate(R.layout.row_card_image_feedback, parent, false), deleteListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        addListener = null;
        deleteListener = null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ScreenshotData screen = getItem(position);
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
        if (isHasFooter()) {
            return super.getItemCount() + 1;
        }

        return super.getItemCount();
    }

    @Override
    public void insert(ScreenshotData object, int index) {
        synchronized (_lock) {
            mItems.add(index, object);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHasFooter() && position == getItemCount() - 1) {
            return FOOTER_VIEW;
        }

        return ITEM_VIEW;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_image)
        SimpleDraweeView mScreenshotView;

        private OnClickDeleteListener mListener;

        public ViewHolder(View itemView, OnClickDeleteListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mListener = listener;
        }

        @OnClick(R.id.iv_delete)
        public void onClick() {
            if (mListener != null) {
                mListener.onClickDelete(getAdapterPosition());
            }
        }

        public void bindView(ScreenshotData image) {
            if (image.mUrl != null) {
                mScreenshotView.setImageURI(image.mUrl);
            } else if (image.mBitmap != null) {
                mScreenshotView.setImageBitmap(image.mBitmap);
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
        public void onClickAdd(View v) {
            if (mListener != null) {
                mListener.onClickAdd();
            }
        }
    }

    public interface OnClickAddListener {
        void onClickAdd();
    }

    public interface OnClickDeleteListener {
        void onClickDelete(int position);
    }
}
