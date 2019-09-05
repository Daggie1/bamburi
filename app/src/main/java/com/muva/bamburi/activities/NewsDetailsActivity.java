package com.muva.bamburi.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import androidx.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.Repository;
import com.muva.bamburi.adapters.NewsResponseAdapter;
import com.muva.bamburi.databinding.ActivityNewsDetailsBinding;
import com.muva.bamburi.models.Comment;
import com.muva.bamburi.models.Comment_;
import com.muva.bamburi.models.News;
import com.muva.bamburi.models.News_;
import com.muva.bamburi.network.Urls;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;
import com.muva.bamburi.utils.audio_player.MediaPlayerHolder;
import com.muva.bamburi.utils.audio_player.PlaybackInfoListener;
import com.muva.bamburi.utils.audio_player.PlayerAdapter;
import com.muva.bamburi.utils.custom_media_views.ResizeSurfaceView;
import com.muva.bamburi.utils.custom_media_views.VideoControllerView;
import com.muva.bamburi.viewmodel.NewsViewModel;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.muva.bamburi.network.Urls.NEWS_URL;
import static com.muva.bamburi.utils.Constants.ARG_PHOTO_URL;
import static com.muva.bamburi.utils.Constants.NEWS_ID;
import static com.muva.bamburi.utils.UniversalMethods.getDeviceHeight;
import static com.muva.bamburi.utils.UniversalMethods.getDeviceWidth;

