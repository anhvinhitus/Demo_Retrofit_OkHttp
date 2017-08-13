package vn.com.zalopay.wallet.view.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import timber.log.Timber;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardCVVFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardExpiryFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardIssueFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNameFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNumberFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CreditCardFragment;

public class CardFragmentBaseAdapter extends FragmentStatePagerAdapter {
    //fragment list(card number,card name,card cvv...)
    ArrayList<CreditCardFragment> cardFragments;

    public CardFragmentBaseAdapter(FragmentManager fm) {
        super(fm);

        cardFragments = new ArrayList<>();
    }

    /*
     * next fragment has an error,user can not swipe to next
     */
    public boolean canNavigateToNext(int pPos) {
        //prevent navigate if previous fragment have an error
        if (pPos > 0) {
            try {
                CreditCardFragment previousFragment = getItemAtPosition(pPos - 1);
                if (previousFragment != null && previousFragment.hasError()) {
                    return false;
                }
            } catch (Exception e) {
                Timber.d(e, "Exception NavigateToNext");
            }
        }
        return true;
    }

    /*
     * previous fragment has an error
     */
    public boolean canNavigateToPrevious(int pPos) {
        //prevent navigate if previous fragment have an error
        if (pPos < getCount() - 1) {
            try {
                CreditCardFragment nextFragment = getItemAtPosition(pPos + 1);
                if (nextFragment != null && nextFragment.hasError()) {
                    return false;
                }
            } catch (Exception e) {
                Timber.d(e, "Exception canNavigateToPrevious");
            }
        }
        return true;
    }

    public int hasError() {
        if (cardFragments == null || cardFragments.size() <= 0) {
            return -1;
        }
        for (int i = 0; i < cardFragments.size(); i++) {
            CreditCardFragment fragment = cardFragments.get(i);
            if (fragment != null && fragment.hasError())
                return i;
        }
        return -1;
    }

    public boolean hasFragment(String pTag) {
        for (CreditCardFragment cardFragment : cardFragments) {
            if (cardFragment != null
                    && cardFragment.tag != null
                    && cardFragment.tag.equalsIgnoreCase(pTag))
                return true;
        }
        return false;
    }

    public int getIndexOfFragment(String pTag) {
        int pos = -1;
        for (int i = 0; i < cardFragments.size(); i++) {
            CreditCardFragment cardFragment = cardFragments.get(i);
            if (cardFragment != null
                    && cardFragment.tag != null
                    && cardFragment.tag.equalsIgnoreCase(pTag)) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    public CreditCardFragment getItemAtPosition(int pPos) throws Exception {
        if (cardFragments != null
                && pPos >= 0
                && pPos < cardFragments.size()) {
            return cardFragments.get(pPos);
        }
        throw new Exception();
    }

    public CardNumberFragment getCardNumberFragment() throws Exception {
        return (CardNumberFragment) getItemAtPosition(getIndexOfFragment(CardNumberFragment.class.getName()));
    }

    public CardNameFragment getCardNameFragment() throws Exception {
        return (CardNameFragment) getItemAtPosition(getIndexOfFragment(CardNameFragment.class.getName()));
    }

    public CardExpiryFragment getCardExpiryFragment() throws Exception {
        return (CardExpiryFragment) getItemAtPosition(getIndexOfFragment(CardExpiryFragment.class.getName()));
    }

    public CardIssueFragment getCardIssueryFragment() throws Exception {
        return (CardIssueFragment) getItemAtPosition(getIndexOfFragment(CardIssueFragment.class.getName()));
    }

    public CardCVVFragment getCardCVVFragment() throws Exception {
        return (CardCVVFragment) getItemAtPosition(getIndexOfFragment(CardCVVFragment.class.getName()));
    }

    public void removeFragment(String pTag) {
        int pos = getIndexOfFragment(pTag);
        if (pos != -1) {
            cardFragments.remove(pos);
        }
    }

    public void addIssueDateFragment() {
        int pos = getIndexOfFragment(CardIssueFragment.class.getName());
        if (pos == -1) {
            CreditCardFragment mCardIssueFragment = new CardIssueFragment();
            mCardIssueFragment.tag = CardIssueFragment.class.getName();
            addFragment(mCardIssueFragment, 1);
        }
    }

    public void addExpireDateFragment() {
        int pos = getIndexOfFragment(CardExpiryFragment.class.getName());
        if (pos == -1) {
            CreditCardFragment mCardExpiryFragment = new CardExpiryFragment();
            mCardExpiryFragment.tag = CardExpiryFragment.class.getName();
            addFragment(mCardExpiryFragment, 1);
        }
    }


    private void addFragment(CreditCardFragment pFragment, int position) {
        cardFragments.add(position, pFragment);
    }

    @Override
    public Fragment getItem(int position) {
        if (position >= cardFragments.size()) {
            return null;
        }
        return cardFragments.get(position);
    }

    //this is called when notifyDataSetChanged() is called
    @Override
    public int getItemPosition(Object object) {
        int index = cardFragments.indexOf(object);
        if (index == -1) {
            return POSITION_NONE;
        } else {
            return index;
        }
    }

    @Override
    public int getCount() {
        return cardFragments.size();
    }
}
