package vn.com.vng.zalopay.bank.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zalopay.ui.widget.recyclerview.AbsRecyclerAdapter;

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
class BankAdapter extends AbsRecyclerAdapter<ZPCard, BankAdapter.ViewHolder> {

    private int mColumnCount;

    BankAdapter(Context context, List<ZPCard> cards, int columnCount) {
        super(context);
        mColumnCount = columnCount;
        insertItems(cards);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.row_bank_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ZPCard item = getItem(position);
        holder.bindView(item);
    }

    @Override
    public int getItemCount() {
        // Lấy số nhỏ nhất chia hết cho 2, lớn hơn itemCount.
        // Để đảm bảo item empty cuối cùng trong grid có màu khác với màu nền.
        return ((super.getItemCount() + (mColumnCount - 1)) / mColumnCount) * mColumnCount;
    }

    @Override
    public void insertItems(List<ZPCard> items) {
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
                mLogoView.setVisibility(View.GONE);
                return;
            }
            mLogoView.setImageBitmap(ResourceManager.getImage(card.getCardLogoName()));
            mLogoView.setVisibility(View.VISIBLE);
        }
    }
}
