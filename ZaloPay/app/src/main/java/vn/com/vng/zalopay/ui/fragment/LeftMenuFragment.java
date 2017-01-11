package vn.com.vng.zalopay.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnItemClick;
import butterknife.internal.DebouncingOnClickListener;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.model.MenuItemType;
import vn.com.vng.zalopay.menu.ui.adapter.MenuItemAdapter;
import vn.com.vng.zalopay.ui.callback.MenuClickListener;
import vn.com.vng.zalopay.ui.presenter.LeftMenuPresenter;
import vn.com.vng.zalopay.ui.view.ILeftMenuView;
import vn.com.vng.zalopay.utils.ImageLoader;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 5/10/16.
 * *
 */
public class LeftMenuFragment extends BaseFragment implements ILeftMenuView {

    public static LeftMenuFragment newInstance() {

        Bundle args = new Bundle();

        LeftMenuFragment fragment = new LeftMenuFragment();
        fragment.setArguments(args);
        return fragment;
    }

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

    @BindView(android.R.id.list)
    ListView listView;

    SimpleDraweeView mImageAvatarView;

    TextView mDisplayNameView;

    TextView mZaloPayNameView;

    private MenuClickListener mMenuListener;

    @Inject
    LeftMenuPresenter presenter;

    @Inject
    ImageLoader mImageLoader;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MenuClickListener) {
            mMenuListener = (MenuClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnClickMenuItemListener");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new MenuItemAdapter(getContext(), new ArrayList<MenuItem>());
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
    }

    @Override
    public void onDestroy() {
        CShareData.dispose();
        super.onDestroy();
        presenter.destroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addHeader(listView);
        listView.setAdapter(mAdapter);
        presenter.attachView(this);
    }

    private void addHeader(ListView listView) {
        View header = LayoutInflater.from(getContext()).inflate(R.layout.nav_header_main, listView, false);
        mImageAvatarView = (SimpleDraweeView) header.findViewById(R.id.im_avatar);
        mDisplayNameView = (TextView) header.findViewById(R.id.tv_name);
        mZaloPayNameView = (TextView) header.findViewById(R.id.tvZaloPayName);
        header.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                mMenuListener.onClickProfile();
            }
        });

        listView.addHeaderView(header);
    }

    public void refreshIconFont() {
        if (mAdapter == null) {
            return;
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.initialize();
    }


    @Override
    public void onDestroyView() {
        presenter.detachView();
        CShareData.dispose();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMenuListener = null;
    }

    @OnItemClick(android.R.id.list)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            return; // Header
        }

        MenuItem item = mAdapter.getItem(position - 1);
        if (item == null) {
            return;
        }

        if (mMenuListener != null && item.itemType == MenuItemType.ITEM) {
            mMenuListener.onMenuItemClick(item.id);
        }

    }

    @Override
    public void setUserInfo(User user) {
        if (user == null) return;
        setAvatar(user.avatar);
        setDisplayName(user.displayName);
        setZaloPayName(user.zalopayname);
    }

    @Override
    public void setAvatar(String avatar) {
        mImageLoader.loadImage(mImageAvatarView, avatar);
    }

    @Override
    public void setDisplayName(String displayName) {
        mDisplayNameView.setText(displayName);
    }

    @Override
    public void setZaloPayName(String zaloPayName) {
        if (TextUtils.isEmpty(zaloPayName)) {
            mZaloPayNameView.setText(getString(R.string.zalopay_name_not_update));
        } else {
            mZaloPayNameView.setText(String.format(getString(R.string.leftmenu_zalopayid), zaloPayName));
        }
    }

    @Override
    public void setMenuItem(List<MenuItem> var) {
        if (mAdapter != null) {
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            mAdapter.setNotifyOnChange(true);
            mAdapter.addAll(var);
        }
    }

}
