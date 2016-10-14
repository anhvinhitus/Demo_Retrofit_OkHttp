package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import vn.com.vng.zalopay.R;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TutorialConnectInternetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TutorialConnectInternetFragment extends BaseFragment {

    @BindView(R.id.tvTurnOnWifi)
    TextView mTvTurnOnWifi;

    @BindView(R.id.tvTurnOn3G)
    TextView mTvTurnOn3G;

    public TutorialConnectInternetFragment() {
        // Required empty public constructor
    }

    public static TutorialConnectInternetFragment newInstance() {
        return new TutorialConnectInternetFragment();
    }

    @Override
    protected void setupFragmentComponent() {

    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_turorial_connect_internet;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTurnOnWifi.setText(Html.fromHtml(getString(R.string.turn_on_wifi)));
        mTvTurnOn3G.setText(Html.fromHtml(getString(R.string.turn_on_3g)));
    }
}
