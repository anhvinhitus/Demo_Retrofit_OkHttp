package vn.com.vng.zalopay.ui.presenter;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseUserPresenter extends BaseAppPresenter {

    protected AppConfigRepository appConfigRepository = AndroidApplication.instance().getUserComponent().appConfigRepository();

    protected ZaloPayRepository zaloPayRepository = AndroidApplication.instance().getUserComponent().zaloPayRepository();

}
