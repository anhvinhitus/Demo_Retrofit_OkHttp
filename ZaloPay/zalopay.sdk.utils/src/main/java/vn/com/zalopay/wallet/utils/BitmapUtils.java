package vn.com.zalopay.wallet.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import rx.Single;
import rx.SingleSubscriber;

public class BitmapUtils {
    public static Bitmap b64ToImage(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    public static Single<Integer> getImageWidth(final Context pContext, final String pFilePath) {
        return Single.create(new Single.OnSubscribe<Integer>() {
            @Override
            public void call(SingleSubscriber<? super Integer> singleSubscriber) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(pFilePath, options);
                    int widthInDp = (int) SdkUtils.px2dp(options.outWidth, pContext);
                    singleSubscriber.onSuccess(widthInDp);
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        });
    }
}
