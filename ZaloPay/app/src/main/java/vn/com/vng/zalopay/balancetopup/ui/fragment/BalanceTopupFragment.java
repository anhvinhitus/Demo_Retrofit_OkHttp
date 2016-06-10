package vn.com.vng.zalopay.balancetopup.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.balancetopup.ui.activity.ConfirmTransactionActivity;
import vn.com.vng.zalopay.balancetopup.ui.view.IBalanceTopupView;
import vn.com.vng.zalopay.balancetopup.ui.widget.InputAmountLayout;
import vn.com.vng.zalopay.data.repository.datasource.UserConfigFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.utils.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BalanceTopupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BalanceTopupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceTopupFragment extends BaseFragment implements IBalanceTopupView, InputAmountLayout.IListenerAmountChanged {
    // TODO: Rename parameter arguments, choose names that match
    private final int MIN_AMOUNT = 10000;

    private OnFragmentInteractionListener mListener;
    private String mValidAmount = "";

    @Inject
    BalanceTopupPresenter balanceTopupPresenter;

    @BindView(R.id.tvResourceMoney)
    TextView tvResourceMoney;

    @BindView(R.id.inputAmountLayout)
    InputAmountLayout inputAmountLayout;

    @BindView(R.id.btnDeposit)
    View btnDeposit;

    @OnClick(R.id.btnDeposit)
    public void onClickDeposit() {
        if (inputAmountLayout.getAmount() < MIN_AMOUNT || inputAmountLayout.getAmount()%10000 != 0) {
            showError(mValidAmount);
            return;
        }
        showProgressDialog();
        balanceTopupPresenter.deposit(inputAmountLayout.getAmount());
    }

    private void gotoConfirmTransaction() {
        Intent intent = new Intent(getContext(), ConfirmTransactionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.ARG_AMOUNT, inputAmountLayout.getAmount());
//        bundle.putString(Constants.ARG_PAYEE, bankSpinner.getSelectedCharSequence().toString());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public BalanceTopupFragment() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BalanceTopupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BalanceTopupFragment newInstance() {
        BalanceTopupFragment fragment = new BalanceTopupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_balance_topup;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        mValidAmount = String.format(getResources().getString(R.string.min_money), CurrencyUtil.formatCurrency(MIN_AMOUNT, true));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        balanceTopupPresenter.setView(this);
        inputAmountLayout.requestFocusEdittext();
        inputAmountLayout.setListener(this);
        String validAmount = String.format(getResources().getString(R.string.min_money), CurrencyUtil.formatCurrency(MIN_AMOUNT, false));
        tvResourceMoney.setText(validAmount);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (inputAmountLayout != null) {
            inputAmountLayout.removeListener();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showLoading() {
        super.showProgressDialog();
    }

    @Override
    public void hideLoading() {
        super.hideProgressDialog();
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        ToastUtil.showToast(getActivity(), message);
    }

    @Override
    public void onAmountChanged(CharSequence amount) {
        if (TextUtils.isEmpty(amount)) {
            btnDeposit.setBackgroundResource(R.color.bg_btn_gray);
        } else {
            btnDeposit.setBackgroundResource(R.drawable.bg_btn_green);
        }
    }

    @Override
    public void onTokenInvalid() {
        AndroidApplication.instance().sigoutAndCleanData(getActivity());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
