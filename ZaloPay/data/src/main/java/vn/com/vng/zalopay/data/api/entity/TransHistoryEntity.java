package vn.com.vng.zalopay.data.api.entity;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class TransHistoryEntity {

    public long transid;
    public long appid;
    public String platform;
    public String description;
    public Integer pmcid;
    public Long reqdate;
    public Integer grossamount;
    public Integer netamount;
    public Integer type;


    /*"userid": "5252536377851275673",
            "transid": 160504000004102,
            "appid": 1,
            "appuser": "5252536377851275673",
            "platform": "android",
            "description": "description123",
            "pmcid": 36,
            "reqdate": 1462348768213,
            "grossamount": 19000,
            "netamount": 20000,
            "type": 1*/
}
