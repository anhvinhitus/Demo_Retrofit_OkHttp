package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by AnhHieu on 5/20/16.
 */
public class PaymentTransTypeEntity {

    @SerializedName("transtype")
    public long transtype;

    @SerializedName("pmclist")
    public List<PCMEntity> pmclist;
}
