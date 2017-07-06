package vn.com.vng.zalopay.bank.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.wallet.business.dao.ResourceManager;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link NotificationLinkCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationLinkCardFragment extends BaseFragment {

    private String mLastCardNumber;
    private String mImageFilePath;
    private String mBankName;

    @BindView(R.id.txtLastNumberOfCard)
    TextView mTxtLastNumberOfCard;

    @BindView(R.id.imgBankIcon)
    ImageView mImgBankIcon;

    @BindView(R.id.txtBankName)
    TextView mTxtBankName;

    @BindView(R.id.scrRootview)
    ScrollView mScrollView;

    @OnClick(R.id.btnCancel)
    public void onCLickCancel() {
        this.getActivity().finish();
    }

    @OnClick(R.id.btnManagerCard)
    public void onCLickManagerCard() {
        navigator.startLinkCardActivityAndFinish(getContext());
    }

    public NotificationLinkCardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NotificationLinkCardFragment.
     */
    public static NotificationLinkCardFragment newInstance(Bundle bundle) {
        NotificationLinkCardFragment fragment = new NotificationLinkCardFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_notification_link_card;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLastCardNumber = getArguments().getString(Constants.LAST4CARDNO, "");
            mImageFilePath = getArguments().getString(Constants.IMAGE_FILE_PATH, "");
            mBankName = getArguments().getString(Constants.BANKNAME, "");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLastCardNumber(mLastCardNumber);
        setBankIcon(mImageFilePath);
        setBankName(mBankName);
        setScrollViewFocusDown();
    }

    private void setLastCardNumber(String lastCardNumber) {
        if (mTxtLastNumberOfCard == null) {
            return;
        }
        if (!TextUtils.isEmpty(lastCardNumber)) {
            mTxtLastNumberOfCard.setText(TextUtils.concat("••••", lastCardNumber));
        }
    }

    private void setBankIcon(String imageFilePath) {
        if (mImgBankIcon == null) {
            return;
        }
        if (!TextUtils.isEmpty(imageFilePath)) {
            mImgBankIcon.setImageBitmap(ResourceManager.getImage(imageFilePath));
        }
    }

    private void setBankName(String bankName) {
        if (mTxtBankName == null) {
            return;
        }
        if (!TextUtils.isEmpty(bankName)) {
            mTxtBankName.setText(bankName);
        }
    }

    private void setScrollViewFocusDown() {
        AndroidUtils.runOnUIThread(mScrollViewRunnable, 300);
    }

    private Runnable mScrollViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (mScrollView != null) {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        }
    };
}
