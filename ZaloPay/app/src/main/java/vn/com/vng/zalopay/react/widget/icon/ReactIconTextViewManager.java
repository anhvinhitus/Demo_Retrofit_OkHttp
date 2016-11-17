package vn.com.vng.zalopay.react.widget.icon;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

/**
 * Created by hieuvm on 11/16/16.
 */

public class ReactIconTextViewManager extends SimpleViewManager<IconTextView> {

    private static final String REACT_CLASS = "RCTIconTextView";
    private static final String PROP_COLORS = "iconColor";
    private static final String PROP_FONT_SIZE = "iconSize";
    private static final String PROP_FONT_FAMILY = "fontFamily";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected IconTextView createViewInstance(ThemedReactContext reactContext) {
        return new IconTextView(reactContext);
    }

    @ReactProp(name = PROP_COLORS)
    public void setColors(IconTextView iconTextView, ReadableArray colors) {

    }

    @ReactProp(name = PROP_FONT_SIZE)
    public void setFontSize(IconTextView iconTextView, ReadableArray size) {

    }

    @ReactProp(name = PROP_FONT_FAMILY)
    public void setFontFamily(IconTextView iconTextView, ReadableArray size) {

    }


}
