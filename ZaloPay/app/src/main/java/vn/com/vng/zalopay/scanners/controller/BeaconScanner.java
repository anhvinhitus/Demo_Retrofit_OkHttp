package vn.com.vng.zalopay.scanners.controller;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by huuhoa on 6/3/16.
 * Beacon scanner
 */
public class BeaconScanner {
    public interface BeaconListener {
        void shouldRequestEnableBluetooth();
        void onDiscoverDevice(String deviceName, int rssi, PaymentRecord data);

        void onScanningStarted();
        void onScanningStopped();
    }

    private BluetoothAdapter mBluetoothAdapter;

    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BeaconListener mListener;
    private boolean initialized = false;
    private static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF;

    public BeaconScanner(BeaconListener listener) {
        mListener = listener;
    }

    public boolean initialize(Context context) {
        mHandler = new Handler();
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Timber.w("BLE Not Supported");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            return false;
        }

        initialized = true;
        return true;
    }

    public void startScan() {
        if (!initialized) {
            return;
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mListener.shouldRequestEnableBluetooth();
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
        }
    }

    public void stopScan() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            return;
        }

        scanLeDevice(false);
    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
//            scanLeDevice(false);
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (mGatt == null) {
//            return;
//        }
//        mGatt.close();
//        mGatt = null;
//        super.onDestroy();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_ENABLE_BT) {
//            if (resultCode == Activity.RESULT_CANCELED) {
//                //Bluetooth not enabled.
//                finish();
//                return;
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    private void scanLeDevice(final boolean enable) {
        if (Build.VERSION.SDK_INT < 18) {
            return;
        }

        if (Build.VERSION.SDK_INT < 21) {
            if (mBluetoothAdapter == null) {
                Timber.w("Trying to control BLE scanning without proper initializing");
                return;
            }
        } else {
            if (mLEScanner == null) {
                Timber.w("Trying to control BLE scanning without proper initializing");
                return;
            }
        }

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 18) {
                        return;
                    }

                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback18);
                    } else {
                        mLEScanner.stopScan(mLeScanCallback21);
                    }

                    mListener.onScanningStopped();
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback18);
            } else {
                mLEScanner.startScan(filters, settings, mLeScanCallback21);
            }

            mListener.onScanningStarted();
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback18);
            } else {
                mLEScanner.stopScan(mLeScanCallback21);
            }

            mListener.onScanningStopped();
        }
    }


    private ScanCallback mLeScanCallback21 = new LeScanCallback21();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class LeScanCallback21 extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Timber.i("callbackType: %s", String.valueOf(callbackType));
            Timber.i("result: %s", result.toString());

            BluetoothDevice btDevice = result.getDevice();
            PaymentRecord paymentRecord = parseScanRecord(result.getScanRecord());
            mListener.onDiscoverDevice(btDevice.getAddress(), result.getRssi(), paymentRecord);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Timber.i("ScanResult - Results: %s", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Timber.e("Scan Failed with Error Code: %d", errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback18 = new LeScanCallback18();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private class LeScanCallback18 implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            Timber.i("onLeScan: %s", device.toString());
            PaymentRecord paymentRecord = parseScanRecord(scanRecord);
            mListener.onDiscoverDevice(device.getAddress(), rssi, paymentRecord);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private PaymentRecord parseScanRecord(ScanRecord scanRecord) {
        if (scanRecord == null) {
            Timber.d("Invalid scanRecord. Null record");
            return null;
        }

        try {
            SparseArray<byte[]> manufacturerSpecificData = scanRecord.getManufacturerSpecificData();
            return getPaymentRecord(manufacturerSpecificData);
        } catch (Exception e) {
            Timber.e(e, "unable to parse scan record: %s", scanRecord);
            return null;
        }
    }

    private PaymentRecord parseScanRecord(byte[] scanRecord) {
        if (scanRecord == null) {
            Timber.d("Invalid scanRecord. Null record");
            return null;
        }

        try {
            SparseArray<byte[]> manufacturerSpecificData = splitScanRecord(scanRecord);
            return getPaymentRecord(manufacturerSpecificData);
        } catch (Exception e) {
            Timber.e(e, "unable to parse scan record: %s", Arrays.toString(scanRecord));
            return null;
        }
    }

    @Nullable
    private PaymentRecord getPaymentRecord(SparseArray<byte[]> manufacturerSpecificData) {
        if (manufacturerSpecificData == null) {
            Timber.d("Invalid scanRecord. No manufacturer specific data.");
            return null;
        }
        
        byte[] data = manufacturerSpecificData.get(0x1710);
        if (data == null) {
            Timber.d("Invalid scanRecord. Specific data not found.");
            return null;
        }

        int currentPos = 0;

        // The first two bytes of the manufacturer specific data are
        // manufacturer ids in little endian.

        long manufacturerId = extractShort(data, currentPos);
        currentPos += 2;
        long amount = 0;
//            long amount = extractLong(data, currentPos);
//            currentPos += 4;
        long appid = extractLong(data, currentPos);
        currentPos += 4;
        byte[] transactionTokenBytes = extractBytes(data, currentPos, 16);
        String transactionToken = Base64.encodeToString(transactionTokenBytes, Base64.DEFAULT);
        currentPos += 16;
        long crc16 = extractShort(data, currentPos);

        PaymentRecord paymentRecord = new PaymentRecord(manufacturerId, amount, appid, transactionToken, crc16);
        Timber.d("Found payment record: %s", paymentRecord);
        return paymentRecord;
    }

    SparseArray<byte[]> splitScanRecord(byte[] scanRecord) {
        if (scanRecord == null) {
            return null;
        }

        int currentPos = 0;
        SparseArray<byte[]> manufacturerData = new SparseArray<>();

        try {
            while (currentPos < scanRecord.length) {
                // length is unsigned int.
                int length = scanRecord[currentPos++] & 0xFF;
                if (length == 0) {
                    break;
                }
                // Note the length includes the length of the field type itself.
                int dataLength = length - 1;
                // fieldType is unsigned int.
                int fieldType = scanRecord[currentPos++] & 0xFF;
                switch (fieldType) {
                    case DATA_TYPE_MANUFACTURER_SPECIFIC_DATA:
                        // The first two bytes of the manufacturer specific data are
                        // manufacturer ids in little endian.
                        int manufacturerId = ((scanRecord[currentPos + 1] & 0xFF) << 8) +
                                (scanRecord[currentPos] & 0xFF);
                        byte[] manufacturerDataBytes = extractBytes(scanRecord, currentPos + 2,
                                dataLength - 2);
                        manufacturerData.put(manufacturerId, manufacturerDataBytes);
                        break;
                    default:
                        // Just ignore, we don't handle such data type.
                        break;
                }
                currentPos += dataLength;
            }

            return manufacturerData;
        } catch (Exception e) {
            Timber.e(e, "unable to parse scan record: " + Arrays.toString(scanRecord));
            // As the record is invalid, ignore all the parsed results for this packet
            // and return an empty record with raw scanRecord bytes in results
            return null;
        }
    }

    // Helper method to extract bytes from byte array.
    private static byte[] extractBytes(byte[] scanRecord, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }

    private short extractShort(byte[] scanRecord, int start) {
        long shortValue = scanRecord[start] & 0xFF;
        shortValue += (scanRecord[start + 1] & 0xFF) << 8;
        return (short)shortValue;
    }

    private long extractLong(byte[] scanRecord, int start) {
        long longValue = scanRecord[start] & 0xFF;
        longValue += (scanRecord[start + 1] & 0xFF) << 8;
        longValue += (scanRecord[start + 2] & 0xFF) << 16;
        longValue += (scanRecord[start + 3] & 0xFF) << 24;
        return longValue;
    }
}
