package vn.com.zalopay.wallet.feedback;

/**
 * Created by lytm on 27/04/2017.
 */

public class FeedBackCollector extends BaseFeedBack{
    protected static FeedBackCollector _object;

    public FeedBackCollector() {
        super();
    }

    public static FeedBackCollector shared() {
        if (FeedBackCollector._object == null) {
            FeedBackCollector._object = new FeedBackCollector();
        }
        return FeedBackCollector._object;
    }

}
