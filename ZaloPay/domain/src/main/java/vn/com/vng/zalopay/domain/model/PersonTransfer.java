package vn.com.vng.zalopay.domain.model;

import org.parceler.Parcel;

/**
 * Created by AnhHieu on 8/31/16.
 * *
 */
@Parcel
public class PersonTransfer extends Person {
    
    public long amount;
    public int state;
    public String transId;

    public long timeTransfer;
}
