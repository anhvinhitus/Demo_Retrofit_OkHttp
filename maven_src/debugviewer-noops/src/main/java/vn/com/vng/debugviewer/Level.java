package vn.com.vng.debugviewer;

/**
 * Created by huuhoa on 12/27/15.
 */
public enum Level {
    V(0), D(1), I(2), W(3), E(4), F(5);

    private static Level[] byOrder = new Level[6];

    static {
        byOrder[0] = V;
        byOrder[1] = D;
        byOrder[2] = I;
        byOrder[3] = W;
        byOrder[4] = E;
        byOrder[5] = F;
    }

    private int mValue;

    private Level(int value) {
        mValue = value;
    }
}
