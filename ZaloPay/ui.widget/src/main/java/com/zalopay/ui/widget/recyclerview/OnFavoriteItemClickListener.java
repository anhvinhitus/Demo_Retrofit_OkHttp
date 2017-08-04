package com.zalopay.ui.widget.recyclerview;

import android.view.View;

/**
 * Created by datnt10 on 8/4/17.
 */

public interface OnFavoriteItemClickListener {
    void onListItemClick(View anchor, int position);

    void onRemoveItemClick(View anchor, int position);
}
