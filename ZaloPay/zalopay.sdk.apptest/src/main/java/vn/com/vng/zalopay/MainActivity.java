package vn.com.vng.zalopay;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.com.vng.zalopay.util.AppUtils;
import vn.com.vng.zalopay.util.HMACUtil;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWNotification;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.enumeration.ELinkAccType;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentChannel;
import vn.com.zalopay.wallet.business.entity.error.CError;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.entity.user.ListUserProfile;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.controller.SDKPayment;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;
import vn.com.zalopay.wallet.merchant.listener.IGetCardSupportListListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

public class MainActivity extends ActionBarActivity implements Callback {

    public static String mUrl;
    IGetCardSupportListListener iGetCardSupportListListener = new IGetCardSupportListListener() {
        @Override
        public void onProcess() {
            Log.d("getCardSupportList", "===onProcess===");
        }

        @Override
        public void onComplete(ArrayList<ZPCard> cardSupportArrayList) {

            Log.d("getCardSupportList", "===onComplete===");

            for (ZPCard card : cardSupportArrayList) {
                Log.d(card.getCardCode(), ResourceManager.getImage(card.getCardLogoName()).toString());
            }
        }

        @Override
        public void onError(String pErrorMess) {

            Log.e("getCardSupportList", "===onError===" + pErrorMess);
        }

        @Override
        public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {

            Log.d("getCardSupportList", "===onUpVersion===");

        }
    };
    OnClickListener onGetAccessTockenListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
            paymentInfo.userInfo = new UserInfo();
            paymentInfo.userInfo.zaloPayUserId = mUserID;
            paymentInfo.userInfo.accessToken = editTextAccessToken.getText().toString();
            SDKApplication.loadGatewayInfo(paymentInfo,
                    new ZPWGatewayInfoCallback()
                    {
                        @Override
                        public void onFinish() {
                            Log.d("loadGatewayInfo", "onSuccess");

                        }

                        @Override
                        public void onProcessing() {
                            Log.d("loadGatewayInfo", "onProcessing");
                        }

                        @Override
                        public void onError(String pMessage) {
                            Log.e("loadGatewayInfo", ! TextUtils.isEmpty(pMessage) ? pMessage: "error");
                        }

                        @Override
                        public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
                            Log.e("loadGatewayInfo", "need to update new version : "+ pVersion + ".Message: "+ pMessage);
                        }
                    });
            for (int i=0;i<10;i++)
            {
                Log.d(this,"i="+i);
                SDKApplication.refreshGatewayInfo(paymentInfo,
                        new ZPWGatewayInfoCallback()
                        {
                            @Override
                            public void onFinish() {
                                Log.d("loadGatewayInfo", "onSuccess");

                            }

                            @Override
                            public void onProcessing() {
                                Log.d("loadGatewayInfo", "onProcessing");
                            }

                            @Override
                            public void onError(String pMessage) {
                                Log.e("loadGatewayInfo", ! TextUtils.isEmpty(pMessage) ? pMessage: "error");
                            }

                            @Override
                            public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
                                Log.e("loadGatewayInfo", "need to update new version : "+ pVersion + ".Message: "+ pMessage);
                            }
                        });
                /*try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
            /*
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("SDKPayment.isOpenSdk()=", SDKPayment.isOpenSdk() + "");
                    Log.e("SDKPayment.canCloseSdk()=", SDKPayment.canCloseSdk() + "");

                    if (SDKPayment.canCloseSdk()) {
                        try {
                            SDKPayment.closeSdk();
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                }
            }, 20000);
            */
            /*
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //CShareData.getInstance().notifyLinkBankAccountFinish(new ZPWNotification(116, "Lien ket VCB thanh cong"));
                    StatusResponse statusResponse = new StatusResponse();
                    statusResponse.zptransid = null;
                    statusResponse.isprocessing = false;
                    statusResponse.returncode = 1;
                    CShareData.getInstance().notifyTransactionFinish(GsonUtils.toJsonString(statusResponse));
                }
            },60000);
            */
            /*
            ZPWRemoveMapCardParams params = new ZPWRemoveMapCardParams();

			DMappedCard mapCard = new DMappedCard();
			mapCard.cardname = "TRAN MINH LY";
			mapCard.first6cardno = "970415";
			mapCard.last4cardno = "8206";
			mapCard.bankcode   = "123PVTB";
			params.accessToken = editTextAccessToken.getText().toString();;
			params.userID = editTextZaloUserID.getText().toString();
			params.mapCard = mapCard;

			SDKApplication.removeCardMap(params, new ZPWRemoveMapCardListener() {
				@Override
				public void onSuccess(DMappedCard mapCard) {
					Log.e("removeCardMap","===mapCard="+GsonUtils.toJsonString(mapCard));
				}

				@Override
				public void onError(BaseResponse pMessage) {
					Log.e("removeCardMap","===pMessage="+ GsonUtils.toJsonString(pMessage));
				}
			});
			*/


			/*
            List<DMappedCard> mapCardLis = null;
			try {
				mapCardLis = CShareData.getInstance().getMappedCardList(editTextZaloUserID.getText().toString());
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			Log.e("DMappedCard-list", GsonUtils.toJsonString(mapCardLis));

			try {
				Log.e("link-card value: ",CShareData.getInstance().getLinkCardValue()+"");
			} catch (Exception e) {
				e.printStackTrace();
			}

			*/

			/*
            try
			{
				ZPWRemoveMapCardParams params = new ZPWRemoveMapCardParams();

				DMappedCard mapCard = new DMappedCard();
				mapCard.cardname = "NGUYEN VAN A";
				mapCard.first6cardno = "41111111";
				mapCard.last4cardno = "1111";
				mapCard.bankcode   = "123PCC";
				params.accessToken = editTextAccessToken.getText().toString();;
				params.userID = editTextZaloUserID.getText().toString();
				params.mapCard = mapCard;

				CShareData.getInstance().reloadMapCardList(params,new IReloadMapCardInfoListener() {
					@Override
					public void onComplete(List<DMappedCard> pMapCardList) {
						Log.e("reloadMapCardList","pMapCardList="+ GsonUtils.toJsonString(pMapCardList));
					}

					@Override
					public void onError(String pErrorMess) {
						Log.e("reloadMapCardList","pErrorMess="+pErrorMess);
					}
				});

			} catch (Exception e)
			{
				e.printStackTrace();
			}
			*/

			/*

			try
			{
				Log.e("detectCardType",CShareData.getInstance().detectCardType("41111").toString());

			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				UserInfo userInfo = new UserInfo();
				userInfo.zaloPayUserId = mUserID;
				userInfo.accessToken = editTextAccessToken.getText().toString();

				CShareData.getInstance().setUserInfo(userInfo).detectCardType("41111", new IDetectCardTypeListener() {
					@Override
					public void onComplete(ECardType pCardType) {
						Log.d("detectCardType","===onComplete===pCardType="+pCardType);
					}

					@Override
					public void onProcess() {
						Log.d("detectCardType","===onProcess===");
					}

					@Override
					public void onError(String pErrorMess) {
						Log.e("detectCardType","===onError===pErrorMess="+pErrorMess);
					}

					@Override
					public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
						Log.d("detectCardType","===onUpVersion==pForceUpdate="+pForceUpdate+"===pVersion="+pVersion);
					}
				});

			} catch (Exception e)
			{
				e.printStackTrace();
			}
			*/
            /*

			try {
				CShareData.getInstance().getWithDrawBankList(new IGetWithDrawBankList() {
					@Override
					public void onComplete(List<BankConfig> pBankConfigList) {
						Log.e("getWithDrawBankList",GsonUtils.toJsonString(pBankConfigList));
					}

					@Override
					public void onError(String pErrorMess) {
						Log.e("getWithDrawBankList","pErrorMess"+pErrorMess);
					}
				});
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try {
				Log.e("getUnzipFolderPath",CShareData.getInstance().getUnzipFolderPath().toString());
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try {
				Log.e("min deposit",CShareData.getInstance().getMinDepositValue()+"");
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try {
				Log.e("max deposit",CShareData.getInstance().getMaxDepositValue() +"");
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try {
				Log.e("min tranfer",CShareData.getInstance().getMinTranferValue()+"");
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try {
				Log.e("max tranfer",CShareData.getInstance().getMaxTranferValue() +"");
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try {
				Log.e("min withdraw",CShareData.getInstance().getMinWithDrawValue()+"");
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try {
				Log.e("max withdraw",CShareData.getInstance().getMaxWithDrawValue() +"");
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try {
				Log.e("enable deposite",CShareData.getInstance().isEnableDeposite() +"");
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				List<DBanner> bannerList = CShareData.getInstance().getBannerList();
				Log.e("banner list size",bannerList.size()+"");
				Log.e("banners: ", GsonUtils.toJsonString(bannerList));
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				ArrayList<Integer> insideApps = (ArrayList<Integer>) CShareData.getInstance().getApproveInsideApps();

				Log.e("apps list size",insideApps.size()+"");
				Log.e("apps id: ", GsonUtils.toJsonString(insideApps));

			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				Log.e("platform info expried time", + CShareData.getInstance().getPlatformInfoExpiredTime() + " ms");
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			*/

            /*
            UserInfo userInfo = new UserInfo();
            userInfo.zaloPayUserId = mUserID;
            userInfo.accessToken = editTextAccessToken.getText().toString();

            List<DBankAccount> accounts = CShareData.getInstance().setUserInfo(userInfo).getMapBankAccountList(userInfo.zaloPayUserId);

            Log.d(this, "accounts=" + GsonUtils.toJsonString(accounts));
            */
            //createListener();
            //CShareData.getInstance().setUserInfo(userInfo).getCardSupportList(iGetCardSupportListListener);

			/*
            new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					iGetCardSupportListListener = null;
					Log.d("iGetCardSupportListListener=",iGetCardSupportListListener == null ? "null": iGetCardSupportListListener.toString());

				}
			},2000);
			*/

			/*
            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
			paymentInfo.userInfo.zaloPayUserId = mUserID;
			paymentInfo.userInfo.accessToken = editTextAccessToken.getText().toString();


			SDKApplication.refreshGatewayInfo(MainActivity.this, paymentInfo,
					new ZPWGatewayInfoCallback()
					{

						@Override
						public void onFinish() {
							Log.d("loadGatewayInfo", "onSuccess");

						}

						@Override
						public void onProcessing() {
							Log.d("loadGatewayInfo", "onProcessing");
						}

						@Override
						public void onError(String pMessage) {
							Log.e("loadGatewayInfo", ! TextUtils.isEmpty(pMessage) ? pMessage: "error");
						}

						@Override
						public void onUpVersion(String pVersion,String pMessage) {
							Log.e("loadGatewayInfo", "need to update new version : "+ pVersion + ".Message: "+ pMessage);
						}
					});
					*/


			/*

			try {
				Log.e("sha256", ZPWUtils.sha256("1234"));
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			*/

			/*
			paymentInfo = new ZPWPaymentInfo();
			paymentInfo.userInfo.zaloUserId = mUserID;
			paymentInfo.userInfo.accessToken = editTextAccessToken.getText().toString();
			paymentInfo.walletTransID = "8603984916740465452";
			ZingMobilePayApplication.saveCardMap(MainActivity.this, paymentInfo, new ZPWSaveMapCardListener()
			{
				@Override
				public void onSuccess()
				{
					Log.e("saveCardMap.onSuccess", "onSuccess");
				}

				@Override
				public void onError(String pMessage)
				{
					Log.e("saveCardMap.onError", "error"+pMessage);
				}
			});
			*/

        }
    };
    private EditText editTextAppID;
    private EditText editTextZaloUserID;
    private EditText editTextAccessToken;
    private EditText username;
    private EditText itemName;
    private EditText itemPrice;
    private EditText desc;
    private CheckBox chkLinkCard, chkWalletTransfer, chkWithDraw, chkLinkAcc;
    private RadioButton radioLink, radioUnlink;
    private boolean isFirstLoad = true;
    private String key1 = "E3kCLDkLL2GDhaYhEahsbviSfzwSCDXi";
    private String key = "YhEahsbviSfzwSCDXiE3kCLDkLL2GDha";
    private String mUserID;
    private ZPWPaymentInfo paymentInfo;
    private ServiceAPI mServiceAPI;
    private Call mCallBack;
    private int mLevel = 1;
    private String mJsontest;
    OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            paymentInfo = new ZPWPaymentInfo();

            paymentInfo.userInfo = new UserInfo();

            paymentInfo.userInfo.phoneNumber = "01224012165";
            paymentInfo.userInfo.userName = "Tran Minh Ly";
            paymentInfo.userInfo.zaloPayName = "lytm";
            paymentInfo.userInfo.level = mLevel;
            paymentInfo.userInfo.balance = 100000;
            paymentInfo.userInfo.userProfile = mJsontest;
            paymentInfo.userInfo.accessToken = editTextAccessToken.getText().toString();
            paymentInfo.userInfo.zaloUserId = "1671417175338866297";
            paymentInfo.userInfo.zaloPayUserId = mUserID;

            //paymentInfo.forceChannelIds = new int[1];
            //paymentInfo.forceChannelIds[0] = CShareData.getInstance().getZaloChannelId();
            //paymentInfo.forceChannelIds[1] = CShareData.getInstance().getATMChannelId();
            //paymentInfo.forceChannelIds[2] = CShareData.getInstance().getCreditCardChannelId();

            EPaymentChannel forcedPaymentChannel = null;

            paymentInfo.chargeInfo = null;

            if (chkLinkAcc.isChecked()) {
                forcedPaymentChannel = EPaymentChannel.LINK_ACC;
            }

            if (chkLinkCard.isChecked()) {
                forcedPaymentChannel = EPaymentChannel.LINK_CARD;
            }

            if (chkWalletTransfer.isChecked()) {
                forcedPaymentChannel = EPaymentChannel.WALLET_TRANSFER;
            }

            if (chkWithDraw.isChecked()) {
                forcedPaymentChannel = EPaymentChannel.WITHDRAW;
            }

            paymentInfo.appID = Long.parseLong(editTextAppID.getText().toString());

            paymentInfo.appTime = System.currentTimeMillis();

            if (forcedPaymentChannel == EPaymentChannel.LINK_ACC) {
                // set linker Type for Link Account Bank
                if (radioLink.isChecked()) {
                    paymentInfo.linkAccInfo = new LinkAccInfo("ZPVCB", ELinkAccType.LINK);
                } else if (radioUnlink.isChecked()) {
                    paymentInfo.linkAccInfo = new LinkAccInfo("ZPVCB", ELinkAccType.UNLINK);
                }
            } else if (forcedPaymentChannel == EPaymentChannel.LINK_CARD) {

            } else {
                paymentInfo.appTransID = AppUtils.convertDate(System.currentTimeMillis()) + System.currentTimeMillis();

                paymentInfo.itemName = MainActivity.this.itemName.getText().toString();
                paymentInfo.amount = Long.parseLong(MainActivity.this.itemPrice.getText().toString());

                paymentInfo.description = MainActivity.this.desc.getText().toString();
                paymentInfo.embedData = "embedData";

                paymentInfo.appUser = MainActivity.this.username.getText().toString();

                String keyMac = (paymentInfo.appID == 1) ? key1 : key;

                paymentInfo.mac = generateHMAC(paymentInfo, 1, keyMac);
            }

            // push Notification
