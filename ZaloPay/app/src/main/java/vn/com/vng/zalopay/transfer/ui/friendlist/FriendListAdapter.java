/*
package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.util.TreeMap;

*/
/**
 * Created by hieuvm on 11/23/16.
 *//*


public class FriendListAdapter extends CursorAdapter {

    private DataSetObserver dataSetObserver;
    private LayoutInflater mInflater;
    private Context context;

    public FriendListAdapter(Context context) {
        super(context, null, 0);
        this.context = context;
        sections = new TreeMap<>();
        this.dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                sections = initializeSections(getCursor());
            }

            @Override
            public void onInvalidated() {
                sections.clear();
            }
        };

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
*/
