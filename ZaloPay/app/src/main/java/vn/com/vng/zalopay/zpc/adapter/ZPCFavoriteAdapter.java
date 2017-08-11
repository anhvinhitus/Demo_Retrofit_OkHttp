package vn.com.vng.zalopay.zpc.adapter;

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
import vn.com.vng.zalopay.data.zpc.ZPCConfig;
import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.transfer.widget.FavoriteView;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.vng.zalopay.zpc.listener.OnFavoriteListener;

/**
 * Created by hieuvm on 7/23/17.
 * *
 */

public final class ZPCFavoriteAdapter extends ZPCAdapter<ZPCFavoriteAdapter.SwipeHolder> {
    private static final int VIEW_TYPE_COUNT = 3;
    private final int mHeaderViewType = 2;
    private final FavoriteView mFavoriteView;
    private final Space mEmptyView;
    protected OnFavoriteListener mListener;
    private SwipeLayout.SwipeListener mSwipeItemListener;

    public ZPCFavoriteAdapter(Context context, OnFavoriteListener listener) {
        super(context, R.layout.row_swipe_contact_layout);
        mListener = listener;
        mEmptyView = new Space(context);
        mFavoriteView = new FavoriteView(context);
        mFavoriteView.setOnEditFavoriteListener(new FavoriteView.OnEditFavoriteListener() {
            @Override
            public void onRemoveFavorite(FavoriteData favorite) {
                if (mListener != null) {
                    mListener.onRemoveFavorite(favorite);
                    notifyDataSetChanged();
                }
                ToastUtil.showCustomToast(mContext, mContext.getString(R.string.friend_favorite_removed), false);
            }

            @Override
            public void onAddFavorite(FavoriteData favorite) {

            }

            @Override
            public void onFavoriteItemClick(FavoriteData favorite) {
                if (mListener != null) {
                    mListener.onSelectFavorite(favorite);
                }
            }
        });

    }

    public int getMaxFavorite() {
        return mFavoriteView.getMaximum();
    }

    public void setMaxFavorite(int max) {
        mFavoriteView.setMaximum(max);
    }

    public void setOnSwipeLayoutListener(SwipeLayout.SwipeListener listener) {
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
        if (ZPCConfig.sEnableDisplayFavorite) {
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
                Timber.d("Loi view type [position %s viewType %s]", position, viewType);
                if (convertView != null) {
                    Timber.d("convertView [%s]", convertView.getClass());
                }
            }
            return view;
        }
    }

    @Override
    SwipeHolder onCreateViewHolder(View view) {
        return new SwipeHolder(view, mFavoriteView, mListener, mSwipeItemListener);
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

    public void setFavorite(@NonNull List<FavoriteData> persons) {
        mFavoriteView.setData(persons);
    }

    static class SwipeHolder extends ViewHolder {
        private final OnFavoriteListener mListener;
        private final FavoriteData mFavoriteData;
        private final FavoriteView mFavoriteView;
        private final int mColorYellow;
        @BindView(R.id.swipe)
        SwipeLayout mSwipeLayout;
        @BindView(R.id.background)
        View mBackground;
        @BindView(R.id.iconFavorite)
        IconFontTextView mIconFontTextView;
        @BindView(R.id.zpcontactlist_if_favorite_star)
        IconFont mFavoriteStar;
        @BindView(R.id.foreground)
        View mForeground;
        private Context mContext;

        SwipeHolder(View view, FavoriteView favoriteView, OnFavoriteListener listener, SwipeLayout.SwipeListener swipeListener) {
            super(view);
            mContext = view.getContext();
            mListener = listener;
            mFavoriteData = new FavoriteData();
            mFavoriteView = favoriteView;
            mColorYellow = ContextCompat.getColor(view.getContext(), R.color.yellow_f8d41c);
            mSwipeLayout.addSwipeListener(new SimpleSwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    if (swipeListener != null) {
                        swipeListener.onStartOpen(layout);
                    }

                    // select item list
                    if (!mForeground.isSelected())
                        mForeground.setSelected(true);

                    // hide star if shown
                    if (mFavoriteStar.isShown())
                        mFavoriteStar.setVisibility(View.GONE);
                }

                @Override
                public void onClose(SwipeLayout layout) {
                    if (swipeListener != null) {
                        swipeListener.onClose(layout);
                    }

                    // unselect item list
                    if (mForeground.isSelected())
                        mForeground.setSelected(false);

                    // show star if iconFontTextView isSelected && star is hide
                    if (!mFavoriteStar.isShown() && mIconFontTextView.isSelected())
                        mFavoriteStar.setVisibility(View.VISIBLE);
                }
            });
            mSwipeLayout.setSwipeEnabled(ZPCConfig.sEnableDisplayFavorite);
        }

        @Override
        public void bindView(long zaloId, String phone, String displayName, String aliasDisPlayName, String avatar, int status) {
            mFavoriteData.zaloId = zaloId;
            mFavoriteData.phoneNumber = phone;
            mFavoriteData.displayName = displayName;
            mFavoriteData.avatar = avatar;
            mFavoriteData.status = status;
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
                ToastUtil.showCustomToast(mContext, mContext.getString(R.string.friend_favorite_removed), false);
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
            mSwipeLayout.close();
            ToastUtil.showCustomToast(mContext, mContext.getString(R.string.friend_favorite_added), false);

        }
    }
}
