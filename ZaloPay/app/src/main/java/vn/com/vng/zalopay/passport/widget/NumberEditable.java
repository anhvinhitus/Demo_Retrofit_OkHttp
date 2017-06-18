package vn.com.vng.zalopay.passport.widget;

/**
 * Created by hieuvm on 6/16/17.
 * *
 */

interface NumberEditable {
    void append(int number);

    void delete();

    String getInputText();

    void setInputText(String text);
}
