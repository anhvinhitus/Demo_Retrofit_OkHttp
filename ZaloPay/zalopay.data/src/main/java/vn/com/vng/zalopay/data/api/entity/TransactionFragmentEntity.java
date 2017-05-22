package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by khattn on 4/10/17.
 * *
 */

public class TransactionFragmentEntity {

    @SerializedName("maxreqdate")
    public long maxreqdate;

    @SerializedName("minreqdate")
    public long minreqdate;

    @Expose(serialize = false, deserialize = false)
    public long statustype = 1;

    @SerializedName("outofdata")
    public boolean outofdata;

    public TransactionFragmentEntity() {

    }

    public TransactionFragmentEntity(long statustype, long maxreqdate, long minreqdate, boolean outofdata) {
        this.statustype = statustype;
        this.maxreqdate = maxreqdate;
        this.minreqdate = minreqdate;
        this.outofdata = outofdata;
    }
}
