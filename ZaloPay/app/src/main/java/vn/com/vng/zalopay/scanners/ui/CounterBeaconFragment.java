package vn.com.vng.zalopay.scanners.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.scanners.controller.BeaconScanner;
import vn.com.vng.zalopay.scanners.controller.PaymentRecord;
import vn.com.vng.zalopay.scanners.ui.beacon.BeaconDevice;
import vn.com.vng.zalopay.scanners.ui.dummy.DummyContent;
import vn.com.vng.zalopay.scanners.ui.dummy.DummyContent.DummyItem;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CounterBeaconFragment extends BaseFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private MenuItem fav;

    private BeaconScanner beaconScanner;
    private CounterBeaconRecyclerViewAdapter mViewAdapter;
    private Handler mMainLooperHandler;

    private final List<BeaconDevice> mDeviceList = new ArrayList<>();
    private PaymentWrapper mPaymentWrapper;
    private final HashMap<String, OrderCache> mTransactionCache = new HashMap<>();

    @Inject
    ZaloPayRepository zaloPayRepository;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CounterBeaconFragment() {
        beaconScanner = new BeaconScanner(new BeaconListener());
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CounterBeaconFragment newInstance(int columnCount) {
        CounterBeaconFragment fragment = new CounterBeaconFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);

        if (!beaconScanner.initialize(this.getActivity())) {
            showToast("Cannot initialize BLE");
            return;
        }

        mPaymentWrapper = new PaymentWrapper(zaloPayRepository,
                new PaymentWrapper.IViewListener() {
                    @Override
                    public Activity getActivity() {
                        return CounterBeaconFragment.this.getActivity();
                    }
                },
                new PaymentWrapper.IResponseListener() {
                    @Override
                    public void onParameterError(String param) {
                        showToast("Error in parameter: " + param);
                        beaconScanner.startScan();
                    }

                    @Override
                    public void onResponseError(int status) {
                        Timber.d("Payment error: " + status);
                        showToast("Error code: " + String.valueOf(status));
                        beaconScanner.startScan();
                    }

                    @Override
                    public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                        zaloPayRepository.transactionUpdate();
                        CounterBeaconFragment.this.getActivity().finish();
                    }

                    @Override
                    public void onResponseTokenInvalid() {
                        Timber.d("Invalid token");
                    }

                    @Override
                    public void onResponseCancel() {
                        Timber.d("User cancel transaction");
                        beaconScanner.startScan();
                    }
                }
        );
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_counterbeacon_list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);

        fav = menu.add("refresh");
        fav.setIcon(R.drawable.ic_more_horiz);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mViewAdapter = new CounterBeaconRecyclerViewAdapter(mDeviceList, new SelectDeviceListener());
            recyclerView.setAdapter(mViewAdapter);
        }
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == fav.getItemId()) {
            Timber.d("Reload list beacon");
            resetDeviceList();
            beaconScanner.startScan();
            return true;
        }

        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainLooperHandler = new Handler(context.getMainLooper());
        mMainLooperHandler.post(new Runnable() {
            @Override
            public void run() {
                beaconScanner.startScan();
            }
        });
