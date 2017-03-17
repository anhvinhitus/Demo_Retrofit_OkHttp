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
        suggest_actions = null;
        suggest_actions = null;
    }

    public StatusResponse(DAtmScriptOutput pScriptOutput) {
        this.data = null;
        this.returncode = pScriptOutput.eventID;
        this.returnmessage = pScriptOutput.message;
        this.suggest_message = pScriptOutput.message;
        this.isprocessing = !pScriptOutput.shouldStop;
    }

    public StatusResponse(BaseResponse pBaseResponse) {
        this.data = null;
        this.returncode = pBaseResponse.returncode;
        this.returnmessage = pBaseResponse.returnmessage;
        this.suggest_message = pBaseResponse.returnmessage;
        this.isprocessing = false;
    }

    public StatusResponse(int pCode, String pMessage) {
        this.data = null;
        this.returncode = pCode;
        this.returnmessage = pMessage;
        this.suggest_message = pMessage;
        this.isprocessing = false;
    }
}
