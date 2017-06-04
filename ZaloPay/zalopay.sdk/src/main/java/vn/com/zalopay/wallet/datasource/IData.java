package vn.com.zalopay.wallet.datasource;


import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.base.SaveCardResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;
import vn.com.vng.zalopay.network.API_NAME;

public interface IData {

    /**
     * load platforminfo
     * @param params
     * @return
     */
    @GET(Constants.URL_PLATFORM_INFO)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_V001GETPLATFORMINFO)
    Observable<DPlatformInfo> loadPlatformInfo(@QueryMap Map<String, String> params);

    /**
     * load appinfo
     *
     * @param params
     * @return
     */
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_GETAPPINFO)
    Observable<AppInfoResponse> loadAppInfo(@QueryMap Map<String, String> params);

    /**
     * load bank list
     *
     * @return
     */
    @GET(Constants.URL_GET_BANKLIST)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_GETBANKLIST)
    Observable<BankConfigResponse> loadBankList(@QueryMap Map<String, String> params);

    /**
     * get transaction status
     *
     * @param params
     * @return
     */
    @GET(Constants.URL_GET_STATUS)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_GETTRANSSTATUS)
    Observable<StatusResponse> getStatus(@QueryMap Map<String, String> params);

    /***
     * get map card info list
     * @param params
     * @return
     */
    @GET(Constants.URL_LISTCARDINFO)
    @API_NAME(ZPEvents.CONNECTOR_UM_LISTCARDINFOFORCLIENT)
    Observable<CardInfoListResponse> loadMapCardList(@QueryMap Map<String, String> params);

    /***
     * get bank account list(vietcombank)
     * @param params
     * @return
     */
    @GET(Constants.URL_LISTBANKACCCOUNT)
    @API_NAME(ZPEvents.CONNECTOR_UM_LISTBANKACCOUNTFORCLIENT)
    Observable<BankAccountListResponse> loadMapBankAccountList(@QueryMap Map<String, String> params);

    /***
     * get status by apptrans id which is created by app when creating order.
     *
     * @param params
     * @return
     */
    @GET(Constants.URL_CHECK_SUBMIT_ORDER_STATUS)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_GETSTATUSBYAPPTRANSIDFORCLIENT)
    Observable<StatusResponse> checkOrderStatusFailSubmit(@QueryMap Map<String, String> params);

    /**
     * get resource file
     * @param url
     * @return
     */
    @GET
    @Streaming
    Call<ResponseBody> getFile(@Url String url);
    /**
     * Api ATM Authen
     *
     * @param userID
     * @param accessToken
     * @param zpTransid
     * @param authenType
     * @param authenValue
     * @return
     */
    @POST(Constants.URL_ATM_AUTHEN)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_ATMAUTHENPAYER)
    Observable<StatusResponse> atmAuthen(@Query(ConstantParams.USER_ID) String userID,
                                   @Query(ConstantParams.ACCESS_TOKEN) String accessToken,
                                   @Query(ConstantParams.ZP_TRANSID) String zpTransid,
                                   @Query(ConstantParams.AUTHEN_TYPE) String authenType,
                                   @Query(ConstantParams.AUTHEN_VALUE) String authenValue,
                                   @Query(ConstantParams.APP_VERSION) String appver);


    @POST(Constants.URL_REPORT_ERROR)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_SDKERRORREPORT)
    Observable<SaveCardResponse> sdkReport(@Query(ConstantParams.USER_ID) String userID,
                                     @Query(ConstantParams.ACCESS_TOKEN) String accessToken,
                                     @Query(ConstantParams.TRANSID) String transid,
                                     @Query(ConstantParams.BANK_CODE) String bankCode,
                                     @Query(ConstantParams.EXINFO) String exinfo,
                                     @Query(ConstantParams.EXCEPTION) String exception);

    /**
     * Api ramove card
     *
     * @param userID
     * @param accessToken
     * @param cardName
     * @param first6Cardno
     * @param last4Cardno
     * @param bankCode
     * @return
     */
    @POST(Constants.URL_REMOVE_MAPCARD)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_REMOVEMAPCARD)
    Observable<BaseResponse> removeCard(@Query(ConstantParams.USER_ID) String userID,
                                  @Query(ConstantParams.ACCESS_TOKEN) String accessToken,
                                  @Query(ConstantParams.CARD_NAME) String cardName,
                                  @Query(ConstantParams.FIRST6_CARDNO) String first6Cardno,
                                  @Query(ConstantParams.LAST4_CARDNO) String last4Cardno,
                                  @Query(ConstantParams.BANK_CODE) String bankCode,
                                  @Query(ConstantParams.APP_VERSION) String appver);

    /**
     * Api Send Log
     *
     * @param userID
     * @param accessToken
     * @param pmcID
     * @param transID
     * @param atmcaptchaBegindate
     * @param atmcaptchaEnddate
     * @param atmotpBegindate
     * @param atmotpEnddate
     * @return
     */
    @POST(Constants.URL_TRACKING_LOG)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_SDKWRITEATMTIME)
    Observable<BaseResponse> sendLog(@Query(ConstantParams.USER_ID) String userID,
                               @Query(ConstantParams.ACCESS_TOKEN) String accessToken,
                               @Query(ConstantParams.PMC_ID) String pmcID,
                               @Query(ConstantParams.TRANS_ID) String transID,
                               @Query(ConstantParams.ATM_CAPTCHA_BEGINDATE) String atmcaptchaBegindate,
                               @Query(ConstantParams.ATM_CAPTCHA_ENDDATE) String atmcaptchaEnddate,
                               @Query(ConstantParams.ATM_OTP_BEGINDATE) String atmotpBegindate,
                               @Query(ConstantParams.ATM_OTP_ENDDATE) String atmotpEnddate,
                               @Query(ConstantParams.APP_VERSION) String appver);

    /**
     * Api submit Transaction
     *
     * @param appID
     * @param zaloID
     * @param appTransaction
     * @param appUser
     * @param appTime
     * @param item
     * @param description
     * @param embeddata
     * @param mac
     * @param platform
     * @param platformcode
     * @param amount
     * @param deviceID
     * @param deviceModel
     * @param appver
     * @param sdkver
     * @param osver
     * @param connType
     * @param mno
     * @param pmcID
     * @param chargeInfo
     * @param pin
     * @param transType
     * @param accessToken
     * @param userID
     * @return
     */
    @POST(Constants.URL_SUBMIT_ORDER)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_SUBMITTRANS)
    Observable<StatusResponse> submitOrder(@Query(ConstantParams.APP_ID) String appID,
                                     @Query(ConstantParams.ZALO_ID) String zaloID,
                                     @Query(ConstantParams.APP_TRANS_ID) String appTransaction,
                                     @Query(ConstantParams.APP_USER) String appUser,
                                     @Query(ConstantParams.APP_TIME) String appTime,
                                     @Query(ConstantParams.ITEM) String item,
                                     @Query(ConstantParams.DESCRIPTION) String description,
                                     @Query(ConstantParams.EMBED_DATA) String embeddata,
                                     @Query(ConstantParams.MAC) String mac,
                                     @Query(ConstantParams.PLATFORM) String platform,
                                     @Query(ConstantParams.PLATFORM_CODE) String platformcode,
                                     @Query(ConstantParams.AMOUNT) String amount,
                                     @Query(ConstantParams.DEVICE_ID) String deviceID,
                                     @Query(ConstantParams.DEVICE_MODEL) String deviceModel,
                                     @Query(ConstantParams.APP_VERSION) String appver,
                                     @Query(ConstantParams.SDK_VERSION) String sdkver,
                                     @Query(ConstantParams.OS_VERSION) String osver,
                                     @Query(ConstantParams.CONN_TYPE) String connType,
                                     @Query(ConstantParams.MNO) String mno,
                                     @Query(ConstantParams.PMC_ID) String pmcID,
                                     @Query(ConstantParams.CHARGE_INFO) String chargeInfo,
                                     @Query(ConstantParams.PIN) String pin,
                                     @Query(ConstantParams.TRANS_TYPE) String transType,
                                     @Query(ConstantParams.ACCESS_TOKEN) String accessToken,
                                     @Query(ConstantParams.USER_ID) String userID,
                                     @Query(ConstantParams.LATTITUDE) String longitude,
                                     @Query(ConstantParams.LONGITUDE) String latitude);

    @POST(Constants.URL_VERIFY_CARDMAP)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_VERIFYCARDFORMAPPING)
    Observable<StatusResponse> verfiyCardMap(
            @Query(ConstantParams.USER_ID) String userId,
            @Query(ConstantParams.ACCESS_TOKEN) String accessToken,
            @Query(ConstantParams.ZALO_ID) String zaloId,
            @Query(ConstantParams.DEVICE_ID) String deviceId,
            @Query(ConstantParams.PLATFORM) String platform,
            @Query(ConstantParams.SDK_VERSION) String sdkVersion,
            @Query(ConstantParams.OS_VERSION) String osVersion,
            @Query(ConstantParams.CONN_TYPE) String conType,
            @Query(ConstantParams.MNO) String mno,
            @Query(ConstantParams.DEVICE_MODEL) String deviceModel,
            @Query(ConstantParams.CARDINFO) String cardInfo,
            @Query(ConstantParams.APP_VERSION) String appver);

    @GET(Constants.URL_GET_STATUS_MAPCARD)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_GETSTATUSMAPCARD)
    Observable<StatusResponse> getMapCardStatus(@QueryMap Map<String, String> params);

    @POST(Constants.URL_AUTHEN_CARD_MAP)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_AUTHCARDHOLDERFORMAPPING)
    Observable<StatusResponse> authenMapCard(@Query(ConstantParams.USER_ID) String userID,
                                       @Query(ConstantParams.ACCESS_TOKEN) String accessToken,
                                       @Query(ConstantParams.ZP_TRANSID) String zpTransid,
                                       @Query(ConstantParams.AUTHEN_TYPE) String authenType,
                                       @Query(ConstantParams.AUTHEN_VALUE) String otp,
                                       @Query(ConstantParams.APP_VERSION) String appver);


    /***
     *
     * @param userID
     * @param zaloID
     * @param accessToken
     * @param bankAccountInfo
     * @param platform
     * @param deviceID
     * @param appVer
     * @param mno
     * @param sdkVer
     * @param osVer
     * @param deviceModel
     * @param connType
     * @return
     */
    @POST(Constants.URL_SUBMIT_MAP_ACCOUNT)
    @API_NAME(ZPEvents.CONNECTOR_V001_TPE_SUBMITMAPACCOUNT)
    Observable<StatusResponse> submitMapAccount(@Query(ConstantParams.USER_ID) String userID,
                                          @Query(ConstantParams.ZALO_ID) String zaloID,
                                          @Query(ConstantParams.ACCESS_TOKEN) String accessToken,
                                          @Query(ConstantParams.BANK_ACCOUNT_INFO) String bankAccountInfo,
                                          @Query(ConstantParams.PLATFORM) String platform,
                                          @Query(ConstantParams.DEVICE_ID) String deviceID,
                                          @Query(ConstantParams.APP_VERSION) String appVer,
                                          @Query(ConstantParams.MNO) String mno,
                                          @Query(ConstantParams.SDK_VERSION) String sdkVer,
                                          @Query(ConstantParams.OS_VERSION) String osVer,
                                          @Query(ConstantParams.DEVICE_MODEL) String deviceModel,
                                          @Query(ConstantParams.CONN_TYPE) String connType);
}
