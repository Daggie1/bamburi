package com.muva.bamburi.activities;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import androidx.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.LinearLayout;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.databinding.ActivityVideoPlayerBinding;
import com.muva.bamburi.models.Video;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.custom_media_views.ResizeSurfaceView;
import com.muva.bamburi.utils.custom_media_views.VideoControllerView;
import com.muva.bamburi.viewmodel.VideoViewModel;

import java.io.File;
import java.io.IOException;

import io.objectbox.Box;
import io.objectbox.BoxStore;

import static com.muva.bamburi.utils.Constants.VIDEO_ID;
import static com.muva.bamburi.utils.UniversalMethods.getDeviceHeight;
import static com.muva.bamburi.utils.UniversalMethods.getDeviceWidth;


public class VideoPlayerActivity extends AppCompatActivity implements
        SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener, VideoControllerView.MediaPlayerControlListener{


    private long video_id;
    private Box<Video> videoBox;
    private Video video;
    private ActivityVideoPlayerBinding binding;
    private LinearLayout videoContainer;
    private ResizeSurfaceView resizeSurfaceView;
    private MediaPlayer mMediaPlayer;
    private VideoControllerView controller;
    private boolean mIsComplete;
    private int mVideoHeight,mVideoWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(VideoPlayerActivity.this, R.layout.activity_video_player);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            video_id = bundle.getLong(VIDEO_ID);
        }

        BoxStore boxStore = Bamburi.getInstance().getBoxStore();
        videoBox = boxStore.boxFor(Video.class);
        video = videoBox.get(video_id);

        if (video != null) {
            setTitle(video.getCaption());
            binding.setViewModel(new VideoViewModel(VideoPlayerActivity.this, video,null,0));

            resizeSurfaceView = binding.videoSurface;
            videoContainer = binding.mediaFrame;

            setupVideo();

        }
    }

    private void setupVideo() {
        SurfaceHolder videoHolder = resizeSurfaceView.getHolder();
        videoHolder.addCallback(this);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnVideoSizeChangedListener(this);


        controller = new VideoControllerView.Builder(this, this)
                .withMediaControlListener(this)
                .withVideoSurfaceView(resizeSurfaceView)//to enable toggle display controller view
                .canControlBrightness(true)
                .canControlVolume(true)
                .canSeekVideo(true)
                .exitIcon(R.drawable.video_top_back)
                .pauseIcon(R.drawable.ic_media_pause)
                .playIcon(R.drawable.ic_media_play)
                .shrinkIcon(R.drawable.ic_media_fullscreen_shrink)
                .stretchIcon(R.drawable.ic_media_fullscreen_stretch)
                .build(videoContainer);//layout container that hold video play view


        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            File directory = new File(getFilesDir(), "Bamburi");
            File videoFile = new File(directory.getAbsolutePath(), "/" + video.getVideo_url());


            Uri sharedFileUri = FileProvider.getUriForFile(VideoPlayerActivity.this, "com.muva.bamburi.fileprovider", videoFile);

            mMediaPlayer.setDataSource(VideoPlayerActivity.this, sharedFileUri);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        resizeSurfaceView.setOnTouchListener((v, event) -> {
            controller.toggleControllerView();
            return false;
        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            resizeSurfaceView.adjustSize(getDeviceWidth(VideoPlayerActivity.this), getDeviceHeight(VideoPlayerActivity.this), resizeSurfaceView.getWidth(), resizeSurfaceView.getHeight());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mIsComplete = true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        resizeSurfaceView.setVisibility(View.VISIBLE);
        mMediaPlayer.start();
        mIsComplete = false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mVideoHeight = mp.getVideoHeight();
        mVideoWidth = mp.getVideoWidth();
        if (mVideoHeight > 0 && mVideoWidth > 0) {
            if (resizeSurfaceView != null) {
                resizeSurfaceView.adjustSize(videoContainer.getWidth(), videoContainer.getHeight(), mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
            }
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mMediaPlayer.setDisplay(holder);
            mMediaPlayer.prepareAsync();
        } catch (Exception exception) {
            L.t(getApplicationContext(), "Video Player failed!");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        resetPlayer();
    }

    @Override
    public void start() {
        if (null != mMediaPlayer) {
            mMediaPlayer.start();
            mIsComplete = false;
        }
    }

    @Override
    public void pause() {
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public int getDuration() {
        return null != mMediaPlayer ? mMediaPlayer.getDuration() : 0;
    }

    @Override
    public int getCurrentPosition() {
        return null != mMediaPlayer ? mMediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public void seekTo(int position) {
        if (null != mMediaPlayer) {
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public boolean isPlaying() {
        return null != mMediaPlayer && mMediaPlayer.isPlaying();
    }

    @Override
    public boolean isComplete() {
        return mIsComplete;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean isFullScreen() {
        return (VideoPlayerActivity.this).getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }

    @Override
    public void toggleFullScreen() {
        if (isFullScreen()) {
            (VideoPlayerActivity.this).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            (VideoPlayerActivity.this).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

    }

    @Override
    public void exit() {
        resetPlayer();
    }

    private void resetPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
