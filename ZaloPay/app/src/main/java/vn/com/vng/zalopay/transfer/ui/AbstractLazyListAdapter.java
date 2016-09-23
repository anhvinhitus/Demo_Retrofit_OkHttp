/*
 * ShoLi, a simple tool to produce short lists.
 * Copyright (C) 2014,2015  David Soulayrol
 *
 * ShoLi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ShoLi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vn.com.vng.zalopay.transfer.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import de.greenrobot.dao.query.LazyList;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.model.IPersistentObject;

public abstract class AbstractLazyListAdapter<T extends IPersistentObject,VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private LazyList<T> _lazyList;
    private Context _context;

    public AbstractLazyListAdapter(Context context, LazyList<T> lazyList) {
        this._lazyList = lazyList;
        this._context = context;
    }

    public LazyList<T> getLazyList() {
        return _lazyList;
    }

    public void setLazyList(LazyList<T> list) {
        Timber.d("setLazyList list %s", list);
        if (list != _lazyList) {
            if (_lazyList != null) {
                _lazyList.close();
            }
            _lazyList = list;
            notifyDataSetChanged();
        }
    }

    public Context getContext() {
        return _context;
    }

    @Override
    public int getItemCount() {
        if (_lazyList != null) {
            return _lazyList.size();
        } else {
            return 0;
        }
    }

    public T getItem(int position) {
        if (_lazyList != null && position < _lazyList.size()) {
            return _lazyList.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        if (_lazyList != null && position < _lazyList.size()) {
            T item = _lazyList.get(position);
            if (item != null)
                return item.getId();
        }
        return 0;
    }
}
