package vn.com.vng.zalopay.transfer.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Utils;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 8/25/16.
 * QR Code for receiving money
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

    @BindView(R.id.imageViewQrCode)
    ImageView mMyQrCodeView;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String content = generateQrContent();
        if (!TextUtils.isEmpty(content)) {
            new GenerateQrCodeTask(mMyQrCodeView, content).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    private String generateQrContent() {
        try {
            User user = userConfig.getCurrentUser();
            if (user == null) {
                return "";
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", Constants.QRCode.RECEIVE_MONEY);
            jsonObject.put("uid", Long.parseLong(user.zaloPayId));
            jsonObject.put("checksum",
                    Utils.sha256(String.valueOf(Constants.QRCode.RECEIVE_MONEY), user.zaloPayId));
            return jsonObject.toString();
        } catch (Exception ex) {
            Timber.d(ex, "generate content");
            return "";
        }
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
            ImageView image = mImageView.get();
            if (image == null) {
                return;
            }

            if (bitmap != null) {
                image.setImageBitmap(bitmap);
            } else {
                image.setImageResource(R.color.silver);
                Toast.makeText(image.getContext(), "Sinh mã QR thất bại!", Toast.LENGTH_SHORT).show();
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
