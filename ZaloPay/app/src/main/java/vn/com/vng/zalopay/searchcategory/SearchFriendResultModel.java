package vn.com.vng.zalopay.searchcategory;

import android.os.Build;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.ZPProfile;

/**
 * Created by khattn on 3/27/17.
 *
 */

public class SearchFriendResultModel extends EpoxyModelWithHolder<SearchFriendResultModel.ItemHolder> {

    interface OnItemClickListener {
        void onFriendClick(SearchFriendResultModel app);
    }

    private ZPProfile friend;
    private boolean isLastPos;

    private SearchFriendResultModel.OnItemClickListener itemClickListener;

    SearchFriendResultModel(ZPProfile friend, boolean isLastPos) {
        this.friend = friend;
        this.isLastPos = isLastPos;
    }

    @Override
    protected SearchFriendResultModel.ItemHolder createNewHolder() {
        return new SearchFriendResultModel.ItemHolder();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.row_zalo_friend_list;
    }

    @Override
    public void bind(SearchFriendResultModel.ItemHolder holder) {
        super.bind(holder);
        String displayName = friend.displayName;
        String avatar = friend.avatar;
        long status = friend.status;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.mTvDisplayName.setText(Html.fromHtml(displayName, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.mTvDisplayName.setText(Html.fromHtml(displayName));
        }

        holder.mImgAvatar.setImageURI(avatar);
        holder.itemLayout.setOnClickListener(viewClickListener);
    }

    @Override
    public void unbind(SearchFriendResultModel.ItemHolder holder) {
        super.unbind(holder);
    }

    private final View.OnClickListener viewClickListener = v -> {
        if (itemClickListener != null) {
            itemClickListener.onFriendClick(this);
        }
    };

    void setClickListener(SearchFriendResultModel.OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public ZPProfile getFriend() {
        return friend;
    }

    static class ItemHolder extends EpoxyHolder {
        @BindView(R.id.tvDisplayName)
        TextView mTvDisplayName;

        @BindView(R.id.imgAvatar)
        SimpleDraweeView mImgAvatar;

        @BindView(R.id.itemLayout)
        View itemLayout;

        @Override
        protected void bindView(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}