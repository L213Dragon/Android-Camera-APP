package com.globalreviewcenter.view.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.CustomMultipartRequest;
import com.android.volley.toolbox.Volley;
import com.globalreviewcenter.R;
import com.globalreviewcenter.controller.Utilities.FileUtility;
import com.globalreviewcenter.controller.Utilities.MediaUtility;
import com.globalreviewcenter.controller.Utilities.UIUtility;
import com.globalreviewcenter.controller.Utilities.Utils;
import com.globalreviewcenter.controller.Utilities.VideoPlay;
import com.globalreviewcenter.model.Constant;
import com.globalreviewcenter.view.HomeActivity;
import com.globalreviewcenter.widget.CameraPreview;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_LONG;


public class MainFragment extends Fragment implements OnClickListener{

    private HomeActivity homeActivity;
    private EditText etFirstName, etLastName, etCompanyName, etCity, etCountry, etEmail;
    private TextView tvVideo, tvAudio, tvText;
    private RelativeLayout rlpurchasePhoto,rlAdditionalPhoto, rlVideo, rlAudio, rlText;
    private ImageView[] ivStars;
    private ImageView ivCheck;
    private Button btnSubmit;
    private LinearLayout  llPhotoContainer;
    private LinearLayout llVideoContainer;
    RelativeLayout rlMainPurcharsePhotoBtn, rlAdditionalPhotoBtn;

    String firstname, lastname, email, company, city, country, message, proofPhotoPath, additionalPhotoPath, audioPath, videoPath, reviewText;
    int rate;
    boolean isAgree;

    private LayoutInflater mLayoutInfrater;
    /////////////////for capture photo & video
    private ProgressBar progressBar;
    private Button btnChangeCamera;
    private Button btnStartCapturePhoto;
    private Button btnStartCaptureVideo;
    private boolean isVideo = false;
    //////////////////////////
    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private LinearLayout cameraPreview;
    //////// for video capture
    private MediaRecorder mediaRecorder;
    boolean recording = false;
    /////////////for write text
    EditText etText;
    int currentState;///0: no visible, 1: photo/show, 2:photo/hide, 3: video/show, 4: video/hide, 5: audio/show, 6: audio/hide, 7: text/show, 8: text/hide
    int photoState; //0: purchasePhoto, 1: additional photo
    int photoShowHide = 0;//0: hide, 1: show
    ////
    private View captureView, recordAudioView, writeTextView, photoView, videoView;
    private VideoView videoPlayView;
    private ImageView imageView;
    private TextView tvCapturing , tvRecording;

    private MediaPlayer mediaPlayer;

    Button btnPlayAudio;

    Button btnRecord;


