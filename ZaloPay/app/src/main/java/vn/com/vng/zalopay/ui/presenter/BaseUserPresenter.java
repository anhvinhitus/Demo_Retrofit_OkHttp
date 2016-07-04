package vn.com.vng.zalopay.ui.presenter;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.appresources.AppResource;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.cache.NotificationStore;
import vn.com.vng.zalopay.data.cache.TransactionStore;
import vn.com.vng.zalopay.domain.repository.BalanceRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseUserPresenter extends BaseAppPresenter {

    protected AppResource.Repository mAppResourceRepository = AndroidApplication.instance().getUserComponent().appResourceRepository();

    protected ZaloPayRepository zaloPayRepository = AndroidApplication.instance().getUserComponent().zaloPayRepository();

    protected AccountStore.Repository accountRepository = AndroidApplication.instance().getUserComponent().accountRepository();

    protected TransactionStore.Repository transactionRepository = AndroidApplication.instance().getUserComponent().transactionRepository();

    protected BalanceRepository balanceRepository = AndroidApplication.instance().getUserComponent().balanceRepository();

    protected NotificationStore.Repository notificationRepository = AndroidApplication.instance().getUserComponent().notificationRepository();
}
