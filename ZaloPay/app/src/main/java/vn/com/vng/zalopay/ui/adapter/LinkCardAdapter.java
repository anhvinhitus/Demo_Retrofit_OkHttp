package vn.com.vng.zalopay.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.vng.uicomponent.widget.recyclerview.AbsRecyclerAdapter;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class LinkCardAdapter extends AbsRecyclerAdapter<BankCard, LinkCardAdapter.ViewHolder> {

    public LinkCardAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_bank_card_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BankCard bankCard = getItem(position);
        if (bankCard != null) {
            holder.bindView(bankCard);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }


        public void bindView(BankCard bankCard) {

        }
    }
}
