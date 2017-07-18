package vn.com.zalopay.wallet.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkSelectedChannelMessage;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.RenderHelper;
import vn.com.zalopay.wallet.pay.PayProxy;
import vn.com.zalopay.wallet.ui.channellist.ChannelListAdapter;
import vn.com.zalopay.wallet.view.adapter.RecyclerTouchListener;

import static vn.com.zalopay.wallet.constants.Constants.AMOUNT_EXTRA;
import static vn.com.zalopay.wallet.constants.Constants.BANKCODE_EXTRA;
import static vn.com.zalopay.wallet.constants.Constants.BUTTON_LEFT_TEXT_EXTRA;
import static vn.com.zalopay.wallet.constants.Constants.CARDNUMBER_EXTRA;
import static vn.com.zalopay.wallet.constants.Constants.NOTICE_CONTENT_EXTRA;
import static vn.com.zalopay.wallet.ui.channellist.ChannelListAdapter.ItemType.MAP;

/**
 * Created by lytm on 17/07/2017.
 */

public class MapBankDialogFragment extends BaseDialogFragment {
    public static final String TAG = "MapBankDialogFragment";
    protected String mCloseButtonText = null;
    protected String mBankCode = null;
    protected String mCardNumber = null;
    protected String mContent = null;
    protected double orderAmount;
    protected TextView mContentTextView;
    protected View mSelectButton, mSelectOtherButton;
    protected List<PaymentChannel> mChannelList;
    private RecyclerView mChannelRecyclerView;
    private EventBus mBus;
    private WeakReference<ZPWResultCallBackListener> mZPZpwResultCallBackListener;

    private void setDialogListener(ZPWResultCallBackListener pListener) {
        mZPZpwResultCallBackListener = new WeakReference<>(pListener);
    }

    public static BaseDialogFragment newInstance(Bundle args, ZPWResultCallBackListener pListener) {
        MapBankDialogFragment fragment = new MapBankDialogFragment();
        fragment.setArguments(args);
        fragment.setDialogListener(pListener);
        return fragment;
    }