public class NewsDetailsActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener, VideoControllerView.MediaPlayerControlListener {
    private long news_id;
    private Box<News> newsBox;
    private Query<News> newsQuery;
    private News news;
    private NewsViewModel newsViewModel;
    private ResizeSurfaceView resizeSurfaceView;
    private LinearLayout mContentView;
    private ActivityNewsDetailsBinding binding;
    private MediaPlayer mMediaPlayer;
    private VideoControllerView controller;
    private int mVideoHeight, mVideoWidth;
    private boolean mIsComplete;
    private SeekBar seekBar;
    private PlayerAdapter mPlayerAdapter;
    private boolean mUserIsSeeking = false;
    private Button btnPlay, btnPause, btnReset;
    private TextView btnViewPdf;
    private BoxStore boxStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_news_details);

        setSupportActionBar(binding.toolbar);

        Settings settings = new Settings(NewsDetailsActivity.this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            news_id = bundle.getLong(NEWS_ID);
            settings.setNewsId(news_id);
        }
        if (news_id == 0) {
            news_id = settings.getNewsId();
        }

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        boxStore = Bamburi.getInstance().getBoxStore();
        newsBox = boxStore.boxFor(News.class);

        newsQuery = newsBox.query().equal(News_.id, news_id).build();
        news = newsQuery.findFirst();

        if (news != null) {
            btnViewPdf = binding.btnViewPdf;

            L.d("PDF URL :: " + news.getPdf_url());
            if (news.getPdf_url() != null) {
                btnViewPdf.setVisibility(VISIBLE);
                btnViewPdf.setOnClickListener(v -> {
                    String pdfUrl = NEWS_URL + news.getPdf_url();

//                    Intent intent = new Intent(NewsDetailsActivity.this, PDFWebViewActivity.class);
//                    intent.putExtra(PDF_URL, pdfUrl);
//                    intent.putExtra(PDF_TITLE, news.getTitle());
//                    startActivity(intent);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(pdfUrl));
                    L.d(pdfUrl);
                    startActivity(intent);
                });
            } else {
                btnViewPdf.setVisibility(GONE);
            }

            setTitle(news.getTitle());
            newsViewModel = new NewsViewModel(this, news, null, 0);
            binding.setViewModel(newsViewModel);

            //make link clickable
            Linkify.addLinks(binding.tvMessage, Linkify.ALL);

            if (newsViewModel.isVideo()) {
                resizeSurfaceView = binding.videoSurface;
                mContentView = binding.mediaFrame;
                handleVideoFiles();
            } else if (newsViewModel.isAudio()) {
                seekBar = binding.seekBar;
                btnPlay = binding.buttonPlay;
                btnPause = binding.buttonPause;
                btnReset = binding.buttonReset;

                btnPause.setOnClickListener(view -> mPlayerAdapter.pause());
                btnPlay.setOnClickListener(view -> mPlayerAdapter.play());
                btnReset.setOnClickListener(view -> mPlayerAdapter.reset());

                initializeSeekbar();
                initializePlaybackController();

            }

            binding.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(NewsDetailsActivity.this, PhotoFullActivity.class);
                intent.putExtra(ARG_PHOTO_URL, Urls.NEWS_URL + newsViewModel.getMediaUrl());
                startActivity(intent);
            });
        } else {
            L.t(NewsDetailsActivity.this, "Could not find news item");
            finish();
        }
    }

    public void handleVideoFiles() {
        if (newsViewModel.isVideo()) {
            L.e("showing a video");

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
                    .build(findViewById(R.id.media_frame));//layout container that hold video play view


            try {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                File directory = new File(getFilesDir(), "Bamburi");
                File videoFile = new File(directory.getAbsolutePath(), "/" + news.getMedia_url());

                Uri sharedFileUri = FileProvider.getUriForFile(NewsDetailsActivity.this, "com.muva.bamburi.fileprovider", videoFile);

                mMediaPlayer.setDataSource(NewsDetailsActivity.this, sharedFileUri);
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
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mVideoHeight = mp.getVideoHeight();
        mVideoWidth = mp.getVideoWidth();
        if (mVideoHeight > 0 && mVideoWidth > 0) {
            if (resizeSurfaceView != null) {
                resizeSurfaceView.adjustSize(mContentView.getWidth(), mContentView.getHeight(), mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            resizeSurfaceView.adjustSize(getDeviceWidth(NewsDetailsActivity.this), getDeviceHeight(NewsDetailsActivity.this), resizeSurfaceView.getWidth(), resizeSurfaceView.getHeight());
        }
    }

    private void resetPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

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
    public void surfaceDestroyed(SurfaceHolder holder) {
        resetPlayer();
    }

// End SurfaceHolder.Callback


    // Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
        resizeSurfaceView.setVisibility(VISIBLE);
        mMediaPlayer.start();
        mIsComplete = false;
    }
// End MediaPlayer.OnPreparedListener


    @Override
    public int getBufferPercentage() {
        return 0;
    }


    @Override
    public int getCurrentPosition() {
        return null != mMediaPlayer ? mMediaPlayer.getCurrentPosition() : 0;
    }


    @Override
    public int getDuration() {
        return null != mMediaPlayer ? mMediaPlayer.getDuration() : 0;
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
    public void pause() {
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
        }
    }


    @Override
    public void seekTo(int i) {
        if (null != mMediaPlayer) {
            mMediaPlayer.seekTo(i);
        }
    }


    @Override
    public void start() {
        if (null != mMediaPlayer) {
            mMediaPlayer.start();
            mIsComplete = false;
        }
    }


    @Override
    public boolean isFullScreen() {
        return (NewsDetailsActivity.this).getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }


    @Override
    public void toggleFullScreen() {
        if (isFullScreen()) {
            (NewsDetailsActivity.this).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            (NewsDetailsActivity.this).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }


    @Override
    public void exit() {
        resetPlayer();
        //finish();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        mIsComplete = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPlayerAdapter != null) {
            File directory = new File(getFilesDir(), "Bamburi");

            String file_path = directory.getPath() + "/" + news.getMedia_url();

            mPlayerAdapter.loadMedia(file_path);
        }

        setUpComments();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPlayerAdapter != null) {
            if (isChangingConfigurations() && mPlayerAdapter.isPlaying()) {
                L.d("onStop: don't release MediaPlayer as screen is rotating & playing");
            } else {
                mPlayerAdapter.release();
                L.d("onStop: release MediaPlayer");
            }
        }
    }

    private void initializePlaybackController() {
        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder(this);
        L.d("initializePlaybackController: created MediaPlayerHolder");
        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mPlayerAdapter = mMediaPlayerHolder;
        L.d("initializePlaybackController: MediaPlayerHolder progress callback set");
    }

    private void initializeSeekbar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int userSelectedPosition = 0;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    userSelectedPosition = progress;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mUserIsSeeking = false;
                mPlayerAdapter.seekTo(userSelectedPosition);
            }
        });
    }

    public class PlaybackListener extends PlaybackInfoListener {


        @Override
        public void onDurationChanged(int duration) {
            seekBar.setMax(duration);
            L.d(String.format("setPlaybackDuration: setMax(%d)", duration));
        }

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                seekBar.setProgress(position);
                L.d(String.format("setPlaybackPosition: setProgress(%d)", position));
            }
        }

        @Override
        public void onStateChanged(@State int state) {
            String stateToString = PlaybackInfoListener.convertStateToString(state);
            onLogUpdated(String.format("onStateChanged(%s)", stateToString));
        }

        @Override
        public void onPlaybackCompleted() {
        }

        @Override
        public void onLogUpdated(String message) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }


    /*
    |-------------------------------------------------------
    | Bringing comments back
    |--------------------------------------------------------
    */
    private void setUpComments() {
        Query<Comment> commentQuery = boxStore.boxFor(Comment.class).query().equal(Comment_.news_id, news_id).order(Comment_.created_at).build();
        List<Comment> comments = commentQuery.find();

        binding.commentsRecyclerview.setLayoutManager(new LinearLayoutManager(NewsDetailsActivity.this));
        NewsResponseAdapter adapter = new NewsResponseAdapter(NewsDetailsActivity.this, comments);
        binding.commentsRecyclerview.setAdapter(adapter);


        binding.sendButton.setOnClickListener(view -> {
            String comment = binding.commentText.getText().toString().trim();
            if (TextUtils.isEmpty(comment)) {
                binding.commentText.setError("You cannot post an empty comment");
            } else {
                binding.sendButton.setVisibility(View.INVISIBLE);
                L.t(NewsDetailsActivity.this, "Posting comment....");
                Repository.replyNews(this, new Settings(this).getUserLoggedinId(), news_id, comment, binding.sendButton);
            }
        });

        binding.layoutComment.setVisibility(news.isRepliable() ? VISIBLE : GONE);

        commentQuery.subscribe().on(AndroidScheduler.mainThread()).observer(data -> {
            if (data.size() > 0) {
                //clear the edittext
                binding.commentText.setText("");
                binding.commentText.clearFocus();

                //hide the keyboard
                View focusedView = getCurrentFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (focusedView != null && inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                }

                adapter.updateComments(data);
                //go to the latest comment
                binding.commentsRecyclerview.scrollToPosition(data.size() - 1);
            }
        });
    }
}
