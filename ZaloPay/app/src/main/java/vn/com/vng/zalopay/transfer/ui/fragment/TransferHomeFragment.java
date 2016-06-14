package vn.com.vng.zalopay.transfer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.model.TransferRecentContentProvider;
import vn.com.vng.zalopay.data.cache.model.TransferRecentDao;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.models.TransferRecent;
import vn.com.vng.zalopay.transfer.provider.TransferRecentContentProviderImpl;
import vn.com.vng.zalopay.transfer.ui.adapter.TransferRecentRecyclerViewAdapter;
import vn.com.vng.zalopay.transfer.ui.fragment.dummy.DummyContent;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class TransferHomeFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, TransferRecentRecyclerViewAdapter.OnTransferRecentItemListener {
    private final int LOADER_TRANSACTION_RECENT = 1;
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private TransferRecentRecyclerViewAdapter mAdapter;

    @Inject
    Navigator navigator;

    @BindView(R.id.tvTileTransactionRecent)
    View mTvTileTransactionRecent;

    @BindView(R.id.list)
    RecyclerView mList;

    @OnClick(R.id.layoutTransferAccZaloPay)
    public void onClickTransferAccZaloPay(View view) {
        navigator.startZaloContactActivity(this);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TransferHomeFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TransferHomeFragment newInstance(int columnCount) {
        TransferHomeFragment fragment = new TransferHomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_transfer_home;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set the adapter
        if (mColumnCount <= 1) {
            mList.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            mList.setLayoutManager(new GridLayoutManager(getContext(), mColumnCount));
        }
        mAdapter = new TransferRecentRecyclerViewAdapter(getContext(), new ArrayList<TransferRecent>(), this);
        mList.setAdapter(mAdapter);
        if (mAdapter != null && mAdapter.getItemCount() > 0) {
            mTvTileTransactionRecent.setVisibility(View.VISIBLE);
        } else {
            mTvTileTransactionRecent.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_TRANSACTION_RECENT, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnListFragmentInteractionListener");
        }
        getLoaderManager().initLoader(LOADER_TRANSACTION_RECENT, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(LOADER_TRANSACTION_RECENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == vn.com.vng.zalopay.Constants.REQUEST_CODE_TRANSFER) {
            if (resultCode == Activity.RESULT_OK) {
                getActivity().setResult(Activity.RESULT_OK, null);
                getActivity().finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String orderByWithLimit = TransferRecentDao.Properties.Id.columnName + " DESC" + " LIMIT 3";
        return new CursorLoader(getActivity(), TransferRecentContentProviderImpl.CONTENT_URI, null, null, null, orderByWithLimit);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        List<TransferRecent> transferRecents = new ArrayList<>();
        if (cursor == null || cursor.getCount() <= 0) {
            return;
        }
        if (cursor.moveToFirst()) {
            do {
                TransferRecent item = new TransferRecent(cursor);
                if (item != null) {
                    transferRecents.add(item);
                }
            } while (cursor.moveToNext());
        }
        if (transferRecents == null || transferRecents.size() <= 0) {
            onGetDataDBEmpty();
        } else {
            onGetDataDBSuccess(transferRecents);
        }
    }

    private void onGetDataDBSuccess(List<TransferRecent> transferRecents) {
        if (mAdapter == null) {
            return;
        }
        mAdapter.setData(transferRecents);
        if (transferRecents != null && transferRecents.size() > 0) {
            mTvTileTransactionRecent.setVisibility(View.VISIBLE);
        } else {
            mTvTileTransactionRecent.setVisibility(View.GONE);
        }
    }

    private void onGetDataDBEmpty() {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(TransferRecent item) {
        if (item == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(vn.com.vng.zalopay.Constants.ARG_TRANSFERRECENT, item);
        navigator.startTrasferActivity(this, bundle);
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(TransferRecent item);
    }
}
