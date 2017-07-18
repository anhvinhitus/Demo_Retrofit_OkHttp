package vn.com.zalopay.wallet.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lytm on 17/07/2017.
 */

public abstract class BaseCardSupportAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected List<T> mItems;

    protected final Context context;
    protected LayoutInflater mInflater;
    protected final Object _lock = new Object();

    public BaseCardSupportAdapter(Context context) {
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
        if (position >= 0 && position < mItems.size()) {
            return this.mItems.get(position);
        }

        return null;
    }

    public List<T> getItems() {
        return mItems;
    }

    public void setData(List<T> items) {
        if (items == null) {
            return;
        }

        synchronized (_lock) {
            mItems.clear();
            mItems.addAll(items);
        }

        notifyDataSetChanged();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mItems.clear();
    }


    public void insertItems(List<T> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        int before = mItems.size();

        synchronized (_lock) {
            mItems.addAll(items);
        }

        notifyItemRangeInserted(before, getItemCount());
    }

    public void remove(T values) {
        int location = mItems.indexOf(values);
        if (location >= 0) {
            remove(location);
        }
    }

    public void remove(int location) {
        synchronized (_lock) {
            mItems.remove(location);
        }
        notifyItemRemoved(location);
    }

    public void removeAll() {
        synchronized (_lock) {
            mItems.clear();
        }
        notifyDataSetChanged();
    }

    public void removeAll(Collection<T> items) {
        synchronized (_lock) {
            mItems.removeAll(items);
        }
        notifyDataSetChanged();
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

    public void insertOrReplace(T object) {
        int location = mItems.indexOf(object);
        if (location >= 0) {
            synchronized (_lock) {
                mItems.set(location, object);
                notifyItemChanged(location);
            }
        } else {
            insert(object);
        }
    }

    public void replace(int location, T object) {
        synchronized (_lock) {
            mItems.set(location, object);
        }
        notifyItemChanged(location);
    }

}
