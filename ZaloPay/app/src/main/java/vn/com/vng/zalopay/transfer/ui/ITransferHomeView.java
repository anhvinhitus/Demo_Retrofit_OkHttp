package vn.com.vng.zalopay.transfer.ui;

import android.graphics.drawable.AnimationDrawable;

import java.util.List;

import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by AnhHieu on 8/15/16.
 * *
 */
interface ITransferHomeView extends ILoadDataView {

    void setData(List<RecentTransaction> list);

    void reloadIntroAnimation();

    void setIntroductionAnimation(AnimationDrawable animationDrawable);
}
