package vn.com.vng.zalopay.data.zfriend.contactloader;

import java.util.ArrayList;

public class Contact {
    public String id;
    public String name;
    public ArrayList<ContactPhone> numbers;

    public Contact(String id, String name) {
        this.id = id;
        this.name = name;
        this.numbers = new ArrayList<>();
    }

    public void addNumber(String number, String type) {
        numbers.add(new ContactPhone(number, type));
    }

    public boolean inside(long phoneNumber) {
        return inside(String.valueOf(phoneNumber));
    }

    public boolean inside(String phoneNumber) {
        int count = 0;
        // Timber.d("number phone contact %s", numbers.size());
        for (ContactPhone number : numbers) {
            //   Timber.d("number [%s] : [%s] : [%s]", count, number.number, phoneNumber);
            if (number.number.equals(phoneNumber)) {
                return true;
            }
        }
        return false;
    }
}
