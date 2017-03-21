package vn.com.zalopay.wallet.view.component.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannelView;
import vn.com.zalopay.wallet.listener.IMoveToChannel;
import vn.com.zalopay.wallet.listener.ZPWOnCloseDialogListener;
import vn.com.zalopay.wallet.view.adapter.GatewayChannelListViewAdapter;
import vn.com.zalopay.wallet.view.custom.ZPWRippleButton;

public class MapListSelectionActivity extends BasePaymentDialogActivity {
    public static final String BUTTON_LEFT_TEXT_EXTRA = "button_left_text";
    public static final String BANKCODE_EXTRA = "bankcode_extra";
    public static final String CARDNUMBER_EXTRA = "cardnumber_extra";
    protected static WeakReference<ZPWOnCloseDialogListener> mCloseDialog;
    protected String mCloseButtonText = null;
    protected String mBankCode = null;
    protected String mCardNumber = null;
    protected ListView mChannelListView;
    protected TextView mContentTextView;
    protected View mSelectButton, mSelectOtherButton;
    protected GatewayChannelListViewAdapter mChannelListViewAdapter = null;
    protected ArrayList<DPaymentChannelView> mChannelList = new ArrayList<>();
    protected WeakReference<IMoveToChannel> mMoveToChannelListener;
    protected WeakReference<BasePaymentActivity> mGatewayActivity;
    private AdapterView.OnItemClickListener mChannelItemClick = (parent, view, position, id) -> {
        boolean selectedChannel = selectChannel(position);
        if (selectedChannel) {
            finish();
        }
    };

    public static void setCloseDialogListener(ZPWOnCloseDialogListener pListener) {
        MapListSelectionActivity.mCloseDialog = new WeakReference<>(pListener);
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

        if (mChannelListViewAdapter != null) {
            mChannelListViewAdapter.clear();
            mChannelListViewAdapter = null;
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
        }
    }

    @Override
    public void initViews() {
        this.mGatewayActivity = new WeakReference<BasePaymentActivity>(BasePaymentActivity.getPaymentGatewayActivity());

        if (getGatewayActivity() != null) {
            this.mMoveToChannelListener = new WeakReference<IMoveToChannel>(getGatewayActivity().getMoveToChannelListener());
        }

        this.mChannelListView = (ListView) findViewById(R.id.channelListView);
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
    }

    protected int getChannel(String pCardNumber) {
        for (int i = 0; i < mChannelList.size(); i++) {
            if (mChannelList.get(i).isBankAccountMap) {
                return i;
            } else if (mChannelList.get(i).isCardNumber(pCardNumber)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void initData() {
        this.mChannelListViewAdapter = new GatewayChannelListViewAdapter(this, RS.getLayout(RS.layout.listview__item__channel__gateway), createChannels());
        this.mChannelListView.setAdapter(mChannelListViewAdapter);
        this.mChannelListView.setOnItemClickListener(mChannelItemClick);
    }

    protected ArrayList<DPaymentChannelView> createChannels() {
        mChannelList.clear();
        if (getGatewayActivity() != null && !getGatewayActivity().isFinishing()) {
            ArrayList<DPaymentChannelView> channelList = getGatewayActivity().getChannelList();

            for (DPaymentChannelView channel : channelList) {
                if (channel.isMapCardChannel() && !TextUtils.isEmpty(channel.bankcode) && channel.bankcode.equals(mBankCode)) {
                    mChannelList.add(channel.clone());
                }
            }
        }
        return mChannelList;
    }

    protected boolean selectChannel(int pPosition) {
        if (pPosition >= 0 && getMoveToChannelListener() != null) {
            getMoveToChannelListener().moveToChannel(mChannelList.get(pPosition));
        }
        return true;
    }
}