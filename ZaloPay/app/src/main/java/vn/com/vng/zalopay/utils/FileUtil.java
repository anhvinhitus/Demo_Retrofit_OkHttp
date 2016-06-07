package vn.com.vng.zalopay.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import vn.com.vng.zalopay.AndroidApplication;

/**
 * Created by longlv on 05/06/2016.
 */
public class FileUtil {
    private static final String FILE_DIRECTORY = "zalopay";
    /**
     * returns absolute file directory
     *
     * @return
     * @throws Exception
     */
    public static String getFilename(String fileName) throws Exception {
        String filepath = null;
        String myDate = null;
        File file = null;
        if (fileName == null)
            throw new Exception("Phone number can't be empty");
        try {
            filepath = getFilePath();

            file = new File(filepath, FILE_DIRECTORY);

            if (!file.exists()) {
                file.mkdirs();
            }

            myDate = (String) DateFormat.format("yyyyMMdd_kkmmss", new Date());

            // Clean characters in file name
            fileName = fileName.replaceAll("[\\*\\+-]", "");
            if (fileName.length() > 10) {
                fileName.substring(fileName.length() - 10, fileName.length());
            }
        } catch (Exception e) {
            Log.e("FileHelper", "Exception " + fileName);
            e.printStackTrace();
        }

        return (file.getAbsolutePath() + "/" + fileName + "_day_" + myDate + ".wav");
    }

    public static String getFilePath() {
        // TODO: Change to user selected directory
        String[] sdcards = getStorageDirectories();
        if (sdcards != null) {
            return sdcards[0];
        }
//		return Environment.getExternalStorageDirectory().getAbsolutePath();
        return getPreviewTempExternalDirectory(AndroidApplication.instance().getApplicationContext()).getPath();
//		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
    }

    private static File getPreviewTempExternalDirectory(Context context) {
        File previewDir = new File(context.getExternalFilesDir(null), FILE_DIRECTORY);
        if (!previewDir.exists()) previewDir.mkdir();
        return previewDir;
    }

    public static void deleteRecord(Activity caller, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file1 = new File(filePath);
        if (file1.exists()) {
            file1.delete();
        }
    }

    public static void deleteAllRecords(Activity caller) {
        String filepath = getFilePath() + "/" + FILE_DIRECTORY;
        File file = new File(filepath);

        String listOfFileNames[] = file.list();

        for (int i = 0; i < listOfFileNames.length; i++) {
            File file2 = new File(filepath, listOfFileNames[i]);
            if (file2.exists()) {
                file2.delete();
            }
        }

        filepath = caller.getFilesDir().getAbsolutePath() + "/"
                + FILE_DIRECTORY;
        file = new File(filepath);

        String listOfFileNames2[] = file.list();

        for (int i = 0; i < listOfFileNames2.length; i++) {
            File file2 = new File(filepath, listOfFileNames2[i]);
            if (file2.exists()) {
                file2.delete();
            }
        }
    }

    /**
     * Obtains the contact list for the currently selected account.
     *
     * @return A cursor for for accessing the contact list.
     */
    public static String getContactName(String phoneNum, Activity caller) {
        String res = phoneNum;
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selection = null;
        String[] selectionArgs = null;
        Cursor names = caller.getContentResolver().query(uri, projection,
                selection, selectionArgs, null);

        int indexName = names
                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = names
                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        if (names.getCount() > 0) {
            names.moveToFirst();
            do {
                String name = names.getString(indexName);
                String number = names.getString(indexNumber).replaceAll(
                        "[\\*\\+-]", "");

                if (number.compareTo(phoneNum) == 0) {
                    res = name;
                    break;
                }
            } while (names.moveToNext());
        }

        return res;
    }

//	/**
//	 * Fetches list of previous recordings
//	 *
//	 * @param f
//	 * @return
//	 */
//	public static List<Model> listDir2(File f, Activity caller) {
//		File[] files = f.listFiles();
//		List<Model> fileList = new ArrayList<Model>();
//		for (File file : files) {
//			if (!file.getName().matches(Constants.FILE_NAME_PATTERN)) {
//				Log.d(Constants.TAG, String.format(
//						"'%s' didn't match the file name pattern",
//						file.getName()));
//				continue;
//			}
//
//			Model mModel = new Model(file.getName());
//			String phoneNum = mModel.getCallName().substring(16,
//					mModel.getCallName().length() - 4);
//			mModel.setUserNameFromContact(getContactName(phoneNum, caller));
//			fileList.add(mModel);
//		}
//
//		Collections.sort(fileList);
//		Collections.sort(fileList, Collections.reverseOrder());
//
//		return fileList;
//	}
//
//	public static List<Model> listFiles(Activity caller) {
//		String filepath = FileHelper.getFilePath();
//		final File file = new File(filepath, FILE_DIRECTORY);
//
//		if (!file.exists()) {
//			file.mkdirs();
//		}
//
//		final List<Model> listDir = FileHelper.listDir2(file, caller);
//
//		filepath = caller.getFilesDir().getAbsolutePath();
//		final File file2 = new File(filepath, FILE_DIRECTORY);
//
//		if (!file2.exists()) {
//			file2.mkdirs();
//		}
//
//		final List<Model> listDir2 = FileHelper.listDir2(file2, caller);
//
//		listDir.addAll(listDir2);
//
//		return listDir;
//	}

    public static void deleteFile(String fileName) {
        if (fileName == null)
            return;
        Log.d("FileHelper", "FileHelper deleteFile " + fileName);
        try {
            File file = new File(fileName);

            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            Log.e("FileHelper", "Exception");
            e.printStackTrace();
        }
    }

    private static final Pattern DIR_SEPORATOR = Pattern.compile("/");

    /**
     * Raturns all available SD-Cards in the system (include emulated)
     * <p>
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standart way to get it.
     * TODO: Test on future Android versions 4.4+
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public static String[] getStorageDirectories() {
        // Final set of paths
        final Set<String> rv = new HashSet<String>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPORATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        return rv.toArray(new String[rv.size()]);
    }
}
