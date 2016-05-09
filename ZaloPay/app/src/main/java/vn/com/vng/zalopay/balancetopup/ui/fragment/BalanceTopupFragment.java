package vn.com.vng.zalopay.balancetopup.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.balancetopup.ui.activity.ConfirmTransactionActivity;
import vn.com.vng.zalopay.balancetopup.ui.widget.InputAmountLayout;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BalanceTopupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BalanceTopupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceTopupFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match

    private OnFragmentInteractionListener mListener;

    @Bind(R.id.list)
    RecyclerView recyclerView;

    @Bind(R.id.tvResourceMoney)
    TextView tvResourceMoney;

    @Bind(R.id.inputAmountLayout)
    InputAmountLayout inputAmountLayout;

    @OnClick(R.id.btnContinue)
    public void onBtnContinueClick(View view) {
        Timber.tag(TAG).d("onBtnContinueClick............");
        gotoConfirmTransaction();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
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
