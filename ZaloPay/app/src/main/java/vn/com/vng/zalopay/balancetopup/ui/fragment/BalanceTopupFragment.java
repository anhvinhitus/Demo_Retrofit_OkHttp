package vn.com.vng.zalopay.balancetopup.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.balancetopup.ui.activity.ConfirmTransactionActivity;
import vn.com.vng.zalopay.balancetopup.ui.view.IBalanceTopupView;
import vn.com.vng.zalopay.balancetopup.ui.widget.InputAmountLayout;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BalanceTopupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BalanceTopupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceTopupFragment extends BaseFragment implements IBalanceTopupView {
    // TODO: Rename parameter arguments, choose names that match

    private OnFragmentInteractionListener mListener;

    @Inject
    BalanceTopupPresenter balanceTopupPresenter;

    @Bind(R.id.tvResourceMoney)
    TextView tvResourceMoney;

    @Bind(R.id.inputAmountLayout)
    InputAmountLayout inputAmountLayout;

    @OnClick(R.id.btnDeposit)
    public void onClickDeposit() {
        showProgressDialog();
        balanceTopupPresenter.deposit(inputAmountLayout.getText().toString());
    }

    private void gotoConfirmTransaction() {
        Intent intent = new Intent(getContext(), ConfirmTransactionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.ARG_AMOUNT, Long.valueOf(inputAmountLayout.getText().toString()));
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
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        balanceTopupPresenter.setView(this);
        inputAmountLayout.requestFocusEdittext();
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
        showError(message);
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
