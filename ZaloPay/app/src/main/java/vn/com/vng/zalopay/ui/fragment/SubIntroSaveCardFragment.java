package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link SubIntroSaveCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubIntroSaveCardFragment extends BaseFragment {
    private static final String ARG_INTRO_RESOURCE = "intro_resource";

    private int mIntroResource;

    public SubIntroSaveCardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param introStep mIntroResource.
     * @return A new instance of fragment SubIntroSaveCardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SubIntroSaveCardFragment newInstance(int introStep) {
        SubIntroSaveCardFragment fragment = new SubIntroSaveCardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INTRO_RESOURCE, introStep);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {

    }

    @Override
    protected int getResLayoutId() {
        return mIntroResource;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIntroResource = getArguments().getInt(ARG_INTRO_RESOURCE);
        }
    }
}
