package com.zalopay.ui.widget.edittext;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by AnhHieu on 8/29/16.
 * *
 */
public class NonSelectionActionModeCallback implements ActionMode.Callback {
    
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
