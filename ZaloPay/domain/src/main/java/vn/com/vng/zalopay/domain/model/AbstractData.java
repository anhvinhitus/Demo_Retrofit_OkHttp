package vn.com.vng.zalopay.domain.model;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by AnhHieu on 3/25/16.
 */
public abstract class AbstractData implements Parcelable {
    //Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    public Uri getUri(){
        return null;
    }

    public ContentValues getContentValues(){
        return null;
    }

    public ContentValues getContentValues(int pos){
        return null;
    }

    public static <T extends AbstractData> ContentValues[] getContentValues(List<T> list){
        if (list == null) return null;
        ContentValues[] contentValues = new ContentValues[list.size()];
        for (int i = 0; i < list.size(); i++){
            contentValues[i] = list.get(i).getContentValues();
        }
        return contentValues;
    }

    public static <T extends AbstractData> ContentValues[] getContentValues(List<T> list, int page, int pageSize){
        if (list == null) return null;
        ContentValues[] contentValues = new ContentValues[list.size()];
        for (int i = 0; i < list.size(); i++){
            contentValues[i] = list.get(i).getContentValues(page*pageSize+i);
        }
        return contentValues;
    }
}
