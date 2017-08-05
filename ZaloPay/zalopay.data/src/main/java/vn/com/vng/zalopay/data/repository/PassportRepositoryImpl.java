package vn.com.vng.zalopay.data.repository;

import android.text.TextUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.ServerErrorMessage;
import vn.com.vng.zalopay.data.api.PassportService;
import vn.com.vng.zalopay.data.api.entity.mapper.UserEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.InvitationCodeException;
import vn.com.vng.zalopay.data.exception.RequirePhoneException;
import vn.com.vng.zalopay.data.exception.VerifyTimeoutException;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.domain.repository.PassportRepository;

import static vn.com.vng.zalopay.data.util.Utils.sha256Base;

/**
 * Implementation for PassportRepository
 * Created by AnhHieu on 3/26/16.
 */

@Singleton
public class PassportRepositoryImpl implements PassportRepository {

    private UserEntityDataMapper userEntityDataMapper;

    private UserConfig userConfig;
    private PassportService passportService;

    private final int payAppId;

    private ApplicationSession applicationSession;

    @Inject
    public PassportRepositoryImpl(UserEntityDataMapper userEntityDataMapper, UserConfig userConfig,
                                  PassportService passportService, ApplicationSession applicationSession,
                                  @Named("payAppId") int payAppId) {
        this.userEntityDataMapper = userEntityDataMapper;
        this.userConfig = userConfig;
        if (userConfig.hasCurrentUser()) {
            Timber.d("accessToken[%s]", userConfig.getCurrentUser().accesstoken);
        }

        if (passportService == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!!!");
        }

        this.passportService = passportService;
        this.userConfig = userConfig;
        this.payAppId = payAppId;
        this.applicationSession = applicationSession;
    }

    @Override
    public Observable<User> login(final long zuid, String zAuthCode) {
        return passportService.loginViaZalo(payAppId, zuid, zAuthCode)
                .doOnNext(response -> checkIfOldAccount(response.userid))
                .onErrorResumeNext(throwable -> {
                    if (!(throwable instanceof BodyException)) {
                        return Observable.error(throwable);
                    }

                    if (((BodyException) throwable).errorCode != ServerErrorMessage.REQUEST_PHONE_NUMBER) {
                        return Observable.error(throwable);
                    }

                    return Observable.error(new RequirePhoneException());
                })
                .doOnError(throwable -> {
                    if (throwable instanceof InvitationCodeException) {
                        LoginResponse loginResponse = (LoginResponse) ((InvitationCodeException) throwable).response;
                        userConfig.saveInvitationInfo(loginResponse.userid, loginResponse.accesstoken);
                    }
                }).map(this::saveUser);
    }

    @Override
    public Observable<Boolean> logout() {
        return passportService.logout(payAppId, userConfig.getUserId(), userConfig.getSession())
                .doOnTerminate(() -> applicationSession.cancelAllRequest())
                .map(logoutResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<User> verifyCode(String code) {
        return passportService.verifyCode(userConfig.getUserIdInvitation(), userConfig.getSessionInvitation(), code)
                .doOnNext(response -> checkIfOldAccount(response.userid))
                .map(this::saveUser);
    }

    private User transformWithZaloInfo(LoginResponse response) {
        User user = userEntityDataMapper.transform(response);
        user.displayName = userConfig.getDisPlayName();
        user.avatar = userConfig.getAvatar();
        user.zaloId = userConfig.getZaloId();
        Timber.d("displayName %s avatar %s zaloid %s", user.displayName, user.avatar, user.zaloId);
        return user;
    }

    private User saveUser(LoginResponse response) {
        User user = transformWithZaloInfo(response);
        userConfig.setCurrentUser(user);
        userConfig.saveConfig(user);
        userConfig.updateZaloPayName(user.zalopayname);
        return user;
    }

    @Override
    public Observable<Boolean> registerPhoneNumber(long zaloid, String oauthcode, String paymentPassword, String phonenumber) {
        String pwdSha = sha256Base(paymentPassword);
        return passportService.registerPhoneNumber(zaloid, oauthcode, pwdSha, phonenumber)
                .map(BaseResponse::isSuccessfulResponse)
                .onErrorResumeNext(this::errorVerify);
    }

    @Override
    public Observable<User> authenticatePhoneNumber(long zaloid, String oauthcode, String otp) {
        return passportService.authenticatePhoneNumber(zaloid, oauthcode, otp)
                .doOnNext(response -> checkIfOldAccount(response.userid))
                .map(this::saveUser)
                .onErrorResumeNext(this::errorVerify);
    }

    private <T> Observable<T> errorVerify(Throwable throwable) {
        if (!(throwable instanceof BodyException)) {
            return Observable.error(throwable);
        }

        if (((BodyException) throwable).errorCode != ServerErrorMessage.ZALO_LOGIN_FAIL) {
            return Observable.error(throwable);
        }

        return Observable.error(new VerifyTimeoutException());
    }

    private void checkIfOldAccount(String userId) {
        String lastUid = userConfig.getLastUid();

        Timber.d("checkIfOldAccount: user %s lastUid %s", userId, lastUid);

        if (!TextUtils.isEmpty(lastUid) && !lastUid.equals(userId)) {
            applicationSession.clearAllUserDB();
            userConfig.removeFingerprint();
        }

        userConfig.setLastUid(userId);
    }
}
