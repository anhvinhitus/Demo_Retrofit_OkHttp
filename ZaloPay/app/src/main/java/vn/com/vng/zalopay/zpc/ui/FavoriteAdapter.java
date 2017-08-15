package vn.com.vng.zalopay.zpc.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnFavoriteItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.FavoriteData;

/**
 * Created by hieuvm on 7/23/17.
 * *
 */

final class FavoriteAdapter extends AbsRecyclerAdapter<FavoriteData, FavoriteAdapter.ViewHolder> {

    protected OnClickFavoriteListener mListener;
    private boolean mEditMode = false;
    private OnFavoriteItemClickListener mOnItemClickListener = new OnFavoriteItemClickListener() {

        @Override
        public void onListItemClick(View anchor, int position) {
            FavoriteData data = getItem(position);
            if (data == null) {
                return;
            }

            if (mListener != null) {
                mListener.onFavoriteItemClick(data);
            }
        }

        @Override
        public void onRemoveItemClick(View anchor, int position) {
            FavoriteData data = getItem(position);
            if (data == null) {
                return;
            }

            if (mListener != null) {
                mListener.onRemoveFavorite(data);
            }
        }
    };

    FavoriteAdapter(Context context, OnClickFavoriteListener listener) {
        super(context);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_favorite_layout, parent, false), mOnItemClickListener);
    }

    void setEditMode(boolean isEdit) {
        mEditMode = isEdit;
        notifyDataSetChanged();
    }

//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        return new ViewHolder(mInflater.inflate(R.layout.row_favorite_layout, parent, false), mOnItemClickListener);
//    }
//
//    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
//        @Override
//        public void onListItemClick(View anchor, int position) {
//            FavoriteData data = getItem(position);
//            if (data == null) {
//                return;
//            }
//
//            if (mListener != null) {
//                mListener.onRemoveFavorite(data);
//            }
//        }
//
//        @Override
//        public boolean onListItemLongClick(View anchor, int position) {
//            return false;
//        }
//    };

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FavoriteData profile = getItem(position);
        if (profile != null) {
            holder.bindView(profile, mEditMode);
        }
    }

    interface OnClickFavoriteListener {
        void onRemoveFavorite(FavoriteData favorite);

        void onFavoriteItemClick(FavoriteData favoriteData);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgAvatar)
        SimpleDraweeView mImgAvatar;
        @BindView(R.id.edit)
        View mEdit;
        @BindView(R.id.tvDisplayName)
        TextView mTvDisplayName;
        @BindView(R.id.placeHolder)
        TextView mPlaceHolder;
        //        private OnItemClickListener mListener;
        private OnFavoriteItemClickListener mListener;

        public ViewHolder(View itemView, OnFavoriteItemClickListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mListener = listener;
        }

//        public ViewHolder(View itemView, OnItemClickListener listener) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//            mListener = listener;
//        }

        public void bindView(FavoriteData profile, boolean isEdit) {

            if (TextUtils.isEmpty(profile.avatar) && !TextUtils.isEmpty(profile.displayName)) {
                mPlaceHolder.setVisibility(View.VISIBLE);
                String first = Strings.stripAccents(profile.displayName).substring(0, 1);
                mPlaceHolder.setText(first);
            } else {
                mPlaceHolder.setVisibility(View.INVISIBLE);
                mImgAvatar.setImageURI(profile.avatar);
            }

            mTvDisplayName.setText(profile.displayName);
            mEdit.setVisibility(isEdit ? View.VISIBLE : View.INVISIBLE);
        }

        @OnClick(R.id.edit)
        public void onClickEdit(View v) {
            if (mListener != null) {
                mListener.onRemoveItemClick(v, getAdapterPosition());
            }
        }

        @OnClick(R.id.imgAvatar)
        public void onClickItem(View v) {
            if (mListener == null) {
                return;
            }

            if (mEdit.isShown()) {
                mListener.onRemoveItemClick(v, getAdapterPosition());
            } else {
                mListener.onListItemClick(v, getAdapterPosition());
            }
        }
    }
}
