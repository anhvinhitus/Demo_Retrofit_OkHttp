package vn.com.zalopay.wallet.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.repository.ResourceManager;

/*
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

        SimpleDraweeView imImageIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            imImageIcon = (SimpleDraweeView) itemView.findViewById(R.id.imBankIcon);
        }

        void bindView(String card, int position) {
            if (TextUtils.isEmpty(card)) {
                return;
            }
            String bankLogoName = String.format("bank_%s%s", card, Constants.BITMAP_EXTENSION);
            if (TextUtils.isEmpty(bankLogoName)) {
                return;
            }

            // load resource to SimpleDraweeView
            ResourceManager.loadLocalSDKImage(imImageIcon, bankLogoName);
        }

    }

}
