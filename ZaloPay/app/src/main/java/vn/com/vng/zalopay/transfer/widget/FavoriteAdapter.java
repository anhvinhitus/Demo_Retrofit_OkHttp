package vn.com.vng.zalopay.transfer.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.domain.model.Person;

/**
 * Created by hieuvm on 7/23/17.
 * *
 */

final class FavoriteAdapter extends AbsRecyclerAdapter<FavoriteData, FavoriteAdapter.ViewHolder> {

    interface OnClickFavoriteListener {
        void onRemoveFavorite(FavoriteData favorite);
    }

    private boolean mEditMode = false;

    protected OnClickFavoriteListener mListener;

    FavoriteAdapter(Context context, OnClickFavoriteListener listener) {
        super(context);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_favorite_layout, parent, false), mOnItemClickListener);
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {
            FavoriteData data = getItem(position);
            if (data == null) {
                return;
            }

            if (mListener != null) {
                mListener.onRemoveFavorite(data);
            }
        }

        @Override
        public boolean onListItemLongClick(View anchor, int position) {
            return false;
        }
    };

    void setEditMode(boolean isEdit) {
        mEditMode = isEdit;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FavoriteData profile = getItem(position);
        if (profile != null) {
            holder.bindView(profile, mEditMode);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgAvatar)
        SimpleDraweeView mImgAvatar;
        @BindView(R.id.edit)
        View mEdit;
        @BindView(R.id.tvDisplayName)
        TextView mTvDisplayName;
        private OnItemClickListener mListener;

        @BindView(R.id.placeHolder)
        TextView mPlaceHolder;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mListener = listener;
        }

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
                mListener.onListItemClick(v, getAdapterPosition());
            }
        }
    }
}
