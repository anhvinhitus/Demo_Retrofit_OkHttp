package vn.com.vng.zalopay.ui.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.view.ILinkCardView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.wallet.application.ZingMobilePayApplication;
import vn.com.zalopay.wallet.data.GlobalData;
import vn.com.zalopay.wallet.entity.base.BaseResponse;
import vn.com.zalopay.wallet.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.listener.ZPWRemoveMapCardListener;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 5/11/16.
 */
public class LinkCardPresenter extends BaseUserPresenter implements IPresenter<ILinkCardView> {

    private ILinkCardView linkCardView;
    private Subscription subscription;

    @Inject
    User user;

    public LinkCardPresenter(User user) {
        this.user = user;
    }

    @Override
    public void setView(ILinkCardView iLinkCardView) {
        linkCardView = iLinkCardView;
    }

    @Override
    public void destroyView() {
        linkCardView = null;
        this.zpwRemoveMapCardListener = null;
        unsubscribeIfNotNull(subscription);
    }

    public void getListCard() {
        linkCardView.showLoading();
        subscription = makeObservable(new Callable<List<BankCard>>() {
            @Override
            public List<BankCard> call() throws Exception {
                List<DMappedCard> mapCardLis = CShareData.getInstance(linkCardView.getActivity()).getMappedCardList();
                return transform(mapCardLis);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LinkCardSubscriber());
    }


    private BankCard transform(DMappedCard card) {
        BankCard bankCard = null;

        if (card != null) {
            bankCard = new BankCard(card.cardname, card.first6cardno, card.last4cardno, card.bankcode, card.expiretime);
            try {
                bankCard.type = CShareData.getInstance(linkCardView.getActivity()).detectCardType(card.first6cardno).toString();
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
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
        //release cache
        CShareData.dispose();
    }

    protected final void onGetLinkCardSuccess(List<BankCard> list) {
        linkCardView.setData(list);
        linkCardView.hideLoading();
    }

    public void removeLinkCard(BankCard bankCard) {
        linkCardView.showLoading();

        ZPWRemoveMapCardParams params = new ZPWRemoveMapCardParams();
        DMappedCard mapCard = new DMappedCard();
        mapCard.cardname = bankCard.cardname;
        mapCard.first6cardno = bankCard.first6cardno;
        mapCard.last4cardno = bankCard.last4cardno;
        mapCard.bankcode   = bankCard.bankcode;

        if (user == null) {
            linkCardView.showError("Thông tin người dùng không hợp lệ.");
            linkCardView.hideLoading();
            return;
        }
        params.accessToken = user.accesstoken;;
        params.userID = String.valueOf(user.uid);
        params.mapCard = mapCard;

        ZingMobilePayApplication.removeCardMap(linkCardView.getActivity(), params, zpwRemoveMapCardListener);
    }

    ZPWRemoveMapCardListener zpwRemoveMapCardListener = new ZPWRemoveMapCardListener() {
        @Override
        public void onSuccess(DMappedCard mapCard) {
            Timber.tag("LinkCardPresenter").d("removed map card: ", mapCard);
            linkCardView.hideLoading();
            if (mapCard != null) {
                BankCard bankCard = new BankCard(mapCard.cardname, mapCard.first6cardno, mapCard.last4cardno, mapCard.bankcode, mapCard.expiretime);
                linkCardView.removeData(bankCard);
            }
        }

        @Override
        public void onError(BaseResponse pMessage) {
            Timber.tag("LinkCardPresenter").e("onError: " + pMessage);
            linkCardView.hideLoading();
            if (pMessage == null) {
                if (!AndroidUtils.isNetworkAvailable(linkCardView.getContext())) {
                    linkCardView.showError("Vui lòng kiểm tra kết nối mạng và thử lại.");
                } else {
                    linkCardView.showError("Lỗi xảy ra trong quá trình hủy liên kết thẻ. Vui lòng thử lại sau.");
                }
            } else {
                Timber.tag("LinkCardPresenter").e("err removed map card " + pMessage.returnmessage);
//                linkCardView.showError(pMessage.returnmessage);
            }
        }
    };

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
