package vn.com.vng.zalopay.react.widget.input;

import android.text.Editable;
import android.text.TextWatcher;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.zalopay.ui.widget.edittext.ZPEditText;

import java.util.Map;

import javax.annotation.Nullable;

import timber.log.Timber;

/**
 * Created by hieuvm on 11/16/16.
 */

public class ReactInputTextViewManager extends SimpleViewManager<ZPEditText> {
    private static final String REACT_CLASS = "RCTInputView";
    private static final String PROP_HINT = "hint";
    private static final int COMMAND_SET_ERROR = 1;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected ZPEditText createViewInstance(ThemedReactContext reactContext) {
        ZPEditText view = new ZPEditText(reactContext);
        view.addTextChangedListener(new InputTextWatcher());
        return view;
    }

    @ReactProp(name = PROP_HINT)
    public void setHint(ZPEditText iconTextView, String hint) {
        iconTextView.setHint(hint);
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("setError", COMMAND_SET_ERROR);
    }

    @Override
    public
    @Nullable
    Map getExportedCustomDirectEventTypeConstants() {
        return super.getExportedCustomDirectEventTypeConstants();
    }

    @Override
    public void receiveCommand(ZPEditText view, int commandType, @Nullable ReadableArray args) {
        Timber.d("receiveCommand: %s", commandType);
        if (commandType == COMMAND_SET_ERROR) {
            if (args != null) {
                String error = args.getString(0);
                view.setError(error);
            }
        }
    }

    private static class InputTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
          /*  WritableMap event = Arguments.createMap();
            event.putString("text", editable.toString());
            ReactContext reactContext = (ReactContext) v.getContext();
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(v.getId(), "afterTextChanged", event);*/
        }
    }


}