    @Override
    protected int getWidthLayout() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float percentWitdh = getResources().getInteger(R.integer.dialog_percent_ondefault);
        if (SdkUtils.isTablet(getActivity().getApplicationContext())) {
            percentWitdh = getResources().getInteger(R.integer.dialog_percent_ontablet);
        }
        return (int)  (metrics.widthPixels * percentWitdh / 100);
    }

    @Override
    protected void getArgument() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCloseButtonText = bundle.getString(BUTTON_LEFT_TEXT_EXTRA);
            mBankCode = bundle.getString(BANKCODE_EXTRA);
            mCardNumber = bundle.getString(CARDNUMBER_EXTRA);
            mContent = bundle.getString(NOTICE_CONTENT_EXTRA);
            orderAmount = bundle.getDouble(AMOUNT_EXTRA);
        }
    }
    @Override
    protected void initData() {
        try {
            mChannelList = getMapChannel();
            setupRecycler(mChannelList);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(getLayout(), container, false);
        initViews(v);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mBus = SDKApplication.getApplicationComponent().eventBus();
    }

    protected List<PaymentChannel> getMapChannel() throws Exception {
        List<PaymentChannel> channelList = new ArrayList<>();
        List<Object> objectList = new ArrayList<>();
        try {
            objectList = PayProxy.get().getChannels();
        } catch (Exception e) {
            Log.e(this, e);
        }
        for (int i = 0; i < objectList.size(); i++) {
            Object object = objectList.get(i);
            if (object instanceof PaymentChannel) {
                PaymentChannel channel = (PaymentChannel) object;
                channel.position = i;
                if (BankAccountHelper.isBankAccount(mBankCode) && validBankMap(channel, mBankCode)) {
                    channelList.add(channel);
                } else if (validCardMap(channel, mBankCode)) {
                    channelList.add(channel);
                }
            }
        }
        return channelList;
    }

    protected void setupRecycler(List<PaymentChannel> channelList) {
        ChannelListAdapter channelListAdapter = new ChannelListAdapter();
        channelListAdapter.addZaloPayBinder(getActivity().getApplicationContext(), (long) orderAmount, 0, TransactionType.PAY);
        channelListAdapter.addMapBinder(getActivity().getApplicationContext(), (long) orderAmount);
        channelListAdapter.addAll(MAP, channelList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mChannelRecyclerView.setHasFixedSize(true);
        mChannelRecyclerView.setLayoutManager(mLayoutManager);
        mChannelRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mChannelRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), mChannelRecyclerView));
        mChannelRecyclerView.setAdapter(channelListAdapter);
    }

    @Override
    protected void initViews(View v) {
        this.mChannelRecyclerView = (RecyclerView) v.findViewById(R.id.channel_recycler_view);
        this.mContentTextView = (TextView) v.findViewById(R.id.contentTextView);
        this.mSelectButton = v.findViewById(R.id.selectButton);
        this.mSelectOtherButton = v.findViewById(R.id.selectOtherButton);
        this.mSelectButton.setOnClickListener(view -> {
            PaymentChannel channel = getChannel(mCardNumber);
            if (channel != null) {
                selectChannel(channel);
            }
        });
        this.mSelectOtherButton.setOnClickListener(view -> dismiss());
        if (!TextUtils.isEmpty(mCloseButtonText)) {
            ((Button) v.findViewById(R.id.selectOtherButton)).setText(mCloseButtonText);
        }
        if (!TextUtils.isEmpty(mContent)) {
            this.mContentTextView.setText(RenderHelper.getHtml(mContent));
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnSelectChannelEvent(SdkSelectedChannelMessage pMessage) {
        Log.d(this, "select at position", pMessage.position);
        if (mChannelList == null || mChannelList.size() <= 0) {
            Timber.d("channel list is empty");
            return;
        }
        if (pMessage.position >= 0) {
            try {
                setOK(mChannelList.get(pMessage.position).position);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.screen__map__selection;
    }


    protected PaymentChannel getChannel(String pCardNumber) {
        if (mChannelList == null) {
            return null;
        }
        for (int i = 0; i < mChannelList.size(); i++) {
            PaymentChannel channel = mChannelList.get(i);
            if (BankAccountHelper.isBankAccount(mBankCode) && !TextUtils.isEmpty(mBankCode) && mBankCode.equals(channel.bankcode)) {
                return channel;
            } else if (channel.compareToCardNumber(pCardNumber) && !TextUtils.isEmpty(mBankCode) && mBankCode.equals(channel.bankcode)) {
                return channel;
            }
        }
        return null;
    }


    @Override
    protected int getLayoutSize() {
        return 0;
    }

    private boolean validBankMap(PaymentChannel channel, String pBankCode) {
        return channel != null && channel.isBankAccountMap() && channel.isMapValid() && !TextUtils.isEmpty(pBankCode) &&
                pBankCode.equals(channel.bankcode);
    }

    private boolean validCardMap(PaymentChannel channel, String pBankCode) {
        return channel != null && channel.isMapCardChannel() && channel.isMapValid() && !TextUtils.isEmpty(pBankCode) &&
                pBankCode.equals(channel.bankcode);
    }

    protected void selectChannel(PaymentChannel channel) {
        if (channel != null) {
            setOK(channel.position);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        setCancel();
        super.onDismiss(dialog);
    }

    private void setCancel() {
        if (mZPZpwResultCallBackListener == null) {
            return;
        }
        mZPZpwResultCallBackListener.get().onCancel(Activity.RESULT_CANCELED);
    }

    private void setOK(int position) {
        if (mZPZpwResultCallBackListener == null) {
            return;
        }
        mZPZpwResultCallBackListener.get().onResultOk(Activity.RESULT_OK, position);
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBus.unregister(this);
    }

}
