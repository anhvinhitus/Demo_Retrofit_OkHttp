package vn.com.zalopay.wallet.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.constants.Constants;

/**
 * Created by lytm on 17/07/2017.
 */

public class BankSupportAdapter extends BaseCardSupportAdapter<String, BankSupportAdapter.ViewHolder> {
    public BankSupportAdapter(Context context) {
        super(context);
    }


    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.gridview__item__bank, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String item = getItem(position);
        holder.bindView(item, position);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void insertItems(List<String> items) {
        if (items == null || items.isEmpty()) return;
        synchronized (_lock) {
            for (String item : items) {
                if (!exist(item)) {
                    insert(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean exist(String item) {
        List<String> list = getItems();
        return list.indexOf(item) >= 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imImageIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            imImageIcon = (ImageView) itemView.findViewById(R.id.imBankIcon);
        }
        void bindView(String card, int position) {
            String bankCode =card;
            if (!TextUtils.isEmpty(bankCode)) {
                Bitmap bitmap = ResourceManager.getImage(String.format("bank_%s%s", bankCode, Constants.BITMAP_EXTENSION));
                if (bitmap != null) {
                    imImageIcon.setImageBitmap(bitmap);
                    imImageIcon.setBackgroundResource(R.drawable.bg_card);
                }
            }
        }

    }

}
