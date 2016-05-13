package vn.com.vng.zalopay.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.fragment.LinkCardFragment;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class LinkCardActivity extends BaseToolBarActivity {
    public static final int REQUEST_CODE = 123;
    private LinkCardFragment mLinkCardFragment;

    @Override
    public BaseFragment getFragmentToHost() {
        mLinkCardFragment=  LinkCardFragment.newInstance();
        return mLinkCardFragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                BankCard bankCard = new BankCard();
                bankCard.userName = bundle.getString(Constants.CARDNAME);
                String first6CardNo = bundle.getString(Constants.FIRST6CARDNO);
                String last4CardNo = bundle.getString(Constants.LAST4CARDNO);
                bankCard.subAccNumber = last4CardNo;
//                bankCard.type = Enums.BankCard.valueOf(CShareData.getInstance().detectCardType(first6CardNo + last4CardNo));
                if (mLinkCardFragment!=null) {
                    mLinkCardFragment.updateData(bankCard);
                }
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
