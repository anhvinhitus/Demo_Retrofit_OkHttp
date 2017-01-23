package vn.com.vng.zalopay.data.cache.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by khattn on 1/23/17.
 */

@Entity
public class LogGD {

    @Unique
    public String apptransid;
    public int appid;
    public int step;
    public int step_result;
    public int pcmid;
    public int transtype;
    public long transid;
    public int sdk_result;
    public int server_result;
    public String source;
}
