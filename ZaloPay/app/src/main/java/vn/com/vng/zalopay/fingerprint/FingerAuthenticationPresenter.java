package vn.com.vng.zalopay.fingerprint;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

import static vn.com.vng.zalopay.data.NetworkError.INCORRECT_PIN;
import static vn.com.vng.zalopay.fingerprint.Stage.FINGERPRINT_DECRYPT;
import static vn.com.vng.zalopay.fingerprint.Stage.FINGERPRINT_ENCRYPT;
import static vn.com.vng.zalopay.fingerprint.Stage.PASSWORD;
import static vn.com.vng.zalopay.fingerprint.Stage.PASSWORD_SETTING;

/**
 * Created by hieuvm on 12/27/16.
 */

public class FingerAuthenticationPresenter extends AbstractPresenter<IFingerprintAuthenticationView> implements FingerprintUiHelper.Callback {

    private AccountStore.Repository mAccountRepository;
    private Context mApplicationContext;

    @Inject
    SharedPreferences mPreferences;

    FingerprintUiHelper mFingerprintUiHelper;

    @Inject
    KeyStore mKeyStore;

    @Inject
    KeyGenerator mKeyGenerator;

    private Cipher mEncryptCipher;

    private Cipher mDecryptCipher;

    private Stage mStage = Stage.FINGERPRINT_DECRYPT;

    private String mPassword;

    @Inject
    FingerAuthenticationPresenter(AccountStore.Repository accountRepository,
                                  Context applicationContext) {
        this.mAccountRepository = accountRepository;
        this.mApplicationContext = applicationContext;
    }


    void onViewCreated() {
        updateStage();

        mFingerprintUiHelper = mView.getFingerprintUiHelper(mStage);

        if (!mFingerprintUiHelper.isFingerprintAuthAvailable()) {
            enterPassword();
        } else if (mStage == PASSWORD_SETTING) {
            enterPassword(mStage);
        }
    }

    void verify(String password) {
        switch (mStage) {
            case FINGERPRINT_DECRYPT:
                enterPassword();
                break;
            case PASSWORD:
                verifyPassword(password);
                break;
            case PASSWORD_SETTING:
                verifyPassword(password);
                break;
        }
    }

    private void enterPassword(Stage stage) {
        setStage(stage);
        updateStage();
        if (mView != null) {
            mView.showKeyboard();
        }

        mFingerprintUiHelper.stopListening();
    }

    private void enterPassword() {
        enterPassword(Stage.PASSWORD);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void verifyPassword(String password) {
        validatePin(password);
    }

    private void updateStage() {
        switch (mStage) {
            case FINGERPRINT_DECRYPT:
                mFingerprintUiHelper = mView.getFingerprintUiHelper(mStage);
                break;
            case PASSWORD:
                break;
            case PASSWORD_SETTING:
                break;
            case FINGERPRINT_ENCRYPT:
                mFingerprintUiHelper = mView.getFingerprintUiHelper(mStage);
                break;
        }

        mView.updateStage(mStage);
    }


    public void resume() {
        startListening();
    }

    public void setStage(Stage stage) {
        Timber.d("setStage: [%s]", stage);
        mStage = stage;
    }

    public Stage getStage() {
        return mStage;
    }

    public void pause() {
        mFingerprintUiHelper.stopListening();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void startListening() {
        if (!mFingerprintUiHelper.isFingerprintAuthAvailable()) {
            return;
        }

        mFingerprintUiHelper.stopListening();

        if (mStage == Stage.FINGERPRINT_DECRYPT) {
            mFingerprintUiHelper.startListening(new FingerprintManager.CryptoObject(mDecryptCipher));
        }
    }

    private void showLoadingView() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    private void hideLoadingView() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    void validatePin(String password) {
        Subscription subscription = mAccountRepository.validatePin(password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ValidatePinSubscriber(password));
        mSubscription.add(subscription);
    }


    private boolean encrypt(String secret) {
        Timber.d("encrypt [%s]", secret);
        try {

            byte[] encrypted = mEncryptCipher.doFinal(secret.getBytes());

            IvParameterSpec ivParams = mEncryptCipher.getParameters().getParameterSpec(IvParameterSpec.class);
            String iv = Base64.encodeToString(ivParams.getIV(), Base64.DEFAULT);

            SharedPreferences.Editor editor = mPreferences.edit();
            String secretBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT);
            Timber.d("secret base64 : [%s]", secretBase64);
            editor.putString(Constants.PREF_KEY_PASSWORD, secretBase64);
            editor.putString(Constants.PREF_KEY_PASSWORD_IV, iv);
            editor.apply();
            return true;

        } catch (Exception e) {
            Timber.e(e, "Failed to encrypt the data with the generated key.");
        }

        return false;
    }


    private String decrypt(Cipher cipher) {
        try {
            byte[] encodedData = Base64.decode(mPreferences.getString(Constants.PREF_KEY_PASSWORD, ""), Base64.DEFAULT);
            byte[] decodedData = cipher.doFinal(encodedData);
            return new String(decodedData);
        } catch (Exception e) {
            Timber.e(e, "Failed to decrypt the data with the generated key.");
        }
        return null;
    }

    @Override
    public void onAuthenticated(Cipher cipher) {
        Timber.d("onAuthenticated");
        if (mStage == FINGERPRINT_DECRYPT) {
            String password = decrypt(mDecryptCipher);
            Timber.d("onAuthenticated: [%s]", password);
        }

        if (mStage == FINGERPRINT_ENCRYPT) {
            Timber.d("onAuthenticated: [%s]", mPassword);
            if (!TextUtils.isEmpty(mPassword)) {
                encrypt(mPassword);
                mPassword = null;
            }
        }

        if (mView == null) {
            return;
        }

        mView.clearPassword();
        mView.onAuthenticated();
        mView.dismiss();

    }

    @Override
    public void onError() {
        enterPassword();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onValidatePinSuccess(String password) {
        if (mStage == PASSWORD_SETTING) {
            hideLoadingView();
            initEncryptCipher();
            mStage = FINGERPRINT_ENCRYPT;
            updateStage();
            mFingerprintUiHelper.stopListening();
            mPassword = password;
            mFingerprintUiHelper.startListening(new FingerprintManager.CryptoObject(mEncryptCipher));

          /*  //test
            hideLoadingView();
            boolean success = false;
            if (initEncryptCipher()) {
                success = encrypt(password);
            }
            Timber.d("onValidatePinSuccess: %s", success);
            if (success) {
                if (mView != null) {
                    mView.onPinSuccess(password);
                }
            }*/
        } else if (mStage == PASSWORD) {
            if (mView == null) {
                return;
            }

            hideLoadingView();
            mView.onPinSuccess(password);
        }

    }

    private final class ValidatePinSubscriber extends DefaultSubscriber<Boolean> {

        public String password;

        ValidatePinSubscriber(String password) {
            this.password = password;
        }

        @Override
        public void onStart() {
            showLoadingView();
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "valid pin");
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            if (mView == null) {
                return;
            }

            hideLoadingView();
            mView.showError(ErrorMessageFactory.create(mApplicationContext, e));
            if (e instanceof BodyException) {
                if (((BodyException) e).errorCode == INCORRECT_PIN) {
                    mView.showKeyboard();
                }
            }
        }

        @Override
        public void onCompleted() {
            Timber.d("onCompleted");
            onValidatePinSuccess(password);
        }
    }

    private SecretKey getKey() {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(Constants.KEY_ALIAS_NAME, null);
            if (key != null) return key;
            return createKey();

        } catch (Exception e) {
            Timber.e(e, "get secret key fail ");
        }
        return null;
    }


