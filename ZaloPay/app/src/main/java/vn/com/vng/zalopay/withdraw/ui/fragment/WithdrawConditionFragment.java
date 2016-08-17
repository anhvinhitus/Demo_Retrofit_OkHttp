package vn.com.vng.zalopay.withdraw.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.wallet.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link WithdrawConditionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WithdrawConditionFragment extends BaseFragment {

    @BindView(R.id.chkEmail)
    CheckBox chkEmail;

    @BindView(R.id.chkIdentityNumber)
    CheckBox chkIdentityNumber;

    @BindView(R.id.chkVietinBank)
    CheckBox chkVietinBank;

    @BindView(R.id.chkSacomBank)
    CheckBox chkSacomBank;

    @OnClick(R.id.tvUpdateProfile)
    public void onClickUpdateProfile() {
        navigator.startUpdateProfile3Activity(getActivity());
    }

    @OnClick(R.id.tvSaveCard)
    public void onClickSaveCard() {
        navigator.startLinkCardActivity(getActivity());
    }

    public WithdrawConditionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WithdrawConditionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WithdrawConditionFragment newInstance() {
        return new WithdrawConditionFragment();
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_withdraw_condition;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean isValidCondition = checkValidCondition();
        if (isValidCondition) {
            navigator.startWithdrawActivity(getContext());
            getActivity().finish();
        }
    }

    private boolean checkValidCondition() {
        return checkValidProfileLevel() && checkValidLinkCard();
    }

    private boolean checkValidProfileLevel() {
        User user = getUserComponent().currentUser();
        if (user == null) {
            return false;
        }
        if (TextUtils.isEmpty(user.email)) {
            return false;
        } else {
            chkEmail.setChecked(true);
        }
        if (TextUtils.isEmpty(user.identityNumber)) {
            return false;
        } else {
            chkIdentityNumber.setChecked(true);
        }
        return true;
    }

    private boolean checkValidLinkCard() {
        User user = getUserComponent().currentUser();
        boolean isMapped = false;
        try {
            List<DMappedCard> mapCardLis = CShareData.getInstance(getActivity()).getMappedCardList(user.uid);
            for (int i = 0; i < mapCardLis.size(); i++) {
                DMappedCard card = mapCardLis.get(i);
                if (card == null || TextUtils.isEmpty(card.bankcode)) {
                    continue;
                }
                if (ECardType.PVTB.toString().equals(card.bankcode)) {
                    chkVietinBank.setChecked(true);
                    isMapped = true;
                } else if (ECardType.PSCB.toString().equals(card.bankcode)) {
                    chkSacomBank.setChecked(true);
                    isMapped = true;
                }
            }
            return isMapped;
        } catch (Exception e) {
            Timber.w(e, "Get mapped card exception: %s", e.getMessage());
        }
        return isMapped;
    }
}
