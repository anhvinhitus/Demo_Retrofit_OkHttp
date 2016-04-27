package vn.com.vng.zalopay.balancetopup.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import vn.com.vng.zalopay.R;

/**
 * Created by longlv on 26/04/2016.
 */
public class BankSpinner extends Spinner {

    private final CharSequence[] depositOptions = {
            "Chọn nguồn nạp",
            "Điểm giao dịch",
            "Vietcombank",
            "Techcombank",
            "Thẻ nội địa",
            "Thẻ Visa/MasterCard",
            "Tìm hiểu về phí nạp tiền",
    };
    private final int[] depositIcons = {
            R.drawable.logo_no_card,
            R.drawable.logo_vng,
            R.drawable.logo_vcb,
            R.drawable.logo_techcombank,
            R.drawable.logo_123pay_1,
            R.drawable.logo_visa,
            R.drawable.logo_no_card
    };

    public int getSelectedIcon() {
        int position = getSelectedItemPosition();
        if (getAdapter() == null) {
            return -1;
        }

        return ((DepositAdapter)getAdapter()).getIcon(position);
    }

    public CharSequence getSelectedCharSequence() {
        int position = getSelectedItemPosition();
        if (getAdapter() == null) {
            return null;
        }
        return ((DepositAdapter)getAdapter()).getCharSequence(position);
    }

    public void initData(Context context) {
        DepositAdapter adapter = new DepositAdapter(context,R.layout.item_text_with_icon,depositOptions,depositIcons);
        adapter.setDropDownViewResource(R.layout.item_text_with_icon);
        // Apply the adapter to the spinner
        setAdapter(adapter);
    }

    public BankSpinner(Context context) {
        super(context);
        initData(context);
    }

    public BankSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BankSpinner(Context context, int mode) {
        super(context, mode);
        initData(context);
    }

    public BankSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BankSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
        initData(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BankSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
        initData(context);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public BankSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes, mode, popupTheme);
        initData(context);
    }

    public class DepositAdapter extends ArrayAdapter<CharSequence> {

        private final Context context;
        private final CharSequence[] items;
        private final int[] icons;
        private int mItemLayout;

        public DepositAdapter(Context context, int itemLayout, CharSequence[] items, int[] icons) {
            super(context, itemLayout, items);
            // TODO Auto-generated constructor stub
            this.context=context;
            this.items=items;
            this.icons=icons;
            this.mItemLayout = itemLayout;
        }

        public int getIcon(int position) {
            if (icons == null) {
                return -1;
            }
            if (icons.length <= position) {
                return -1;
            }
            return icons[position];
        }

        public CharSequence getCharSequence(int position) {
            if (items == null) {
                return null;
            }
            if (items.length <= position) {
                return null;
            }
            return items[position];
        }

        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                LayoutInflater inflater= LayoutInflater.from(context);
                view = inflater.inflate(mItemLayout, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            if (icons[position] > 0) {
                holder.icon.setImageResource(icons[position]);
                holder.icon.setVisibility(View.VISIBLE);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
            holder.text.setText(items[position]);

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        public class ViewHolder{
            private ImageView icon;
            private TextView text;

            ViewHolder(View view){
                if (view == null) {
                    return;
                }
                icon = (ImageView)view.findViewById(R.id.im_icon);
                text = (TextView)view.findViewById(R.id.tv_item);
            }
        }
    }
}
