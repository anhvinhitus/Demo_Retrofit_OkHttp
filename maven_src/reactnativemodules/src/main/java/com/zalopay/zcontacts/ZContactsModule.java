package com.zalopay.zcontacts;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.content.Context;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;

import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by LAP11123-local on 7/25/2016.
 */
public class ZContactsModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    static final int PICK_CONTACT_REQUEST = 1;
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
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        //contactSuccessCallback.invoke("In onActivityResult");
        if (contactSuccessCallback != null) {
            if (requestCode == PICK_CONTACT_REQUEST) {

                if (resultCode == Activity.RESULT_OK) {
                    Uri pickedPhoneNumber = intent.getData();
                    // handle the picked phone number in here.
                    if (pickedPhoneNumber == null) {
                        contactCancelCallback.invoke("No phone number data found");
                    } else {
                        try {
                            Cursor c = null;
                            Context context = getReactApplicationContext();
                            try {
                                c = context.getContentResolver().query(pickedPhoneNumber, new String[]{
                                                ContactsContract.CommonDataKinds.Phone.NUMBER,
                                                ContactsContract.CommonDataKinds.Phone.TYPE},
                                        null, null, null);

                                if (c != null && c.moveToFirst()) {
                                    String number = c.getString(0);
                                    contactSuccessCallback.invoke(number);
                                }
                            } finally {
                                if (c != null) {
                                    c.close();
                                }
                            }
                        } catch (Exception e) {
                            contactCancelCallback.invoke("No phone number data found");
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

        Uri contactURI = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        ContentResolver cr = getReactApplicationContext().getApplicationContext().getContentResolver();

        Cursor cur = null;
        try {
            // Query contact information from Contact provider
            cur = cr.query(contactURI, null, null, null, null);

            if (cur.getCount() > 0) {
                bExistedContact = true;
            }
        } catch (Exception e) {
            Timber.e("EXCEPTION", e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            resultCallback.invoke(e);
            return;
        } finally {
            if (cur != null) {
                cur.close();
            }
        }

        resultCallback.invoke(bExistedContact);
    }
}
