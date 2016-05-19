package vn.com.vng.iot.debugviewer.json;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by huuhoa on 12/26/15.
 */
public class JSONTokenizer {
    public class Range {
        public int start;
        public int end;
        public int type;

        public static final int TYPE_ATTRIBUTE = 0;
        public static final int TYPE_VALUE_STRING = 1;
        public static final int TYPE_VALUE_NUMBER = 2;

        public Range(int type_, int start_, int end_) {
            type = type_;
            start = start_;
            end = end_;
        }
    }

    private ArrayList<Range> mTokenList = new ArrayList<>();

    public JSONTokenizer(String json) {
        byte[] jsona = json.getBytes();
        Lexer.lexer.lex(jsona, cb);
    }

    public ArrayList<Range> getTokenList() {
        return mTokenList;
    }

    private Lexer.CB cb = new Lexer.CB() {
        private boolean isStringValue = false;
        void tok(Lexer.Token tok) {
            isStringValue = tok == Lexer.Token.COLON;
        }

        void tok(String c) {
            int rangeType = isStringValue ? Range.TYPE_VALUE_STRING : Range.TYPE_ATTRIBUTE;
            Range range = new Range(rangeType, pos - c.length(), pos);
            mTokenList.add(range);
        }

        void tok(BigDecimal c) {
            int rangeType = Range.TYPE_VALUE_NUMBER;
            Range range = new Range(rangeType, pos - cache.length(), pos);
            mTokenList.add(range);
        }
    };
}
