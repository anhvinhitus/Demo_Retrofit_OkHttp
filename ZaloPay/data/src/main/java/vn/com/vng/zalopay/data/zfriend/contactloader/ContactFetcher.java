package vn.com.vng.zalopay.data.zfriend.contactloader;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.data.util.PhoneUtil;

public class ContactFetcher {

    private final Context context;
    private static final int MAX_PHONE_NUMBER = 1; // Lấy duy nhất số đầu tiên của contact

    @Inject
    public ContactFetcher(Context c) {
        this.context = c;
    }

    public ArrayList<Contact> fetchAll() throws Exception {
        String[] projectionFields = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
        };
        ArrayList<Contact> listContacts = new ArrayList<>();
        Cursor c = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projectionFields, null, null, ContactsContract.Contacts.DISPLAY_NAME);
        final Map<String, Contact> contactsMap = new HashMap<>();
        try {
            if (c != null && c.moveToFirst()) {

                int idIndex = c.getColumnIndex(ContactsContract.Contacts._ID);
                int nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                do {
                    String contactId = c.getString(idIndex);
                    String contactDisplayName = c.getString(nameIndex);
                    Contact contact = new Contact(contactId, contactDisplayName);
                    contactsMap.put(contactId, contact);
                    listContacts.add(contact);
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        matchContactNumbers(contactsMap);
        return listContacts;
    }

    private void matchContactNumbers(Map<String, Contact> contactsMap) throws Exception {
        Timber.d("match Contact Numbers %s ", contactsMap.size());

        final String[] numberProjection = new String[]{
                Phone.NUMBER,
                Phone.TYPE,
                Phone.CONTACT_ID,
        };

        Cursor phone = context.getContentResolver().query(Phone.CONTENT_URI, numberProjection, null, null, null);
        try {
            if (phone != null && phone.moveToFirst()) {
                final int contactNumberColumnIndex = phone.getColumnIndex(Phone.NUMBER);
                final int contactTypeColumnIndex = phone.getColumnIndex(Phone.TYPE);
                final int contactIdColumnIndex = phone.getColumnIndex(Phone.CONTACT_ID);

                while (!phone.isAfterLast()) {
                    final String rawNumber = phone.getString(contactNumberColumnIndex);

                    String number = PhoneUtil.formatPhoneNumber(rawNumber);

                    final String contactId = phone.getString(contactIdColumnIndex);
                    Contact contact = contactsMap.get(contactId);
                    if (contact == null) {
                        continue;
                    }
                    final int type = phone.getInt(contactTypeColumnIndex);
                    String customLabel = "Custom";
                    CharSequence phoneType = Phone.getTypeLabel(context.getResources(), type, customLabel);
                    contact.addNumber(number, phoneType.toString());
                   /* if (MAX_PHONE_NUMBER >= contact.numbers.size()) {
                        break;
                    }*/
                    phone.moveToNext();
                }
            }
        } finally {
            if (phone != null) {
                phone.close();
            }
        }
    }
}
