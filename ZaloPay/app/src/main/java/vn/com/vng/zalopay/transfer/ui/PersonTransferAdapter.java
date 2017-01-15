package vn.com.vng.zalopay.transfer.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.IconFont;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.PersonTransfer;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.utils.CurrencyUtil;
import vn.com.vng.zalopay.utils.ImageLoader;

/**
 * Created by AnhHieu on 8/31/16.
 * *
 */
final class PersonTransferAdapter extends AbsRecyclerAdapter<PersonTransfer, RecyclerView.ViewHolder> {

    private static final int VIEWTYPE_HEADER = 0;
    private static final int VIEWTYPE_INPROGRESS = 1;
    private static final int VIEWTYPE_DONE = 2;
    private User mOwner;

    PersonTransferAdapter(Context context, User user) {
        super(context);
        mOwner = user;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_HEADER:
                return new HeaderViewHolder(mInflater.inflate(R.layout.header_my_qr_code, parent, false));
            case VIEWTYPE_INPROGRESS:
                return new ViewHolder(mInflater.inflate(R.layout.row_person_transfer_inprogress, parent, false));
            case VIEWTYPE_DONE:
                return new ViewHolder(mInflater.inflate(R.layout.row_person_transfer_done, parent, false));
            default:
                Timber.w("Unknown viewType: %s", viewType);
                return null;
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).detach();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            PersonTransfer person = getItem(position);
            if (person != null) {
                ((ViewHolder) holder).bindView(person);
            }
        } else if (holder instanceof HeaderViewHolder) {
            if (mOwner != null) {
                ((HeaderViewHolder) holder).bindView(mOwner);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEWTYPE_HEADER;
        }

        PersonTransfer person = getItem(position);
        if (person == null) {
            return VIEWTYPE_INPROGRESS;
        }

        if (person.state == Constants.MoneyTransfer.STAGE_TRANSFER_SUCCEEDED) {
            return VIEWTYPE_DONE;
        } else {
            return VIEWTYPE_INPROGRESS;
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public PersonTransfer getItem(int position) {
        return super.getItem(position - 1);
    }

    @Override
    public void insert(PersonTransfer item, int index) {
        synchronized (_lock) {
            mItems.add(index, item);
        }
        notifyItemInserted(index + 1);
    }

    @Override
    public void replace(int location, PersonTransfer object) {
        synchronized (_lock) {
            mItems.set(location, object);
        }
        notifyItemChanged(location + 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgAvatar)
        SimpleDraweeView imgAvatar;

        @BindView(R.id.tvDisplayName)
        TextView displayNameView;

        @BindView(R.id.tvAmount)
        TextView tvAmountView;

        ImageLoader mImageLoader;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageLoader = AndroidApplication.instance().getAppComponent().imageLoader();
            ButterKnife.bind(this, itemView);
        }

        void bindView(PersonTransfer person) {

            Timber.d("bindView: person name [%s] state [%s]", person.displayName, person.state);
            loadImage(imgAvatar, person.avatar);
            displayNameView.setText(person.displayName);

            if (person.state == Constants.MoneyTransfer.STAGE_PRETRANSFER) {
                tvAmountView.setText(R.string.transferring_money);
            } else if (person.state == Constants.MoneyTransfer.STAGE_TRANSFER_FAILED) {
                tvAmountView.setText(R.string.cancel_transfer_money);
            } else if (person.state == Constants.MoneyTransfer.STAGE_TRANSFER_CANCEL) {
                tvAmountView.setText(R.string.cancel_transfer_money);
            } else {
                tvAmountView.setText(CurrencyUtil.spanFormatCurrency(person.amount, false));
            }
        }

        private void loadImage(SimpleDraweeView imageView, String url) {
            mImageLoader.loadImage(imageView, url);
        }

    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageViewQrCode)
        ImageView mMyQrCodeView;

        @BindView(R.id.imageAvatar)
        SimpleDraweeView mImageAvatarView;

        @BindView(R.id.layoutSuccess)
        View layoutSuccess;

        @BindView(R.id.tvName)
        TextView mNameView;

        @BindView(R.id.imageAvatarLarge)
        SimpleDraweeView imageAvatarLarge;

        @BindView(R.id.tvAmount)
        TextView tvAmountView;

        @BindView(R.id.tvMessage)
        TextView tvNoteView;

        @BindView(R.id.iconState)
        IconFont mIconState;

        @BindView(R.id.tvMoney)
        TextView mMoneyChangeSuccess;

        @BindView(R.id.totalView)
        TextView totalView;

        @BindView(R.id.layoutTotal)
        View layoutTotal;

        private long mTotal;

        ImageLoader mImageLoader;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            mImageLoader = AndroidApplication.instance().getAppComponent().imageLoader();
            ButterKnife.bind(this, itemView);
        }

        public void bindView(User owner) {
            setUserInfo(owner.displayName, owner.avatar);
        }

        public void setAmount(long amount) {
            tvAmountView.setText(amount <= 0 ? "" : CurrencyUtil.spanFormatCurrency(amount, false));
            tvAmountView.setVisibility(amount <= 0 ? View.GONE : View.VISIBLE);
        }

        public boolean hasAmount() {
            return tvAmountView.getText().length() > 0;
        }

        public void setNote(String message) {
            tvNoteView.setText(message);
            tvNoteView.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
        }

        public void setQrImage(Bitmap image) {
            if (mMyQrCodeView == null) {
                return;
            }

            if (image != null) {
                mMyQrCodeView.setImageBitmap(image);
            }
        }

        public void displayWaitForMoney() {
            layoutSuccess.setVisibility(View.GONE);
        }

        public void displayReceivedMoney(String senderDisplayName, String senderAvatar, long amount, String pTransId) {
            mTotal += amount;
            totalView.setText(CurrencyUtil.spanFormatCurrency(mTotal, false));
            setResult(true, amount);
            loadImage(imageAvatarLarge, senderAvatar);
            mNameView.setText(senderDisplayName);
            layoutSuccess.postDelayed(mRunnable, 5000);
        }

        public void setUserInfo(String displayName, String avatar) {
            Timber.d("setUserInfo: displayName %s avatar %s", displayName, avatar);
            loadImage(mImageAvatarView, avatar);
        }

        private void loadImage(SimpleDraweeView imageView, String url) {
            mImageLoader.loadImage(imageView, url);
        }

        private Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                if (layoutSuccess != null) {
                    slideToBottom(layoutSuccess);
                }
            }
        };

        void slideToBottom(View view) {
            TranslateAnimation animate = new TranslateAnimation(0, view.getWidth(), 0, view.getHeight());
            animate.setDuration(500);
            animate.setFillAfter(true);
            view.startAnimation(animate);
            view.setVisibility(View.GONE);
        }

        void simpleGrow(View view) {
            view.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.simple_grow);
            view.startAnimation(animation);
        }


        public void setResult(boolean success, long amount) {
            if (layoutSuccess != null) {
                simpleGrow(layoutSuccess);
            }

            if (mMoneyChangeSuccess != null) {
                mMoneyChangeSuccess.setTextColor(ContextCompat.getColor(itemView.getContext(), success ? R.color.green : R.color.red));
                mMoneyChangeSuccess.setText(CurrencyUtil.spanFormatCurrency(amount, false));
                mIconState.setIcon(success ? R.string.general_success : R.string.general_failed);
            }
        }

        public void showTotalView() {
            if (!layoutTotal.isShown()) {
                layoutTotal.setVisibility(View.VISIBLE);
            }
        }

        public void detach() {
            if (layoutSuccess != null) {
                layoutSuccess.removeCallbacks(mRunnable);
                layoutSuccess.clearAnimation();
            }
        }

    }
}
