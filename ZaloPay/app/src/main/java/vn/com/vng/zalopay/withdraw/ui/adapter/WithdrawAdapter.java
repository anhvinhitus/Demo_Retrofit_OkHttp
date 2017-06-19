package vn.com.vng.zalopay.withdraw.ui.adapter;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;

import java.util.List;

import vn.com.vng.zalopay.data.util.Lists;

/**
 * Created by hieuvm on 6/18/17.
 * *
 */

public class WithdrawAdapter extends EpoxyAdapter {

    public interface OnClickDenominationListener {
        void onClickDenomination(long money);
    }

    private final HeaderWithdrawModel mBalanceModel = new HeaderWithdrawModel();

    private OnClickDenominationListener mListener;

    public WithdrawAdapter(OnClickDenominationListener listener) {
        enableDiffing();
        addModel(mBalanceModel);
        mListener = listener;
    }

    public void setBalance(long balance) {
        mBalanceModel.setBalance(balance);

        for (EpoxyModel<?> model : getAllModelsAfter(mBalanceModel)) {
            ((DenominationMoneyModel) model).setBalance(balance);
        }

        notifyModelsChanged();
    }

    public void insertItems(List<Long> items) {
        addModels(transform(items));
    }

    private DenominationMoneyModel transform(long money) {
        DenominationMoneyModel model = new DenominationMoneyModel();
        model.setDenominationMoney(money);
        model.setClickListener(m -> {
            if (mListener != null) {
                mListener.onClickDenomination(m);
            }
        });
        return model;
    }

    private List<DenominationMoneyModel> transform(List<Long> items) {
        return Lists.transform(items, this::transform);
    }
}
