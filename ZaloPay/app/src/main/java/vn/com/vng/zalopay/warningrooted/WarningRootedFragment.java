package vn.com.vng.zalopay.warningrooted;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.RootUtils;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link WarningRootedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WarningRootedFragment extends BaseFragment {

    @BindView(R.id.chkRemind)
    CheckBox chkRemind;

    @OnClick(R.id.btnContinue)
    public void onClickBtnContinue() {
        saveCheckboxState();
        saveCheckboxRemind();
        getActivity().finish();
    }

    @OnClick(R.id.btnClose)
    public void onClickBtnClose() {
        saveCheckboxState();
        quitApp();
    }

    private void quitApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
            System.exit(0);
        } catch (Exception e) {
            Timber.w(e, "quitApp exception [%s]", e.getMessage());
        }
    }

    @OnClick(R.id.layoutRemind)
    public void onClickRemind() {
        if (chkRemind == null) {
            return;
        }
        if (chkRemind.isChecked()) {
            chkRemind.setChecked(false);
        } else {
            chkRemind.setChecked(true);
        }
    }

    private boolean latestCheckboxState() {
        return RootUtils.isCheckboxCheckedate();
    }

    private void saveCheckboxState() {
        if (chkRemind == null) {
            return;
        }
        RootUtils.setLastestCheckboxState(chkRemind.isChecked());
    }

    private void saveCheckboxRemind() {
        if (chkRemind == null) {
            return;
        }
        RootUtils.setHideWarningRooted(chkRemind.isChecked());
    }

    public WarningRootedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WarningRootedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WarningRootedFragment newInstance() {
        return new WarningRootedFragment();
    }

    @Override
    protected void setupFragmentComponent() {
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_warning_rooted;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reloadLatestCheckboxState();
    }

    private void reloadLatestCheckboxState() {
        if (chkRemind == null) {
            return;
        }
        chkRemind.setChecked(latestCheckboxState());
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }
}
