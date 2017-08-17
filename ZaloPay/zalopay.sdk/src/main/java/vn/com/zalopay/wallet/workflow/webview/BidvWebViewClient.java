package vn.com.zalopay.wallet.workflow.webview;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.entity.bank.AtmScriptInput;
import vn.com.zalopay.wallet.entity.bank.AtmScriptOutput;
import vn.com.zalopay.wallet.entity.bank.BankScript;
import vn.com.zalopay.wallet.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.entity.enumeration.EJavaScriptType;
import vn.com.zalopay.wallet.entity.response.BaseResponse;
import vn.com.zalopay.wallet.event.SdkParseWebsiteCompleteEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteErrorEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteRenderEvent;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.workflow.ui.BankCardGuiProcessor;

public class BidvWebViewClient extends BankWebViewClient {
    private static final long DELAY_TIME_TO_RUN_SCRIPT = 4000;
    private static final int MAX_INTERVAL_CHECK_COUNT = 15;
    private int countIntervalCheck = 0;
    private boolean mIsRunningScript = false;
    private BankScript mCurrentBankScript = null;

    private Handler mHandler = new Handler();

    public BidvWebViewClient(AbstractWorkFlow pAdapter, SdkWebView webView) {
        super(pAdapter, webView);
    }

    @Override
    public void hit() {
        matchAndRunJs(EJavaScriptType.HIT, false);
    }

    @NonNull
    @Override
    protected AtmScriptInput genJsInput() throws Exception {
        AtmScriptInput input = super.genJsInput();
        AbstractWorkFlow workFlow = null;
        try {
            workFlow = getWorkFlow();
        } catch (Exception e) {
            Timber.d(e);
        }
        if (workFlow == null) {
            return input;
        }
        BankCardGuiProcessor guiProcessor = null;
        try {
            if (workFlow.getGuiProcessor() instanceof BankCardGuiProcessor) {
                guiProcessor = (BankCardGuiProcessor) workFlow.getGuiProcessor();
            }
        } catch (Exception e) {
            Timber.d(e);
        }

        if (guiProcessor != null) {
            input.onlinePassword = guiProcessor.getOnlinePassword();
        }
        return input;
    }

    private boolean isMatchUrl(String pUrl) {
        boolean isMatch = false;
        for (BankScript bankScript : mBankScripts) {
            if (bankScript.eventID != IGNORE_EVENT_ID_FOR_HTTPS && pUrl.matches(bankScript.url)) {
                mCurrentBankScript = bankScript;
                isMatch = true;
            }
        }
        return isMatch;
    }

    private void matchAndRunJs(EJavaScriptType pType, boolean pIsAjax) {
        if (mCurrentBankScript == null) {
            return;
        }
        mEventID = mCurrentBankScript.eventID;
        mPageCode = mCurrentBankScript.pageCode;

        AtmScriptInput input = null;
        try {
            input = genJsInput();
        } catch (Exception e) {
            Timber.w(e);
        }
        if (input == null) {
            return;
        }
        input.isAjax = pIsAjax;
        String inputScript = GsonUtils.toJsonString(input);
        if (pType == EJavaScriptType.AUTO) {
            executeJs(mCurrentBankScript.autoJs, inputScript);
        }
        if (pType == EJavaScriptType.HIT) {
            executeJs(mCurrentBankScript.hitJs, inputScript);
        }
    }

    private boolean loadingStaticResource(String pUrl) {
        return TextUtils.isEmpty(pUrl)
                || pUrl.contains(GlobalData.getStringResource(RS.string.sdk_website_callback_domain))
                || pUrl.endsWith(".css")
                || pUrl.endsWith(".js")
                || pUrl.endsWith(".png")
                || pUrl.endsWith(".ico")
                || pUrl.endsWith(".xml")
                || pUrl.endsWith(".jpg")
                || pUrl.endsWith(".jpeg")
                || pUrl.endsWith(".gif");
    }

    private void intervalCheck() {
        if (countIntervalCheck >= MAX_INTERVAL_CHECK_COUNT) {
            return;
        }
        countIntervalCheck++;
        mIsRunningScript = true;
        mHandler.postDelayed(() -> matchAndRunJs(EJavaScriptType.AUTO, false), DELAY_TIME_TO_RUN_SCRIPT);
    }

    public void onLoadResource(WebView view, final String url) {
        if (!loadingStaticResource(url) && isMatchUrl(url)) {
            if (mIsRunningScript) {
                return;
            }
            intervalCheck();
        }
    }

    @Override
    public void onPageFinished(String url) {
        if (isMatchUrl(url)) {
            matchAndRunJs(EJavaScriptType.AUTO, false);
        }
    }

    @JavascriptInterface
    public void onJsPaymentResult(String pResult) {
        mIsRunningScript = false;
        Timber.d("onJsPaymentResult %s", pResult);
        final String result = pResult;
        try {
            if (getWorkFlow() == null) {
                return;
            }
            Activity activity = getWorkFlow().getActivity();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.runOnUiThread(() -> {
                AtmScriptOutput scriptOutput = GsonUtils.fromJsonString(result, AtmScriptOutput.class);
                countIntervalCheck = 0;
                EEventType eventType = convertPageIdToEvent(mEventID);
                BaseResponse response = genResponse(eventType, scriptOutput);
                if (mEventID == 0 && mIsFirst && scriptOutput != null && !scriptOutput.isError()) {
                    // Auto hit at first step
                    mIsFirst = false;
                    hit();
                } else {
                    EventBus eventBus = SDKApplication.getApplicationComponent().eventBus();
                    if (eventType == EEventType.ON_REQUIRE_RENDER) {
                        eventBus.postSticky(new SdkParseWebsiteRenderEvent(scriptOutput, mPageCode));
                    } else if (eventType == EEventType.ON_FAIL) {
                        eventBus.postSticky(new SdkParseWebsiteErrorEvent());
                    } else if (eventType == EEventType.ON_PAYMENT_COMPLETED) {
                        eventBus.postSticky(new SdkParseWebsiteCompleteEvent(response));
                    }
                }
            });
        } catch (Exception e) {
            Timber.d(e);
        }
    }
}
