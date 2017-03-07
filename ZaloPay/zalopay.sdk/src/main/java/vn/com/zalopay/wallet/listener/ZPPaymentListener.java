package vn.com.zalopay.wallet.listener;

import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.error.CError;

public interface ZPPaymentListener {

    void onComplete(ZPPaymentResult pPaymentResult);

    void onError(CError pError);

    void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage);

    /***
     * soft access token
     * app get new access token on this and
     * update on app's local cache again
     *
     * @param pNewAccessToken
     */
    void onUpdateAccessToken(String pNewAccessToken);

    /***
     * @param pIsSuccess  true/false
     * @param pTransId    transaction id
     * @param pAppTransId app transactin id, this is created when create order.
     */
    void onPreComplete(boolean pIsSuccess, String pTransId, String pAppTransId);
}
