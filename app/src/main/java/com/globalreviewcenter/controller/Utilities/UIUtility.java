package com.globalreviewcenter.controller.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Administrator on 1/4/2016.
 */
public class UIUtility {
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
    public static int getScreenWidthDP(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int dp = Math.round(width / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

        return dp;
    }
    public static int getScreenWidthPixel(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
//        int dp = Math.round(width / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

        return width;
    }
    public static Typeface getFont(Context ctx) {

        AssetManager assetManager = ctx.getAssets();

        Typeface tf = Typeface.createFromAsset(assetManager, "");

        return tf;
    }
//    public static void hideSoftKeyboard(Context context, View view) {
//        try {
//            InputMethodManager inputMethodManager = (InputMethodManager) context
//                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
//            inputMethodManager
//                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    public static void showSoftKeyboard(Context context, EditText editText) {
        InputMethodManager mImm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mImm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
    public static boolean keyboardShown(Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isAcceptingText()) {
            return true;
        } else {
            return false;
        }


    }
    public static void dismissKeyboard(Context mContext, View registerPlan) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                registerPlan.getWindowToken(), 0);

    }
}
