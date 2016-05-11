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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.Bind;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.interactor.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.menu.listener.MenuItemClickListener;
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.ui.adapter.MenuItemAdapter;
import vn.com.vng.zalopay.menu.utils.MenuItemUtil;
import vn.com.vng.zalopay.ui.presenter.LeftMenuPresenter;
import vn.com.vng.zalopay.ui.view.ILeftMenuView;
import vn.com.vng.zalopay.utils.CurrencyUtil;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class LeftMenuFragment extends BaseFragment implements AdapterView.OnItemClickListener, ILeftMenuView {

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

    @Inject
    User user;

    @Inject
    LeftMenuPresenter presenter;

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
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
        addHeader(listView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        setUserInfo(user);
    }

    private void addHeader(ListView listView) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.nav_header_main, listView, false);
        imageAvatar = (ImageView) view.findViewById(R.id.im_avatar);
        tvName = (TextView) view.findViewById(R.id.tv_name);
        tvBalance = (TextView) view.findViewById(R.id.tv_balance);
        listView.addHeaderView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.getBalance();
    }


    @Override
    public void onDestroyView() {
        presenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMenuListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) return; // Header

        MenuItem item = mAdapter.getItem(position - 1);
        if (item != null) {
            mMenuListener.onMenuHeaderClick(item);
        }
    }


    public void setUserInfo(User user) {
        setAvatar(user.avatar);
        setDisplayName(user.dname);
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
        tvBalance.setText(CurrencyUtil.formatCurrency(balance));
    }

    @Override
    public void setAvatar(String avatar) {
        loadAvatarImage(imageAvatar, avatar);
    }

    @Override
    public void setDisplayName(String displayName) {
        tvName.setText(displayName);
    }

    @Subscribe
    public void onEventMainThread(ZaloProfileInfoEvent event) {
        Timber.tag(TAG).d("avatar %s displayName %s", event.avatar, event.displayName);
        setAvatar(event.avatar);
        setDisplayName(event.displayName);
    }
}