    @TargetApi(Build.VERSION_CODES.M)
    private SecretKey createKey() {
        Timber.d("create secret key");
        try {
            mKeyGenerator.init(new KeyGenParameterSpec.Builder(Constants.KEY_ALIAS_NAME,
                    KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            return mKeyGenerator.generateKey();

        } catch (Exception e) {
            Timber.e(e, "create secret key fail");
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private Cipher getCipher(int mode) {
        Cipher cipher;

        try {
            mKeyStore.load(null);
            byte[] iv;
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            IvParameterSpec ivParams;
            if (mode == Cipher.ENCRYPT_MODE) {
                cipher.init(mode, getKey());

            } else {
                SecretKey key = (SecretKey) mKeyStore.getKey(Constants.KEY_ALIAS_NAME, null);
                iv = Base64.decode(mPreferences.getString(Constants.PREF_KEY_PASSWORD_IV, ""), Base64.DEFAULT);
                ivParams = new IvParameterSpec(iv);
                cipher.init(mode, key, ivParams);
            }
            return cipher;
        } catch (Exception e) {
            Timber.e(e, "get cipher error");
        }
        return null;
    }


    private boolean initEncryptCipher() {
        Timber.d("initEncryptCipher %s", mEncryptCipher);
        if (!FingerprintUiHelper.checkAndroidMVersion()) {
            return false;
        }

      /*  if (mEncryptCipher != null) {
            return true;
        }*/

        mEncryptCipher = getCipher(Cipher.ENCRYPT_MODE);
        if (mEncryptCipher == null) {
            createKey();
            mEncryptCipher = getCipher(Cipher.ENCRYPT_MODE);
        }
        Timber.d("encrypt cipher: %s", mEncryptCipher);
        return (mEncryptCipher != null);

    }

    public boolean initDecryptCipher() {
        Timber.d("initDecryptCipher");
        if (!FingerprintUiHelper.checkAndroidMVersion()) {
            return false;
        }

       /* if (mDecryptCipher != null) {
            return true;
        }*/

        mDecryptCipher = getCipher(Cipher.DECRYPT_MODE);
        Timber.d("decrypt cipher: %s", mDecryptCipher);
        return (mDecryptCipher != null);
    }


    public void show() {
        if (mStage == PASSWORD_SETTING) {
            return;
        }

        String password = mPreferences.getString(Constants.PREF_KEY_PASSWORD, "");
        Timber.d("show password [%s]", password);
        if (!TextUtils.isEmpty(password)) {
            if (initDecryptCipher()) {
                // Show the fingerprint dialog to unlock the password
                setStage(FINGERPRINT_DECRYPT);
                return;
            }
        }

        setStage(PASSWORD);
    }


}