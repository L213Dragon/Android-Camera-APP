package com.globalreviewcenter.controller.Utilities;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by Administrator on 1/4/2016.
 */
public class StringUtility {
    public static String capitalize(String str) {
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
    public static String getEmoji(String str) {
        String strKey = "\\ud83d";
        while(str.contains(strKey)) {

            int start = str.indexOf("\\ud83d");
            String str3 = str.substring(start , start + 12);
            ///get emoji from string
            String str4 = getUnicode(str3);
            //replace string with emoji
            String result = str.replace(str3, str4);
            str = result;

        }
        return str;
    }
    public static String getUnicode(String myString) {
        String str = myString.split(" ")[0];
        str = str.replace("\\","");
        String[] arr = str.split("u");
        String text = "";
        for(int i = 1; i < arr.length; i++){
            int hexVal = Integer.parseInt(arr[i], 16);
            text += (char)hexVal;
        }
        return text;
    }
    ////seperate string by comma (",")
    private ArrayList<String> getAmenities(String strTag) {
        ArrayList<String> arrString = new ArrayList<String >();
        String strKey = ",";
        int first = 0;
        int cutNum = 0;
        do{
            int second = strTag.indexOf(strKey);
            int length = strTag.length();
            if(second == -1){

                second = length;
                cutNum = second;
            }else{
                cutNum = second + 1;
            }
            String str3 = strTag.substring(first, second);
            arrString.add(str3);
//            first = 1;
            strTag = strTag.substring(cutNum, length);
        }while (strTag.contains(strKey));
        if(!strTag.isEmpty()){
            arrString.add(strTag);
        }

        return arrString;
    }
}
