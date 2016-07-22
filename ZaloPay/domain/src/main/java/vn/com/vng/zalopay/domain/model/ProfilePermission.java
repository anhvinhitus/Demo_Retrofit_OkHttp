package vn.com.vng.zalopay.domain.model;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by longlv on 03/06/2016.
 *
 */
@Parcel
public class ProfilePermission {
    public int profileLevel;
    public List<Permission> profilePermissions;

    @Parcel
    public static class Permission {
        public int transtype;
        public long pmcid;
        public int profilelevel;
        public boolean allow;
        public boolean requireotp;
        public boolean requirepin;

//        @Override
//        public String toString() {
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("transtype", transtype);
//            return stringBuilder.toString();
//        }
    }
}
