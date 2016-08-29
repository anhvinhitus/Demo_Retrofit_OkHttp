package vn.com.vng.zalopay.banner.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.R;
import vn.com.zalopay.wallet.entity.gatewayinfo.DBanner;

/**
 * Created by longlv on 12/05/2016.
 *
 */
public class BannerPagerAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    List<DBanner> mResources = new ArrayList<>();
    private IBannerClick mListener;

    public interface IBannerClick {
        void onBannerItemClick(DBanner banner, int position);
    }

    public BannerPagerAdapter(Context context, List<DBanner> resources, IBannerClick iBannerClick) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources = resources;
        mListener = iBannerClick;
    }

    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.row_banner_image, container, false);
        final DBanner banner = mResources.get(position);
        if (banner == null) {
            return itemView;
        }
        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        setImage(imageView, banner.logourl);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBannerItemClick(banner, position);
                }
            }
        });
        container.addView(itemView);

        return itemView;
    }

    private void setImage(ImageView imageView, String imageUrl) {
        Glide.with(mContext).load(imageUrl)
                .placeholder(R.color.silver)
                .error(R.color.background)
                .centerCrop()
                .into(imageView);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}