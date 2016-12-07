package vn.com.vng.zalopay.data.cache.model;

import java.util.List;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;

/**
 * Created by longlv on 21/07/2016.
 *
 */

public class GetReceivePacket {
    public long totalofrevamount;
    public long totalofrevpackage;
    public long numofluckiestdraw;
    public List<ReceivePackage> revpackageList;
}
