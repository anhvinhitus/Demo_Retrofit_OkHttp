package vn.com.vng.zalopay.account.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.presenter.PreProfilePresenter;
import vn.com.vng.zalopay.account.ui.view.IPreProfileView;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PreProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PreProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreProfileFragment extends BaseFragment implements IPreProfileView {
    private OnFragmentInteractionListener mListener;

    @Inject
    PreProfilePresenter presenter;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    @BindView(R.id.tvSex)
    TextView tvSex;

    @BindView(R.id.tvBirthday)
    TextView tvBirthday;

    @Bind(R.id.tv_name)
    TextView tvName;

    @Bind(R.id.tvTermsOfUser)
    TextView tvTermsOfUser;

    public PreProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PreProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PreProfileFragment newInstance() {
        PreProfileFragment fragment = new PreProfileFragment();
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_pre_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setView(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    public void updateUserInfo(User user) {
        if (user == null) {
            return;
        }
        Date date = new Date(user.birthDate*1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        tvBirthday.setText(simpleDateFormat.format(date));
        tvName.setText(user.dname);
        tvSex.setText(user.getGender());
        Glide.with(this).load(user.avatar)
                .placeholder(R.color.silver)
                .centerCrop()
                .into(imgAvatar);
        tvTermsOfUser.setClickable(true);
        tvTermsOfUser.setMovementMethod (LinkMovementMethod.getInstance());
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
        super.showToast(message);
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
