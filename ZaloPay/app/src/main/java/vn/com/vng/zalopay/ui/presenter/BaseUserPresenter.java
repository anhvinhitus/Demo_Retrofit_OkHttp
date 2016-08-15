package vn.com.vng.zalopay.ui.presenter;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.transfer.TransferRepository;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseUserPresenter extends BaseAppPresenter {

    protected AppResourceStore.Repository mAppResourceRepository = AndroidApplication.instance().getUserComponent().appResourceRepository();

    protected ZaloPayRepository zaloPayRepository = AndroidApplication.instance().getUserComponent().zaloPayRepository();

    protected AccountStore.Repository accountRepository = AndroidApplication.instance().getUserComponent().accountRepository();

    protected TransactionStore.Repository transactionRepository = AndroidApplication.instance().getUserComponent().transactionRepository();

    protected BalanceStore.Repository balanceRepository = AndroidApplication.instance().getUserComponent().balanceRepository();

    protected NotificationStore.Repository notificationRepository = AndroidApplication.instance().getUserComponent().notificationRepository();

    protected TransferStore.Repository transferRepository = AndroidApplication.instance().getUserComponent().transferRepository();

}
