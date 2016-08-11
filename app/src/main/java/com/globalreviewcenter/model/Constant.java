package com.globalreviewcenter.model;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2/1/2016.
 */
public class Constant {
    public static  String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator ;

    public static String video_upload_url = "http://app.globalreviewcenter.com/videoupload.php";
    public static String audio_upload_url = "http://app.globalreviewcenter.com/audioupload.php";
    public static String text_upload_url = "http://app.globalreviewcenter.com/textupload.php";
    //public static String text_upload_url = "http://app.globalreviewcenter.com/textupload.php";


}