    ///for record audio
    private TimerTask timerTask = null;
    Timer timer;
    boolean timerState = false;
    boolean isPlayingAudio = false;
    int audioLength = 1200;
    private int cameraId = -1;
    private Camera.Size preferredPreviewSize;
    private boolean isCameraFront = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initVariables();
        initUI(view);
        initViews();
        initTimer();
        return view;
    }
    ////only to control progress bar of recording and playing audio
    private void initTimer(){

        final Handler handler = new Handler();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (timerState) {
                            int time =progressBar.getProgress();
                            if (time < audioLength) {
                                time ++;
                                progressBar.setProgress(time );
                            } else if (!isPlayingAudio){
                                timerState = false;
                                stopRecordingAudio();
                            } else {
                                timerState = false;
                            }
                        }
                    }
                });
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 50);

    }
    /////record audio*****
    View.OnClickListener startRecordAudioListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            try {
                if (!timerState) {
                    timerState = true;
                    progressBar.setProgress(0);
                    audioLength = 1200;
                    isPlayingAudio = false;
                    tvRecording.setText("Recording...");
                    startRecordingAudio();

                    btnPlayAudio.setVisibility(View.GONE);
                } else {
                    timerState = false;
                    tvRecording.setText("Touch down to record");
                    stopRecordingAudio();

                    btnPlayAudio.setVisibility(View.VISIBLE);
                }
            }catch (Exception e){

            }
        }
    };

    View.OnTouchListener stopRecordAudioListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (timerState) {
                    tvRecording.setText("Touch down to record");
                    stopRecordingAudio();
                }
            }
            return timerState;
        }
    };
    public void startRecordingAudio()
    {
        // Verify that the device has a mic
        PackageManager pmanager = getActivity().getPackageManager();
        if (!pmanager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            Toast.makeText(getActivity(), "This device doesn't have a mic!", Toast.LENGTH_LONG).show();
            return;
        }
        // Start the recording

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(Constant.PATH + "audio.wav");

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            timerState = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stopRecordingAudio() {
//        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();

        finalizeRecordingParams();
    }
    private void finalizeRecordingParams() {
        timerState = false;
//        timer.cancel();
        audioLength = progressBar.getProgress();
        audioPath = Constant.PATH + "audio.wav";
    }
    ///*****
    private void submit() {
        checkField();
    }
    private void capturePhoto() {
        if (captureView != null) {
            if (mCamera == null) {
                createCamera();
            }
            if (llPhotoContainer.getChildCount() > 0) {
                llPhotoContainer.removeAllViews();
            }
            llPhotoContainer.addView(captureView);
            btnStartCaptureVideo.setVisibility(View.GONE);
            btnStartCapturePhoto.setVisibility(View.VISIBLE);
            tvCapturing.setVisibility(View.INVISIBLE);
        }
    }
    private void showPhoto() {
        if (photoView != null ) {
            if (llPhotoContainer.getChildCount() > 0) {
                llPhotoContainer.removeAllViews();
            }
            llPhotoContainer.addView(photoView);
            Bitmap bitmap = null;
            if (photoState == 0) {
                bitmap = MediaUtility.getBitmap(proofPhotoPath, 2);
            } else if (photoState == 1) {
                bitmap = MediaUtility.getBitmap(additionalPhotoPath, 2);
            }

            //imageView.setImageBitmap(MediaUtility.rotateImage(bitmap, 90));
            imageView.setImageBitmap(bitmap);
            if(isCameraFront)
                imageView.setImageBitmap(MediaUtility.rotateImage(bitmap, 270));
                //imageView.setRotation(270);
            else
                imageView.setImageBitmap(MediaUtility.rotateImage(bitmap, 90));
                //imageView.setRotation(0);
        }
    }
    private void captureVideo() {

        if (captureView != null) {
            if (mCamera == null) {
                createCamera();
            }
            llVideoContainer.addView(captureView);
            btnStartCapturePhoto.setVisibility(View.GONE);
            btnStartCaptureVideo.setVisibility(View.VISIBLE);
            tvCapturing.setVisibility(View.VISIBLE);
        }
    }
    private void playVideo() {
        if (videoView != null) {
            if (llVideoContainer.getChildCount() > 0) {
                llVideoContainer.removeAllViews();
            }
            llVideoContainer.addView(videoView);
            VideoPlay videoPlayer = new VideoPlay(getActivity(), videoPlayView, videoPath);
            videoPlayer.playVideo();
            videoPlayer.pauseVideo(100);
        }
    }
    private void recordAudio() {
        if (recordAudioView != null) {
            progressBar.setProgress(0);
            llVideoContainer.addView(recordAudioView);
        }
    }
    private void writeText() {
        if (writeTextView != null) {
            etText.setText("");
            llVideoContainer.addView(writeTextView);
        }
    }
    ///init custom views*****
    private void initViews() {
        initCameraView();
        initRecordAudioView();
        initWriteTextView();
        initVideoView();
        initPhotoView();
    }
    private void initPhotoView() {
        photoView = mLayoutInfrater.inflate(R.layout.photo_view, null);
        if (videoView != null) {

            imageView= (ImageView)photoView.findViewById(R.id.iv_photo_view);
            imageView.setVisibility(View.VISIBLE);

            ImageButton ibCancel = (ImageButton)photoView.findViewById(R.id.ib_photo_view_cancel);
            ibCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (photoState == 0) {
                        if (!proofPhotoPath.equals("")) {
                            FileUtility.deleteFile(proofPhotoPath);
                            proofPhotoPath = "";
                        }

                    } else if (photoState == 1) {
                        if (!additionalPhotoPath.equals("")) {
                            FileUtility.deleteFile(additionalPhotoPath);
                            additionalPhotoPath = "";
                        }

                    }

                    llPhotoContainer.removeAllViews();
                    capturePhoto();
                }
            });
        }
    }
    private void initVideoView() {
        videoView = mLayoutInfrater.inflate(R.layout.video_view, null);
        if (videoView != null) {
            videoPlayView = (VideoView)videoView.findViewById(R.id.vv_video_view);
            ImageButton ibCancel = (ImageButton)videoView.findViewById(R.id.ib_video_view_cancel);
            ibCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!videoPath.equals("")) {
                        FileUtility.deleteFile(videoPath);
                        videoPath = "";
                    }

                    llVideoContainer.removeAllViews();
                    captureVideo();
                }
            });
        }
    }
    private void initCameraView() {
        if (!hasCamera(homeActivity)) {
            Toast toast = Toast.makeText(homeActivity, "Sorry, your phone does not have a camera!", LENGTH_LONG);
            toast.show();
        }
        captureView = mLayoutInfrater.inflate(R.layout.video_capture_view, null);
        if (captureView == null) {
            return ;
        }
        ///init
        cameraPreview = (LinearLayout)captureView.findViewById(R.id.ll_video_preview);
        mPreview = new CameraPreview(homeActivity, mCamera);
        cameraPreview.addView(mPreview);
        createCamera();

        btnStartCapturePhoto = (Button)captureView.findViewById(R.id.btn_start_capture_photo);
        btnStartCaptureVideo = (Button)captureView.findViewById(R.id.btn_start_capture_video);
        btnChangeCamera = (Button)captureView.findViewById(R.id.btn_change_camera);
        tvCapturing = (TextView)captureView.findViewById(R.id.tv_video_capture_status);
        ///set onClickListener
        btnChangeCamera.setOnClickListener(switchCameraListener);
        btnStartCapturePhoto.setOnClickListener(captrurePhotoListener);

        //////////////////////////////////
        btnStartCaptureVideo.setOnClickListener(captrureVideoListener);

        ///////////////////////////
//        btnStartCaptureVideo.setOnLongClickListener(captrureVideoListener);
//        btnStartCaptureVideo.setOnTouchListener(touchListener);

    }
    private void initRecordAudioView() {
        recordAudioView = mLayoutInfrater.inflate(R.layout.audio_record_view, null);
        if (recordAudioView != null) {
            TextView tvStatus = (TextView)recordAudioView.findViewById(R.id.tv_record_status);
            btnRecord = (Button)recordAudioView.findViewById(R.id.btn_start_record);

            btnRecord.setOnClickListener(startRecordAudioListener);
            //////////////////////
//            btnRecord.setOnLongClickListener(startRecordAudioListener);
//            btnRecord.setOnTouchListener(stopRecordAudioListener);

            progressBar = (ProgressBar)recordAudioView.findViewById(R.id.progressBar_record);
            progressBar.setMax(1200);
            btnPlayAudio = (Button)recordAudioView.findViewById(R.id.btn_play_audio);

            btnPlayAudio.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setProgress(0);


                    if (MediaUtility.checkFileExist(FileUtility.getFileNameFromPath(audioPath), Constant.PATH)) {
                        mediaPlayer = MediaPlayer.create(getActivity(), Uri.parse(audioPath));
                        if (mediaPlayer != null) {
                            timerState = true;
                            isPlayingAudio = true;
                            mediaPlayer.setLooping(false);
                            mediaPlayer.start();

                            btnRecord.setClickable(false);

                            mediaPlayer.setOnCompletionListener(onCompletionListener);
                        }

                    } else {
                        Utils.showOKDialog(getActivity(), "Please record audio");
                    }

                }
            });
            tvRecording = (TextView)recordAudioView.findViewById(R.id.tv_record_status);
