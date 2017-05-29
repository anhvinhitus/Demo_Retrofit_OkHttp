package vn.com.vng.zalopay.bank.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuLayout;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.bank.models.BankCardStyle;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

/**
 * Created by longlv on 5/25/17.
 * Adapter of recycleView in BankFragment
 */
class BankAdapter extends AbstractSwipeMenuRecyclerAdapter<DBaseMap, RecyclerView.ViewHolder> {

    private class ViewType {
        static final int UNKNOWN = 0;
        //static final int HEADER = 1;
        static final int LINK_CARD = 2;
        static final int LINK_ACCOUNT = 3;
        static final int FOOTER = 4;
    }

    private final User mUser;
    private boolean mCurrentMenuState = false; //false: menu is hide.
    private IBankListener mListener;

    BankAdapter(Context context, User user, IBankListener listener) {
        super(context);
        this.mListener = listener;
        this.mUser = user;
    }

    @Override
    public boolean enableMenu(int viewType) {
        return viewType != ViewType.FOOTER && super.enableMenu(viewType);
    }

    @Override
    public int getItemViewType(int position) {
        DBaseMap dBaseMap = getItem(position);
        if (dBaseMap instanceof DMappedCard) {
            return ViewType.LINK_CARD;
        } else if (dBaseMap instanceof DBankAccount) {
            return ViewType.LINK_ACCOUNT;
        } else if (dBaseMap == null && isFooter(position)) {
            return ViewType.FOOTER;
        } else {
            return ViewType.UNKNOWN;
        }
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        if (viewType == ViewType.LINK_CARD || viewType == ViewType.LINK_ACCOUNT) {
            return mInflater.inflate(R.layout.row_bank_card_layout, parent, false);
        } else if (viewType == ViewType.FOOTER) {
            return mInflater.inflate(R.layout.layout_footer_list_bank, parent, false);
        } else {
            return mInflater.inflate(R.layout.layout_empty, parent, false);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        if (viewType == ViewType.LINK_CARD || viewType == ViewType.LINK_ACCOUNT) {
            return new BankViewHolder(realContentView);
        } else if (viewType == ViewType.FOOTER) {
            return new FooterHolder(realContentView);
        } else {
            return null;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DBaseMap baseMap = getItem(position);
        boolean isLastItem = isLatestItem(position);
        if (holder instanceof BankViewHolder) {
            ((BankViewHolder) holder).bindView(baseMap, isLastItem);
        }
    }

    @Override
    public void onBindSwipeMenuViewHolder(SwipeMenuView swipeLeftMenuView, SwipeMenuView swipeRightMenuView, int position) {
        if (isFooter(position)) {
            return;
        }
        boolean isLastItem = isLatestItem(position);
        FrameLayout.LayoutParams params = new FrameLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = getContext().getResources().getDimensionPixelSize(R.dimen.spacing_medium_s);
        int marginBottom = 0;
        if (isLastItem) {
            marginBottom = getContext().getResources()
                    .getDimensionPixelSize(R.dimen.linkcard_margin_bottom_lastitem);
        }
        params.setMargins(0, margin, 0, marginBottom);
        swipeRightMenuView.setLayoutParams(params);
    }

    @Override
    public void onShowRightMenu(RecyclerView.ViewHolder holder, int position) {
        super.onShowRightMenu(holder, position);
        if (holder instanceof BankViewHolder) {
            ((BankViewHolder) holder).changeBackgroundCorner(true);
        }
    }

    @Override
    public void onHideRightMenu(RecyclerView.ViewHolder holder, int position) {
        super.onHideRightMenu(holder, position);
        if (holder instanceof BankViewHolder) {
            ((BankViewHolder) holder).changeBackgroundCorner(false);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size() + 1;
    }

    private boolean isFooter(int position) {
        return (position == getItemCount() - 1);
    }

    private boolean isLatestItem(int position) {
        return (position == getItemCount() - 2);
    }

    class FooterHolder extends RecyclerView.ViewHolder {

        @OnClick(R.id.btn_add_more)
        public void onClickAddMore() {
            if (mListener != null) {
                mListener.onClickAddMoreBank();
            }
        }

        FooterHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class BankViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root)
        View mRoot;

        @BindView(R.id.iv_logo)
        ImageView imgLogo;

        @BindView(R.id.tv_num_acc)
        TextView mCardNumber;

        @OnClick(R.id.root)
        public void onItemClickListener() {
            smoothOpenRightMenu();
        }

        BankViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindView(DBaseMap bankCard, boolean isLastItem) {
            bindBankCard(mRoot, imgLogo, bankCard, true);
            bindBankInfo(bankCard);
            setMargin(isLastItem);
        }

        private String getBankInfo(DBankAccount bankAccount) {
            if (bankAccount == null) {
                return "";
            }
            if (ECardType.PVCB.toString().equalsIgnoreCase(bankAccount.bankcode)) {
                return PhoneUtil.getPhoneNumberScreened(mUser.phonenumber);
            } else {
                return bankAccount.firstaccountno + bankAccount.lastaccountno;
            }
        }

        private void bindBankInfo(DBaseMap baseMap) {
            if (baseMap instanceof DMappedCard) {
                DMappedCard mappedCard = (DMappedCard) baseMap;
                String bankCardNumber = BankUtils.formatBankCardNumber(mappedCard.first6cardno, mappedCard.last4cardno);
                mCardNumber.setText(Html.fromHtml(bankCardNumber));
            } else if (baseMap instanceof DBankAccount) {
                mCardNumber.setText(getBankInfo((DBankAccount) baseMap));
            }
        }

        private void setMargin(boolean isLastItem) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mRoot.getLayoutParams();
            int margin = getContext().getResources().getDimensionPixelSize(R.dimen.spacing_medium_s);
            int marginBottom = 0;
            if (isLastItem) {
                marginBottom = getContext().getResources()
                        .getDimensionPixelSize(R.dimen.linkcard_margin_bottom_lastitem);
            }
            params.setMargins(0, margin, 0, marginBottom);
            mRoot.setLayoutParams(params);
        }

        private void smoothOpenRightMenu() {
            if (!(itemView instanceof SwipeMenuLayout)) {
                return;
            }
            SwipeMenuLayout swipeMenuLayout = ((SwipeMenuLayout) itemView);
            if (!swipeMenuLayout.isRightMenuOpen()) {
                swipeMenuLayout.smoothOpenRightMenu();
            }
        }

        void changeBackgroundCorner(boolean isShowMenu) {
            if (mCurrentMenuState == isShowMenu) {
                return;
            }
            float border = getContext().getResources().getDimension(R.dimen.border_link_card);
            GradientDrawable drawable = (GradientDrawable) mRoot.getBackground();
            if (isShowMenu) {
                drawable.setCornerRadii(new float[]{border, border, 0, 0, 0, 0, 0, 0});
            } else {
                drawable.setCornerRadii(new float[]{border, border, border, border, 0, 0, 0, 0});
            }
            mCurrentMenuState = isShowMenu;
        }
    }

    private void setBankIcon(ImageView imgLogo, @StringRes int bankIcon) {
        if (imgLogo == null) {
            return;
        }
        if (bankIcon == 0) {
            imgLogo.setImageDrawable(null);
        } else {
            String iconName = getContext().getString(bankIcon);
            imgLogo.setImageBitmap(ResourceHelper
                    .getBitmap(getContext(), BuildConfig.ZALOPAY_APP_ID, iconName));
        }
    }

    private void setBankBackground(View mRoot, BankCardStyle bankCardStyle, boolean borderTopOnly) {
        if (mRoot == null || bankCardStyle == null) {
            return;
        }

        int[] colors = new int[3];
        colors[0] = ContextCompat.getColor(getContext(), bankCardStyle.backgroundGradientStart);
        colors[1] = ContextCompat.getColor(getContext(), bankCardStyle.backgroundGradientEnd);
        colors[2] = ContextCompat.getColor(getContext(), bankCardStyle.backgroundGradientStart);

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        float radius = getContext().getResources().getDimension(R.dimen.border_link_card);
        if (borderTopOnly) {
            gradientDrawable.setCornerRadii(new float[]{radius, radius,
                    radius, radius,
                    0, 0,
                    0, 0});
        } else {
            gradientDrawable.setCornerRadius(radius);
        }
        mRoot.setBackground(gradientDrawable);
    }

    private void bindBankCard(View mRoot, ImageView imgLogo, DBaseMap bankCard, boolean borderTopOnly) {
        BankCardStyle bankCardStyle = BankUtils.getBankCardStyle(bankCard, mUser);
        setBankIcon(imgLogo, bankCardStyle.bankIcon);
        setBankBackground(mRoot, bankCardStyle, borderTopOnly);
    }

    @Override
    public void insert(DBaseMap object) {
        super.insert(object);
        notifyDataSetChanged();
    }

    interface IBankListener {
        void onClickAddMoreBank();
    }
}
