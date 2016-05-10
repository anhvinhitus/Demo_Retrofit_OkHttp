package vn.com.vng.zalopay.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import butterknife.Bind;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.menu.listener.MenuItemClickListener;
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.ui.adapter.MenuItemAdapter;
import vn.com.vng.zalopay.menu.utils.MenuItemUtil;
import vn.com.vng.zalopay.utils.CurrencyUtil;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class LeftMenuFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    public LeftMenuFragment() {
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.left_menu_layout;
    }

    private MenuItemAdapter mAdapter;

    @Bind(android.R.id.list)
    ListView listView;

    public ImageView imageAvatar;

    public TextView tvName;

    public TextView tvBalance;

    private MenuItemClickListener mMenuListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MenuItemClickListener) {
            mMenuListener = (MenuItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnClickMenuItemListener");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new MenuItemAdapter(getContext(), MenuItemUtil.getMenuItems());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addHeader(listView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    }

    private void addHeader(ListView listView) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.nav_header_main, null);
        imageAvatar = (ImageView) view.findViewById(R.id.im_avatar);
        tvName = (TextView) view.findViewById(R.id.tv_name);
        tvBalance = (TextView) view.findViewById(R.id.tv_balance);
        listView.addHeaderView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMenuListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MenuItem item = mAdapter.getItem(position - 1);
        if (item != null) {
            mMenuListener.onMenuHeaderClick(item);
        }
    }


    public void setUserInfo(User user) {
        tvName.setText(user.dname);
        loadAvatarImage(imageAvatar, user.avatar);
    }

    private void loadAvatarImage(final ImageView imageView, String url) {
//        Glide.with(this).load(url).placeholder(R.color.background).into(imageView);
        Glide.with(this).load(url).asBitmap().centerCrop().into(new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    public void setBalance(long balance) {
        tvBalance.setText(CurrencyUtil.formatCurrency(balance, false));
    }

   /* String name = "Nguyen Van A";
    long balance = 1232425;
    String avatar = "https://plus.google.com/u/0/_/focus/photos/public/AIbEiAIAAABECI7LguvYhZ7MuAEiC3ZjYXJkX3Bob3RvKig0MDE5NGQ2ODRhNjU5ODJiYTgxNjkwNWU3Njk3MWI5MDA1MGJjZmRhMAGGAaoGCMD24SAz49-T4-e-nZAtIA?sz=96";
    if (!TextUtils.isEmpty(name)) {
        header.tvName.setText(name);
        header.tvName.setVisibility(View.VISIBLE);
    } else {
        header.tvName.setVisibility(View.INVISIBLE);
    }
    header.tvBalance.setText(CurrencyUtil.formatCurrency(balance, false));

    loadAvatarImage(header.imageAvatar, avatar);*/

}
