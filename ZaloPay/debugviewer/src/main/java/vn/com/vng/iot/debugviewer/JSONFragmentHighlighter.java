package vn.com.vng.iot.debugviewer;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import java.util.ArrayList;

import vn.com.vng.iot.debugviewer.json.JSONTokenizer;

/**
 * Created by huuhoa on 12/26/15.
 */
class JSONFragmentHighlighter implements IFragmentHighlighter {
    public SpannableString apply(String input) {
        SpannableString builder = new SpannableString(input);
        builder.setSpan(new BackgroundColorSpan(Color.parseColor("#F7F7D5")), 0, input.length(), 0);

        JSONTokenizer tokenizer = new JSONTokenizer(input);
        ArrayList<JSONTokenizer.Range> allTokens = tokenizer.getTokenList();
        for (JSONTokenizer.Range range : allTokens) {
            Object format;
            switch (range.type) {
                case JSONTokenizer.Range.TYPE_ATTRIBUTE:
                    format = new ForegroundColorSpan(Color.RED);
                    break;
                case JSONTokenizer.Range.TYPE_VALUE_STRING:
                    format = new ForegroundColorSpan(Color.parseColor("#a31515"));
                    break;
                case JSONTokenizer.Range.TYPE_VALUE_NUMBER:
                    format = new ForegroundColorSpan(Color.BLACK);
                    break;
                default:
                    format = new ForegroundColorSpan(Color.DKGRAY);
            }
            builder.setSpan(format, range.start, range.end, 0);
        }

        return builder;
    }
}