//            llVideoContainer.addView(recordAudioView);
        }
    }

    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            btnRecord.setClickable(true);
        }
    };

    private void initWriteTextView() {
        writeTextView = mLayoutInfrater.inflate(R.layout.write_text_view, null);
        if (writeTextView != null) {
            etText = (EditText)writeTextView.findViewById(R.id.et_write_view);
//            llVideoContainer.addView(writeTextView);
        }

    }
    ///*****
    //////////// init camera
    public void toggleCamera() {
        //if the camera preview is the front
        if (isCameraFront) {
            cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                if(!isVideo){
                    mPicture = getPictureCallback();///////////////===============*
                }
                setCameraDisplayOrientation(homeActivity, cameraId, mCamera);
                mPreview.refreshCamera(mCamera);
                isCameraFront = false;
            }
        } else {
            cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                if(!isVideo){
                    mPicture = getPictureCallback();///////////////===============*
                }
                setCameraDisplayOrientation(homeActivity, cameraId, mCamera);
                mPreview.refreshCamera(mCamera);
                isCameraFront = true;
            }
        }
    }
    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
    ///creat camera*****
    private void createCamera(){
        if (mCamera == null) {
            boolean hasFrontCamera = true;
            //if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(homeActivity, "No front facing camera found.", LENGTH_LONG).show();
                btnChangeCamera.setVisibility(View.GONE);
                hasFrontCamera = false;
            }
            /////////////////////////////////////////////////////////////////
            if(isCameraFront && hasFrontCamera){
                cameraId = findFrontFacingCamera();
            }else {
                cameraId = findBackFacingCamera();
            }
            /////////////////////////////////////////////////////////////////
            mCamera = Camera.open(cameraId);
            ////if fail to open camera then do below
            if(mCamera == null){
                Utils.showOKDialog(homeActivity, "Your device does not support camera for this app!");
                return;
            }
            //if success to open camera
            ///set camera parameters
            DisplayMetrics display = this.getResources().getDisplayMetrics();
            int width = display.widthPixels;
            int height = display.heightPixels;

            Camera.Parameters parameters = mCamera.getParameters();

            /////////set orientation.
            parameters.set("orientation", "portrait");
            //parameters.setRotation(90);

            preferredPreviewSize = pickPreferredPreviewSize(mPreview);

            parameters.setPreviewSize(preferredPreviewSize.width, preferredPreviewSize.height);
            parameters.setPictureSize(preferredPreviewSize.width, preferredPreviewSize.height);
            mCamera.setParameters(parameters);

            int optimalwidth = preferredPreviewSize.width;
            int optimalheight = preferredPreviewSize.height;

            float ratio = (float)optimalwidth / (float)optimalheight;

            MediaUtility.setLinearLayoutSize(llPhotoContainer, width , ratio);
            MediaUtility.setLinearLayoutSize(llVideoContainer,width , ratio);
            setCameraDisplayOrientation(homeActivity, cameraId, mCamera);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        }
    }
    public static void setCameraDisplayOrientation(HomeActivity activity,int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //get the number of cameras
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    //release the old camera instance
                    //switch camera, from the front and the back and vice versa

                    releaseCamera();
                    toggleCamera();
                } else {
                    Toast toast = Toast.makeText(homeActivity, "Sorry, your phone has only one camera!", LENGTH_LONG);
                    toast.show();
                }
            }
        }
    };
    @Override
    public void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCamera == null) {
            createCamera();
        }
    }

    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    //////////////////take photo
    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //make a new picture file
