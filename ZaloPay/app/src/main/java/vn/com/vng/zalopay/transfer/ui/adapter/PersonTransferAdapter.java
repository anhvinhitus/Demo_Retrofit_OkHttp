package vn.com.vng.zalopay.transfer.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.PersonTransfer;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.utils.CurrencyUtil;

/**
 * Created by AnhHieu on 8/31/16.
 * *
 */
public class PersonTransferAdapter extends AbsRecyclerAdapter<PersonTransfer, RecyclerView.ViewHolder> {

    private User mOwner;

    public PersonTransferAdapter(Context context, User user) {
        super(context);
        mOwner = user;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new HeaderViewHolder(mInflater.inflate(R.layout.header_my_qr_code, parent, false));
        } else {
            return new ViewHolder(mInflater.inflate(R.layout.row_person_transfer, parent, false));
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
        return position == 0 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public PersonTransfer getItem(int position) {
        position = position - 1;
        return super.getItem(position);
    }

    @Override
    public void insert(PersonTransfer object, int index) {

        if (getItemCount() == 1) {
            insert(object);
            return;
        }

        if (getItems().indexOf(object) < 0) {
            super.insert(object, index);
            return;
        }

        for (int i = 0; i < getItems().size(); i++) {
            PersonTransfer item = getItems().get(i);
            if (item.equals(object)) {
                if (item.state == Constants.MoneyTransfer.STAGE_TRANSFER_SUCCEEDED) {
                    super.insert(object, index);
                } else {
                    Timber.d("insert: replace %s", i);
                    getItems().set(i, object);
                    notifyItemChanged(i + 1); // 1 -> header
                }

                break;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imgAvatar)
        ImageView imgAvatar;

        @BindView(R.id.tvDisplayName)
        TextView displayNameView;

        @BindView(R.id.tvAmount)
        TextView tvAmountView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindView(PersonTransfer person) {
            loadImage(imgAvatar, person.avatar);
            displayNameView.setText(person.displayName);

            if (person.state == Constants.MoneyTransfer.STAGE_PRETRANSFER) {
                tvAmountView.setText("đang chuyển tiền");
            } else if (person.state == Constants.MoneyTransfer.STAGE_TRANSFER_FAILED) {
                tvAmountView.setText("hủy chuyển tiền");
            } else if (person.state == Constants.MoneyTransfer.STAGE_TRANSFER_CANCEL) {
                tvAmountView.setText("hủy chuyển tiền");
            } else {
                tvAmountView.setText(TextUtils.concat("đã chuyển ", CurrencyUtil.spanFormatCurrency(person.amount)));
            }
        }

        private void loadImage(ImageView imageView, String url) {
            Glide.with(imageView.getContext()).load(url)
                    .placeholder(R.color.silver)
                    .error(R.drawable.ic_avatar_default)
                    .centerCrop()
                    .dontAnimate()
                    .into(imageView);
        }

    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageViewQrCode)
        ImageView mMyQrCodeView;

        @BindView(R.id.imageAvatar)
        ImageView mImageAvatarView;

        @BindView(R.id.layoutSuccess)
        View layoutSuccess;

        @BindView(R.id.tvName)
        TextView mNameView;

        @BindView(R.id.imageAvatarLarge)
        ImageView imageAvatarLarge;

        @BindView(R.id.tvAmount)
        TextView tvAmountView;

        @BindView(R.id.tvMessage)
        TextView tvNoteView;

        @BindView(R.id.tvMoney)
        TextView mMoneyChangeSuccess;

        @BindView(R.id.totalView)
        TextView totalView;

        @BindView(R.id.layoutTotal)
        View layoutTotal;

        private long mTotal;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bindView(User owner) {
            setUserInfo(owner.displayName, owner.avatar);
        }

        public void setAmount(long amount) {
            tvAmountView.setText(CurrencyUtil.spanFormatCurrency(amount));
            tvAmountView.setVisibility(amount <= 0 ? View.GONE : View.VISIBLE);
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

        public void displayReceivedMoney(long amount) {
            mTotal += amount;
            totalView.setText(CurrencyUtil.spanFormatCurrency(mTotal));
            setResult(true, amount);
            layoutSuccess.postDelayed(mRunnable, 5000);
        }

        public void setUserInfo(String displayName, String avatar) {
            Timber.d("setUserInfo: displayName %s avatar %s", displayName, avatar);
            loadImage(mImageAvatarView, avatar);
            loadImage(imageAvatarLarge, avatar);
            mNameView.setText(displayName);
        }

        private void loadImage(ImageView imageView, String url) {
            Glide.with(imageView.getContext()).load(url)
                    .placeholder(R.color.silver)
                    .error(R.drawable.ic_avatar_default)
                    .centerCrop()
                    .dontAnimate()
                    .into(imageView);
        }

        private Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                layoutSuccess.setVisibility(View.GONE);
            }
        };

        public void setResult(boolean success, long amount) {
            if (layoutSuccess != null) {
                layoutSuccess.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.simple_grow);
                layoutSuccess.startAnimation(animation);
            }

            if (mMoneyChangeSuccess != null) {
                mMoneyChangeSuccess.setTextColor(ContextCompat.getColor(itemView.getContext(), success ? R.color.green : R.color.red));
                mMoneyChangeSuccess.setText(CurrencyUtil.spanFormatCurrency(amount));
                mMoneyChangeSuccess.setCompoundDrawablesWithIntrinsicBounds(success ? R.drawable.ic_thanhcong_24dp : R.drawable.ic_thatbai_24dp, 0, 0, 0);
            }
        }

        public void showTotalView() {
            if (!layoutTotal.isShown()) {
                layoutTotal.setVisibility(View.VISIBLE);
            }
        }

    }
}
