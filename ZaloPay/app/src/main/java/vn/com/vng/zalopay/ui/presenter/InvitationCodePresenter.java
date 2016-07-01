package vn.com.vng.zalopay.ui.presenter;

import vn.com.vng.zalopay.ui.view.IInvitationCodeView;

/**
 * Created by AnhHieu on 6/27/16.
 */
public class InvitationCodePresenter extends BaseUserPresenter implements IPresenter<IInvitationCodeView> {

    IInvitationCodeView mView;

    @Override
    public void setView(IInvitationCodeView iInvitationCodeView) {
        mView = iInvitationCodeView;
    }

    @Override
    public void destroyView() {
        mView = null;
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

    public void sendCode(String coe) {
        
    }
}
