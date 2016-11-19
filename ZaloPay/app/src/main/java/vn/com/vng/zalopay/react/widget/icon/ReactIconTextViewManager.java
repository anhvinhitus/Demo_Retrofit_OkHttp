package vn.com.vng.zalopay.react.widget.icon;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import timber.log.Timber;

/**
 * Created by hieuvm on 11/16/16.
 */

public class ReactIconTextViewManager extends SimpleViewManager<IconTextView> {

    private static final String REACT_CLASS = "RCTIconTextView";
    private static final String PROP_COLOR = "iconColor";
    private static final String PROP_FONT_SIZE = "iconSize";
    private static final String PROP_FONT_FAMILY = "fontFamily";
    private static final String PROP_NAME = "name";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected IconTextView createViewInstance(ThemedReactContext reactContext) {
        return new IconTextView(reactContext);
    }

    @ReactProp(name = PROP_COLOR)
    public void setColor(IconTextView iconTextView, int colors) {
        Timber.d("set color: %s", colors);
    }

    @ReactProp(name = PROP_NAME)
    public void setName(IconTextView iconTextView, String name) {
        Timber.d("set name: %s", name);
    }

    @ReactProp(name = PROP_FONT_SIZE)
    public void setFontSize(IconTextView iconTextView, int size) {
        Timber.d("set font size %s", size);
    }

    @ReactProp(name = PROP_FONT_FAMILY)
    public void setFontFamily(IconTextView iconTextView, String fontFamily) {
        Timber.d("set font family %s", fontFamily);
    }


}
