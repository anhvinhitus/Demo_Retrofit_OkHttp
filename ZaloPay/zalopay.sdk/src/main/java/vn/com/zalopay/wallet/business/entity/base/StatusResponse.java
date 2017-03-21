package vn.com.zalopay.wallet.business.entity.base;

import vn.com.zalopay.wallet.business.entity.atm.DAtmScriptOutput;

public class StatusResponse extends BaseResponse {
    public boolean isprocessing = false;
    public String data = null;
    public String zptransid;
    public StatusResponse() {
        data = null;
        zptransid = null;
        isprocessing = false;
        returncode = -1;
        returnmessage = null;
        suggestactions = null;
    }

    public StatusResponse(DAtmScriptOutput pScriptOutput) {
        this.data = null;
        this.returncode = pScriptOutput.eventID;
        this.returnmessage = pScriptOutput.message;
        this.suggestmessage = pScriptOutput.message;
        this.isprocessing = !pScriptOutput.shouldStop;
    }

    public StatusResponse(int pCode, String pMessage) {
        this.data = null;
        this.returncode = pCode;
        this.returnmessage = pMessage;
        this.suggestmessage = pMessage;
        this.isprocessing = false;
    }
}
