package vn.com.vng.zalopay.transfer.ui.friendlist;

/**
 * Created by hieuvm on 11/23/16.
 */

final class SectionObject {

    String firstChar;
    boolean usingApp;

    public SectionObject(String firstChar, boolean usingApp) {
        this.firstChar = firstChar;
        this.usingApp = usingApp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SectionObject that = (SectionObject) o;

        if (usingApp != that.usingApp) return false;
        return firstChar.equals(that.firstChar);

    }

    @Override
    public int hashCode() {
        int result = firstChar.hashCode();
        result = 31 * result + (usingApp ? 1 : 0);
        return result;
    }
}