//            CShareData.getInstance().notifyLinkBankAccountFinish(new ZPWNotification(116, "Lien ket VCB thanh cong"));

//			// push Notification
//			JSONObject jsonObject = new JSONObject();
//			try {
//				jsonObject.put("notifictiontype", 115);
//				jsonObject.put("message", "Huy Lien ket VCB thanh cong");
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
            /*
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    CShareData.getInstance().notifyLinkBankAccountFinish(new ZPWNotification(116, "Lien ket VCB thanh cong"));
                }
            },60000);
            */


            onPay(forcedPaymentChannel);

        }
    };

    /**
     * HMAC data send to server
     *
     * @param pPaymentInfo  payment info
     * @param pSecurityMode Security Mode
     * @param pSecretKey    Secret key
     * @return String hmac
     */
    private static String generateHMAC(ZPWPaymentInfo pPaymentInfo, int pSecurityMode, String pSecretKey) {
        String stringBuilder = String.valueOf(pPaymentInfo.appID) + '|' + pPaymentInfo.appTransID + '|' +
                pPaymentInfo.appUser + '|' + pPaymentInfo.amount + '|' +
                pPaymentInfo.appTime + '|' + pPaymentInfo.embedData + '|' +
                pPaymentInfo.itemName;

        String hmac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACS.get(pSecurityMode), pSecretKey, stringBuilder);

        return hmac;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chkLinkCard = (CheckBox) findViewById(R.id.chkLinkCard);
        chkWalletTransfer = (CheckBox) findViewById(R.id.chkTransfer);
        chkWithDraw = (CheckBox) findViewById(R.id.chkWithDraw);
        chkLinkAcc = (CheckBox) findViewById(R.id.chkLinkAcc);

        radioLink = (RadioButton) findViewById(R.id.radio_link);
        radioUnlink = (RadioButton) findViewById(R.id.radio_unlink);

        editTextAppID = (EditText) findViewById(R.id.editTextAppID);
        editTextZaloUserID = (EditText) findViewById(R.id.editTextZaloUserID);
        editTextAccessToken = (EditText) findViewById(R.id.editTextAccessToken);
        username = (EditText) findViewById(R.id.editTextUsername);
        itemName = (EditText) findViewById(R.id.editTextIName);
        itemPrice = (EditText) findViewById(R.id.editTextPrice);
        desc = (EditText) findViewById(R.id.editTextDesc);

        findViewById(R.id.btn).setOnClickListener(onClickListener);
        findViewById(R.id.buttonGetAccessToken).setOnClickListener(onGetAccessTockenListener);
        getAccessTocken();

    }

    private void getAccessTocken() {
        mUrl = BuildConfig.HOST;
        mUrl = mUrl.concat("um/");

        mServiceAPI = RetrofitSetup.createService(ServiceAPI.class);
        //mCallBack = mServiceAPI.getAccessToken("1",editTextZaloUserID.getText().toString(),"EDjnd71KeIDOznRJNbBpIc749RuJS-DC9Ciakc5QbpSUvLIv1K3PEoZP3DOlSTieMimQv1LEaIfltoAb5cpY8GF9K_0NIRGR6RbXxs1KenmAw1N6TKlsKaUW2CvyFBuyUu9Zt7KziI8RWdBIBotaBYVh3k462j5z98TuZp0_s3zfsq782G2DAKVXRg5aBumu6OjClnHAXb1kutkvQJwwDrpP8P9y6lGm0emae4icwXrMi4tR8YYX2ZM-GxA0MUDJYTPaYeBfNpFAOw-kjh5_JyS5mREaodbPiMx7iF7841lzEBNdaU1W3rvP9gItQ4fmT_Sb");
        mCallBack = mServiceAPI.getAccessToken("1", editTextZaloUserID.getText().toString(), "APR0ZRUMPqb9kgV2cP0r0729au7UkIO3FhEMZgokKdGAck67nBLHCooO-gJvlrWXNudebiQI52DaZDNHoxjWBWEVgOJ1a24LHRQ6c-FMDn9Vj_UczQbaIH7DxQxytYWkCF26l__S93j7x9spfSnyBNk5uPcCwqbiMSUNvPlmG2uKcwwYeiyLE3gXWSlFu1GfIj2ztehj7Yf5lBoubEuAJNU6vfE1oaHAATFZeRRsMcTzxuhQt_WHS1xXX8iSSbmHmtAiHgnyt8GoULjYc2Jy-3yVDN-AN_hkINGpVDzTawm58a4hltNZk5vP9d8u7p6tjJ5F");
        //mCallBack = mServiceAPI.getAccessToken("1",editTextZaloUserID.getText().toString(),"i0OorLMrz5lGAbMQUxEcCkaMP_qtdBSuhHOiiq25YGsJ1rso8fpx0Q4vDjuCfV4Ir2OdxpQFkX3l8XoeCwpB7O8mO_qxZ8ecc4KVwHULcYo14ZtN1BYQSfjlSjyDpz8TfqqPmmpErWZFJWNCLzs56F8dUUP--h1Pq6KFccdXkHkTEJRTKDxi3RC36RGzyUqSmd0beNd--ZUO9tF_DAh6Tx1qGwXSXPPQZKGlqqZFo6xCGLtTNUsl5S13mZMsgGF351gM09YJ1eXz9S5KlTnpuW0Yh5x4rdts0osqIOZETC8-Tg5kYUKJlLac2Oj-UrMMebq");
        //mCallBack = mServiceAPI.getAccessToken("1",editTextZaloUserID.getText().toString(),"-VL6zNmIrLIPsI6wKXI7FQ3g5zWLEPCnvUaNsM4GuaxLm6BCD3giKDJBEw0S1wf5fD4cboOxxaUHmHFIBHkIG_tPOemaCTvuvlnAo5iZpIJxzcgb70kVB-E88AqLVjL5mhTCinPvm52ihq-lOLVFAepkO-OcFlvrwFGQWLrbspFVfrcqQM6i1S_q1uWg6zaQWSbHc39-k0hojYU1A0tmEicFQwbPC9ryZjPP-pKrsbtrorw933lHIVLzkiKlkmWQxKdNEEg64fzlHV1zs9nnXND1YXpGddta55264eRc0g4aREDKXDeEhoPftojKV363KW");
        //mCallBack = mServiceAPI.getAccessToken("1", editTextZaloUserID.getText().toString(), "ennFfUnPTW33C2ZgmbyaQVeC7egCVJTZiW1Jm_Lb2LQD4o39btb_KRuYGwgkSazFs0vbcP1H04tZ2bdNctfuI84J8x2zIIOJcH0V_V1qI23M1WZngLt-Sl82ghkYCwWFy4QOul0zk7ZU5zFRxNByLS9j_9_f3TCNytkY_li1xd3c6zwHaHJW2Emuueo86C1jzM6FyFzEbm7iLUBTh6AFN8v4skxoBe4zZctTgE45ZqQV4AsoZ2R16jbTZgcJUliTiX_3c8i1nLIgRkZIv375OgK4hSIwQeLYw02fWTXswLYYEykKZnA-LuKPiUdNCTsAhshKV7Gl-gDSRjHB5mkRxrlAiDGfijWC6iYJkfEJcspKzjJtYv3_CvBOnCw4leX2vywfiuJ5x2UNsy2ZzSk08IQZVO5Gx7eW1W");
        //mCallBack = mServiceAPI.getAccessToken("1", editTextZaloUserID.getText().toString(), "g3y5eZJCd0l84sBj6ihEOl4lL8jHxTDkioWNmYVxxLoCE6V6HU2HSgW53OHytf9dtXuPlKNsssNb7oFuIT-lQuS9Lwn7zU1HaYahisFgf5QFBbZS9f6cNi06VzCxsOaf-sWgo2-hioQDP2tMITE2LgTX9jO9kQvIm7T8oadrc7sHE6MS9OkrBumN4gSqqUicc0bZjq6ku4xWUdAnIBUO7hKfGkOZbyuuWXeebZwia0p3ObgOAuxtTyzWp2WxobBvu3aYMkL4xVLvidKEm0kOdmIr60Iv0PQB1xe04_KPdxywuHKqz3Bum5acSCiXN3JloWq");

        mCallBack.enqueue(this);

    }

    public void createListener() {
        if (iGetCardSupportListListener == null) {
            iGetCardSupportListListener = new IGetCardSupportListListener() {
                @Override
                public void onProcess() {
                    Log.d("getCardSupportList", "===onProcess===");
                }

                @Override
                public void onComplete(ArrayList<ZPCard> cardSupportArrayList) {

                    Log.d("getCardSupportList", "===onComplete===");

                    for (ZPCard card : cardSupportArrayList) {
                        //Log.d(card.getCardCode(), ResourceManager.getImage(card.getCardLogoName()).toString());
                    }
                }

                @Override
                public void onError(String pErrorMess) {

                    Log.e("getCardSupportList", "===onError===" + pErrorMess);
                }

                @Override
                public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {

                    Log.d("getCardSupportList", "===onUpVersion===");

                }
            };
        }
    }

    private void onPay(EPaymentChannel forcedPaymentChannel) {
        SDKPayment.pay(MainActivity.this, forcedPaymentChannel,
                paymentInfo, new ZPPaymentListener() {
                    @Override
                    public void onComplete(final ZPPaymentResult pPaymentResult) {

                        Log.e("ZMP", "onComplete CallBack: " + GsonUtils.toJsonString(pPaymentResult));

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                pPaymentResult.paymentInfo.userInfo = null;

                                DialogManager.showSweetDialogCustom(GlobalData.getMerchantActivity(), "onComplete CallBack: " + GsonUtils.toJsonString(pPaymentResult), "Text Button",
                                        SweetAlertDialog.NORMAL_TYPE, null);
					/*			DialogManager.showSweetDialogNoInternet(GlobalData.getMerchantActivity(), getString(R.string.zingpaysdk_alert_content_nointernet), "right", "left", new OnCustomContentDialogEventListener() {
									@Override
									public void onLeftButtonClick() {

									}

									@Override
									public void onRightButtonClick() {

									}
								});*/

							/*	DialogManager.showSweetDialogNormal(GlobalData.getMerchantActivity(),"","mesage texxt",null,"left button", new OnCustomContentDialogEventListener() {
									@Override
									public void onLeftButtonClick() {

									}

									@Override
									public void onRightButtonClick() {

									}
								});
*/

                            }
                        });
                    }

                    @Override
                    public void onError(CError pError) {
                        Log.e("ZMP", "onError CallBack: " + pError.payError.toString() + ",message: " + pError.messError);
                    }

                    @Override
                    public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
                        Log.e("pay", "force udpate " + pForceUpdate + ".Need to update new version : " + pVersion + ".Message: " + pMessage);
                    }

                    @Override
                    public void onUpdateAccessToken(String pNewAccessToken) {
                        Log.e("pay", "onUpdateAccessToken pNewAccessToken = " + pNewAccessToken);
                    }

                    @Override
                    public void onPreComplete(boolean pIsSuccess, String pTransId, String pAppTransId) {
                        Log.e("pay", "pre complete " + pIsSuccess + " , transid " + pTransId);
                    }

                }, new TestFingerPrint());
    }

    @Override
    public void onResponse(Call call, Response response) {
        TokenModel mToken = (TokenModel) response.body();
        if (isFirstLoad) {
            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();

            mUserID = mToken.userid;

            editTextZaloUserID.setText(mUserID);

            paymentInfo.userInfo = new UserInfo();

            paymentInfo.userInfo.zaloPayUserId = mUserID;
            paymentInfo.userInfo.accessToken = mToken.accesstoken;

            //set user profile
            ListUserProfile mUserProfile;

            mUserProfile = GsonUtils.fromJsonString(GsonUtils.toJsonString(mToken), ListUserProfile.class);
            mJsontest = GsonUtils.toJsonString(mUserProfile);

            mLevel = mToken.profilelevel;

            Log.e("GetAccessToken.onResponse", mJsontest);


          /*  SDKApplication.loadGatewayInfo(paymentInfo,
                    new ZPWGatewayInfoCallback() {

                        @Override
                        public void onFinish() {
                            Log.d("loadGatewayInfo", "onSuccess");

                        }

                        @Override
                        public void onProcessing() {
                            Log.d("loadGatewayInfo", "onProcessing");
                        }

                        @Override
                        public void onError(String pMessage) {
                            Log.e("loadGatewayInfo", !TextUtils.isEmpty(pMessage) ? pMessage : "error");
                        }

                        @Override
                        public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
                            Log.e("loadGatewayInfo", "force udpate " + pForceUpdate + ".Need to update new version : " + pVersion + ".Message: " + pMessage);
                        }
                    });*/


            isFirstLoad = false;
        }


        editTextAccessToken.setText(mToken.accesstoken);
    }

    @Override
    public void onFailure(Call call, Throwable t) {
        Log.e("ZMP", "Create AccessToken Fail");
    }

    public class TestFingerPrint implements IPaymentFingerPrint {
        @Override
        public DialogFragment getDialogFingerprintAuthentication(Activity pActivity, IFPCallback pCallback) throws Exception {
            return null;
        }

        @Override
        public void updatePassword(String pOldPassword, String pNewPassword) throws Exception {
            Log.e("updatePassword", "===pOldPassword=" + pOldPassword + " pNewPassword=" + pNewPassword);
        }

        @Override
        public void showSuggestionDialog(Activity activity, String hashPassword) throws Exception {

        }
    }

}
