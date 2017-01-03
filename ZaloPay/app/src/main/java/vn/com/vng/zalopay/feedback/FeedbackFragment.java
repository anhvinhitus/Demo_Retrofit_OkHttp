package vn.com.vng.zalopay.feedback;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.fragment.AbsPickerImageFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

public class FeedbackFragment extends BaseFragment implements
        FeedbackAdapter.OnClickAddListener, FeedbackAdapter.OnClickDeleteListener {

    private FeedbackAdapter mAdapter;

    @BindView(R.id.txtTitleImage)
    TextView tvTitleImage;
    @BindView(R.id.listView)
    RecyclerView mRecyclerView;

    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

    @Override
    protected void setupFragmentComponent() {
//        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_send_feedback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAdapter = new FeedbackAdapter(getContext(), this, this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        List<Integer> tmp = new ArrayList<>();
//        tmp.add(R.drawable.ic_agribank);
//        tmp.add(R.drawable.ic_bidv);
//        tmp.add(R.drawable.ic_chungchi);
//        tmp.add(R.drawable.ic_eximbank);
        setData(tmp);
    }

    @Override
    public void onDestroyView() {
//        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
//        mPresenter.destroy();
        mAdapter = null;
        super.onDestroy();
    }

    public void setData(List<Integer> images) {
        mAdapter.setData(images);
        setImageCount();
    }

    public void updateData(Integer image) {
        mAdapter.insert(image);
        setImageCount();
    }

    public void removeData(int position) {
        mAdapter.remove(position);
        setImageCount();
    }

    @Override
    public void onClickAdd() {
        showBottomSheetDialog();
    }

    @Override
    public void onClickDelete(int position) {
        removeData(position);
    }

    private void showBottomSheetDialog() {
        AbsPickerImageFragment.CoverBottomSheetDialogFragment dialog = AbsPickerImageFragment.CoverBottomSheetDialogFragment.newInstance();
        dialog.setOnClickListener(new AbsPickerImageFragment.CoverBottomSheetDialogFragment.OnClickListener() {
            @Override
            public void onClickCapture() {
//                startCaptureImage(requestCode, getImageNameFromReqCode(requestCode));
            }

            @Override
            public void onClickGallery() {
//                startPickImage(requestCode);
            }
        });
        dialog.show(getChildFragmentManager(), "bottomsheet");
    }

    private void setImageCount() {
        int size = mAdapter.getItemCount() - 1;
        if(mAdapter.getItemViewType(size) == 0) {
            size = size + 1;
        }
        tvTitleImage.setText(String.format(getResources().getString(R.string.txt_attach_screen),
                String.valueOf(size)));
    }
}
