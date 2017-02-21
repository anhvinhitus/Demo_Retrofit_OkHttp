package vn.com.vng.zalopay.scanners.beacons;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import vn.com.vng.zalopay.scanners.ui.FragmentLifecycle;
import vn.com.vng.zalopay.service.DefaultPaymentRedirectListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.ui.widget.RippleBackground;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;

public class CounterBeaconFragment extends RuntimePermissionFragment implements FragmentLifecycle {

    private BeaconScanner beaconScanner;
    private CounterBeaconRecyclerViewAdapter mViewAdapter;

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

    @BindView(R.id.waveView)
    RippleBackground mWareWaveView;

    @BindView(R.id.tvLabel)
    View mLableView;

    private Timer timer;

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

        mPaymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(mBalanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(mTransactionRepository)
                .setResponseListener(new PaymentResponseListener())
                .setRedirectListener(new DefaultPaymentRedirectListener(navigator) {
                    @Override
                    public Object getContext() {
                        return CounterBeaconFragment.this;
                    }
                })
                .build();
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
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getUserVisibleHint()) {
            startScanning();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopScanning();
        AndroidUtils.cancelRunOnUIThread(removeDeviceExpiredRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopScanning();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mRecyclerView.setAdapter(null);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void startBeaconScanner() {
        Timber.d("startBeaconScanner");
        stopTimer();

        timer = new Timer();
        timer.scheduleAtFixedRate(new MyTimerTask(), 0, 1000);

        getAppComponent().threadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (beaconScanner != null) {
                    beaconScanner.startScan();
                }
            }
        });
    }

    private void stopBeaconScanner() {
        Timber.d("stopBeaconScanner");
        getAppComponent().threadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (beaconScanner != null) {
                    beaconScanner.stopScan();
                }
            }
        });

        stopTimer();
    }

    private void resetDeviceList() {
        mViewAdapter.removeAll();
    }

    public void startScanning() {
        starAnimation();
        if (isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            startBeaconScanner();
        } else {
            DialogHelper.showConfirmDialog(getActivity(),
                    getString(R.string.request_permission_bluetooth),
                    getString(R.string.accept),
                    getString(R.string.cancel),
                    new ZPWOnEventConfirmDialogListener() {
                        @Override
                        public void onCancelEvent() {
                        }

                        @Override
                        public void onOKevent() {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    PERMISSION_CODE.ACCESS_COARSE_LOCATION);
                        }
                    });
        }

    }

    public void stopScanning() {
        stopAnimation();
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
                mPaymentWrapper.payWithOrder(getActivity(), item.order);
            } else {
                mPaymentWrapper.payWithToken(getActivity(), item.paymentRecord.appId, item.paymentRecord.transactionToken);
            }
        }
    }

    private class BeaconListener implements BeaconScanner.BeaconListener {
        private int REQUEST_ENABLE_BT = 1;

        @Override
        public void shouldRequestEnableBluetooth() {
            if (!isAdded()) {
                return;
            }

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        @Override
        public void onDiscoverDevice(String deviceName, int rssi, PaymentRecord data) {
            if (!isAdded()) {
                return;
            }

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
            checkIfEmpty();

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
            if (!isAdded()) {
                return;
            }

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
            if (!isAdded()) {
                return;
            }
            
            Timber.i("Error in getting order information for transaction %s", device.paymentRecord.transactionToken);
            OrderCache cache = mTransactionCache.get(device.paymentRecord.transactionToken);
            cache.status = OrderCache.STATUS_ERROR;
            mTransactionCache.put(device.paymentRecord.transactionToken, cache);
        }
    }

    private static class OrderCache {
        static final int STATUS_ERROR = 48;
        static final int STATUS_CACHED = 49;
        static final int STATUS_FETCHING = 50;
        static final int STATUS_EMPTY = 51;

        public Order order;
        public int status;
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            AndroidUtils.runOnUIThread(removeDeviceExpiredRunnable);
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
                checkIfEmpty();
            }
        }
    };

    @Override
    public void onStartFragment() {
        startScanning();
    }

    @Override
    public void onStopFragment() {
        stopScanning();
    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {
        switch (permissionRequestCode) {
            case PERMISSION_CODE.ACCESS_COARSE_LOCATION:
                handleGrantedAccessCoarseLocation(isGranted);
                break;
        }
    }

    private void handleGrantedAccessCoarseLocation(boolean isGranted) {
        if (isGranted) {
            startBeaconScanner();
        } else {
            DialogHelper.showConfirmDialog(getActivity(),
                    getString(R.string.deny_permission_bluetooth),
                    getString(R.string.accept),
                    null,
                    null);
        }
    }

    private class PaymentResponseListener implements PaymentWrapper.IResponseListener {
        @Override
        public void onParameterError(String param) {
//                        showToast("Error in parameter: " + param);
            beaconScanner.startScan();
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            Timber.d("Payment error: %s", paymentError.value());
            beaconScanner.startScan();
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            CounterBeaconFragment.this.getActivity().finish();
        }

        @Override
        public void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId) {

        }

        @Override
        public void onResponseTokenInvalid() {
            Timber.d("Invalid token");
        }

        @Override
        public void onAppError(String msg) {
            Timber.d("onAppError msg [%s]", msg);
            showToast(getString(R.string.exception_generic));
            beaconScanner.startScan();
        }

    }

    private void stopAnimation() {
        if (mWareWaveView != null) {
            mWareWaveView.stopRippleAnimation();
        }
    }

    private void starAnimation() {
        if (mWareWaveView != null) {
            mWareWaveView.startRippleAnimation();
        }
    }

    private void checkIfEmpty() {
        if (mViewAdapter == null ||
                mWareWaveView == null
                || mLableView == null) {
            return;
        }

        if (mViewAdapter.getItemCount() == 0) {
            mWareWaveView.setVisibility(View.VISIBLE);
            mLableView.setVisibility(View.VISIBLE);
        } else {
            mWareWaveView.setVisibility(View.INVISIBLE);
            mLableView.setVisibility(View.INVISIBLE);
        }
    }
}
