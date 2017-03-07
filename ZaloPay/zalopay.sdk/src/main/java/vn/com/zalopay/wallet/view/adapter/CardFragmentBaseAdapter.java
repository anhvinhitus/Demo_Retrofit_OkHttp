package vn.com.zalopay.wallet.view.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.TextUtils;

import java.util.ArrayList;

import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardCVVFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardExpiryFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardIssueFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNameFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNumberFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CreditCardFragment;

/**
 * base adapter
 */

public class CardFragmentBaseAdapter extends FragmentStatePagerAdapter {
    //fragment list(card number,card name,card cvv...)
    protected ArrayList<CreditCardFragment> cardFragments;

    public CardFragmentBaseAdapter(FragmentManager fm) {
        super(fm);

        cardFragments = new ArrayList<>();
    }

    public void switchToLocalCardFragmentAdapter() {
        removeFragment(CardExpiryFragment.class.getName());
        removeFragment(CardCVVFragment.class.getName());

        addIssueDateFragment();
    }

    public void switchToCreditCardFragmentAdapter() {
        removeFragment(CardIssueFragment.class.getName());

        addExpireDateFragment();
        addCardCVVFragment();
    }

    /***
     * next fragment has an error,user can not swipe to next
     *
     * @param pPos
     * @return
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
                Log.e(this, e);
            }
        }

        return true;
    }

    /***
     * previous fragment has an error
     *
     * @param pPos
     * @return
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
                Log.e(this, e);
            }
        }
        return true;
    }

    /***
     * get error fragment
     *
     * @return index of error fragment
     */
    public int hasError() {
        if (cardFragments == null || cardFragments.size() <= 0)
            return -1;

        for (int i = 0; i < cardFragments.size(); i++) {
            CreditCardFragment fragment = cardFragments.get(i);

            if (fragment.hasError())
                return i;
        }

        return -1;
    }

    public boolean hasFragment(String pTag) {
        for (CreditCardFragment cardFragment : cardFragments) {
            if (cardFragment.tag.equalsIgnoreCase(pTag))
                return true;
        }

        return false;
    }

    public int getIndexOfFragment(String pTag) {
        int pos = -1;

        for (int i = 0; i < cardFragments.size(); i++) {
            if (cardFragments.get(i).tag.equalsIgnoreCase(pTag)) {
                pos = i;
                break;
            }
        }

        return pos;
    }

    public CreditCardFragment getItemAtPosition(int pPos) throws Exception {
        if (cardFragments != null && pPos >= 0 && pPos < cardFragments.size()) {
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

            Log.d(this, "===removeFragment===pos=" + pos);
        }
    }

    public void addIssueDateFragment() {
        int pos = getIndexOfFragment(CardIssueFragment.class.getName());

        if (pos == -1) {
            CreditCardFragment mCardIssueFragment = new CardIssueFragment();
            mCardIssueFragment.tag = CardIssueFragment.class.getName();

            addFragment(mCardIssueFragment, 1);

            Log.d(this, "===addIssueDateFragment===");
        }
    }

    public void addExpireDateFragment() {
        int pos = getIndexOfFragment(CardExpiryFragment.class.getName());

        if (pos == -1) {


            CreditCardFragment mCardExpiryFragment = new CardExpiryFragment();
            mCardExpiryFragment.tag = CardExpiryFragment.class.getName();

            addFragment(mCardExpiryFragment, 1);

            Log.d(this, "===addExpireDateFragment===");
        }
    }

    public void addCardCVVFragment() {
        int pos = getIndexOfFragment(CardCVVFragment.class.getName());

        if (pos == -1) {


            CreditCardFragment mCardCVVFragment = new CardCVVFragment();
            mCardCVVFragment.tag = CardCVVFragment.class.getName();

            addFragment(mCardCVVFragment, 2);

            Log.d(this, "===addCardCVVFragment===");
        }
    }

    public void addCardNameFragment() {
        int pos = getIndexOfFragment(CardNameFragment.class.getName());

        if (pos == -1) {


            CreditCardFragment mCardNameFragment = new CardNameFragment();
            mCardNameFragment.tag = CardNameFragment.class.getName();

            addFragment(mCardNameFragment, cardFragments.size());

            Log.d(this, "===addCardNameFragment===");
        }
    }

    public void addFragment(CreditCardFragment pFragment, int position) {
        cardFragments.add(position, pFragment);
    }

    //set error
    public void setErrorText(int pIndex, String pMessage) {
        if (pIndex >= 0 && pIndex < cardFragments.size() && !TextUtils.isEmpty(pMessage)) {
            CreditCardFragment fragment = cardFragments.get(pIndex);

            if (fragment != null) {
                fragment.setError(pMessage);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (position >= cardFragments.size())
            return null;

        return cardFragments.get(position);
    }

    //this is called when notifyDataSetChanged() is called
    @Override
    public int getItemPosition(Object object) {
        int index = cardFragments.indexOf(object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    @Override
    public int getCount() {
        return cardFragments.size();
    }
}
