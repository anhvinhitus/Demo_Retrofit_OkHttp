package vn.com.vng.zalopay.webapp;

/**
 * Created by khattn on 2/21/17.
 */

public class WebAppBottomSheetItem {
    public int id;
    public Integer iconResource;
    public Integer iconColor;
    public int resStrId;
    public int resImgId;

    public WebAppBottomSheetItem(int id) {
        this.id = id;
    }

    public WebAppBottomSheetItem(int id, int title) {
        this.id = id;
        this.resStrId = title;
        this.iconResource = null;
        this.iconColor = null;
    }

    public WebAppBottomSheetItem(int id, int title, int resImgId) {
        this.id = id;
        this.resStrId = title;
        this.resImgId = resImgId;
        this.iconResource = null;
        this.iconColor = null;
    }

    public WebAppBottomSheetItem(int id,
                    int title,
                    Integer iconResource,
                    Integer iconColor) {
        this.id = id;
        this.resStrId = title;
        this.iconResource = iconResource;
        this.iconColor = iconColor;
    }
}
