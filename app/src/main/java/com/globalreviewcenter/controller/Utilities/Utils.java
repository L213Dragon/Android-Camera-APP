package com.globalreviewcenter.controller.Utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.globalreviewcenter.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hic on 2015-11-20.
 */
public class Utils {

    private static ProgressDialog mProgressDialog;

    public static void garbageCollect() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public static boolean haveNetworkConnection(Context activity) {
        ConnectivityManager connectivity = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public static void showOKDialog(Context context, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Global Review Center");
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });
//        builder1.setNegativeButton("No",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//        builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface arg0, int arg1) {
//                // TODO Auto-generated method stub
//                Toast.makeText(getApplicationContext(), "Close is clicked", Toast.LENGTH_LONG).show();
//
//            }
//        });
//        dialog.setCancelable(true);
//        dialog.setCanceledOnTouchOutside(false);
        AlertDialog alert = builder.create();
        alert.show();
    }
    public static void showToast(final Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static Dialog showProgressDialog(Context mContext, String text,
                                            boolean cancelable) {
        Dialog mDialog = new Dialog(mContext, R.style.ProgressBarTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater mInflater = LayoutInflater.from(mContext);
        mDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        View layout = mInflater.inflate(R.layout.custom_progressbar, null);
        mDialog.setContentView(layout);

        TextView tvProgressMessage = (TextView) layout
                .findViewById(R.id.tvProgressMessage);

        if (text.equals(""))
            tvProgressMessage.setVisibility(View.GONE);
        else
            tvProgressMessage.setText(text);

        mDialog.setCancelable(cancelable);
        mDialog.setCanceledOnTouchOutside(false);
		/*
		 * mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND
		 * );
		 */
        mDialog.show();

        return mDialog;
    }
    public static synchronized void hideProgress () {
        if (mProgressDialog != null)
        {
            try
            {
                mProgressDialog.dismiss();
            }
            catch (Exception ex)
            {}
            finally
            {
                mProgressDialog = null;
            }

        }
    }

    public static void showProgress(Context mContext) {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            return;

        mProgressDialog = new ProgressDialog(mContext, R.style.ProgressDialogTheme);
        mProgressDialog.setCancelable(false);

        mProgressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        mProgressDialog.show();
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences objSharedPreferences = null;
        try {
            objSharedPreferences = context.getSharedPreferences(
                    "APP_INDECATOR", Context.MODE_PRIVATE);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return objSharedPreferences;
    }
    public static void setOnPreference(Context context, String key, String value){
        SharedPreferences preferences = getSharedPreferences(context);
        /////////save user information in preference
        SharedPreferences.Editor edit = preferences.edit();

        edit.putString(key, value);


        edit.commit();
    }
    public static String getFromPreference(Context context, String key) {
        return getSharedPreferences(context).getString(key, "");
    }

    public static void sendEmail(Context mContext, String toAddress) {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[] { toAddress });
        email.putExtra(Intent.EXTRA_SUBJECT, "Perceptual Yoga");
        email.putExtra(
                Intent.EXTRA_TEXT,
                "Namaste\n\nKeep track of your Yoga activity such as Practice, Teaching and Learning, browse our members database, share your achievements and more. \nDownload Perpetual Yoga \n Find it at your App Store and Google Play \n https://play.google.com/store?hl=en&tab=i8");
        email.setType("message/rfc822");
        mContext.startActivity(Intent.createChooser(email,
                "Choose an Email client :"));
    }

    public static void call(Context mContext, String phone) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phone));
//        mContext.startActivity(callIntent);
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (;;) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }







    public static JSONObject getJSONGetPractitionerRecords(String contactNo) {
        HashMap<String, Object> outerMap = new HashMap<String, Object>();
        HashMap<String, String> innerMap = new HashMap<String, String>();

        innerMap.put("contactNo", contactNo);
        outerMap.put("practitionerDetails", new JSONObject(innerMap));

        return new JSONObject(outerMap);
    }

    public static String convertStreamToString(InputStream is)
            throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[2048];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is,
                    "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        String text = writer.toString();
        return text;
    }

    public static String getSimCountryIso(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimCountryIso().toUpperCase();
    }

    public static String getNetworkCountryIso(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkCountryIso().toUpperCase();
    }

    public static boolean isValidPhoneNumber(CharSequence phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            return Patterns.PHONE.matcher(phoneNumber).matches();
        }
        return false;
    }

    //////check email

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
                    .matches();
        }
    }
    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
    ///send email
    private void sendEmail(Context mContext, String[] recipients, String subject){
        Intent intent = new Intent(Intent.ACTION_SEND);
        if(intent == null){
            this.showToast(mContext, "Not Available to send mail.");
        }else{
//            String[] recipients = {"fafuserservices@gmail.com"};
            intent.putExtra(Intent.EXTRA_EMAIL, recipients);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_CC, getDeviceName());
            intent.setType("message/rfc822");
            mContext.startActivity(Intent.createChooser(intent, "Send mail"));
        }

    }

    //sharing
    private void sharing(Context context, String shareBody){
//        String shareBody = "Fix-A-Friend is a great photo & video sharing and editing app." +
//                "Please download from Google Play Store and enjoy!" +
//                " https://play.google.com/store/apps/details?id=************************";

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Global Review Center");

        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));

    }
    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

}
