package com.android.volley.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.multipart.FilePart;
import com.android.volley.toolbox.multipart.MultipartEntity;
import com.android.volley.toolbox.multipart.StringPart;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class CustomMultipartRequest extends Request<JSONObject> {
    public static final String KEY_PICTURE = "mypicture";
    public static final String KEY_PICTURE_NAME = "filename";
    public static final String KEY_ROUTE_ID = "route_id";

//    private HttpEntity mHttpEntity;
    private MultipartEntity multipartEntity;

    private String mRouteId;
    private Response.Listener mListener;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////start
    public CustomMultipartRequest(String url, Response.Listener<JSONObject> listener,
                                  Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        multipartEntity = new MultipartEntity();
        mListener = listener;
//        mHttpEntity = new MultipartEntity() ;

    }
    public CustomMultipartRequest addStringPart(String key, String value) {
        StringPart stringPart = new StringPart(key, value, "UTF-8");
        multipartEntity.addPart(stringPart);
        return this;
    }
    public CustomMultipartRequest addVideoPart(String key, String filePath) {
        FilePart filePart = new FilePart(key, new File(filePath), filePath.substring(filePath.lastIndexOf("/") + 1), "video/mp4");
        multipartEntity.addPart(filePart);
        return this;
    }
    public CustomMultipartRequest addImagePart(String key, String filePath) {
        FilePart filePart = new FilePart(key, new File(filePath), filePath.substring(filePath.lastIndexOf("/") + 1), "image/jpeg");
        multipartEntity.addPart(filePart);
        return this;
    }
    public CustomMultipartRequest addAudioPart(String key, String filePath) {
        FilePart filePart = new FilePart(key, new File(filePath), filePath.substring(filePath.lastIndexOf("/") + 1), "audio/wav");
        multipartEntity.addPart(filePart);
        return this;
    }
//    public HttpEntity buildMultipartEntity() {
//        mHttpEntity = multipartEntity;
//        return mHttpEntity;
//    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////end
//
//    public CustomMultipartRequest(String url, String filePath, String routeId,
//                                  Response.Listener<String> listener,
//                                  Response.ErrorListener errorListener) {
//        super(Method.POST, url, errorListener);
//
//        mRouteId = routeId;
//        mListener = listener;
//        mHttpEntity = buildMultipartEntity(filePath);
//    }
//
//    public CustomMultipartRequest(String url, File file, String routeId,
//                                  Response.Listener<String> listener,
//                                  Response.ErrorListener errorListener) {
//        super(Method.POST, url, errorListener);
//
//        mRouteId = routeId;
//        mListener = listener;
//        mHttpEntity = buildMultipartEntity(file);
//    }
//
//    private HttpEntity buildMultipartEntity(String filePath) {
//        File file = new File(filePath);
//        return buildMultipartEntity(file);
//    }
//
//    private HttpEntity buildMultipartEntity(File file) {
//        MultipartEntity multipartEntity = new MultipartEntity();
//        FilePart filePart = new FilePart(KEY_PICTURE_NAME, file, KEY_PICTURE_NAME, "image/jpg");
//        multipartEntity.addPart(filePart);
//        return multipartEntity;
////        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
////        String fileName = file.getName();
////        FileBody fileBody = new FileBody(file);
////        builder.addPart(KEY_PICTURE, fileBody);
////        builder.addTextBody(KEY_PICTURE_NAME, fileName);
////        builder.addTextBody(KEY_ROUTE_ID, mRouteId);
////        return builder.build();
//    }

    @Override
    public String getBodyContentType() {
        return multipartEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            multipartEntity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

//    @Override
//    protected Response<String> parseNetworkResponse(NetworkResponse response) {
//        return Response.success("Uploaded", getCacheEntry());
//    }
//
//    @Override
//    protected void deliverResponse(String response) {
//        mListener.onResponse(response);
//    }
    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
        String jsonString = new String(response.data,
                HttpHeaderParser.parseCharset(response.headers));
        return Response.success(new JSONObject(jsonString),
                HttpHeaderParser.parseCacheHeaders(response));
    } catch (UnsupportedEncodingException e) {
        return Response.error(new ParseError(e));
    } catch (JSONException je) {
        return Response.error(new ParseError(je));
    }
}

    @Override
    protected void deliverResponse(JSONObject response) {
        // TODO Auto-generated method stub
        mListener.onResponse(response);
    }
}