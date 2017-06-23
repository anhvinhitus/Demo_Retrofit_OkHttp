package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.react.base.HomePagerAdapter;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.BottomNavigationViewHelper;

/**
 * Created by hieuvm on 4/25/17.
 * *
 */

public class HomeBottomNavigationView extends BottomNavigationView implements BottomNavigationView.OnNavigationItemSelectedListener {

    public HomeBottomNavigationView(Context context) {
        this(context, null);
    }

    public HomeBottomNavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeBottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private final SharedPreferences mPreferences = AndroidApplication.instance().getAppComponent().sharedPreferences();
    private final static String PREF_KEY_BADGE = "pref_badge_key";
    private boolean mShowIconNewPromotion;
    private View mIconNewPromotion;
    private ViewPager mPager;

    private void init() {
        inflateMenu(R.menu.bottom_navigation_items);
        changeBottomNavigationLayout();
        initTabIconFont();
        addLineSeparate();
        BottomNavigationViewHelper.disableShiftMode(this);
        setOnNavigationItemSelectedListener(this);
        initBadge();
    }

    private void initBadge() {
        mShowIconNewPromotion = mPreferences.getBoolean(PREF_KEY_BADGE, false);
        setBadgeViewPromotion(mShowIconNewPromotion);
    }

    private void changeBottomNavigationLayout() {
        int paddingBottom = (int) getResources().getDimension(R.dimen.spacing_tiny_s);
        View tabHome = findViewById(R.id.menu_home);
        tabHome.findViewById(android.support.design.R.id.icon).setPadding(0, 0, 0, paddingBottom);
        View tabNearby = findViewById(R.id.menu_transaction);
        tabNearby.findViewById(android.support.design.R.id.icon).setPadding(0, 0, 0, paddingBottom);
        View tabPromotion = findViewById(R.id.menu_promotion);
        tabPromotion.findViewById(android.support.design.R.id.icon).setPadding(0, 0, 0, paddingBottom);
        View tabProfile = findViewById(R.id.menu_profile);
        tabProfile.findViewById(android.support.design.R.id.icon).setPadding(0, 0, 0, paddingBottom);
    }

    private void addLineSeparate() {
        View line = new View(getContext());
        line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.separate));
        addView(line, new LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtils.dp(0.5F)));
    }

    private StateListDrawable createStateListDrawable(@StringRes int iconNameActive,
                                                      @ColorRes int iconColorActive,
                                                      @StringRes int iconNameNormal,
                                                      @ColorRes int iconColorNormal) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_checked},
                new BottomNavigationDrawable(getContext(), iconNameActive, iconColorActive));
        stateListDrawable.addState(new int[]{},
                new BottomNavigationDrawable(getContext(), iconNameNormal, iconColorNormal));
        return stateListDrawable;
    }

    public void initTabIconFont() {
        Menu menu = getMenu();

        menu.getItem(HomePagerAdapter.TAB_MAIN_INDEX).setIcon(
                createStateListDrawable(R.string.tab_home_active, R.color.colorPrimary,
                        R.string.tab_home, R.color.txt_item_sub));

        menu.getItem(HomePagerAdapter.TAB_TRANSACTION_INDEX).setIcon(
                createStateListDrawable(R.string.tab_history_active, R.color.colorPrimary,
                        R.string.tab_history, R.color.txt_item_sub));

        menu.getItem(HomePagerAdapter.TAB_PROMOTION_INDEX).setIcon(
                createStateListDrawable(R.string.tab_promotion_active, R.color.colorPrimary,
                        R.string.tab_promotion, R.color.txt_item_sub));

        menu.getItem(HomePagerAdapter.TAB_PERSONAL_INDEX).setIcon(
                createStateListDrawable(R.string.tab_personal_active, R.color.colorPrimary,
                        R.string.tab_personal, R.color.txt_item_sub));
    }

    public void setSelected(int position) {
        setSelectedInternal(position);
        if (position == HomePagerAdapter.TAB_MAIN_INDEX) {
            setSelectedItemId(R.id.menu_home);
        } else if (position == HomePagerAdapter.TAB_TRANSACTION_INDEX) {
            setSelectedItemId(R.id.menu_transaction);
        } else if (position == HomePagerAdapter.TAB_PROMOTION_INDEX) {
            setSelectedItemId(R.id.menu_promotion);
        } else if (position == HomePagerAdapter.TAB_PERSONAL_INDEX) {
            setSelectedItemId(R.id.menu_profile);
        }
    }

    private void setSelectedInternal(int position) {
        if (mPager == null) {
            throw new RuntimeException("Viewpager not initialized");
        }

        int previousPosition = mPager.getCurrentItem();

        mPager.setCurrentItem(position, false);

        if (position == HomePagerAdapter.TAB_PROMOTION_INDEX || previousPosition == HomePagerAdapter.TAB_PROMOTION_INDEX) {
            setBadgePromotion(false);
        }
    }

    public void setViewPager(@NonNull ViewPager pager) {
        mPager = pager;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (mPager == null) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.menu_home:
                setSelectedInternal(HomePagerAdapter.TAB_MAIN_INDEX);
                return true;
            case R.id.menu_transaction:
                setSelectedInternal(HomePagerAdapter.TAB_TRANSACTION_INDEX);
                return true;
            case R.id.menu_promotion:
                setSelectedInternal(HomePagerAdapter.TAB_PROMOTION_INDEX);
                return true;
            case R.id.menu_profile:
                setSelectedInternal(HomePagerAdapter.TAB_PERSONAL_INDEX);
                return true;
        }

        return false;
    }

    public void setBadgePromotion(boolean enable) {
        Timber.d("set badge promotion: state [old:%s new:%s]", mShowIconNewPromotion, enable);
        if (mShowIconNewPromotion == enable) {
            return;
        }

        mPreferences.edit().putBoolean(PREF_KEY_BADGE, enable)
                .apply();
        mShowIconNewPromotion = enable;

        setBadgeViewPromotion(enable);
    }

    private void setBadgeViewPromotion(boolean enable) {
        FrameLayout tabPromotion = (FrameLayout) findViewById(R.id.menu_promotion);
        if (enable) {
            addIconNew(tabPromotion);
        } else {
            removeIconNew(tabPromotion);
        }
    }

    private void addIconNew(FrameLayout frameLayout) {
        if (mIconNewPromotion != null) {
            return;
        }

        mIconNewPromotion = View.inflate(getContext(), R.layout.icon_new, null);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        frameLayout.addView(mIconNewPromotion, layoutParams);
    }

    private void removeIconNew(FrameLayout frameLayout) {
        if (frameLayout == null || mIconNewPromotion == null) {
            return;
        }

        frameLayout.removeView(mIconNewPromotion);
        mIconNewPromotion = null;
    }
}
