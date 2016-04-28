package vn.com.vng.zalopay.data.api.response;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.AppEntity;
import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.api.entity.PCMEntity;

/**
 * Created by AnhHieu on 4/28/16.
 */
public class PlatformInfoResponse extends BaseResponse {


    public long expiredtime;

    public boolean isupdateresource;

    public String platforminfochecksum;

    public boolean isupdateplatforminfo;

    public PlatformInfoEntity platforminfo;

    public ResourceEntity resource;

    public List<CardEntity> cardlist;


    public static class PlatformInfoEntity {
        public List<AppEntity> applist;
        public List<PCMEntity> pmclist;
    }

    public static class ResourceEntity {
        public String rsversion;
        public String rsurl;
    }
}
