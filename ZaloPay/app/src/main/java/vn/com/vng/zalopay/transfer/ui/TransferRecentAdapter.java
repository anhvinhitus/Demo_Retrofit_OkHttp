package vn.com.vng.zalopay.transfer.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.IconFont;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.model.RecentTransaction;

/**
 * Created by AnhHieu on 8/17/16.
 * *
 */
public class TransferRecentAdapter extends AbsRecyclerAdapter<RecentTransaction, TransferRecentAdapter.ViewHolder> {

    interface OnClickTransferRecentListener {
        void onItemRecentClick(RecentTransaction item);
    }

    private OnClickTransferRecentListener listener;

    TransferRecentAdapter(Context context, OnClickTransferRecentListener listener) {
        super(context);
        this.listener = listener;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {
            if (listener != null) {
                RecentTransaction item = getItem(position);
                if (item != null) {
                    listener.onItemRecentClick(item);
                }
            }
        }

        @Override
        public boolean onListItemLongClick(View anchor, int position) {
            return false;
        }
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.fragment_trasfer_recent_item, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RecentTransaction item = getItem(position);
        if (item != null) {
            holder.bindView(item);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        OnItemClickListener listener;

        @BindView(R.id.tvDisplayName)
        TextView mTvDisplayName;

        @BindView(R.id.tvPhone)
        TextView mTvPhone;

        @BindView(R.id.imgAvatar)
        SimpleDraweeView mImgAvatar;

        @BindView(R.id.imgTransferType)
        IconFont mImgTransferType;

        Context context;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.listener = listener;
            context = AndroidApplication.instance();
        }

        private void bindView(RecentTransaction item) {
            mImgAvatar.setImageURI(item.avatar);
            mTvDisplayName.setText(item.displayName);

            String phone = PhoneUtil.formatPhoneNumber(item.phoneNumber);
            String zaloPayName = item.zaloPayName;

            if (!TextUtils.isEmpty(zaloPayName)) {
                mTvPhone.setText(String.format(context.getString(R.string.account_format), zaloPayName));
                mImgTransferType.setIcon(R.string.sendmoney_zalopayid);
                mImgTransferType.setIconColor(R.color.menu_font_ic_green);
            } else if (!TextUtils.isEmpty(phone)) {
                mTvPhone.setText(String.format(context.getString(R.string.phone_format), phone));
                mImgTransferType.setIcon(R.string.sendmoney_friend);
                mImgTransferType.setIconColor(R.color.menu_font_ic_blue);
            } else {
                mTvPhone.setText(R.string.not_update_zalopay_id);
                mImgTransferType.setIcon(R.string.sendmoney_friend);
                mImgTransferType.setIconColor(R.color.menu_font_ic_blue);
            }
        }

        @OnClick(R.id.itemLayout)
        public void onClickItemLayout(View v) {
            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }
    }
}
