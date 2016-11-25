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
}
