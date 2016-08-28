package vn.com.vng.zalopay.transfer.ui.presenter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

import timber.log.Timber;

/**
 * Created by huuhoa on 8/28/16.
 * Generate QR code image
 */
class GenerateQrCodeTask extends AsyncTask<Void, Void, Bitmap> {

    interface ImageListener {
        void onImageGenerated(Bitmap bitmap);
        void onImageGeneratedError();
    }

    private ImageListener mImageListener;
    private String mContent;

    public GenerateQrCodeTask(ImageListener imageListener, String content) {
        mImageListener = imageListener;
        this.mContent = content;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            return generateQrCode(mContent);
        } catch (Exception e) {
            Timber.e(e, "exception");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mImageListener == null) {
            return;
        }

        if (bitmap != null) {
            mImageListener.onImageGenerated(bitmap);
        } else {
            mImageListener.onImageGeneratedError();
        }
    }

    private Bitmap generateQrCode(String myCodeText) throws WriterException {
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        int size = 384;
        BitMatrix result = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
