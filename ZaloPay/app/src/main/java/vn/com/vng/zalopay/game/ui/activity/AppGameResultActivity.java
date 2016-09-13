package vn.com.vng.zalopay.game.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import timber.log.Timber;
import vn.com.vng.zalopay.game.ui.fragment.AppGameFragment;
import vn.com.vng.zalopay.game.ui.fragment.FragmentPayGame;
import vn.com.vng.zalopay.game.ui.fragment.FragmentPayResult;
import vn.com.zalopay.game.R;
import vn.com.zalopay.game.businnesslogic.base.AppGameGlobal;
import vn.com.zalopay.game.businnesslogic.base.AppGameSingletonLifeCircle;

public class AppGameResultActivity extends AppGameActivity {

    @Override
    protected Fragment getView() {
        mFragment = FragmentPayResult.newInstance();
        Timber.d("getView fragment [%s]", mFragment);
        return mFragment;
    }
}
