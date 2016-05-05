package vn.com.vng.zalopay.ui.presenter;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseUserPresenter extends BaseAppPresenter {

    AppConfigRepository appConfigRepository = AndroidApplication.instance().getUserComponent().appConfigRepository();

}
