package vn.com.zalopay.wallet.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.constants.CardType;
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
import static vn.com.zalopay.wallet.constants.Constants.SELECTED_PMC_POSITION;
import static vn.com.zalopay.wallet.ui.channellist.ChannelListAdapter.ItemType.MAP;

public class MapBankPopup extends BasePaymentDialogActivity {
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

    public static Intent createBidvIntent(Activity activity, String cardNumber, double orderAmount) {
        Intent intentBidv = new Intent(activity, MapBankPopup.class);
        intentBidv.putExtra(BANKCODE_EXTRA, CardType.PBIDV);
        intentBidv.putExtra(CARDNUMBER_EXTRA, cardNumber);
        intentBidv.putExtra(NOTICE_CONTENT_EXTRA, GlobalData.getStringResource(RS.string.zpw_warning_bidv_select_linkcard_payment));
        intentBidv.putExtra(BUTTON_LEFT_TEXT_EXTRA, GlobalData.getStringResource(RS.string.dialog_retry_input_card_button));
        intentBidv.putExtra(AMOUNT_EXTRA, orderAmount);
        return intentBidv;
    }

    public static Intent createVCBIntent(Activity activity, String btnCloseTxt, double orderAmount) {
        Intent intentVCB = new Intent(activity, MapBankPopup.class);
        intentVCB.putExtra(BANKCODE_EXTRA, CardType.PVCB);
        intentVCB.putExtra(BUTTON_LEFT_TEXT_EXTRA, btnCloseTxt);
        intentVCB.putExtra(AMOUNT_EXTRA, orderAmount);
        return intentVCB;
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
    protected void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBus.unregister(this);
    }

    protected void setupRecycler(List<PaymentChannel> channelList) {
        ChannelListAdapter channelListAdapter = new ChannelListAdapter();
        channelListAdapter.addZaloPayBinder(getApplicationContext(), (long) orderAmount, 0, TransactionType.PAY);
        channelListAdapter.addMapBinder(getApplicationContext(), (long) orderAmount);
        channelListAdapter.addAll(MAP, channelList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mChannelRecyclerView.setHasFixedSize(true);
        mChannelRecyclerView.setLayoutManager(mLayoutManager);
        mChannelRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mChannelRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mChannelRecyclerView));
        mChannelRecyclerView.setAdapter(channelListAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setCancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChannelList = null;
        mBankCode = null;
        mCloseButtonText = null;
        mCardNumber = null;
    }

    @Override
    protected int getLayout() {
        return R.layout.screen__map__selection;
    }

    @Override
    protected void getArguments() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mCloseButtonText = bundle.getString(BUTTON_LEFT_TEXT_EXTRA);
            mBankCode = bundle.getString(BANKCODE_EXTRA);
            mCardNumber = bundle.getString(CARDNUMBER_EXTRA);
            mContent = bundle.getString(NOTICE_CONTENT_EXTRA);
            orderAmount = bundle.getDouble(AMOUNT_EXTRA);
        }
    }

    @Override
    public void initViews() {
        this.mBus = SDKApplication.getApplicationComponent().eventBus();
        this.mChannelRecyclerView = (RecyclerView) findViewById(R.id.channel_recycler_view);
        this.mContentTextView = (TextView) findViewById(R.id.contentTextView);
        this.mSelectButton = findViewById(R.id.selectButton);
        this.mSelectOtherButton = findViewById(R.id.selectOtherButton);
        this.mSelectButton.setOnClickListener(view -> {
            PaymentChannel channel = getChannel(mCardNumber);
            if (channel != null) {
                selectChannel(channel);
            }
        });
        this.mSelectOtherButton.setOnClickListener(view -> onBackPressed());
        if (!TextUtils.isEmpty(mCloseButtonText)) {
            ((Button) findViewById(R.id.selectOtherButton)).setText(mCloseButtonText);
        }
        if (!TextUtils.isEmpty(mContent)) {
            this.mContentTextView.setText(RenderHelper.getHtml(mContent));
        }
    }

    @Override
    public void initData() {
        try {
            mChannelList = getMapChannel();
            setupRecycler(mChannelList);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }


    protected PaymentChannel getChannel(String pCardNumber) {
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

    private void setCancel() {
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
    }

    private void setOK(int position) {
        Intent intent = new Intent();
        intent.putExtra(SELECTED_PMC_POSITION, position);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}