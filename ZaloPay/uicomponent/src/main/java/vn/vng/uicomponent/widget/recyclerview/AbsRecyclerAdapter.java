package vn.vng.uicomponent.widget.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by AnhHieu on 5/10/16.
 */
public abstract class AbsRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<T> mItems;

    protected final Context context;
    protected LayoutInflater mInflater;
    private final Object _lock = new Object();

    public AbsRecyclerAdapter(Context context) {
        this.context = context;
        mItems = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Context getContext() {
        return context;
    }

    public T getItem(int position) {
        if (position >= 0 && position < mItems.size())
            return this.mItems.get(position);

        return null;
    }

    public List<T> getItems() {
        return mItems;
    }

    public void setData(Collection<T> items) {
        if (items == null) return;

        synchronized (_lock) {
            mItems.clear();
            mItems.addAll(items);
        }

        notifyDataSetChanged();
    }

    public void insertItems(Collection<T> items) {
        if (items == null || items.isEmpty()) return;

        int before = mItems.size();

        synchronized (_lock) {
            mItems.addAll(items);
        }

        notifyItemRangeInserted(before, getItemCount());
    }

    public void remove(T values) {
        synchronized (_lock) {
            mItems.remove(values);
        }
        //notifyItemRemoved();
    }

    public void remove(int location) {
        synchronized (_lock) {
            mItems.remove(location);
        }
        notifyItemRemoved(location);
    }

    public void insert(T object, int index) {
        synchronized (_lock) {
            mItems.add(index, object);
        }
        notifyItemInserted(index);
    }

    public void insert(T object) {
        synchronized (_lock) {
            mItems.add(object);
        }
        notifyItemInserted(getItemCount());
    }


}
