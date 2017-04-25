package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

    private void addLineTop() {
        View line = new View(getContext());
        line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.separate));
        addView(line, new LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtils.dp(0.5F)));
    }

    private void init() {
        inflateMenu(R.menu.bottom_navigation_items);
        changeBottomNavigationLayout();
        initTabIconFont();
        addLineTop();
        BottomNavigationViewHelper.disableShiftMode(this);
        setOnNavigationItemSelectedListener(this);
    }


    private void changeBottomNavigationLayout() {
        int paddingBottom = (int) getResources().getDimension(R.dimen.spacing_tiny_s);
        View tabHome = findViewById(R.id.menu_home);
        tabHome.findViewById(android.support.design.R.id.icon).setPadding(0, 0, 0, paddingBottom);
        View tabNearby = findViewById(R.id.menu_nearby);
        tabNearby.findViewById(android.support.design.R.id.icon).setPadding(0, 0, 0, paddingBottom);
        View tabPromotion = findViewById(R.id.menu_promotion);
        tabPromotion.findViewById(android.support.design.R.id.icon).setPadding(0, 0, 0, paddingBottom);
        View tabProfile = findViewById(R.id.menu_profile);
        tabProfile.findViewById(android.support.design.R.id.icon).setPadding(0, 0, 0, paddingBottom);
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

        menu.getItem(HomePagerAdapter.TAB_SHOW_SHOW_INDEX).setIcon(
                createStateListDrawable(R.string.tab_showshow_active, R.color.colorPrimary,
                        R.string.tab_showshow, R.color.txt_item_sub));

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
        } else if (position == HomePagerAdapter.TAB_SHOW_SHOW_INDEX) {
            setSelectedItemId(R.id.menu_nearby);
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

        mPager.setCurrentItem(position, false);
    }

    private ViewPager mPager;

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
            case R.id.menu_nearby:
                setSelectedInternal(HomePagerAdapter.TAB_SHOW_SHOW_INDEX);
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


     /*   private void setPromotionNewState(boolean isActive) {
        FrameLayout tabPromotion = (FrameLayout) mBottomNavigationView.findViewById(R.id.menu_promotion);
        if (isActive) {
            if (mShowIconNewPromotion) {
                mShowIconNewPromotion = false;
                removeIconNew(tabPromotion);
            }
        } else {
            if (mShowIconNewPromotion) {
                addIconNew(tabPromotion);
            }
        }
    }*/

  /*  private void addIconNew(FrameLayout frameLayout) {
        if (mIconNewPromotion != null) {
            return;
        }
        mIconNewPromotion = getLayoutInflater().inflate(R.layout.icon_new, null);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        frameLayout.addView(mIconNewPromotion, -1, layoutParams);
    }

    private void removeIconNew(FrameLayout frameLayout) {
        if (frameLayout == null || mIconNewPromotion == null) {
            return;
        }
        frameLayout.removeView(mIconNewPromotion);
        mIconNewPromotion = null;
    }*/
}
