package vn.com.vng.zalopay.webapp;

/**
 * Created by khattn on 2/21/17.
 */

public class WebAppBottomSheetItem {
    public int id;
    public Integer iconResource;
    public Integer iconColor;
    public String title;
    public int resImgId;

    public WebAppBottomSheetItem(int id) {
        this.id = id;
    }

    public WebAppBottomSheetItem(int id, String title) {
        this.id = id;
        this.title = title;
        this.iconResource = null;
        this.iconColor = null;
    }

    public WebAppBottomSheetItem(int id, String title, int resImgId) {
        this.id = id;
        this.title = title;
        this.resImgId = resImgId;
        this.iconResource = null;
        this.iconColor = null;
    }

    public WebAppBottomSheetItem(int id,
                    String title,
                    Integer iconResource,
                    Integer iconColor) {
        this.id = id;
        this.title = title;
        this.iconResource = iconResource;
        this.iconColor = iconColor;
    }
}
