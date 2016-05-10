package vn.com.vng.zalopay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.Bind;
import vn.com.vng.zalopay.R;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class LinkCardFragment extends BaseFragment {


    public static LinkCardFragment newInstance() {

        Bundle args = new Bundle();

        LinkCardFragment fragment = new LinkCardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }


    @Bind(R.id.listview)
    RecyclerView recyclerView;

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_recycleview;
    }




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
