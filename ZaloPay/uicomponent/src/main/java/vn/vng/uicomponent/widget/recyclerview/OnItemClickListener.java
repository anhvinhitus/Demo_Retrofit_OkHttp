package vn.vng.uicomponent.widget.recyclerview;

import android.view.View;

/**
 * Created by AnhHieu on 9/10/15.
 */
public interface OnItemClickListener {

    void onListItemClick(View anchor, int position);

    boolean onListItemLongClick(View anchor, int position);
}
