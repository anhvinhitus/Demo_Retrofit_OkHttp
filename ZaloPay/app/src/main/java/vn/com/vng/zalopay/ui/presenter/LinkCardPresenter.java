package vn.com.vng.zalopay.ui.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.ui.view.ILinkCardView;
import vn.zing.pay.zmpsdk.entity.gatewayinfo.DMappedCard;
import vn.zing.pay.zmpsdk.merchant.CShareData;

/**
 * Created by AnhHieu on 5/11/16.
 */
public class LinkCardPresenter extends BaseUserPresenter implements Presenter<ILinkCardView> {

    private ILinkCardView linkCardView;

    @Override
    public void setView(ILinkCardView iLinkCardView) {
        linkCardView = iLinkCardView;
    }

    @Override
    public void destroyView() {
        linkCardView = null;
        unsubscribeIfNotNull(subscription);
    }

    private Subscription subscription;

    public void getListCard() {
        linkCardView.showLoading();
        subscription = makeObservable(new Callable<List<BankCard>>() {
            @Override
            public List<BankCard> call() throws Exception {
                List<DMappedCard> mapCardLis = CShareData.getInstance().getMappedCardList();
                return transform(mapCardLis);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LinkCardSubscriber());
    }


    private BankCard transform(DMappedCard card) {
        BankCard bankCard = null;

        if (card != null) {
            bankCard = new BankCard(card.cardname, card.first6cardno, card.last4cardno, card.bankcode, card.expiretime);
            bankCard.type = CShareData.getInstance().detectCardType(card.first6cardno).toString();
        }

        return bankCard;
    }

    private List<BankCard> transform(List<DMappedCard> cards) {
        if (Lists.isEmptyOrNull(cards)) return Collections.emptyList();

        List<BankCard> list = new ArrayList<>();

        for (DMappedCard dMappedCard : cards) {
            BankCard bCard = transform(dMappedCard);
            if (bCard != null) {
                list.add(bCard);
            }
        }

        return list;
    }


    @Override
    public void resume() {
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    protected final void onGetLinkCardSuccess(List<BankCard> list) {
        linkCardView.setData(list);
        linkCardView.hideLoading();
    }

    private final class LinkCardSubscriber extends DefaultSubscriber<List<BankCard>> {
        public LinkCardSubscriber() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "LinkCardSubscriber ");
        }

        @Override
        public void onNext(List<BankCard> bankCards) {
            LinkCardPresenter.this.onGetLinkCardSuccess(bankCards);
        }
    }
}
