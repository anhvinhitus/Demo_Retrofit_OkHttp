package vn.com.vng.zalopay.banner.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.ImageLoader;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;

/**
 * Created by longlv on 12/05/2016.
 */
public class BannerPagerAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<DBanner> mResources = new ArrayList<>();
    private IBannerClick mListener;

    ImageLoader mImageLoader;

    public interface IBannerClick {
        void onBannerItemClick(DBanner banner, int position);
    }

    public BannerPagerAdapter(Context context, List<DBanner> resources, IBannerClick iBannerClick) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources = resources;
        mListener = iBannerClick;
        mImageLoader = AndroidApplication.instance().getAppComponent().imageLoader();
    }

    public void setData(List<DBanner> banners) {
        if (mResources.equals(banners)) {
            Timber.d("not set data because the same.");
            return;
        }
        mResources.clear();
        if (banners != null && banners.size() >0) {
            mResources.addAll(banners);
        }
        notifyDataSetChanged();
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
        SimpleDraweeView imageView = (SimpleDraweeView) mLayoutInflater.inflate(R.layout.row_banner_image, container, false);
        final DBanner banner = mResources.get(position);
        if (banner == null) {
            return imageView;
        }
        mImageLoader.loadImage(imageView, banner.logourl);
        imageView.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                if (mListener != null) {
                    mListener.onBannerItemClick(banner, position);
                }
            }
        });

        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        Timber.d("detachView %s", position);
        container.removeView((View) object);
    }
}