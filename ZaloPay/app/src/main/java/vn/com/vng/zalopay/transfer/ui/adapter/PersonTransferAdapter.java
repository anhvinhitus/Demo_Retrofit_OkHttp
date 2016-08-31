package vn.com.vng.zalopay.transfer.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.PersonTransfer;
import vn.com.vng.zalopay.utils.CurrencyUtil;

/**
 * Created by AnhHieu on 8/31/16.
 * *
 */
public class PersonTransferAdapter extends AbsRecyclerAdapter<PersonTransfer, PersonTransferAdapter.ViewHolder> {

    public PersonTransferAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_person_transfer, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PersonTransfer person = getItem(position);
        if (person != null) {
            holder.bindView(person);
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
            tvAmountView.setText(CurrencyUtil.spanFormatCurrency(person.amount));
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
}
