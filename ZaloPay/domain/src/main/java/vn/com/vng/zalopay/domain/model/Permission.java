package vn.com.vng.zalopay.domain.model;

/**
 * Created by AnhHieu on 7/28/16.
 */
@org.parceler.Parcel
public class Permission {
    public int transtype;

    public long pmcid;

    public int profilelevel;

    public boolean allow;

    public boolean requireotp;

    public boolean requirepin;
}
