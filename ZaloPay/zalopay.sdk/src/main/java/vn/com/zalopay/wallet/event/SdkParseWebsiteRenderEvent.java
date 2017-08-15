package vn.com.zalopay.wallet.event;

import vn.com.zalopay.wallet.business.entity.atm.AtmScriptOutput;

/*
 * Created by chucvv on 7/20/17.
 */

public class SdkParseWebsiteRenderEvent {
    public AtmScriptOutput response;
    public String pageName;

    public SdkParseWebsiteRenderEvent(AtmScriptOutput response, String pageName) {
        this.response = response;
        this.pageName = pageName;
    }
}
