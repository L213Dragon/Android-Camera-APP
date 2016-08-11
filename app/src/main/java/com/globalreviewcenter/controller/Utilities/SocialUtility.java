package com.globalreviewcenter.controller.Utilities;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 1/26/2016.
 */
public class SocialUtility {
    private void sendEmail(Context mContext, String[] recipients, String subject){
        Intent intent = new Intent(Intent.ACTION_SEND);
        if(intent == null){
            Utils.showToast(mContext, "Not Available to send mail.");
        }else{
//            String[] recipients = {"fafuserservices@gmail.com"};
            intent.putExtra(Intent.EXTRA_EMAIL, recipients);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_CC, DeviceUtility.getDeviceName());
            intent.setType("message/rfc822");
            mContext.startActivity(Intent.createChooser(intent, "Send mail"));
        }

    }
    private void sharingText(Context mContext, String subject, String title, String shareBody){

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        mContext.startActivity(Intent.createChooser(sharingIntent, title));

    }
}
