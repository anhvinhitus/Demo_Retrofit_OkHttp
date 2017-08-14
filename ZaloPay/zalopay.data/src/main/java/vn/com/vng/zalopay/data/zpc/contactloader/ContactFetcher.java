package vn.com.vng.zalopay.data.zpc.contactloader;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.data.util.Lists;
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
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
        };
        ArrayList<Contact> listContacts = new ArrayList<>();
        Cursor c = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projectionFields, null, null, ContactsContract.Contacts.DISPLAY_NAME);
        final Map<String, Contact> contactsMap = new HashMap<>();
        try {
            if (c != null && c.moveToFirst()) {

                int idIndex = c.getColumnIndex(ContactsContract.Contacts._ID);
                int nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                int photoIndex = c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);

                do {
                    String contactId = c.getString(idIndex);
                    String contactDisplayName = c.getString(nameIndex);
                    String photoUri = c.getString(photoIndex);
                    Contact contact = new Contact(contactId, contactDisplayName, photoUri);
                    setContactDetailInfo(contactId, contact);
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
        return filterMobileNumber(listContacts);
    }

    private void setContactDetailInfo(String contactId, Contact contact) throws Exception {
        String where = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] whereParameters = new String[]{contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        String[] projectionFields = new String[]{
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME
        };

        Cursor c = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                projectionFields,
                where,
                whereParameters,
                null);

        try {
            if (c != null && c.moveToFirst()) {
                int familyNameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
                int givenNameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);

                do {
                    String familyName = c.getString(familyNameIndex);
                    String givenName = c.getString(givenNameIndex);

                    contact.lastName = familyName;
                    contact.firstName = givenName;
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void matchContactNumbers(Map<String, Contact> contactsMap) throws Exception {
        Timber.d("match contact numbers %s ", contactsMap.size());

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

    private ArrayList<Contact> filterMobileNumber(ArrayList<Contact> contacts) {
        ArrayList<Contact> filteredContacts = new ArrayList<>();

        for (Contact contact : contacts) {
            if (Lists.isEmptyOrNull(contact.numbers)) {
                continue;
            }

            for (ContactPhone number : contact.numbers) {
                String phoneNumber = PhoneUtil.formatPhoneNumber(number.number);
                if (PhoneUtil.isMobileNumber(phoneNumber)) {
                    filteredContacts.add(contact);
                }
            }
        }

        return filteredContacts;
    }
}
