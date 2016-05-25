package vn.com.vng.zalopay.ui.presenter;


import vn.com.vng.zalopay.ui.view.IHomeView;

/**
 * Created by AnhHieu on 3/26/16.
 */


public class HomePresenter extends BaseUserPresenter implements IPresenter<IHomeView> {
    private IHomeView mView;

    public HomePresenter() {
    }

    @Override
    public void setView(IHomeView view) {
        mView = view;
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

    public void initialize() {
        loadListVideo();
    }

    public void refreshListVideos() {
        this.hideViewRetry();
        this.showViewLoading();
    }

    private void loadListVideo() {
        this.hideViewRetry();
        this.showViewLoading();
        this.getListVideos();
    }

    private void hideViewRetry() {
        mView.hideRetry();
    }

    private void showViewLoading() {
        mView.showLoading();
    }

    private void getListVideos() {
    }


}
