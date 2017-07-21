package vn.com.zalopay.wallet.event;

import vn.com.zalopay.wallet.business.entity.atm.DAtmScriptOutput;

/**
 * Created by chucvv on 7/20/17.
 */

public class SdkParseWebsiteRenderEvent {
    public DAtmScriptOutput response;
    public String pageName;

    public SdkParseWebsiteRenderEvent(DAtmScriptOutput response, String pageName) {
        this.response = response;
        this.pageName = pageName;
    }
}
