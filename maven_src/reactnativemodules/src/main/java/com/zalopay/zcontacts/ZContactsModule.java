package com.zalopay.zcontacts;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;

/**
 * Created by LAP11123-local on 7/25/2016.
 */
public class ZContactsModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    static final int PICK_CONTACT_REQUEST = 1;
    public static final int REQUEST_READ_CONTACT = 102;
    private boolean hasPermission = true;

    private Callback contactSuccessCallback;
    private Callback contactCancelCallback;

    public ZContactsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ZContacts";
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onActivityResult(Activity activity, final int requestCode, final int resultCode, final Intent intent) {
        //contactSuccessCallback.invoke("In onActivityResult");
        if (contactSuccessCallback != null) {
            if (requestCode == PICK_CONTACT_REQUEST) {

                if (resultCode == Activity.RESULT_OK) {
                    Uri contactURI = intent.getData();
                    String phoneNumber = "";
                    String displayName = "";

                    if (contactURI == null) {
                        contactCancelCallback.invoke("No phone number found");
                    } else {
                        ContentResolver cr = getReactApplicationContext().getContentResolver();

                        // Query contact information from Contact provider
                        Cursor cur = null;
                        try {
                            cur = cr.query(contactURI, null, null, null, null);
                            // Read phone number
                            if (cur != null && cur.getCount() > 0) {
                                if (cur.moveToFirst()) {
                                    phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    displayName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                }
                            }
                        } catch (Exception ex) {
                            //empty
                        } finally {
                            if (cur != null) {
                                cur.close();
                            }
                        }
                        try {
                            contactSuccessCallback.invoke(phoneNumber, displayName);
                        } catch (Exception e) {
                            contactCancelCallback.invoke("No phone number found");
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    contactCancelCallback.invoke("ZContacts was cancelled");
                }
            }
        }
    }

    @ReactMethod
    public void openContacts(Callback successCallback, Callback cancelCallback) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            cancelCallback.invoke("Activity doesn't exist");
            return;
        }

        contactSuccessCallback = successCallback;
        contactCancelCallback = cancelCallback;

        try {
            final Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            //pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            currentActivity.startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
        } catch (Exception e) {
            cancelCallback.invoke(e);
        }
    }

    @ReactMethod
    public void lookupPhoneNumber(String phoneNumber, Callback resultCallback) {
        boolean bExistedContact = false;

        Uri contactURI = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        ContentResolver cr = getReactApplicationContext().getContentResolver();

        // Query contact information from Contact provider
        Cursor cur = cr.query(contactURI, null, null, null, null);

        if (cur.getCount() > 0) {
            bExistedContact = true;
        }

        resultCallback.invoke(bExistedContact);
    }

    @ReactMethod
    public void requestReadContact(Callback resultCallback) {
        boolean hasPermission = checkAndRequestPermission(getCurrentActivity());
        if (resultCallback != null) {
            resultCallback.invoke(hasPermission);
        }
    }

    public boolean checkAndRequestPermission(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity == null) {
                hasPermission = false;
            } else {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    hasPermission = false;
                    activity.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACT);
                } else {
                    hasPermission = true;
                }
            }
        }
        return hasPermission;
    }

}
