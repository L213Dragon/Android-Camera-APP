package com.globalreviewcenter.controller.Utilities;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 1/4/2016.
 */
public class MediaUtility {
    private static String IMAGEPATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator;

    public static boolean checkFileExist(String fileName, String PATH){

        File file = new File(PATH + fileName);
        if(file.exists()){
            return true;
        }else
            return false;
    }

    public static void setImageViewSize(ImageView imageview, int screenWidth){

        imageview.setMaxWidth(screenWidth);
        imageview.setMaxHeight(screenWidth);

    }

//    public static void setRelativeLayoutSize(RelativeLayout relativeLayout, int width, int height){
//        ViewGroup.LayoutParams layoutParams = null;
//        try{
//            layoutParams =  relativeLayout.getLayoutParams();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        layoutParams.width = width ;
//        layoutParams.height = height;
//
//
//        relativeLayout.setLayoutParams(layoutParams);
//
//
//    }
//
//    public static void setLinearLayoutSize(LinearLayout linearLayout, int width, int height){
//        ViewGroup.LayoutParams layoutParams = null;
//        try{
//            layoutParams =  linearLayout.getLayoutParams();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        layoutParams.width = width ;
//        layoutParams.height = height;
//
//
//        linearLayout.setLayoutParams(layoutParams);
//
//
//    }
    public static void setRelativeLayoutSize(RelativeLayout relativeLayout, int width, float ratio){
        ViewGroup.LayoutParams layoutParams = null;
        try{
            layoutParams =  relativeLayout.getLayoutParams();
        }catch (Exception e){
            e.printStackTrace();
        }
        layoutParams.width = width ;
        layoutParams.height = (int) ((int) width * ratio);


        relativeLayout.setLayoutParams(layoutParams);


    }

    public static void setLinearLayoutSize(LinearLayout linearLayout, int width, float ratio){
        ViewGroup.LayoutParams layoutParams = null;
        try{
            layoutParams =  linearLayout.getLayoutParams();
        }catch (Exception e){
            e.printStackTrace();
        }
        layoutParams.width = width ;
        layoutParams.height = (int) ((int) width * ratio);


        linearLayout.setLayoutParams(layoutParams);


    }
    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }
    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }
    //get bitmap from path
    public static Bitmap getBitmap(String path, int bounds) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //////
//        options.inSampleSize = 7;
        options.inSampleSize = bounds;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }
    //    convert bitmap to byte[] from Uri============================================start
    public static byte[] convertImageToBytes(Context context, Uri uri){
        byte[] data = null;
        try {
            ContentResolver cr = context.getContentResolver();
            InputStream inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            data = baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }
    //    convert bitmap to byte[] from Uri============================================end
//    convert video to byte[] from Uri============================================start
    public static byte[] convertVideoToBytes(Context context, Uri uri){
        byte[] videoBytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            FileInputStream fis = new FileInputStream(new File(getRealPathFromURI(context, uri)));
            FileInputStream fis = new FileInputStream(new File(String.valueOf(uri)));
            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);

            videoBytes = baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videoBytes;
    }
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Video.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null,
                    null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    //    convert video to byte[] from Uri============================================end

    public final static boolean isValidCharacter (String txtInput){
        Pattern pattern;
        Matcher matcher;

        final String USERNAME_PATTERN = "^[a-z0-9A-Z]{2,25}$";
        pattern = Pattern.compile(USERNAME_PATTERN);

        matcher = pattern.matcher(txtInput);
        return matcher.matches();
    }

    public static String saveProgressimageToSDCARD(Bitmap bitmap, String fileName, String folderName){
        File sdCardDirectory = new File(Environment.getExternalStorageDirectory().toString() + folderName);
        if (!sdCardDirectory.exists()) {
            sdCardDirectory.mkdirs();
        }
        String sdCardDirectoryPath = sdCardDirectory.getPath() ;
        File image = new File(sdCardDirectoryPath, fileName);
        if (image.exists()) {
            image.delete();
        }

        boolean success = false;

        // Encode the file as a PNG image.
//        FileOutputStream outStream;
        try {
//            bitmap = rotateImage(bitmap, 90);
            FileOutputStream outStream = new FileOutputStream(image);
            if (bitmap.getHeight() < 2400) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            } else if (bitmap.getHeight() > 2400 && bitmap.getHeight() < 3500) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            } else if (bitmap.getHeight() > 3500) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            }


            outStream.flush();
            outStream.close();
            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        if (success) {
            return sdCardDirectoryPath + fileName;
        } else {
            return "";
        }
    }

    public static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
