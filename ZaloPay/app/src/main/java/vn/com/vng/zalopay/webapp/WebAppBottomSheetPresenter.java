package vn.com.vng.zalopay.webapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.R;

/**
 * Created by khattn on 2/27/17.
 */

final class WebAppBottomSheetPresenter implements IWebAppBottomSheet {
    private Context mContext;

    WebAppBottomSheetPresenter(Context context) {
        mContext = context;
    }

    @Override
    public void handleClickShareOnZalo(String currentUrl) {
        List<Intent> targetShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        List<ResolveInfo> resolveInfos = mContext.getPackageManager().queryIntentActivities(shareIntent, 0);

        if (!resolveInfos.isEmpty()) {
            for (ResolveInfo resolveInfo : resolveInfos) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (packageName.contains("zalo")) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, currentUrl);
                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                }
            }

            if (!targetShareIntents.isEmpty()) {
                Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Choose app to share");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                mContext.startActivity(chooserIntent);
            }
        }
    }

    @Override
    public void handleClickCopyURL(String currentUrl) {
        setClipboard(mContext, currentUrl);
        Toast.makeText(mContext, mContext.getResources().getText(R.string.copy_clipboard),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleClickOpenInBrowser(String currentUrl) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
        mContext.startActivity(browserIntent);
    }

    private void setClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }
}
