package vn.com.vng.zalopay.webview.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;
import com.zalopay.ui.widget.MultiSwipeRefreshLayout;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.ui.fragment.tabmain.UserBaseTabFragment;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.webapp.WebBottomSheetDialogFragment;
import vn.com.vng.zalopay.webview.widget.ZPWebView;
import vn.com.vng.zalopay.webview.widget.ZPWebViewPromotionProcessor;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPScreens;


/**
 * Created by datnt10 on 6/28/17.
 * Fragment
 */
public class WebViewPromotionFragment extends UserBaseTabFragment implements ZPWebViewPromotionProcessor.IWebViewPromotionListener,
        WebBottomSheetDialogFragment.BottomSheetEventListener,
        SwipeRefreshLayout.OnRefreshListener {

    public static WebViewPromotionFragment newInstance() {

        Bundle args = new Bundle();

        WebViewPromotionFragment fragment = new WebViewPromotionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.promotion_webview)
    ZPWebView mWebView;

    @BindView(R.id.layoutRetry)
    View layoutRetry;

    @BindView(R.id.imgError)
    ImageView imgError;

    @BindView(R.id.tvError)
    TextView tvError;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @BindView(R.id.promotion_refresh_layout)
    MultiSwipeRefreshLayout refreshLayout;

    private String mUrl;

    private ZPWebViewPromotionProcessor mWebViewProcessor;
    private WebBottomSheetDialogFragment mBottomSheetDialog;

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.webview_fragment_promotion;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUrl = getUrl();
        refreshLayout.setSwipeableChildren(R.id.promotion_webview);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.back_ground_blue);

        mWebViewProcessor = new ZPWebViewPromotionProcessor(mWebView, this);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                Timber.d("WebLoading progress: %s", progress);
                if (mProgressBar == null) {
                    return;
                }

                if (progress < 100 && mProgressBar.getVisibility() == ProgressBar.GONE) {
                    mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
                mProgressBar.setProgress(progress);
                if (progress >= 100) {
                    mProgressBar.setVisibility(ProgressBar.GONE);
                }
            }
        });
    }

    private String getUrl() {
        HashMap<String, String> params = new HashMap<>();
        params.put("userid", getUserComponent().currentUser().zaloPayId);
        return Strings.addUrlQueryParams(BuildConfig.PROMOTION_URL, params);
    }

    public void loadUrl(String pUrl) {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.start(pUrl, getActivity());
        }
    }

    @OnClick(R.id.btnRetry)
    public void onClickRetryWebView() {
        hideError();
        refreshWeb();
    }

    private void showErrorNoConnection() {
        if (layoutRetry == null || imgError == null || tvError == null) {
            return;
        }
        imgError.setImageResource(R.drawable.webapp_ic_noconnect);
        tvError.setText(R.string.exception_no_connection_try_again);
        layoutRetry.setVisibility(View.VISIBLE);
    }

    private void showErrorNoLoad() {
        if (layoutRetry == null || imgError == null || tvError == null) {
            return;
        }
        imgError.setImageResource(R.drawable.webapp_ic_noload);
        tvError.setText(R.string.load_data_error);
        layoutRetry.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(int errorCode) {
        if(getContext() == null) {
            return;
        }

        Timber.d("showError errorCode [%s]", errorCode);
        if (NetworkHelper.isNetworkAvailable(getContext())) {
            showErrorNoLoad();
        } else {
            showErrorNoConnection();
        }
        hideLoading();
    }

    @Override
    public void openWebDetail(String url) {
        navigator.startWebPromotionDetailActivity(getActivity(), url);
    }

    @Override
    public void setRefresh(boolean refresh) {
        refreshLayout.setRefreshing(refresh);
    }

    @Override
    protected void onTimeoutLoading(long timeout) {
        super.onTimeoutLoading(timeout);
        showConfirmExitDialog(timeout);
    }

    private void showConfirmExitDialog(final long timeout) {
        DialogHelper.showConfirmDialog(getActivity(),
                getActivity().getResources().getString(R.string.appgame_waiting_loading),
                getActivity().getResources().getString(R.string.btn_wait_loading),
                getActivity().getResources().getString(R.string.btn_exit),
                new ZPWOnEventConfirmDialogListener
                        () {
                    @Override
                    public void onCancelEvent() {
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onOKEvent() {
                        if (mWebViewProcessor != null && mWebViewProcessor.isLoadPageFinished()) {
                            return;
                        }
                        showProgressDialog(timeout);
                    }
                });
    }

    public void hideLoading() {
        super.hideProgressDialog();
    }

    public void showLoading() {
        super.showProgressDialogWithTimeout();
    }

    public void showError(String message) {
        showErrorDialog(message, getString(R.string.txt_close), null);
    }

    protected void hideError() {
        Timber.d("hideError layoutRetry [%s]", layoutRetry);
        if (layoutRetry == null) {
            return;
        }
        layoutRetry.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUrl(mUrl);
    }

    @Override
    public void onDestroyView() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onDestroyView();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.onDestroy();
        }
        super.onDestroy();
    }

    public boolean onBackPressed() {
        return mWebViewProcessor != null && mWebViewProcessor.onBackPress() || super.onBackPressed();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.webapp_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem = menu.findItem(R.id.action_settings);
        View view = menuItem.getActionView();
        IconFont mIcon = (IconFont) view.findViewById(R.id.imgSettings);
        mIcon.setIcon(R.string.webapp_3point_android);
        view.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                showBottomSheetDialog();
            }
        });
    }

    public void refreshWeb() {
        Timber.d("Request to reload web view");
        hideError();
        mWebViewProcessor.refreshWeb(getActivity());
    }

    private void showBottomSheetDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("current_url", mWebViewProcessor.getCurrentUrl());
        mBottomSheetDialog = WebBottomSheetDialogFragment.newInstance(bundle);
        mBottomSheetDialog.setBottomSheetEventListener(this);
        mBottomSheetDialog.show(getChildFragmentManager(), "bottomsheet");
    }

    @Override
    public void onRequestRefreshPage() {
        refreshWeb();
        mBottomSheetDialog.dismiss();
    }

    @Override
    public void onRefresh() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.refreshWeb(getActivity());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            ZPAnalytics.trackScreen(ZPScreens.PROMOTION);
        }
    }
}
