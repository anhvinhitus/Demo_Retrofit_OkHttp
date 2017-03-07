package vn.com.zalopay.wallet.view.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.adapter.holder.ZPWItemBankAccountHolder;

/***
 * Vietcombank card have 2 account
 */
public class VietComBankAccountListViewAdapter extends ArrayAdapter<String> {
    private Activity mActivity;
    private ArrayList<String> mAccountList = null;
    private int mLayoutId;

    private HashMap<String, ImageView> iconList;

    private int mSelectedIndex;

    //getter and setter
    public VietComBankAccountListViewAdapter(Activity pActivity, int pLayoutId, ArrayList<String> pAccountList) {
        super(pActivity, pLayoutId, pAccountList);

        this.mActivity = pActivity;
        this.mLayoutId = pLayoutId;
        this.mAccountList = pAccountList;
        this.iconList = new HashMap<>();

        mSelectedIndex = 0;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int pPosition) {
        if (pPosition >= 0) {
            mSelectedIndex = pPosition;

            Iterator it = iconList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();

                if (pair.getKey().toString().equals(String.valueOf(pPosition)))
                    ((ImageView) pair.getValue()).setVisibility(View.VISIBLE);
                else
                    ((ImageView) pair.getValue()).setVisibility(View.GONE);
            }
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TextView txtAccountName;
        ImageView imIcon;
        View line;

        ZPWItemBankAccountHolder holder;
        //the first load
        if (convertView == null) {
            LayoutInflater inflater = this.mActivity.getLayoutInflater();
            convertView = inflater.inflate(mLayoutId, null);
            holder = new ZPWItemBankAccountHolder();

            txtAccountName = (TextView) convertView.findViewById(R.id.txtAccountName);
            imIcon = (ImageView) convertView.findViewById(R.id.imIcon);
            line = convertView.findViewById(R.id.line);

            holder.txtAccountName = txtAccountName;
            holder.imIcon = imIcon;
            holder.line = line;
            convertView.setTag(holder);
        } else {
            holder = (ZPWItemBankAccountHolder) convertView.getTag();
            txtAccountName = holder.txtAccountName;
            imIcon = holder.imIcon;
            line = holder.line;
        }


        if (position == this.mAccountList.size() - 1)
            line.setVisibility(View.GONE);
        else
            line.setVisibility(View.VISIBLE);

        final String accountName = this.mAccountList.get(position);

        try {

            if (!TextUtils.isEmpty(accountName)) {
                if (!iconList.containsKey(String.valueOf(position)))
                    iconList.put(String.valueOf(position), imIcon);

                imIcon.setImageBitmap(ResourceManager.getImage(RS.drawable.ic_checked));

                txtAccountName.setText(accountName);
            }

        } catch (Exception ex) {
            Log.e(this, ex);
        }

        return convertView;
    }
}

