package vn.com.vng.zalopay.transfer.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by hieuvm on 7/23/17.
 * *
 */

public class FavoriteView extends LinearLayout implements FavoriteAdapter.OnClickFavoriteListener {

    public interface OnEditFavoriteListener {
        void onRemoveFavorite(FavoriteData favorite);

        void onAddFavorite(FavoriteData favorite);
    }

    @BindView(R.id.tvDescription)
    TextView mTvDescription;
    @BindView(R.id.btnEdit)
    TextView mBtnEdit;
    @BindView(R.id.listview)
    RecyclerView mRecyclerView;

    @BindView(R.id.emptyView)
    View mEmptyView;

    private OnEditFavoriteListener mListener;
    private FavoriteAdapter mAdapter;
    private int mMaximum = 10;

    public FavoriteView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init(context);
        }
    }

    private void init(Context context) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                context.getResources().getDimensionPixelOffset(R.dimen.height_favorite_view));
        setLayoutParams(layoutParams);
        setOrientation(VERTICAL);
        inflate(context, R.layout.favorite_view, this);
        ButterKnife.bind(this);
        mAdapter = new FavoriteAdapter(context, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        checkItemCount();
    }

    @OnClick(R.id.btnEdit)
    public void onClickEdit(View view) {
        boolean isEdit = mBtnEdit.length() == 0;
        mAdapter.setEditMode(!isEdit);
        if (isEdit) {
            mBtnEdit.setText(R.string.edit);
            checkItemCount();
        } else {
            mBtnEdit.setText("");
        }
    }

    public boolean isMaximum() {
        return mAdapter.getItemCount() >= mMaximum;
    }

    public void setMaximum(int maximum) {
        mMaximum = maximum;
    }

    public void setData(List<FavoriteData> personList) {
        mAdapter.setData(personList);
        checkItemCount();
    }

    public void remove(FavoriteData person) {
        mAdapter.remove(person);
        checkItemCount();
    }

    public void add(FavoriteData person) {
        mAdapter.insert(person, 0);
        AndroidUtils.runOnUIThread(mScrollRunnable);
        checkItemCount();
    }

    private Runnable mScrollRunnable = this::scrollToTop;

    private void scrollToTop() {
        if (mRecyclerView == null) {
            return;
        }

        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)) {
            return;
        }

        if (mRecyclerView.getChildCount() == 0) {
            return;
        }

        int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        if (firstVisibleItemPosition != 0) {
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    private void checkItemCount() {
        int count = mAdapter.getItemCount();
        setCountView(count);
        mEmptyView.setVisibility(count == 0 ? VISIBLE : GONE);
        mBtnEdit.setVisibility(count == 0 ? INVISIBLE : VISIBLE);
    }

    private void setCountView(int count) {
        mTvDescription.setText(getContext().getString(R.string.favorite_friend_format, count, mMaximum));
    }

    public boolean contain(FavoriteData favorite) {
        return mAdapter.indexOf(favorite) >= 0;
    }

    @Override
    public void onRemoveFavorite(FavoriteData person) {
        mAdapter.remove(person);
        setCountView(mAdapter.getItemCount());
        if (mListener != null) {
            mListener.onRemoveFavorite(person);
        }
    }

    public void setOnEditFavoriteListener(OnEditFavoriteListener listener) {
        mListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        AndroidUtils.cancelRunOnUIThread(mScrollRunnable);
        super.onDetachedFromWindow();
    }
}
