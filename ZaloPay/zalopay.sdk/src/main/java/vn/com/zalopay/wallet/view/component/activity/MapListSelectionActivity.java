package vn.com.zalopay.wallet.view.component.activity;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.zalopay.ui.widget.dialog.listener.ZPWOnCloseDialogListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.listener.IMoveToChannel;
import vn.com.zalopay.wallet.event.SdkSelectedChannelMessage;
import vn.com.zalopay.wallet.view.adapter.ChannelAdapter;
import vn.com.zalopay.wallet.view.adapter.RecyclerTouchListener;
import vn.com.zalopay.wallet.view.custom.ZPWRippleButton;

public class MapListSelectionActivity extends BasePaymentDialogActivity {
    public static final String BUTTON_LEFT_TEXT_EXTRA = "button_left_text";
    public static final String BANKCODE_EXTRA = "bankcode";
    public static final String CARDNUMBER_EXTRA = "cardnumber";
    public static final String NOTICE_CONTENT_EXTRA = "content";
    protected static WeakReference<ZPWOnCloseDialogListener> mCloseDialog;
    protected ChannelAdapter mChannelAdapter;
    protected String mCloseButtonText = null;
    protected String mBankCode = null;
    protected String mCardNumber = null;
    protected String mContent = null;
    protected TextView mContentTextView;
    protected View mSelectButton, mSelectOtherButton;
    protected ArrayList<PaymentChannel> mChannelList = new ArrayList<>();
    protected WeakReference<IMoveToChannel> mMoveToChannelListener;
    protected WeakReference<BasePaymentActivity> mGatewayActivity;
    private RecyclerView mChannelRecyclerView;

    public static void setCloseDialogListener(ZPWOnCloseDialogListener pListener) {
        MapListSelectionActivity.mCloseDialog = new WeakReference<>(pListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnSelectChannelEvent(SdkSelectedChannelMessage pMessage) {
        boolean selectedChannel = selectChannel(pMessage.position);
        if (selectedChannel) {
            finish();
        }
    }

    protected void initializeChannelRecycleView(List<PaymentChannel> pChannelList) {
        mChannelAdapter = new ChannelAdapter(getApplicationContext(), pChannelList, R.layout.channel_item_recyclerview, null);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mChannelRecyclerView.setHasFixedSize(true);
        mChannelRecyclerView.setLayoutManager(mLayoutManager);
        mChannelRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mChannelRecyclerView.setAdapter(mChannelAdapter);
        mChannelRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mChannelRecyclerView));
    }

    public ZPWOnCloseDialogListener getListener() {
        return MapListSelectionActivity.mCloseDialog.get();
    }

    protected PaymentGatewayActivity getGatewayActivity() {
        return (PaymentGatewayActivity) mGatewayActivity.get();
    }

    protected IMoveToChannel getMoveToChannelListener() {
        return mMoveToChannelListener.get();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getListener() != null) {
            getListener().onCloseCardSupportDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mChannelAdapter != null) {
            mChannelAdapter = null;
        }
        mGatewayActivity = null;
        mBankCode = null;
        mCloseButtonText = null;
        mCardNumber = null;
    }

    @Override
    protected int getLayout() {
        return R.layout.screen__map__list__selection;
    }

    @Override
    protected void getArguments() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mCloseButtonText = bundle.getString(BUTTON_LEFT_TEXT_EXTRA);
            mBankCode = bundle.getString(BANKCODE_EXTRA);
            mCardNumber = bundle.getString(CARDNUMBER_EXTRA);
            mContent = bundle.getString(NOTICE_CONTENT_EXTRA);
        }
    }

    @Override
    public void initViews() {
        this.mGatewayActivity = new WeakReference<>(BasePaymentActivity.getPaymentGatewayActivity());

        if (getGatewayActivity() != null) {
            this.mMoveToChannelListener = new WeakReference<>(getGatewayActivity().getMoveToChannelListener());
        }

        this.mChannelRecyclerView = (RecyclerView) findViewById(R.id.channel_recycler_view);
        this.mContentTextView = (TextView) findViewById(R.id.contentTextView);

        this.mSelectButton = findViewById(R.id.selectButton);
        this.mSelectOtherButton = findViewById(R.id.selectOtherButton);

        this.mSelectButton.setOnClickListener(view -> {
            int positionToSelect = getChannel(mCardNumber);
            if (positionToSelect > -1) {
                selectChannel(positionToSelect);
            }
            finish();
        });

        this.mSelectOtherButton.setOnClickListener(view -> onBackPressed());

        if (!TextUtils.isEmpty(mCloseButtonText)) {
            ((ZPWRippleButton) findViewById(R.id.selectOtherButton)).setText(mCloseButtonText);
        }
        if (!TextUtils.isEmpty(mContent)) {
            this.mContentTextView.setText(Html.fromHtml(mContent));
        }
    }

    protected int getChannel(String pCardNumber) {
        for (int i = 0; i < mChannelList.size(); i++) {
            if (mChannelList.get(i).isBankAccountMap) {
                return i;
            } else if (mChannelList.get(i).compareToCardNumber(pCardNumber)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void initData() {
        initializeChannelRecycleView(createChannels());
    }

    protected List<PaymentChannel> createChannels() {
        mChannelList.clear();
        if (getGatewayActivity() != null && !getGatewayActivity().isFinishing()) {
            List<PaymentChannel> channelList = getGatewayActivity().getChannelList();
            for (PaymentChannel channel : channelList) {
                if (channel.isMapCardChannel() && !TextUtils.isEmpty(channel.bankcode) && channel.bankcode.equals(mBankCode)) {
                    mChannelList.add(channel.clone());
                }
            }
        }
        return mChannelList;
    }

    protected boolean selectChannel(int pPosition) {
        if (pPosition >= 0 && getMoveToChannelListener() != null) {
            if (mChannelList != null && mChannelList.size() > 0) {
                getMoveToChannelListener().moveToChannel(mChannelList.get(pPosition));
            }
        }
        return true;
    }
}