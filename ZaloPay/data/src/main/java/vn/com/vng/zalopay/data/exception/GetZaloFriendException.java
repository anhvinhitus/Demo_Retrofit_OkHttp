package vn.com.vng.zalopay.data.exception;

/**
 * Created by huuhoa on 7/4/16.
 * Exception declaration for API getZaloFriend
 */
public class GetZaloFriendException extends Exception {
    private final int mPageIndex;

    public GetZaloFriendException(int pageIndex, Throwable cause) {
        super(cause);
        mPageIndex = pageIndex;
    }

    public int getPageIndex() {
        return mPageIndex;
    }
}
