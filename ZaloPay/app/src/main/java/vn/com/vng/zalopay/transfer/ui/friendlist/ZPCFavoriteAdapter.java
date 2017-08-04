package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.zalopay.ui.widget.IconFont;
import com.zalopay.ui.widget.IconFontTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.zfriend.FriendConfig;
import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.transfer.widget.FavoriteView;
import vn.com.vng.zalopay.utils.ToastUtil;

/**
 * Created by hieuvm on 7/23/17.
 * *
 */

final class ZPCFavoriteAdapter extends ZPCAdapter<ZPCFavoriteAdapter.SwipeHolder> {
    private static final int VIEW_TYPE_COUNT = 3;
    private final int mHeaderViewType = 2;
    private final FavoriteView mFavoriteView;
    private final Space mEmptyView;
    protected OnFavoriteListener mListener;
    protected SwipeLayout.SwipeListener mSwipeItemListener;

    ZPCFavoriteAdapter(Context context, OnFavoriteListener listener) {
        super(context, R.layout.row_swipe_contact_layout);
        mListener = listener;
        mEmptyView = new Space(context);
        mFavoriteView = new FavoriteView(context);
        mFavoriteView.setOnEditFavoriteListener(new FavoriteView.OnEditFavoriteListener() {
            @Override
            public void onRemoveFavorite(FavoriteData favorite) {
                if (mListener != null) {
                    mListener.onRemoveFavorite(favorite);
                }
            }

            @Override
            public void onAddFavorite(FavoriteData favorite) {

            }
        });

    }

    void setMaxFavorite(int max) {
        mFavoriteView.setMaximum(max);
    }

    int getMaxFavorite(){
        return mFavoriteView.getMaximum();
    }

    private SwipeLayout.SwipeListener mSwipeListener = new SimpleSwipeListener() {
        @Override
        public void onStartOpen(SwipeLayout layout) {
            if (mSwipeItemListener != null) {
                mSwipeItemListener.onStartOpen(layout);
            }
        }
    };


    void setOnSwipeLayoutListener(SwipeLayout.SwipeListener listener) {
        mSwipeItemListener = listener;
    }

    @Override
    public int getCount() {
        int count = super.getCount();
        if (count == 0) {
            return count;
        }
        return count + 1;
    }

    private View getHeaderView() {
        if (FriendConfig.sEnableDisplayFavorite) {
            if (isSearching()) {
                return mEmptyView;
            }

            return mFavoriteView;
        } else {
            return mEmptyView;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        if (viewType == mHeaderViewType) {
            return getHeaderView();
        } else {
            View view = super.getView(position - 1, convertView, parent);
            if (view instanceof FavoriteView) {
                Timber.w("Loi view type [position %s viewType %s]", position, viewType);
                if (convertView != null) {
                    Timber.w("convertView [%s]", convertView.getClass());
                }
            }
            return view;
        }
    }

    @Override
    SwipeHolder onCreateViewHolder(View view) {
        return new SwipeHolder(view, mFavoriteView, mListener, mSwipeListener);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return mHeaderViewType;
        }

        return super.getItemViewType(position - 1);
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    void setFavorite(@NonNull List<FavoriteData> persons) {
        mFavoriteView.setData(persons);
    }

    static class SwipeHolder extends ViewHolder {
        private Context mContext;

        @BindView(R.id.swipe)
        SwipeLayout mSwipeLayout;

        @BindView(R.id.background)
        View mBackground;

        @BindView(R.id.iconFavorite)
        IconFontTextView mIconFontTextView;

        @BindView(R.id.zpcontactlist_if_favorite_star)
        IconFont mFavoriteStar;

        private final OnFavoriteListener mListener;
        private final FavoriteData mFavoriteData;
        private final FavoriteView mFavoriteView;
        private final int mColorYellow;

        SwipeHolder(View view, FavoriteView favoriteView, OnFavoriteListener listener, SwipeLayout.SwipeListener swipeListener) {
            super(view);
            mContext = view.getContext();
            mListener = listener;
            mFavoriteData = new FavoriteData();
            mFavoriteView = favoriteView;
            mColorYellow = ContextCompat.getColor(view.getContext(), R.color.yellow_f8d41c);
            mSwipeLayout.addSwipeListener(swipeListener);
            mSwipeLayout.setSwipeEnabled(FriendConfig.sEnableDisplayFavorite);
        }

        @Override
        public void bindView(long zaloId, String phone, String displayName, String aliasDisPlayName, String avatar) {
            mFavoriteData.zaloId = zaloId;
            mFavoriteData.phoneNumber = phone;
            mFavoriteData.displayName = displayName;
            mFavoriteData.avatar = avatar;
         /*   if (mSwipeLayout.getOpenStatus() != SwipeLayout.Status.Close) {
                mSwipeLayout.close(false, false);
            }*/
            boolean isFavorite = mFavoriteView.contain(mFavoriteData);
            setFavorite(isFavorite);
        }

        private void setFavorite(boolean isFavorite) {
            mIconFontTextView.setSelected(isFavorite);
            if (isFavorite) {
                mIconFontTextView.setIcon(IconFontTextView.Top, mIconFontTextView.getContext().getString(R.string.ct_list_fav_choose), mColorYellow);
                mFavoriteStar.setVisibility(View.VISIBLE);
            } else {
                mIconFontTextView.setIcon(IconFontTextView.Top, mIconFontTextView.getContext().getString(R.string.ct_list_fav_line), Color.WHITE);
                mFavoriteStar.setVisibility(View.GONE);
            }
        }

        @OnClick(R.id.background)
        public void onFavoriteClick(View v) {
            boolean isFavorite = !mIconFontTextView.isSelected();
            if (!isFavorite) {
                setFavorite(false);
                mFavoriteView.remove(new FavoriteData(mFavoriteData));
                if (mListener != null) {
                    mListener.onRemoveFavorite(mFavoriteData);
                }
                ToastUtil.showCustomToast(mContext, mContext.getString(R.string.friend_favorite_removed));
                return;
            }


            if (mFavoriteView.isMaximum()) {
                if (mListener != null) {
                    mListener.onMaximumFavorite();
                }
                return;
            }

            setFavorite(true);
//            mFavoriteView.add(new FavoriteData(mFavoriteData));
            mFavoriteView.addLast(new FavoriteData(mFavoriteData));

            if (mListener != null) {
                mListener.onAddFavorite(mFavoriteData);
            }
            ToastUtil.showCustomToast(mContext, mContext.getString(R.string.friend_favorite_added));

        }
    }
}
