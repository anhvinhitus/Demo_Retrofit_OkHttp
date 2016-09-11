package vn.com.vng.zalopay.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;

/**
 * Created by AnhHieu on 9/11/16.
 * *
 */
public abstract class BaseDialog extends AlertDialog {

    public abstract int getResLayoutId();

    public BaseDialog(Context context) {
        super(context, R.style.alert_dialog);
        this.setCancelable(true);
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResLayoutId());
        ButterKnife.bind(this, this);
        setWidthDialog();
    }

    public void setWidthDialog() {
      /*  Display display = this.getWindow().getWindowManager().getDefaultDisplay();
        int densityDpi = display.getWidth();
        android.view.ViewGroup.LayoutParams params = this.mRootView.getLayoutParams();
        params.width = (int) ((double) densityDpi * 0.85D);
        params.height = -2;
        this.mRootView.setLayoutParams(params);*/
    }

}
