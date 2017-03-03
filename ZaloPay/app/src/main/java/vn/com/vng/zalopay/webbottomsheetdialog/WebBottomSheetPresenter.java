package vn.com.vng.zalopay.webbottomsheetdialog;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.event.RefreshWebEvent;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

/**
 * Created by khattn on 2/27/17.
 *
 */

final class WebBottomSheetPresenter extends AbstractPresenter<IWebBottomSheetView> {

    private EventBus mEventBus;

    @Inject
    WebBottomSheetPresenter(EventBus eventBus) {
        mEventBus = eventBus;
    }

    void handleClickShareOnZalo(Context context, String currentUrl) {
        List<Intent> targetShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(shareIntent, 0);

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
                Intent chooserIntent = Intent.createChooser(targetShareIntents.get(0), "Choose app to share");
                context.startActivity(chooserIntent);
            }
        }
    }

    void handleClickRefreshWeb() {
        mEventBus.post(new RefreshWebEvent());
        if (mView != null) {
            mView.closeDialog();
        }
    }

    void handleClickCopyURL(Context context, String currentUrl) {
        setClipboard(context, currentUrl);
        Toast.makeText(context, context.getResources().getText(R.string.copy_clipboard), Toast.LENGTH_SHORT).show();
        if (mView != null) {
            mView.closeDialog();
        }
    }

    void handleClickOpenInBrowser(Context context, String currentUrl) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
        context.startActivity(browserIntent);
    }

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied URL", text);
        clipboard.setPrimaryClip(clip);
    }
}
