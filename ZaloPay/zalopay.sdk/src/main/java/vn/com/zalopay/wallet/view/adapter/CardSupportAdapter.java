package vn.com.zalopay.wallet.view.adapter;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.view.adapter.holder.ZPWItemBankHolder;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;

public class CardSupportAdapter extends BaseAdapter {
    protected ArrayList<String> mBankCode = new ArrayList<>();

    public static CardSupportAdapter createAdapterProxy(boolean pIsBank) {
        if (GlobalData.isLinkCardChannel())
            return new LinkCardBankGridViewAdapter();
        else
            return pIsBank ? new BankSupportGridViewAdapter() : new CreditCardSupportGridViewAdapter();
    }

    @Override
    public boolean isEnabled(int position) {
        // Return true for clickable, false for not
        return false;
    }

    public int getBankListRow() {
        if (mBankCode.size() > 0)
            return mBankCode.size() / 3;

        return 1;
    }

    public boolean isBankMaintenance(String pBankCode) {
        if (BankLoader.getInstance().isBankMaintenance(pBankCode)) {
            return true;
        }
        return false;
    }

    @Override
    public int getCount() {
        return mBankCode.size();
    }

    @Override
    public Object getItem(int position) {
        return mBankCode.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ZPWItemBankHolder holder;
        ImageView imImageIcon;

        if (convertView == null) {
            convertView = LayoutInflater.from(BasePaymentActivity.getCurrentActivity().getApplicationContext()).inflate(R.layout.gridview__item__bank, null);
            holder = new ZPWItemBankHolder();
            imImageIcon = (ImageView) convertView.findViewById(R.id.imBankIcon);
            holder.imBankIcon = imImageIcon;

            convertView.setTag(holder);
        } else {
            holder = (ZPWItemBankHolder) convertView.getTag();
            imImageIcon = holder.imBankIcon;
        }

        String bankCode = mBankCode.get(position);

        if (!TextUtils.isEmpty(bankCode)) {
            Bitmap bitmap = ResourceManager.getImage(String.format("bank_%s%s", bankCode, Constants.BITMAP_EXTENSION));

            if (bitmap != null) {
                imImageIcon.setImageBitmap(bitmap);
                imImageIcon.setBackgroundResource(R.drawable.bg_card);
            } else {
                imImageIcon.setBackground(null);
            }
        }
        return convertView;
    }
}
