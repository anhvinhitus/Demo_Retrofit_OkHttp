package vn.com.zalopay.wallet.view.adapter;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import vn.com.zalopay.wallet.view.custom.cardview.pager.CardCVVFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardExpiryFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNameFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNumberFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CreditCardFragment;

public class CreditCardFragmentAdapter extends CardFragmentBaseAdapter {

    public CreditCardFragmentAdapter(FragmentManager fm, Bundle args) {
        super(fm);

        CreditCardFragment mCardNumberFragment = new CardNumberFragment();
        mCardNumberFragment.setArguments(args);
        mCardNumberFragment.tag = CardNumberFragment.class.getName();

        CreditCardFragment mCardExpiryFragment = new CardExpiryFragment();
        mCardExpiryFragment.setArguments(args);
        mCardExpiryFragment.tag = CardExpiryFragment.class.getName();

        CreditCardFragment mCardCVVFragment = new CardCVVFragment();
        mCardCVVFragment.setArguments(args);
        mCardCVVFragment.tag = CardCVVFragment.class.getName();

        CreditCardFragment mCardNameFragment = new CardNameFragment();
        mCardNameFragment.setArguments(args);
        mCardNameFragment.tag = CardNameFragment.class.getName();

        cardFragments.add(mCardNumberFragment);
        cardFragments.add(mCardExpiryFragment);
        cardFragments.add(mCardCVVFragment);
        cardFragments.add(mCardNameFragment);

    }
}
