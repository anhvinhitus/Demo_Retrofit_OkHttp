package vn.com.vng.zalopay.react.base;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;

import timber.log.Timber;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.user.UserBaseActivity;

/**
 * Created by hieuvm on 2/23/17.
 */

public abstract class AbstractReactActivity extends UserBaseActivity implements PermissionAwareActivity {

    private PermissionListener mPermissionListener;

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    public void requestPermissions(
            String[] permissions,
            int requestCode,
            PermissionListener listener) {
        mPermissionListener = listener;
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        Timber.d("onRequestPermissionsResult: requestCode [%s] grantResults [%s]", requestCode, grantResults);
        if (mPermissionListener != null &&
                mPermissionListener.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            mPermissionListener = null;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
