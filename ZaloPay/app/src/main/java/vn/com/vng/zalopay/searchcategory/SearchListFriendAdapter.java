package vn.com.vng.zalopay.searchcategory;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.ZaloFriend;

/**
 * Created by khattn on 3/15/17.
 * *
 */

public class SearchListFriendAdapter extends AbsRecyclerAdapter<ZaloFriend, RecyclerView.ViewHolder> {
    private static final int FOOTER_VIEW = 1;
    private static final int ITEM_VIEW = 0;

    private SearchListFriendAdapter.OnClickFriendListener friendListener;
    private SearchListFriendAdapter.OnClickSeeMoreAppListener seeMoreListener;

    private LayoutInflater mInflater;
    private boolean isLoadMore = false;

    public SearchListFriendAdapter(Context context,
                                   SearchListFriendAdapter.OnClickFriendListener friendListener,
                                   SearchListFriendAdapter.OnClickSeeMoreAppListener seeMoreListener) {
        super(context);
        mInflater = LayoutInflater.from(context);
        this.seeMoreListener = seeMoreListener;
        this.friendListener = friendListener;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {

            ZaloFriend app = getItem(position);
            if (friendListener != null && app != null) {
                friendListener.handleClickFriend(app, position);
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
        friendListener = null;
        seeMoreListener = null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FOOTER_VIEW) {
            return new SearchListFriendAdapter.FooterViewHolder(mInflater
                    .inflate(R.layout.layout_see_more, parent, false), seeMoreListener);
        }

        return new SearchListFriendAdapter.ViewHolder(mInflater
                .inflate(R.layout.row_zalo_friend_list, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ZaloFriend item = getItem(position);
            if (item != null) {
                ((ViewHolder) holder).bindView(item, position);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (isLoadMore) {
            return getItems().size() + 1;
        }

        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadMore && position > 2) {
            return FOOTER_VIEW;
        }

        return ITEM_VIEW;
    }

    @Override
    public List<ZaloFriend> getItems() {
        return mItems;
    }

    @Override
    public void setData(List<ZaloFriend> items) {
        if (items == null) {
            return;
        }

        synchronized (_lock) {
            mItems.clear();
            mItems.addAll(items);
        }

        notifyDataSetChanged();
    }

    void setLoadMore(boolean isLoadMore) {
        this.isLoadMore = isLoadMore;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private OnItemClickListener listener;

        @BindView(R.id.tvDisplayName)
        TextView mTvDisplayName;

        @BindView(R.id.imgAvatar)
        SimpleDraweeView mImgAvatar;

        @BindView(R.id.imgZaloPay)
        View mImgZaloPay;

        @BindView(R.id.viewSeparate)
        View mViewSeparate;


        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.listener = listener;
        }

        @OnClick(R.id.itemLayout)
        public void onClickItem(View v) {
            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }

        void bindView(ZaloFriend zaloFriend, int position) {
            String displayName = zaloFriend.displayName;
            String avatar = zaloFriend.avatar;
            long status = zaloFriend.status;

            mTvDisplayName.setText(Html.fromHtml(displayName));
            mImgAvatar.setImageURI(avatar);
            mImgZaloPay.setSelected(status == 1);
            if(position == getItemCount() - 1) {
                mViewSeparate.setVisibility(View.GONE);
            } else {
                mViewSeparate.setVisibility(View.VISIBLE);
            }
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        private OnClickSeeMoreAppListener mListener;

        private FooterViewHolder(View itemView, final OnClickSeeMoreAppListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.mListener = listener;
        }

        @OnClick(R.id.layoutSeeMore)
        public void handleSeeMore() {
            if (mListener != null) {
                mListener.handleClickSeeMoreFriend();
            }
        }
    }

    public interface OnClickFriendListener {
        void handleClickFriend(ZaloFriend app, int position);
    }

    public interface OnClickSeeMoreAppListener {
        void handleClickSeeMoreFriend();
    }
}
