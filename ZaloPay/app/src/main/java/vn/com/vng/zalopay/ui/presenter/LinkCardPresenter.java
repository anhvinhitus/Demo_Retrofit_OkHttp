package vn.com.vng.zalopay.ui.presenter;

import vn.com.vng.zalopay.ui.view.ILinkCardView;

/**
 * Created by AnhHieu on 5/11/16.
 */
public class LinkCardPresenter extends BaseUserPresenter implements Presenter<ILinkCardView> {

    private ILinkCardView linkCardView;

    @Override
    public void setView(ILinkCardView iLinkCardView) {
        linkCardView = iLinkCardView;
    }

    @Override
    public void destroyView() {
        linkCardView = null;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }
}