//        img.recycle();
        return rotatedImg;
    }

    public static boolean saveProgressFromToGallery(Bitmap bitmap, String fileName){

        File sdCardDirectory = Environment.getExternalStorageDirectory();
        String sdCardDirectoryPath = sdCardDirectory.getPath() + "FOLDER_NAME";
        File image = new File(sdCardDirectoryPath, fileName);
        boolean success = false;

        // Encode the file as a PNG image.
        FileOutputStream outStream;
        try {
            if (bitmap.getHeight() >= bitmap.getWidth()){

            }else {
//               bitmap = rotateImage(bitmap, 90);
            }
            outStream = new FileOutputStream(image);
            if (bitmap.getHeight() < 2400) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            } else if (bitmap.getHeight() > 2400 && bitmap.getHeight() < 3500) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            } else if (bitmap.getHeight() > 3500) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            }


            outStream.flush();
            outStream.close();
            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (success) {
            return true;
        } else {
            return false;
        }
    }

    public static void garbageCollect() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    //Given the bitmap size and View size calculate a subsampling size (powers of 2)
    public static int calculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSampleSize = 1;	//Default subsampling size
        // See if image raw height and width is bigger than that of required view
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            //bigger
            final int halfHeight = options.outHeight / 2;
            final int halfWidth = options.outWidth / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
    //////rotate bitmap accoring to it's orientation
    public static Bitmap adjustBitmap(String photopath) {
        Uri uri = Uri.parse(photopath);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(uri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //get current rotation
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        ///Convert exif rotation to degrees:
        int rotationInDegrees = exifToDegrees(rotation);
        ///Then use the image's actual rotation as a reference point to rotate the image using a Matrix
        Matrix matrix = new Matrix();
        if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}

        //get BitmapFactory option
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(photopath, options);
        //////
//        options.inSampleSize = 7;
        options.inSampleSize = 3;
        options.inJustDecodeBounds = false;

        //get bitmap with local path
        Bitmap yourSelectedImage = BitmapFactory.decodeFile(photopath, options);
        ////create a new rotated image
        Bitmap adjustedBitmap = Bitmap.createBitmap(yourSelectedImage, 0, 0, yourSelectedImage.getWidth(), yourSelectedImage.getHeight(), matrix, true);
        return adjustedBitmap;
    }

    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }
    public static Drawable bitmapToDrawable(Context context, Bitmap bitmap) {
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public static void downloadImageFromURL(Context context, String fileName, String url) {

        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
//        final String fileName = url.substring(url.lastIndexOf('/') + 1);
        ///creat download request
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        if (checkFileExist(fileName, IMAGEPATH)) {
            File file = new File(IMAGEPATH + fileName);
            file.delete();
        }
        ///set destinatin storage path
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        final long downloadId = manager.enqueue(request);
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean downloading = true;
                    while (downloading) {

                        DownloadManager.Query q = new DownloadManager.Query();
                        q.setFilterById(downloadId);

                        Cursor cursor = manager.query(q);
                        cursor.moveToFirst();
                        int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        ///calculate download progress status
                        final double dl_progress = bytes_total != 0 ? (int) ((bytes_downloaded * 100l) / bytes_total) : 0;
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false;
                        }
                        cursor.close();
                    }
                }
            });

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveImageToLocal( String originalPath, String destinationPath){///
        ///destination example
        ///public static String CAMERA_ROLL = Environment.getExternalStorageDirectory().toString() + "/camera_roll" ;
        Bitmap finalBitmap = BitmapFactory.decodeFile(originalPath);
        File myDir = new File(destinationPath);
        myDir.mkdirs();
        Random generator = new Random();
        ///generate random number
//        int n = 10000;
//        n = generator.nextInt(n);
//        String fname = "Image-"+ n +".jpg";

        File file = new File(myDir, originalPath.substring(originalPath.lastIndexOf("/") + 1));
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            if(finalBitmap != null){
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            }

            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
