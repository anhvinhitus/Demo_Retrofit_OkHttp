package vn.com.zalopay.wallet.business.entity.atm;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;

public class DAtmScriptOutput {
    public int eventID = 0;

    public String otpimg;
    public String otpimgsrc;

    public boolean shouldStop = false;
    public String message = null;
    public String info = null;

    public String accountList = null;

    public DStaticViewGroup staticView = null;
    public DDynamicViewGroup dynamicView = null;

    public boolean stopIntervalCheck = false;

    public boolean isError() {
        return (shouldStop == true && !TextUtils.isEmpty(message));
    }
}
