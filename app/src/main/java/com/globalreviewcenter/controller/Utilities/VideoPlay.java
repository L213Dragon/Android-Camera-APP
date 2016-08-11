package com.globalreviewcenter.controller.Utilities;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlay {
    private Context mContext;
    private VideoView videoView;
    private String videoUrl;/////local path
    public VideoPlay(Context context, VideoView videoView, String videoUrl) {
        this.mContext = context;
        this.videoUrl = videoUrl;///local path
        this.videoView = videoView;
    }
    public void playVideo(){
        try {
            // Start the MediaController
            MediaController mediacontroller = new MediaController(
                    mContext);
            mediacontroller.setAnchorView(videoView);
            // Get the URL from String VideoURL
            Uri video = Uri.parse(videoUrl);
            videoView.setMediaController(mediacontroller);
            videoView.setVideoURI(video);
//            videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.asd);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
//        videoView.seekTo(100);
        videoView.requestFocus();
//        videoView.seekTo(100);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                //////////start play
                videoView.start();
                videoView.seekTo(100);
            }
        });
    }
    public void pauseVideo(int at) {
        videoView.seekTo(at);
//        videoView.pause();
    }
}