//        if (context instanceof OnListFragmentInteractionListener) {
//            mListener = (OnListFragmentInteractionListener) context;
//        } else {
//            Timber.w("Activity: %s", context);
//            throw new RuntimeException(context.toString()
//                    + " must implement OnListFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mMainLooperHandler = null;
        beaconScanner.stopScan();
    }

    @Override
    public void onPause() {
        super.onPause();

        beaconScanner.stopScan();
    }

    @Override
    public void onResume() {
        super.onResume();

        beaconScanner.startScan();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            beaconScanner.stopScan();
        } else {
            beaconScanner.startScan();
        }
    }

    private void resetDeviceList() {
        mDeviceList.clear();
        mMainLooperHandler.post(updateDatasetRunnable);
    }

    private final Runnable updateDatasetRunnable = new Runnable() {
        @Override
        public void run() {
            mViewAdapter.notifyDataSetChanged();
        }
    };

    private class SelectDeviceListener implements OnListFragmentInteractionListener {
        @Override
        public void onListFragmentInteraction(BeaconDevice item) {
            if (item.paymentRecord == null) {
                return;
            }
            getAppComponent().monitorTiming().finishEvent(MonitorEvents.BLE_SCANNING);

            beaconScanner.stopScan();
            if (item.order != null) {
                mPaymentWrapper.payWithOrder(item.order);
            } else {
                mPaymentWrapper.payWithToken(item.paymentRecord.appId, item.paymentRecord.transactionToken);
            }
        }
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
        void onListFragmentInteraction(BeaconDevice item);
    }

    private class BeaconListener implements BeaconScanner.BeaconListener {
        private int REQUEST_ENABLE_BT = 1;

        @Override
        public void shouldRequestEnableBluetooth() {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }

        @Override
        public void onDiscoverDevice(String deviceName, int rssi, PaymentRecord data) {
            String title = deviceName;
            if (deviceName == null) {
                title = "<NULL>";
            }

            Timber.d("Found device: %s - rssi: %d", title, rssi);

            OrderCache cache = mTransactionCache.get(data.transactionToken);
            Order order = null;
            if (cache != null) {
                order = cache.order;
            }
            BeaconDevice device = new BeaconDevice(title, rssi, data, order);
            if (cache == null) {
                Timber.i("Start fetching order information for %s", data.transactionToken);
                cache = new OrderCache();
                cache.status = OrderCache.STATUS_FETCHING;
                mTransactionCache.put(data.transactionToken, cache);
                // fetch order info
                mPaymentWrapper.getOrder(data.appId, data.transactionToken, new GetOrderCallback(device));
            }

            if (mDeviceList.contains(device)) {
                Timber.d("Replace existing device");
                int position = mDeviceList.indexOf(device);
                mDeviceList.set(position, device);
            } else {
                Timber.d("Add new device");
                mDeviceList.add(device);
            }

            if (mMainLooperHandler != null) {
                mMainLooperHandler.post(updateDatasetRunnable);
            }
        }

        @Override
        public void onScanningStarted() {
//            showToast("Scanning Started");
        }

        @Override
        public void onScanningStopped() {
//            showToast("Scanning Stopped");
        }
    }

    private class GetOrderCallback implements PaymentWrapper.IGetOrderCallback {
        private final BeaconDevice device;

        public GetOrderCallback(BeaconDevice device) {
            this.device = device;
        }

        @Override
        public void onResponseSuccess(Order order) {
            Timber.i("Got order information for transaction %s", device.paymentRecord.transactionToken);
            OrderCache cache = mTransactionCache.get(device.paymentRecord.transactionToken);
            cache.status = OrderCache.STATUS_CACHED;
            cache.order = order;
            mTransactionCache.put(device.paymentRecord.transactionToken, cache);
            BeaconDevice device = this.device.cloneWithOrder(order);
            int position = mDeviceList.indexOf(device);
            mDeviceList.set(position, device);
            if (mMainLooperHandler != null) {
                mMainLooperHandler.post(updateDatasetRunnable);
            }
        }

        @Override
        public void onResponseError(int status) {
            Timber.i("Error in getting order information for transaction %s", device.paymentRecord.transactionToken);
            OrderCache cache = mTransactionCache.get(device.paymentRecord.transactionToken);
            cache.status = OrderCache.STATUS_ERROR;
            mTransactionCache.put(device.paymentRecord.transactionToken, cache);
        }
    }

    private class OrderCache {
        public static final int STATUS_ERROR = 48;
        public static final int STATUS_CACHED = 49;
        public static final int STATUS_FETCHING = 50;
        public static final int STATUS_EMPTY = 51;

        public Order order;
        public int status;
    }
}
