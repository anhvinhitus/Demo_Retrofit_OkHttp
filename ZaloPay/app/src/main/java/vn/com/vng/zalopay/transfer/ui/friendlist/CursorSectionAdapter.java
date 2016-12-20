package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.SortedMap;
import java.util.TreeMap;

import timber.log.Timber;

/**
 * Created by AnhHieu on 10/4/15.
 * *
 */
public abstract class CursorSectionAdapter extends CursorAdapter {

    protected abstract void bindSeparatorView(View v, Context context, Object item);

    protected abstract View newSeparatorView(Context context2, Object item, ViewGroup parent);

    public abstract void bindView(View view, Context context, Cursor cursor, int position);

    protected abstract SortedMap<Integer, Object> initializeSections(Cursor c);

    protected abstract boolean isSection(int position);


    private Context context;
    private SortedMap<Integer, Object> sections;
    private DataSetObserver dataSetObserver;

    public CursorSectionAdapter(Context context) {
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
    }

    public SortedMap<Integer, Object> getSections() {
        return sections;
    }

    protected int getRealItemPosition(int position) {
        int offset = 0;
        for (Integer k : sections.keySet()) {
            if (position >= k) {
                offset++;
            } else {
                break;
            }
        }
        return (position - offset);
    }

    @Override
    public Object getItem(int position) {
        if (isSection(position)) {
            return this.sections.get(position);
        } else {
            return super.getItem(getRealItemPosition(position));
        }
    }

    @Override
    public long getItemId(int position) {
        if (isSection(position)) {
            return position;
        } else {
            return super.getItemId(getRealItemPosition(position));
        }
    }

    @Override
    public int getCount() {
        if (getCursor() == null) {
            return 0;
        }

        return super.getCount() + sections.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (isSection(position)) {
            View v;
            if (convertView == null) {
                v = newSeparatorView(context, getItem(position), parent);
            } else {
                v = convertView;
            }
            bindSeparatorView(v, context, getItem(position));
            return v;
        } else {
            if (!getCursor().moveToPosition(getRealItemPosition(position)))
                throw new IllegalStateException("couldn't move cursor to position " + position);

            View v;
            if (convertView == null) {
                v = newView(context, getCursor(), parent);
            } else {
                v = convertView;
            }
            //bindView(v, context, getCursor());
            bindView(v, context, getCursor(), position);
            return v;
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //empty
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if (getCursor() != null) {
            getCursor().unregisterDataSetObserver(dataSetObserver);
        }

        if (newCursor != null) {
            this.sections = initializeSections(newCursor);
        }

        Cursor oldCursor = super.swapCursor(newCursor);

        if (newCursor != null) {
            newCursor.registerDataSetObserver(dataSetObserver);
        }

        return oldCursor;
    }


}