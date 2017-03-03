package vn.com.zalopay.wallet.view.adapter;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import vn.com.zalopay.wallet.view.custom.cardview.pager.CardIssueFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNameFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNumberFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CreditCardFragment;

public class LocalCardFragmentAdapter extends CardFragmentBaseAdapter {
    public LocalCardFragmentAdapter(FragmentManager fm, Bundle args) {
        super(fm);

        CreditCardFragment mCardNumberFragment = new CardNumberFragment();
        mCardNumberFragment.setArguments(args);
        mCardNumberFragment.tag = CardNumberFragment.class.getName();

        CreditCardFragment mCardNameFragment = new CardNameFragment();
        mCardNameFragment.setArguments(args);
        mCardNameFragment.tag = CardNameFragment.class.getName();


        CreditCardFragment mCardIssueFragment = new CardIssueFragment();
        mCardIssueFragment.setArguments(args);
        mCardIssueFragment.tag = CardIssueFragment.class.getName();

        cardFragments.add(mCardNumberFragment);
        cardFragments.add(mCardIssueFragment);
        cardFragments.add(mCardNameFragment);

    }

}
