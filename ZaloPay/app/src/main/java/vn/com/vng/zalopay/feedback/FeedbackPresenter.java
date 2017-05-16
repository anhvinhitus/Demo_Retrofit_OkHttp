package vn.com.vng.zalopay.feedback;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.zalopay.apploader.internal.FileUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.UserCollector;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.JsonUtil;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.PhotoUtil;
import vn.zalopay.feedback.FeedbackCollector;
import vn.zalopay.feedback.collectors.TransactionCollector;

import static vn.com.vng.zalopay.data.util.ObservableHelper.makeObservable;

/**
 * Created by hieuvm on 1/6/17.
 */

final class FeedbackPresenter extends AbstractPresenter<IFeedbackView> {

    private Context mContext;
    private UserConfig mUserConfig;

    @Inject
    Navigator mNavigator;

    @Inject
    User mUser;

    @Inject
    FeedbackPresenter(Context context, UserConfig userConfig) {
        this.mContext = context;
        this.mUserConfig = userConfig;
    }

    void onViewCreated() {
        mView.setEmail(mUser.email);
        FeedbackCollector feedbackCollector = FeedbackCollector.instance();

        TransactionCollector transactionCollector = feedbackCollector.getTransactionCollector();
        mView.setTransaction(transactionCollector.category, transactionCollector.transid, transactionCollector.error_message);

        insertScreenshot(feedbackCollector.getScreenshot());
    }

    @Override
    public void detachView() {
        FeedbackCollector.instance().cleanUp();
        super.detachView();
    }

    void sendEmail(String email, String emailText, boolean user, boolean app, boolean device, final List<Uri> screenshot) {

        FeedbackCollector feedbackCollector = FeedbackCollector.instance();

        collectInformation(feedbackCollector, user, app, device);

        feedbackCollector.putDynamicInformation("email", email);

        feedbackCollector.startCollectors(data -> onCollectorFinish(data, screenshot, emailText));
    }

    private void onCollectorFinish(JSONObject data, final List<Uri> screenshot, String emailText) {
        Timber.d("on Collector Finish : data [%s]", data);
        Subscription subscription = saveCollectFile(data)
                .map(filePath -> shareFile(screenshot, filePath))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<ArrayList<Uri>>() {
                    @Override
                    public void onNext(ArrayList<Uri> uris) {
                        if (mView == null) {
                            return;
                        }

                        boolean result = mNavigator.startEmail((Activity) mView.getContext(), mContext.getString(R.string.email_support),
                                null, mContext.getString(R.string.subject_compose_email_support),
                                emailText, uris);
                        if (result) {
                            mView.finish();
                        } else {
                            mView.showError(mContext.getString(R.string.exception_send_feedback));
                        }

                    }
                });
        mSubscription.add(subscription);
    }

    private ArrayList<Uri> shareFile(List<Uri> screenshot, String fileData) {
        ArrayList<Uri> uris = new ArrayList<>(screenshot);
        if (!TextUtils.isEmpty(fileData)) {
            Uri uri = FileProvider.getUriForFile(mContext,
                    mContext.getString(R.string.file_provider),
                    new File(fileData));
            Timber.d("uri file data %s", uri.toString());
            uris.add(uri);
        }
        return uris;
    }

    private Observable<String> saveCollectFile(final JSONObject data) {
        return makeObservable(() -> {
            String filePath = FileUtils.writeStringToFile(mContext, JsonUtil.toPrettyFormat(data.toString()), "data.txt");
            Timber.d("write to file : filePath [%s] ", filePath);
            return filePath;
        });
    }

    private void insertScreenshot(final byte[] screenshot) {
        Subscription subscription = makeObservable(() -> {
            File file = PhotoUtil.createPhotoFile(mContext, "screenshot-1.png");
            FileUtils.writeByteArrayToFile(screenshot, file.getAbsolutePath());
            return FileProvider.getUriForFile(mContext, mContext.getString(R.string.file_provider), file);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Uri>() {
                    @Override
                    public void onNext(Uri uri) {
                        if (mView != null) {
                            mView.insertScreenshot(uri);
                        }
                    }
                });
        mSubscription.add(subscription);
    }

    private void collectInformation(FeedbackCollector mCollector, boolean user, boolean app, boolean device) {

        if (user) {
            mCollector.installCollector(new UserCollector(mUserConfig));
        }

        mCollector.collectDeviceInformation(mContext, app, device, true);
    }


}