//                File pictureFile = getOutputMediaFile();
                String fileName = "";
                if (photoState == 0) {
                    fileName = Constant.PATH + "proofphoto.jpg";
                }else if (photoState == 1) {
                    fileName = Constant.PATH + "additionalphoto.jpg";
                }

                File pictureFile = new File(fileName);
                if (!pictureFile.getParentFile().exists()) {
                    pictureFile.getParentFile().mkdir();
                }
                if (pictureFile == null) {
                    return;
                }
                try {
                    //write the file
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    bos.write(data);
                    bos.flush();
                    bos.close();
                    if (photoState == 0) {
                        proofPhotoPath = Constant.PATH + "proofphoto.jpg";
                    }else if (photoState == 1) {
                        additionalPhotoPath = Constant.PATH + "additionalphoto.jpg";
                    }

                    showPhoto();

                } catch (FileNotFoundException e) {
                    Toast.makeText(homeActivity, "Picture Failed" + e.toString(),
                            LENGTH_LONG).show();
                } catch (IOException e) {
                }
                //refresh camera to continue preview
                mPreview.refreshCamera(mCamera);
            }
        };
        return picture;
    }
    View.OnClickListener captrurePhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.takePicture(null, null, mPicture);
        }
    };
    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    //
    //////////////////Video capture//////

    View.OnClickListener captrureVideoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!recording) {
                if (!prepareMediaRecorder()) {
                    Toast.makeText(homeActivity, "Fail in prepareMediaRecorder()!\n - Ended -", LENGTH_LONG).show();
                }
                // work on UiThread for better performance
                homeActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        // If there are stories, add them to the table
                        try {
                            recording = true;
                            mediaRecorder.start();
                            tvCapturing.setText("Recording...");
                        } catch (final Exception ex) {
                            // Log.i("---","Exception in thread");
                        }
                    }
                });
            } else {
                recording = false;

                releaseMediaRecorder(); // release the MediaRecorder object
                videoPath = Constant.PATH + "grc.mp4";
                tvCapturing.setText("Touch down to record");
                playVideo();
            }
        }
    };

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    private boolean prepareMediaRecorder() {

        releaseCamera();
        createCamera();
        mCamera.unlock();
        mediaRecorder = new MediaRecorder();

        mediaRecorder.setCamera(mCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        configureRecorderProfile(mediaRecorder);
        mediaRecorder.setOutputFile(Constant.PATH + "grc.mp4");
        //rotate video orientation
        if (isCameraFront) {
            mediaRecorder.setOrientationHint(270);
        } else {
            mediaRecorder.setOrientationHint(90);
        }

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    private void configureRecorderProfile(MediaRecorder recorder) {
        CamcorderProfile profile = null;
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
            profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
            profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        } else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH)) {
            profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        } else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) {
            profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        }
        if (profile != null) {
            if (preferredPreviewSize != null) {
                profile.videoFrameHeight = preferredPreviewSize.height;
                profile.videoFrameWidth = preferredPreviewSize.width;
            }
            recorder.setProfile(profile);
        }
    }


    @Nullable
    public Camera.Size pickPreferredPreviewSize(SurfaceView textureView) {
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> supportedSizes = parameters.getSupportedVideoSizes();
        if (supportedSizes == null) {
            //preview and video size could be different
            supportedSizes = parameters.getSupportedPreviewSizes();
        }

        if (preferredPreviewSize == null) {
            preferredPreviewSize = findOptimalPreview(supportedSizes, parameters.getSupportedPreviewSizes());
        }

        if (preferredPreviewSize == null) {
            preferredPreviewSize = parameters.getPreferredPreviewSizeForVideo();
        }

        if (preferredPreviewSize != null) {
            try {
                parameters.setPreviewSize(preferredPreviewSize.width, preferredPreviewSize.height);
                mCamera.setParameters(parameters);
                return preferredPreviewSize;
            } catch (Exception e) {
                e.printStackTrace();
                preferredPreviewSize = null;
            }
        }
        return parameters.getPreviewSize();
    }

    private Camera.Size findOptimalPreview(List<Camera.Size> sizes, List<Camera.Size> supportedPreviewSizes) {
        double targetRatio = 1;
        double screenWidth = getResources().getDisplayMetrics().widthPixels;
        if (sizes == null) {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            double sizeDifference = size.height / screenWidth;
            if (Math.abs(ratio - targetRatio) < minDiff && sizeDifference < 1.0 && sizeDifference > 0.2 && supportedPreviewSizes.contains(size)) {
                optimalSize = size;
                minDiff = Math.abs(ratio - targetRatio);
            }
        }
        return optimalSize;
    }

    @Override
    public void onClick(View v) {
        if (!(v instanceof EditText) && UIUtility.keyboardShown(homeActivity)) {
            UIUtility.hideSoftKeyboard(homeActivity);
        }
        if (v == rlAdditionalPhoto) {
            if (photoShowHide == 0) {

                llPhotoContainer.setVisibility(View.VISIBLE);

                rlAdditionalPhotoBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_dark));
                rlMainPurcharsePhotoBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));

                rlVideo.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvVideo.setTextColor(getResources().getColor(R.color.grey));
                tvVideo.setTypeface(null, Typeface.NORMAL);

                rlAudio.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvAudio.setTextColor(getResources().getColor(R.color.grey));
                tvAudio.setTypeface(null, Typeface.NORMAL);

                rlText.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvText.setTextColor(getResources().getColor(R.color.grey));
                tvText.setTypeface(null, Typeface.NORMAL);

                capturePhoto();
                photoShowHide = 1;
            } else {
                if (llPhotoContainer.getChildCount() > 0 ) {
                    rlAdditionalPhotoBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                    llPhotoContainer.removeAllViews();

                    llPhotoContainer.setVisibility(View.GONE);
                }
                photoShowHide = 0;
            }


            photoState = 1;
        }
        if (v == rlpurchasePhoto) {
            if (photoShowHide == 0) {

                llPhotoContainer.setVisibility(View.VISIBLE);

                rlMainPurcharsePhotoBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_dark));
                rlAdditionalPhotoBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));

                rlVideo.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvVideo.setTextColor(getResources().getColor(R.color.grey));
                tvVideo.setTypeface(null, Typeface.NORMAL);

                rlAudio.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvAudio.setTextColor(getResources().getColor(R.color.grey));
                tvAudio.setTypeface(null, Typeface.NORMAL);

                rlText.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvText.setTextColor(getResources().getColor(R.color.grey));
                tvText.setTypeface(null, Typeface.NORMAL);

                capturePhoto();
                photoShowHide = 1;
            } else {
                if (llPhotoContainer.getChildCount() > 0 ) {
                    rlMainPurcharsePhotoBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                    llPhotoContainer.removeAllViews();
                    llPhotoContainer.setVisibility(View.GONE);
                }
                photoShowHide = 0;
            }
            photoState = 0;

        }
        if (v == rlVideo) {
            if (proofPhotoPath.length() > 0) {
                deleteFile(proofPhotoPath);
                proofPhotoPath = "";
            }
            if (additionalPhotoPath.length() > 0) {
                deleteFile(additionalPhotoPath);
                additionalPhotoPath = "";
            }
            if (audioPath.length() > 0) {
                deleteFile(audioPath);
                audioPath = "";
            }
            reviewText = "";
            photoShowHide = 0;
            rlAdditionalPhoto.setVisibility(View.GONE);
            rlpurchasePhoto.setVisibility(View.GONE);
            rlMainPurcharsePhotoBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
            rlAdditionalPhotoBtn.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
            if (currentState != 3) {
                if (llPhotoContainer.getChildCount() > 0) {
                    llPhotoContainer.removeAllViews();
                }
                if (llVideoContainer.getChildCount() > 0 ) {
                    llVideoContainer.removeAllViews();

                }
                llPhotoContainer.setVisibility(View.GONE);
                llVideoContainer.setVisibility(View.VISIBLE);


                rlVideo.setBackground(getResources().getDrawable(R.drawable.round_corner_strong_grey));
                tvVideo.setTextColor(getResources().getColor(R.color.dark));
                tvVideo.setTypeface(null, Typeface.BOLD);

                rlAudio.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvAudio.setTextColor(getResources().getColor(R.color.grey));
                tvAudio.setTypeface(null, Typeface.NORMAL);

                rlText.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvText.setTextColor(getResources().getColor(R.color.grey));
                tvText.setTypeface(null, Typeface.NORMAL);

                captureVideo();
                currentState = 3;
            } else {
                rlVideo.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvVideo.setTextColor(getResources().getColor(R.color.grey));
                tvVideo.setTypeface(null, Typeface.NORMAL);

                if (llVideoContainer.getChildCount() > 0 ) {
                    llVideoContainer.removeAllViews();
                    currentState = 4;
                    llVideoContainer.setVisibility(View.GONE);
                }
            }

        }
        if (v == rlAudio) {
            if (videoPath.length() > 0) {
                deleteFile(videoPath);
                videoPath = "";
            }
            reviewText = "";
            rlAdditionalPhoto.setVisibility(View.VISIBLE);
            rlpurchasePhoto.setVisibility(View.VISIBLE);
            if (currentState != 5) {
                if (llVideoContainer.getChildCount() > 0 ) {
                    llVideoContainer.removeAllViews();
                }

                llVideoContainer.setVisibility(View.VISIBLE);

                rlVideo.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvVideo.setTextColor(getResources().getColor(R.color.grey));
                tvVideo.setTypeface(null, Typeface.NORMAL);

                rlAudio.setBackground(getResources().getDrawable(R.drawable.round_corner_strong_grey));
                tvAudio.setTextColor(getResources().getColor(R.color.dark));
                tvAudio.setTypeface(null, Typeface.BOLD);

                rlText.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvText.setTextColor(getResources().getColor(R.color.grey));
                tvText.setTypeface(null, Typeface.NORMAL);
                recordAudio();
                currentState = 5;
            } else {
                rlAudio.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvAudio.setTextColor(getResources().getColor(R.color.grey));
                tvAudio.setTypeface(null, Typeface.NORMAL);
                if (llVideoContainer.getChildCount() > 0 ) {
                    llVideoContainer.removeAllViews();
                    currentState = 6;
                    llVideoContainer.setVisibility(View.GONE);
                }
            }

        }
        if (v == rlText) {
            if (videoPath.length() > 0) {
                deleteFile(videoPath);
                videoPath = "";
            }
            if (audioPath.length() > 0) {
                deleteFile(audioPath);
                audioPath = "";
            }
            rlAdditionalPhoto.setVisibility(View.VISIBLE);
            rlpurchasePhoto.setVisibility(View.VISIBLE);
            if (currentState != 7) {
                if (llVideoContainer.getChildCount() > 0) {
                    llVideoContainer.removeAllViews();
                }
                llVideoContainer.setVisibility(View.VISIBLE);

                rlVideo.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvVideo.setTextColor(getResources().getColor(R.color.grey));
                tvVideo.setTypeface(null, Typeface.NORMAL);

                rlAudio.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvAudio.setTextColor(getResources().getColor(R.color.grey));
                tvAudio.setTypeface(null, Typeface.NORMAL);

                rlText.setBackground(getResources().getDrawable(R.drawable.round_corner_strong_grey));
                tvText.setTextColor(getResources().getColor(R.color.dark));
                tvText.setTypeface(null, Typeface.BOLD);

                writeText();
                currentState = 7;
            } else {
                rlText.setBackground(getResources().getDrawable(R.drawable.round_corner_gray));
                tvText.setTextColor(getResources().getColor(R.color.grey));
                tvText.setTypeface(null, Typeface.NORMAL);
                if (llVideoContainer.getChildCount() > 0 ) {
                    llVideoContainer.removeAllViews();
                    currentState = 8;
                    llVideoContainer.setVisibility(View.GONE);
                }
            }


        }
        if (v == ivCheck) {
            if (isAgree) {
                isAgree = false;
                ivCheck.setImageDrawable(getResources().getDrawable(R.drawable.uncheck));
            } else {
                isAgree = true;
                ivCheck.setImageDrawable(getResources().getDrawable(R.drawable.checked));
            }
        }
        if (v == btnSubmit) {
            submit();
        }
        if (v == ivStars[0]) {
            fillStar(1);
        }
        if (v == ivStars[1]) {
            fillStar(2);
        }
        if (v == ivStars[2]) {
            fillStar(3);
        }
        if (v == ivStars[3]) {
            fillStar(4);
        }
        if (v == ivStars[4]) {
            fillStar(5);
        }

    }
    //////////////////input data
    private void fillStar(int mark) {
        for (int i = 0; i < 5; i ++ ) {
            if (i < mark) {
                ivStars[i].setImageDrawable(getResources().getDrawable(R.drawable.rating_star_on));
            } else {
                ivStars[i].setImageDrawable(getResources().getDrawable(R.drawable.rating_star_off));
            }
        }
        rate = mark;
    }
    //////////////  check values
    public  boolean deleteFile(String PATH){

        File file = new File(PATH);
        if(file.exists()){
            file.delete();
            return true;
        }else
            return false;
    }
    public  void showOKDialog(Context context, String message){
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

        AlertDialog alert = builder.create();
        alert.show();
    }
    public  boolean isEmailValid(String email) {//////////////  OK
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
    private boolean checkField() {
        firstname = etFirstName.getText().toString();
        lastname = etLastName.getText().toString();
        email = etEmail.getText().toString();
        company = etCompanyName.getText().toString();
        city = etCity.getText().toString();
        country = etCountry.getText().toString();

        if (firstname.length() == 0) {
            showOKDialog(homeActivity, "Please check your full name");
            return false;
        }
        if (lastname.length() == 0) {
            showOKDialog(homeActivity, "Please check your full name");
            return false;
        }
        if (email.length() == 0 || !isEmailValid(email)) {
            showOKDialog(homeActivity, "Please input valid email address");
            return false;
        }
        if (company.length() == 0) {
            showOKDialog(homeActivity, "Please check your company information");
            return false;
        }
        if (city.length() == 0) {
            showOKDialog(homeActivity, "Please check your company information");
            return false;
        }
        if (country.length() == 0) {
            showOKDialog(homeActivity, "Please check your company information");
            return false;
        }
        if (!isAgree) {
            showOKDialog(homeActivity, "Please agree terms");
            return false;
        }
        if (currentState == 3 || currentState == 4) {
            if (videoPath.length() > 0) {
                videoUpload();
                clearField();

                llVideoContainer.setVisibility(View.GONE);
                btnStartCaptureVideo.setVisibility(View.GONE);
                tvCapturing.setVisibility(View.GONE);

                if (isAgree) {
                    isAgree = false;
                    ivCheck.setImageDrawable(getResources().getDrawable(R.drawable.uncheck));
                }

                return true;
            }  else {
                showOKDialog(homeActivity, "Please take video");
                return false;
            }
        }
        if (currentState == 6 || currentState == 5) {
            if (audioPath.length() == 0) {
                showOKDialog(homeActivity, "Please take audio");
                return false;
            }
//            if ( proofPhotoPath.length() > 0 && additionalPhotoPath.length() > 0) {
            if ( proofPhotoPath.length() > 0) {
                audioUpload();
                clearField();

                llVideoContainer.setVisibility(View.GONE);
                llPhotoContainer.setVisibility(View.GONE);
                btnStartCapturePhoto.setVisibility(View.GONE);
                tvCapturing.setVisibility(View.GONE);

                if (isAgree) {
                    isAgree = false;
                    ivCheck.setImageDrawable(getResources().getDrawable(R.drawable.uncheck));
                }

                return true;
            }
            else {
                showOKDialog(homeActivity, "Please take more photo");
                return false;
            }
        }
        if (currentState == 7 || currentState == 8) {
            reviewText = etText.getText().toString();
            if (reviewText.length() == 0) {
                showOKDialog(homeActivity, "Please input text");
                return false;
            }

//            if (proofPhotoPath.length() > 0 && additionalPhotoPath.length() > 0) {
            if (proofPhotoPath.length() > 0) {
                textUpload();
                clearField();

                llVideoContainer.setVisibility(View.GONE);
                llPhotoContainer.setVisibility(View.GONE);

                if (isAgree) {
                    isAgree = false;
                    ivCheck.setImageDrawable(getResources().getDrawable(R.drawable.uncheck));
                }

                return true;
            } else {
                showOKDialog(homeActivity, "Please take more photo");
                return false;
            }
        }


        return false;
    }

    private void clearField() {
        etFirstName.setText("");
        etLastName.setText("");
        etEmail.setText("");
        etCompanyName.setText("");
        etCity.setText("");
        etCountry.setText("");
    }
    private void videoUpload() {
        final RequestQueue requestQueue = Volley.newRequestQueue(homeActivity);
        Utils.showProgress(getActivity());
        CustomMultipartRequest customMultipartRequest = new CustomMultipartRequest(Constant.video_upload_url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String success = response.getString("success");

                            if (success.equals("1")) {
                                Utils.showOKDialog(homeActivity, "Submit Success!");
                            } else {
                                Utils.showOKDialog(homeActivity, "Submit Failed");
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
//                        requestQueue.getCache().invalidate(Constant.video_upload_url, true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.hideProgress();
                        Toast.makeText(homeActivity, error.toString(), Toast.LENGTH_LONG).show();
                    }


                });
        customMultipartRequest
                .addStringPart("first_name", firstname)
                .addStringPart("last_name", lastname)
                .addStringPart("email", email)
                .addStringPart("company_name", company)
                .addStringPart("company_city", city)
                .addStringPart("company_state", country)
                .addStringPart("ratingvalue", String.valueOf(rate));


        if (videoPath.length() > 0) {
            customMultipartRequest
                    .addVideoPart("video", videoPath);
        } else {
        }


        requestQueue.add(customMultipartRequest);

    }
    private void audioUpload() {
        final RequestQueue requestQueue = Volley.newRequestQueue(homeActivity);
        Utils.showProgress(getActivity());
        CustomMultipartRequest customMultipartRequest = new CustomMultipartRequest(Constant.audio_upload_url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String success = response.getString("success");

                            if (success.equals("1")) {
                                Utils.showOKDialog(homeActivity, "Submit Success!");
                            } else {
                                Utils.showOKDialog(homeActivity, "Submit Failed");
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
//                        requestQueue.getCache().invalidate(Constant.text_upload_url, true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.hideProgress();
                        Toast.makeText(homeActivity, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });


        if (proofPhotoPath.length() > 0 && additionalPhotoPath.length() > 0) {
            customMultipartRequest
                    .addStringPart("first_name", firstname)
                    .addStringPart("last_name", lastname)
                    .addStringPart("email", email)
                    .addStringPart("company_name", company)
                    .addStringPart("company_city", city)
                    .addStringPart("company_state", country)
                    .addStringPart("ratingvalue", String.valueOf(rate))
                    .addImagePart("proofphoto", proofPhotoPath)
                    .addImagePart("additionalphoto", additionalPhotoPath)
                    .addAudioPart("audio", audioPath);
        } else if (proofPhotoPath.length() > 0 && additionalPhotoPath.length() == 0){
            customMultipartRequest
                    .addStringPart("first_name", firstname)
                    .addStringPart("last_name", lastname)
                    .addStringPart("email", email)
                    .addStringPart("company_name", company)
                    .addStringPart("company_city", city)
                    .addStringPart("company_state", country)
                    .addStringPart("ratingvalue", String.valueOf(rate))
                    .addImagePart("proofphoto", proofPhotoPath)
                    .addAudioPart("audio", audioPath);
        }


        requestQueue.add(customMultipartRequest);

    }
    private void textUpload() {
        final RequestQueue requestQueue = Volley.newRequestQueue(homeActivity);
        Utils.showProgress(getActivity());
        final CustomMultipartRequest customMultipartRequest = new CustomMultipartRequest(Constant.text_upload_url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.hideProgress();
                        try {
                            String success = response.getString("success");

                            if (success.equals("1")) {
                                Utils.showOKDialog(homeActivity, "Submit Success!");
                            } else {
                                Utils.showOKDialog(homeActivity, "Submit Failed");
                            }

                        }catch (Exception e) {
                            e.printStackTrace();
                        }
//                        requestQueue.getCache().invalidate(Constant.text_upload_url, true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.hideProgress();
                        Toast.makeText(homeActivity, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });

        if (proofPhotoPath.length() > 0 && additionalPhotoPath.length() > 0) {
            customMultipartRequest
                    .addStringPart("first_name", firstname)
                    .addStringPart("last_name", lastname)
                    .addStringPart("email", email)
                    .addStringPart("company_name", company)
                    .addStringPart("company_city", city)
                    .addStringPart("company_state", country)
                    .addStringPart("ratingvalue", String.valueOf(rate))
                    .addStringPart("reviewtext", reviewText)
                    .addImagePart("proofphoto", proofPhotoPath)
                    .addImagePart("additionalphoto", additionalPhotoPath);
        } else if (proofPhotoPath.length() > 0 && additionalPhotoPath.length() == 0) {
            customMultipartRequest
                    .addStringPart("first_name", firstname)
                    .addStringPart("last_name", lastname)
                    .addStringPart("email", email)
                    .addStringPart("company_name", company)
                    .addStringPart("company_city", city)
                    .addStringPart("company_state", country)
                    .addStringPart("ratingvalue", String.valueOf(rate))
                    .addStringPart("reviewtext", reviewText)
                    .addImagePart("proofphoto", proofPhotoPath);
        }

        requestQueue.add(customMultipartRequest);

    }
    private void initVariables() {
        firstname = "";
        lastname = "";
        email = "";
        company = "";
        city = "";
        country = "";
        message = "";
        proofPhotoPath = "";
        additionalPhotoPath = "";
        audioPath = "";
        videoPath = "";
        rate = 5;
        isAgree = false;
        currentState = 0;
        mLayoutInfrater = homeActivity.getLayoutInflater();
    }

    private void initUI(View view) {
        etFirstName = (EditText)view.findViewById(R.id.et_main_firstname);
        etLastName = (EditText)view.findViewById(R.id.et_main_lastname);
        etCompanyName = (EditText)view.findViewById(R.id.et_main_company_name);
        etCity = (EditText)view.findViewById(R.id.et_main_city);
        etCountry = (EditText)view.findViewById(R.id.et_main_country);
        etEmail = (EditText)view.findViewById(R.id.et_main_email);

        tvVideo = (TextView)view.findViewById(R.id.tv_main_video);
        tvAudio = (TextView)view.findViewById(R.id.tv_main_audio);
        tvText = (TextView)view.findViewById(R.id.tv_main_text);

        rlpurchasePhoto = (RelativeLayout)view.findViewById(R.id.prl_main_purchase_photo);
        rlpurchasePhoto.setOnClickListener(this);
        rlAdditionalPhoto = (RelativeLayout)view.findViewById(R.id.prl_main_additional_photo);
        rlAdditionalPhoto.setOnClickListener(this);
        rlMainPurcharsePhotoBtn = (RelativeLayout)view.findViewById(R.id.tv_main_purchase_photo);
        rlAdditionalPhotoBtn = (RelativeLayout)view.findViewById(R.id.tv_main_additional_photo);
        rlVideo = (RelativeLayout)view.findViewById(R.id.rl_main_video);
        rlVideo.setOnClickListener(this);
        rlAudio = (RelativeLayout)view.findViewById(R.id.rl_main_audio);
        rlAudio.setOnClickListener(this);
        rlText = (RelativeLayout)view.findViewById(R.id.rl_main_text);
        rlText.setOnClickListener(this);

        ivStars = new ImageView[5];
        ivStars[0] = (ImageView)view.findViewById(R.id.iv_main_star_0);
        ivStars[1] = (ImageView)view.findViewById(R.id.iv_main_star_1);
        ivStars[2] = (ImageView)view.findViewById(R.id.iv_main_star_2);
        ivStars[3] = (ImageView)view.findViewById(R.id.iv_main_star_3);
        ivStars[4] = (ImageView)view.findViewById(R.id.iv_main_star_4);

        ivStars[0].setOnClickListener(this);
        ivStars[1].setOnClickListener(this);
        ivStars[2].setOnClickListener(this);
        ivStars[3].setOnClickListener(this);
        ivStars[4].setOnClickListener(this);

        ivCheck = (ImageView)view.findViewById(R.id.iv_main_check);
        ivCheck.setOnClickListener(this);

        btnSubmit = (Button)view.findViewById(R.id.btn_main_submit);
        btnSubmit.setOnClickListener(this);

        llVideoContainer = (LinearLayout)view.findViewById(R.id.ll_main_container);
        llPhotoContainer = (LinearLayout)view.findViewById(R.id.ll_main_container0);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       homeActivity = (HomeActivity)activity;
    }
}
