package vn.com.vng.zalopay.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import javax.inject.Inject;

import butterknife.OnClick;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.LinkCardProcedurePresenter;
import vn.com.vng.zalopay.ui.view.ILinkCardProcedureView;
import vn.com.zalopay.wallet.entity.gatewayinfo.DMappedCard;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LinkCardProcedureFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LinkCardProcedureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LinkCardProcedureFragment extends BaseFragment implements ILinkCardProcedureView {

    private OnFragmentInteractionListener mListener;

    @Inject
    Navigator navigator;

    @Inject
    LinkCardProcedurePresenter linkCardProdurePresenter;

    @OnClick(R.id.btnContinue)
    public void onClickBtnContinute(View view) {
        linkCardProdurePresenter.addLinkCard();
    }

    public LinkCardProcedureFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LinkCardProcedureFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LinkCardProcedureFragment newInstance() {

        LinkCardProcedureFragment fragment = new LinkCardProcedureFragment();
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_link_card_procedure;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linkCardProdurePresenter.setView(this);
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
        linkCardProdurePresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        linkCardProdurePresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void showLoading() {
        showProgressDialog();
    }

    @Override
    public void hideLoading() {
        hideProgressDialog();
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public void onAddCardSuccess(DMappedCard card) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.CARDNAME, card.cardname);
        bundle.putString(Constants.FIRST6CARDNO, card.first6cardno);
        bundle.putString(Constants.LAST4CARDNO, card.last4cardno);
        bundle.putString(Constants.BANKCODE, card.bankcode);
        bundle.putLong(Constants.EXPIRETIME, card.expiretime);
        intent.putExtras(bundle);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onTokenInvalid() {

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
