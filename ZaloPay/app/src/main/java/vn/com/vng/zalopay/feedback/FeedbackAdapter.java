package vn.com.vng.zalopay.feedback;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;

/**
 * Created by cpu11759-local on 03/01/2017.
 */

public class FeedbackAdapter extends AbsRecyclerAdapter<Bitmap, RecyclerView.ViewHolder> {
    private static final int FOOTER_VIEW = 1;

    private FeedbackAdapter.OnClickAddListener addListener;
    private FeedbackAdapter.OnClickDeleteListener deleteListener;

    public FeedbackAdapter(Context context, FeedbackAdapter.OnClickAddListener addListener,
                           FeedbackAdapter.OnClickDeleteListener deleteListener) {
        super(context);
        this.addListener = addListener;
        this.deleteListener = deleteListener;
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
        if (holder instanceof FeedbackAdapter.ViewHolder) {
            Bitmap screen = getItem(position);
            if (screen != null) {
                ((FeedbackAdapter.ViewHolder) holder).bindView(screen);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mItems == null) {
            return 0;
        }

        if (mItems.size() == 0) {
            return 1;
        }

        if(mItems.size() == getContext().getResources().getInteger(R.integer.max_length_add_screenshot)) {
            return mItems.size();
        }

        return mItems.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mItems.size()) {
            return FOOTER_VIEW;
        }

        return super.getItemViewType(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_image)
        ImageView imgScreen;

        @BindView(R.id.iv_delete)
        ImageView mDelete;

        public ViewHolder(View itemView, final OnClickDeleteListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClickDelete(getPosition());
                }
            });
        }

        public void bindView(Bitmap screen) {
            if (imgScreen == null) {
                return;
            }
            if (screen == null) {
                imgScreen.setImageBitmap(null);
            } else {
                imgScreen.setImageBitmap(screen);
            }
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        LinearLayout container;

        public FooterViewHolder(View itemView, final OnClickAddListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClickAdd();
                }
            });
        }
    }

    public interface OnClickAddListener {
        void onClickAdd();
    }

    public interface OnClickDeleteListener {
        void onClickDelete(int position);
    }
}
