package vn.com.vng.zalopay.transfer.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 8/25/16.
 */
public class MyQRCodeFragment extends BaseFragment {

    public static MyQRCodeFragment newInstance() {

        Bundle args = new Bundle();

        MyQRCodeFragment fragment = new MyQRCodeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_my_qr_code;
    }

    @BindView(R.id.ivQrCode)
    ImageView mMyQrCodeView;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new GenerateQrCodeTask(mMyQrCodeView, "{'type':1,'uid':zaloid,checksum:sha256}").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private static class GenerateQrCodeTask extends AsyncTask<Void, Void, Bitmap> {

        WeakReference<ImageView> mImageView;
        String content;

        public GenerateQrCodeTask(ImageView imageView, String content) {
            mImageView = new WeakReference<>(imageView);
            this.content = content;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                return generateQrCode(content);
            } catch (Exception e) {
                Timber.e(e, "exception");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }

            ImageView image = mImageView.get();
            if (image != null) {
                image.setImageBitmap(bitmap);
            }
        }

        private Bitmap generateQrCode(String myCodeText) throws WriterException {
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            int size = 256;
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


}
