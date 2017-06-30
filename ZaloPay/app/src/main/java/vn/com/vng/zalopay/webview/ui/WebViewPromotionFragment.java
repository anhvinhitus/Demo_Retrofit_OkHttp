package vn.com.vng.zalopay.webview.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.webapp.WebBottomSheetDialogFragment;
import vn.com.vng.zalopay.webview.widget.ZPWebView;
import vn.com.vng.zalopay.webview.widget.ZPWebViewPromotionProcessor;


/**
 * Created by datnt10 on 6/28/17.
 * Fragment
 */
public class WebViewPromotionFragment extends BaseFragment implements ZPWebViewPromotionProcessor.IWebViewPromotionListener,
        WebBottomSheetDialogFragment.BottomSheetEventListener,
        SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.webview)
    ZPWebView mWebView;

    protected ZPWebViewPromotionProcessor mWebViewProcessor;

    @BindView(R.id.layoutRetry)
    View layoutRetry;

    @BindView(R.id.imgError)
    ImageView imgError;

    @BindView(R.id.tvError)
    TextView tvError;

    private WebBottomSheetDialogFragment mBottomSheetDialog;

    @Inject
    Navigator mNavigator;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @BindView(R.id.promotion_refresh_layout)
    MultiSwipeRefreshLayout refreshLayout;

    private String mUrl;

    public static WebViewPromotionFragment newInstance(Bundle bundle) {
        WebViewPromotionFragment fragment = new WebViewPromotionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

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
        initArgs(savedInstanceState == null ? getArguments() : savedInstanceState);
    }

    private void initArgs(Bundle bundle) {
        String originalUrl = bundle.getString(Constants.ARG_URL);
        HashMap<String, String> params = new HashMap<>();
        params.put("userid", getUserComponent().currentUser().zaloPayId);
        mUrl = Strings.addUrlQueryParams(originalUrl, params);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshLayout.setSwipeableChildren(R.id.webview);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.ARG_URL, mUrl);
    }

    public void loadUrl(final String pUrl) {
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
        Timber.d("showError errorCode [%s]", errorCode);
        if (errorCode == WebViewClient.ERROR_CONNECT) {
            if (NetworkHelper.isNetworkAvailable(getContext())) {
                showErrorNoLoad();
            } else {
                showErrorNoConnection();
            }
        } else {
            showErrorNoLoad();
        }
        hideLoading();
    }

    @Override
    public void openWebDetail(String url) {
        mNavigator.startWebPromotionDetailActivity(getActivity(), url);
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
                    public void onOKevent() {
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
}
