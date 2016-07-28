package vn.com.vng.zalopay.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.adapter.ParticipatedBankRecyclerAdapter;
import vn.com.vng.zalopay.ui.presenter.LinkCardProcedurePresenter;
import vn.com.vng.zalopay.ui.view.ILinkCardProcedureView;
import vn.com.vng.zalopay.utils.BankCardUtil;
import vn.com.zalopay.wallet.entity.gatewayinfo.DMappedCard;
import vn.vng.uicomponent.widget.recyclerview.GridAutoFitLayoutManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link LinkCardProcedureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LinkCardProcedureFragment extends BaseFragment implements ILinkCardProcedureView {

    private ParticipatedBankRecyclerAdapter mAdapter;

    @Inject
    Navigator navigator;

    @Inject
    LinkCardProcedurePresenter linkCardProdurePresenter;

    @BindView(R.id.listView)
    RecyclerView mRecyclerView;

    @OnClick(R.id.btnContinue)
    public void onClickBtnContinue() {
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
        return new LinkCardProcedureFragment();
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
        initPartcipateBanks();
    }

    private void initPartcipateBanks() {
        int participatedBankWidth = (int) getResources().getDimension(R.dimen.ic_partcipated_bank_width);
        GridAutoFitLayoutManager layoutManager = new GridAutoFitLayoutManager(getContext(), participatedBankWidth);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ParticipatedBankRecyclerAdapter(getContext(), BankCardUtil.PARTICIPATE_BANK_ICONS);
        mRecyclerView.setAdapter(mAdapter);
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
}
