package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;

/**
 * Created by hieuvm on 7/27/17.
 * *
 */

public class ViewHolder {
    @BindView(R.id.tvDisplayName)
    TextView mTvDisplayName;

    @BindView(R.id.imgAvatar)
    SimpleDraweeView mImgAvatar;

    @BindView(R.id.tvPhone)
    TextView mPhoneView;

    @BindView(R.id.logo)
    View mLogo;

    @BindView(R.id.placeHolder)
    TextView mPlaceHolder;

    ViewHolder(View view) {
        ButterKnife.bind(this, view);
    }

    public void bindView(long zaloId, String phone, String displayName, String aliasDisPlayName, String avatar) {

    }
}
