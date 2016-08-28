package vn.com.vng.zalopay.transfer.ui.presenter;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.util.Utils;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.transfer.ui.view.IReceiveMoneyView;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by huuhoa on 8/28/16.
 * Controller for receiving money
 */

public class ReceiveMoneyPresenter extends BaseUserPresenter implements IPresenter<IReceiveMoneyView>,GenerateQrCodeTask.ImageListener {

    private EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();
    private IReceiveMoneyView mView;

    @Override
    public void setView(IReceiveMoneyView view) {
        mView = view;
    }

    @Override
    public void destroyView() {
        mView = null;
    }

    @Override
    public void resume() {
        eventBus.register(this);
    }

    @Override
    public void pause() {
        eventBus.unregister(this);
    }

    @Override
    public void destroy() {
        destroyView();
    }


    private String generateQrContent() {
        try {
            User user = userConfig.getCurrentUser();
            if (user == null) {
                return "";
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", Constants.QRCode.RECEIVE_MONEY);
            jsonObject.put("uid", Long.parseLong(user.zaloPayId));
            jsonObject.put("checksum",
                    Utils.sha256(String.valueOf(Constants.QRCode.RECEIVE_MONEY), user.zaloPayId));
            return jsonObject.toString();
        } catch (Exception ex) {
            Timber.d(ex, "generate content");
            return "";
        }
    }

    public void onViewCreated() {
        String content = generateQrContent();
        if (!TextUtils.isEmpty(content)) {
            new GenerateQrCodeTask(this, content).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        mView.setUserInfo(userConfig.getCurrentUser().displayName, userConfig.getAvatar());
        mView.displayWaitForMoney();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverMoney(NotificationData notify) {
        if (mView == null) {
            return;
        }

        mView.displayReceivedMoney();
    }

    @Override
    public void onImageGenerated(Bitmap bitmap) {
        if (mView == null) {
            return;
        }

        mView.setQrImage(bitmap);
    }

    @Override
    public void onImageGeneratedError() {
        if (mView == null) {
            return;
        }

        mView.showError("Sinh mã QR thất bại!");
    }
}
