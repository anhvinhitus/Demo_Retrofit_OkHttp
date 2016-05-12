package vn.com.vng.zalopay.ui.presenter;

import rx.Subscription;
import vn.com.vng.zalopay.ui.view.ILinkCardProduceView;

/**
 * Created by longlv on 12/05/2016.
 */
public class LinkCardProcedurePresenter extends BaseUserPresenter implements Presenter<ILinkCardProduceView> {

    private ILinkCardProduceView linkCardProduceView;
    private Subscription subscription;

    @Override
    public void setView(ILinkCardProduceView iLinkCardProduceView) {
        this.linkCardProduceView = iLinkCardProduceView;
    }

    @Override
    public void destroyView() {
        linkCardProduceView = null;
        unsubscribeIfNotNull(subscription);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }


    //Zalo payment sdk
    private void pay() {
//        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.==============");
//        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
//
//        EPaymentChannel forcedPaymentChannel = EPaymentChannel.LINK_CARD;
//
//        paymentInfo.appID = BuildConfig.PAYAPPID;
//        paymentInfo.zaloUserID = editTextZaloUserID.getText().toString();
//        paymentInfo.chargeInfo = "";
//        paymentInfo.zaloPayAccessToken = editTextAccessToken.getText().toString();
//
//        paymentInfo.appTime = System.currentTimeMillis();
//
//        paymentInfo.appTransID = convertDate(System.currentTimeMillis()) + System.currentTimeMillis();
//
//        paymentInfo.itemName = MainActivity.this.itemName.getText().toString();
//        paymentInfo.amount = Long.parseLong(MainActivity.this.itemPrice.getText()
//                .toString());
//
//        paymentInfo.description = "";
//        paymentInfo.embedData = "embedData";
//
//        paymentInfo.appUser = "";
//
//        String keyMac = (paymentInfo.appID == 1) ? key1 : key;
//
//        paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1,keyMac);
//
//        ZingMobilePayService.pay(linkCardProduceView.getActivity(), forcedPaymentChannel,
//                paymentInfo, new ZPPaymentListener() {
//
//                    @Override
//                    public void onSMSCallBack(final String appTransID) {
//                    }
//
//                    @Override
//                    public void onComplete(
//                            final ZPPaymentResult pPaymentResult) {
//                    }
//
//                    @Override
//                    public void onCancel() {
//
//                    }
//                });
    }
}
