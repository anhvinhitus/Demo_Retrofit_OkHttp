package vn.zalopay.feedback.collectors;

import org.json.JSONException;
import org.json.JSONObject;

import vn.zalopay.feedback.CollectorSetting;
import vn.zalopay.feedback.IFeedbackCollector;

/**
 * Created by hieuvm on 5/12/17.
 * *
 */

public class TransactionCollector implements IFeedbackCollector {


    private static TransactionCollector _instance;

    public static TransactionCollector instance() {
        if (_instance == null) {
            _instance = new TransactionCollector();
        }
        return _instance;
    }


    private static CollectorSetting sSetting;

    static {
        sSetting = new CollectorSetting();
        sSetting.userVisibility = true;
        sSetting.displayName = "Transaction Information";
        sSetting.dataKeyName = "transinfo";
    }

    public String transid;

    public int error_code;

    public String error_message;

    public String category;

    public TransactionCollector() {
    }

    @Override
    public CollectorSetting getSetting() {
        return sSetting;
    }

    @Override
    public JSONObject doInBackground() throws JSONException {
        JSONObject retVal = new JSONObject();

        retVal.put("transid", transid);
        retVal.put("error_code", error_code);
        retVal.put("error_message", error_message);
        retVal.put("category", category);

        return retVal;
    }

    @Override
    public void cleanUp() {
        transid = "";
        error_code = -1;
        error_message = "";
        category = "";
    }
}
