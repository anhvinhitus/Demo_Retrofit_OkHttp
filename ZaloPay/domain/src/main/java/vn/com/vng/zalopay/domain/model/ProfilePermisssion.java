package vn.com.vng.zalopay.domain.model;

import java.util.List;

/**
 * Created by longlv on 03/06/2016.
 */
public class ProfilePermisssion {
    public int profileLevel;
    public List<Permission> profilePermisssions;

    public class Permission {
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
