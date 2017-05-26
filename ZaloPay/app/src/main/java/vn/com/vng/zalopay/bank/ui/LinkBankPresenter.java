package vn.com.vng.zalopay.bank.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.bank.models.LinkBankPagerIndex;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

/**
 * Created by longlv on 2/6/17.
 * Presenter of LinkBankActivity
 */

class LinkBankPresenter extends AbstractPresenter<ILinkBankView> {

    private final User mUser;
    private SharedPreferences mPreferences;

    @Inject
    LinkBankPresenter(User user, SharedPreferences preferences) {
        this.mUser = user;
        this.mPreferences = preferences;
    }

    void initPageStart(Bundle bundle) {
        if (changePageInBundle(bundle)) {
            Timber.d("Init page in bundle.");
        } else {
            changePageInContext();
            Timber.d("Init page in context.");
        }
    }

    private boolean changePageInBundle(Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        int pageIndex = bundle.getInt(Constants.ARG_PAGE_INDEX, -1);
        return mView.initViewPager(pageIndex);
    }

    /**
     * Khi User launch màn hình này, tab được hiển thị đầu tiên, được xác định theo rule sau:
     * 1) Nếu User chưa có liên kết thẻ, liên kết tài khoản: Hiển thị tab mặc định là "Thẻ"
     * 2) Nếu User đã có liên kết thẻ, chưa có liên kết tài khoản: Hiển thị tab "Thẻ"
     * 3) Nếu User đã có liên kết tài khoản, chưa có liên kết thẻ: Hiển thị tab "Tài khoản"
     * 4) Nếu User đã có cả 2 liên kết thẻ và tài khoản: Hiển thị tab "Thẻ"
     * Reference: https://gitlab.com/zalopay/bugs/issues/273
     */
    private void changePageInContext() {
        int lastPageIndex = mPreferences.getInt(Constants.PREF_LINK_BANK_LAST_INDEX, -1);
        if (lastPageIndex < 0) {
            List<DMappedCard> mapCardList = CShareDataWrapper.getMappedCardList(mUser);
            List<DBankAccount> mapAccList = CShareDataWrapper.getMapBankAccountList(mUser);
            LinkBankPagerIndex linkBankPagerIndex;

            if (Lists.isEmptyOrNull(mapCardList) && Lists.isEmptyOrNull(mapAccList)) {
                linkBankPagerIndex = LinkBankPagerIndex.LINK_CARD;
            } else if (!Lists.isEmptyOrNull(mapCardList)) {
                linkBankPagerIndex = LinkBankPagerIndex.LINK_CARD;
            } else if (!Lists.isEmptyOrNull(mapAccList)) {
                linkBankPagerIndex = LinkBankPagerIndex.LINK_ACCOUNT;
            } else {
                linkBankPagerIndex = LinkBankPagerIndex.LINK_CARD;
            }

            if (mView != null) {
                mView.initViewPager(linkBankPagerIndex.getValue());
            }
        } else {
            mView.initViewPager(lastPageIndex);
        }
    }
}
