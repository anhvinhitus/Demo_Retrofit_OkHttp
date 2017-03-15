package vn.com.vng.zalopay.event;

import com.zalopay.ui.widget.iconfont.IconFontType;

/**
 * Created by longlv on 3/15/17.
 * Notify after reload icon font successfully.
 */

public class LoadIconFontEvent {
    public IconFontType mIconFontType;

    public LoadIconFontEvent(IconFontType mIconFontType) {
        this.mIconFontType = mIconFontType;
    }
}
