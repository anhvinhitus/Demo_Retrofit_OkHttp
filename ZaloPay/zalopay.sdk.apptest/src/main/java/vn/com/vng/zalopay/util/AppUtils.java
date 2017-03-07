package vn.com.vng.zalopay.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by admin on 8/27/16.
 */
public class AppUtils
{
    public static String convertDate(long timestamp)
    {
        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");//yyyy/MM/dd HH:mm:ss
            //get current date time with Date()
            Date date = new Date(timestamp);
            return dateFormat.format(date);
        }catch (Exception e)
        {

        }
        return "";
    }
}
