package vn.com.zalopay.analytics;

/**
 * Created by khattn on 4/28/17.
 * Apptransid Log
 */

public class ZPApptransidLog {
    public String apptransid;
    public long appid;
    public int step;
    public int step_result;
    public int pcmid;
    public int transtype;
    public long transid;
    public int sdk_result;
    public int server_result;
    public String source;
    public long start_time;
    public long finish_time;
    public String bank_code;
    public int status;

    public ZPApptransidLog() {

    }

    public ZPApptransidLog(String apptransid, int step, int step_result) {
        this.apptransid = apptransid;
        this.step = step;
        this.step_result = step_result;
    }

    public ZPApptransidLog(String apptransid, long appid, int step, int step_result, int pcmid, int transtype, long transid,
                           int sdk_result, int server_result, String source, long start_time, long finish_time, String bank_code,
                           int status) {
        this.apptransid = apptransid;
        this.appid = appid;
        this.step = step;
        this.step_result = step_result;
        this.pcmid = pcmid;
        this.transtype = transtype;
        this.transid = transid;
        this.sdk_result = sdk_result;
        this.server_result = server_result;
        this.source = source;
        this.start_time = start_time;
        this.finish_time = finish_time;
        this.bank_code = bank_code;
        this.status = status;
    }
}
