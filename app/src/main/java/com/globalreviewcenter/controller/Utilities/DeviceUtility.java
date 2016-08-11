package com.globalreviewcenter.controller.Utilities;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

/**
 * Created by Administrator on 1/26/2016.
 */
public class DeviceUtility {
    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return StringUtility.capitalize(model);
        }
        return StringUtility.capitalize(manufacturer) + " " + model;
    }

//    private static String capitalize(String str) {
//        if (TextUtils.isEmpty(str)) {
//            return str;
//        }
//        char[] arr = str.toCharArray();
//        boolean capitalizeNext = true;
//        String phrase = "";
//        for (char c : arr) {
//            if (capitalizeNext && Character.isLetter(c)) {
//                phrase += Character.toUpperCase(c);
//                capitalizeNext = false;
//                continue;
//            } else if (Character.isWhitespace(c)) {
//                capitalizeNext = true;
//            }
//            phrase += c;
//        }
//        return phrase;
//    }
    public static String getPhoneNumber(Context context) {
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();
        return mPhoneNumber;
    }
    public static String getSimSerialNumber(Context context) {
        TelephonyManager telemamanger = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String getSimSerialNumber = telemamanger.getSimSerialNumber();
        return getSimSerialNumber;
    }
}
