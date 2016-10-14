package vn.com.vng.zalopay.linkcard.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by longlv on 10/13/16.
 * *
 */
class CardSupportAdapter extends AbsRecyclerAdapter<ZPCard, CardSupportAdapter.ViewHolder> {

    CardSupportAdapter(Context context) {
        super(context);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_card_support_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ZPCard item = getItem(position);
        holder.bindView(item);
    }

    @Override
    public void insertItems(Collection<ZPCard> items) {
        if (items == null || items.isEmpty()) return;
        synchronized (_lock) {
            for (ZPCard item : items) {
                if (!exist(item)) {
                    insert(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean exist(ZPCard item) {
        List<ZPCard> list = getItems();
        return list.indexOf(item) >= 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_logo)
        ImageView mLogoView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindView(ZPCard card) {
            if (card == null) {
                return;
            }
            mLogoView.setImageBitmap(ResourceManager.getImage(card.getCardLogoName()));
        }
    }
}
