package vn.com.vng.zalopay.scanners.beacons;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.scanners.models.PaymentRecord;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;

public class CounterBeaconFragment extends BaseFragment {

    private BeaconScanner beaconScanner;
    private CounterBeaconRecyclerViewAdapter mViewAdapter;
    private Handler mMainLooperHandler;

    private PaymentWrapper mPaymentWrapper;
    private final HashMap<String, OrderCache> mTransactionCache = new HashMap<>();

    @Inject
    ZaloPayRepository zaloPayRepository;

    @Inject
    TransactionStore.Repository mTransactionRepository;

    @Inject
    BalanceStore.Repository mBalanceRepository;


    @BindView(R.id.beaconList)
    RecyclerView mRecyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CounterBeaconFragment() {
        beaconScanner = new BeaconScanner(new BeaconListener());
    }

    public static CounterBeaconFragment newInstance() {
        CounterBeaconFragment fragment = new CounterBeaconFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        Timber.d("Begin setupFragmentComponent");
        getUserComponent().inject(this);

        if (!beaconScanner.initialize(this.getActivity())) {
            showToast("Không thể khởi động Bluetooth");
            return;
        }

        mPaymentWrapper = new PaymentWrapper(mBalanceRepository,
                zaloPayRepository, mTransactionRepository,
                new PaymentWrapper.IViewListener() {
                    @Override
                    public Activity getActivity() {
                        return CounterBeaconFragment.this.getActivity();
                    }
                },
                new PaymentWrapper.IResponseListener() {
                    @Override
                    public void onParameterError(String param) {
//                        showToast("Error in parameter: " + param);
                        beaconScanner.startScan();
                    }

                    @Override
                    public void onResponseError(PaymentError paymentError) {
                        Timber.d("Payment error: " + paymentError.value());
                        beaconScanner.startScan();
                    }

                    @Override
                    public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                        CounterBeaconFragment.this.getActivity().finish();
                    }

                    @Override
                    public void onResponseTokenInvalid() {
                        Timber.d("Invalid token");
                    }

                    @Override
                    public void onAppError(String msg) {
                        Timber.d("onAppError msg[%s]", msg);
                        showToast(getString(R.string.exception_generic));
                        beaconScanner.startScan();
                    }

                    @Override
                    public void onNotEnoughMoney() {
                        navigator.startDepositActivity(CounterBeaconFragment.this.getContext());
                    }
                }
        );
        Timber.d("Finish setupFragmentComponent");
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_counterbeacon_list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewAdapter = new CounterBeaconRecyclerViewAdapter(getContext(), new SelectDeviceListener());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mViewAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mMainLooperHandler = new Handler(context.getMainLooper());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopBeaconScanner();
        mMainLooperHandler.removeCallbacks(removeDeviceExpiredRunnable);
        mMainLooperHandler = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBeaconScanner();
    }

    @Override
    public void onResume() {
        super.onResume();
//        startBeaconScanner();
    }

    private Timer timer;

    private void startBeaconScanner() {
        if (timer == null) {
            timer = new Timer();
        } else {
            timer.cancel();
            timer = new Timer();
        }

        try {
            timer.scheduleAtFixedRate(new MyTimerTask(), 0, 1000);
        } catch (Exception e) {
            Timber.e(e, "Exception");
        }
        getAppComponent().threadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                beaconScanner.startScan();
            }
        });
    }

    private void stopBeaconScanner() {
        getAppComponent().threadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (beaconScanner != null) {
                    beaconScanner.stopScan();
                }
            }
        });
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void resetDeviceList() {
        mViewAdapter.removeAll();
    }

    public void startScanning() {
        startBeaconScanner();
    }

    public void stopScanning() {
        stopBeaconScanner();
    }

    private class SelectDeviceListener implements CounterBeaconRecyclerViewAdapter.OnClickBeaconDeviceListener {
        @Override
        public void onClickBeaconListener(BeaconDevice item) {
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

    private class BeaconListener implements BeaconScanner.BeaconListener {
        private int REQUEST_ENABLE_BT = 1;

        @Override
        public void shouldRequestEnableBluetooth() {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }

        @Override
        public void onDiscoverDevice(String deviceName, int rssi, PaymentRecord data) {

            Timber.d(" threadName %s", Thread.currentThread().getName());

            String title = deviceName;
            if (deviceName == null) {
                title = "<NULL>";
            }

            Timber.v("Found device: %s - rssi: %d", title, rssi);

            OrderCache cache = mTransactionCache.get(data.transactionToken);
            Order order = null;
            if (cache != null) {
                order = cache.order;
            }
            BeaconDevice device = new BeaconDevice(title, rssi, data, order);
            if (cache == null) {
                Timber.i("Start fetching order information for [%s]", data.transactionToken);
                cache = new OrderCache();
                cache.status = OrderCache.STATUS_FETCHING;
                mTransactionCache.put(data.transactionToken, cache);
                // fetch order info
                mPaymentWrapper.getOrder(data.appId, data.transactionToken, new GetOrderCallback(device));
            }

            mViewAdapter.insertOrReplace(device);

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

            Timber.d("threadName %s", Thread.currentThread().getName());

            Timber.i("Got order information for transaction %s", device.paymentRecord.transactionToken);
            OrderCache cache = mTransactionCache.get(device.paymentRecord.transactionToken);
            cache.status = OrderCache.STATUS_CACHED;
            cache.order = order;
            mTransactionCache.put(device.paymentRecord.transactionToken, cache);

            int position = mViewAdapter.getItems().indexOf(device);
            if (position >= 0) {
                BeaconDevice device = mViewAdapter.getItem(position);
                device.order = order;
                mViewAdapter.notifyItemChanged(position);
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

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            mMainLooperHandler.post(removeDeviceExpiredRunnable);
        }
    }

    private final Runnable removeDeviceExpiredRunnable = new Runnable() {
        @Override
        public void run() {
            List<BeaconDevice> expiredList = new ArrayList<>();
            for (BeaconDevice device : mViewAdapter.getItems()) {
                if (device.isExpired()) {
                    expiredList.add(device);
                }
            }

            if (!expiredList.isEmpty()) {
                mViewAdapter.removeAll(expiredList);
            }
        }
    };


}
