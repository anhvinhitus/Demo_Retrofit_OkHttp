package vn.com.zalopay.game.controller;

import android.app.Activity;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.base.AppGameSingletonLifeCircle;
import vn.com.zalopay.game.businnesslogic.behavior.channel.AppGameChannelFactory;
import vn.com.zalopay.game.businnesslogic.behavior.channel.AppGameGateway;
import vn.com.zalopay.game.businnesslogic.entity.base.AppGameError;
import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;
import vn.com.zalopay.game.businnesslogic.enums.EAppGameError;
import vn.com.zalopay.game.businnesslogic.interfaces.behavior.IAppGameStartFlow;
import vn.com.zalopay.game.businnesslogic.interfaces.callback.IAppGameResultListener;
import vn.com.zalopay.game.businnesslogic.interfaces.dialog.IDialogListener;
import vn.com.zalopay.game.businnesslogic.provider.config.IGetUrlConfig;
import vn.com.zalopay.game.businnesslogic.provider.dialog.IDialog;
import vn.com.zalopay.game.businnesslogic.provider.networking.INetworking;
import vn.com.zalopay.game.ui.component.activity.AppGameActivity;
import vn.com.zalopay.game.ui.component.activity.AppGameBaseActivity;

public class AppGameController {
    public synchronized static void startPayFlow(final Activity pOwner, AppGamePayInfo pAppGamePayInfo, IAppGameResultListener pListener,
                                                 IDialog pDialog, IGetUrlConfig pUrlConfig, INetworking pNetworking) {
        if (pOwner == null || pAppGamePayInfo == null || pListener == null || pDialog == null || pUrlConfig == null) {
            if (pListener != null)
                pListener.onError(new AppGameError(EAppGameError.COMPONENT_NULL.COMPONENT_NULL, "Component (activity,httpclient) is null"));

            return;
        }

        //set global static
        try {
            AppGameGlobal.setApplication(pOwner, pAppGamePayInfo, pListener, pDialog, pUrlConfig, pNetworking);
        } catch (Exception e) {
            onReturnCancel(AppGameGlobal.getString(R.string.appgame_alert_input_error));

            return;
        }

        //is networking online?
        if (AppGameGlobal.getNetworking() != null && !AppGameGlobal.getNetworking().isOnline(pOwner)) {
            onReturnCancel(AppGameGlobal.getString(R.string.appgame_alert_no_connection));

            return;
        }

        //validate input
        String strCheck = validatePaymentInfo();

        if (!TextUtils.isEmpty(strCheck)) {
            onReturnCancel(strCheck);

            return;
        }

        startScreen();
    }

    /***
     * view pay game result
     * @param pOwner
     * @param pAppGamePayInfo
     * @param pListener
     * @param pDialog
     * @param pUrlConfig
     * @param pNetworking
     */
    public synchronized static void viewPayResult(final Activity pOwner, AppGamePayInfo pAppGamePayInfo, IAppGameResultListener pListener,
                                                  IDialog pDialog, IGetUrlConfig pUrlConfig, INetworking pNetworking)
    {
        if (pOwner == null || pAppGamePayInfo == null || pListener == null || pDialog == null || pUrlConfig == null) {
            if (pListener != null)
                pListener.onError(new AppGameError(EAppGameError.COMPONENT_NULL.COMPONENT_NULL, "Component (activity,httpclient) is null"));

            return;
        }

        //set global static
        try {
            AppGameGlobal.setApplication(pOwner, pAppGamePayInfo, pListener, pDialog, pUrlConfig, pNetworking);
        } catch (Exception e) {
            onReturnCancel(AppGameGlobal.getString(R.string.appgame_alert_input_error));

            return;
        }

        if (AppGameGlobal.getNetworking() != null && !AppGameGlobal.getNetworking().isOnline(AppGameBaseActivity.getCurrentActivity())) {
            onReturnCancel(AppGameGlobal.getString(R.string.appgame_alert_no_connection));

            return;
        }

        startScreen();
    }

    /***
     * dispose all
     */
    public synchronized static void dispose() {
        try {
            if (AppGameBaseActivity.getCurrentActivity() instanceof AppGameActivity)
                AppGameBaseActivity.getCurrentActivity().finish();
        } catch (Exception e) {
            Timber.e("===dispose===%s", e != null ? e.getMessage() : "error");
            return;
        }
    }

    /***
     * determinate which flow will start.
     */
    private static void startScreen() {
        IAppGameStartFlow startFlow = AppGameChannelFactory.procedureChannel();

        if (startFlow == null) {
            onReturnCancel(AppGameGlobal.getString(R.string.appgame_alert_input_error));

            return;
        }

        AppGameGateway.getInstance(startFlow).startFlow();
    }

    private static String validatePaymentInfo() {
        if (AppGameGlobal.getAppGamePayInfo() == null) {
            return AppGameGlobal.getString(R.string.appgame_alert_input_error);
        }

        if (AppGameGlobal.getAppGamePayInfo().getAppId() <= 0
                || TextUtils.isEmpty(AppGameGlobal.getAppGamePayInfo().getUid())
                || TextUtils.isEmpty(AppGameGlobal.getAppGamePayInfo().getAccessToken()))
            return AppGameGlobal.getString(R.string.appgame_alert_input_error);

        return null;
    }

    /***
     * show dialog and callback to app
     */
    private static void onReturnCancel(final String pMessage) {
        if (AppGameGlobal.getDialog() != null) {
            AppGameGlobal.getDialog().hideLoadingDialog();

            AppGameGlobal.getDialog().showInfoDialog(AppGameGlobal.getOwnerActivity(),
                    pMessage, AppGameGlobal.getString(R.string.appgame_button_dialog_close),
                    3, new IDialogListener() {
                        @Override
                        public void onClose() {
                            if (AppGameGlobal.getResultListener() != null)
                                AppGameGlobal.getResultListener().onError(new AppGameError(EAppGameError.DATA_INVALID, pMessage));

                            AppGameSingletonLifeCircle.disposeAll();
                        }
                    });
        }
    }
}
