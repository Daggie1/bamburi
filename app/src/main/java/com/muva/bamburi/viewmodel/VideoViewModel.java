package com.muva.bamburi.viewmodel;

import android.content.Context;
import android.content.Intent;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.appcompat.widget.PopupMenu;
import android.text.TextUtils;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.activities.VideoPlayerActivity;
import com.muva.bamburi.adapters.VideoRecyclerViewAdapter;
import com.muva.bamburi.models.Video;
import com.muva.bamburi.network.Urls;
import com.muva.bamburi.utils.GlideApp;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.UniversalMethods;

import java.io.File;

import io.objectbox.Box;

import static com.muva.bamburi.utils.Constants.VIDEO_ID;

/**
 * Created by Njoro on 6/14/18.
 */
public class VideoViewModel extends BaseObservable {
    private Context context;
    private Video video;
    private final Box<Video> videoBox;
    private int position;
    private VideoRecyclerViewAdapter adapter;

    public VideoViewModel(Context context, Video video, VideoRecyclerViewAdapter adapter, int position) {
        this.context = context;
        this.video = video;
        this.position = position;
        this.adapter = adapter;

        videoBox = Bamburi.getInstance().getBoxStore().boxFor(Video.class);
    }

    @BindingAdapter({"video_thumbnail"})
    public static void loadThumbnail(ImageView view, String url) {
        if (!TextUtils.isEmpty(url)) {
            GlideApp.with(view.getContext()).load(Urls.VIDEO_THUMBNAILS_URL + url).into(view);
        } else {
            GlideApp.with(view.getContext()).load(R.drawable.bamburi_header_logo).into(view);
        }
    }

    @Bindable
    public String getTitle() {
        return video.getCaption();
    }

    @Bindable
    public String getCreatedAt() {
        return video.getCreated_at();
    }

    @Bindable
    public String getVideoThumbnail() {
        return video.getVideo_thumbnail();
    }

    public boolean isVideoDownloaded() {
        return video.isDownloaded();
    }

    public void onVideoCardClick(ProgressBar progressBar, Button btnDownload) {
        if (isVideoDownloaded()) {
            //open activity to play video...
            goToVideoPlayerActivity();
        } else {
            L.T(context, context.getString(R.string.download_in_progress));
            downloadFile(progressBar,btnDownload);
        }
    }

    public void onOptionsMenuClick(View view) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.delete:
                    L.t(context, "Deleting the video: "+video.getCaption());
                    File directory = new File(context.getFilesDir(), "Bamburi");
                    File internalFile = new File(directory.getAbsolutePath(), video.getVideo_url());

                    if (internalFile.exists()){
                        L.e("file "+internalFile.getAbsolutePath()+" exists");
                        if(internalFile.delete()){
                            video.setDownloaded(false);
                            videoBox.put(video);
                            if (adapter != null) {
                                adapter.notifyItemChanged(position);
                            }
                        }
                    }
                    return true;
                default:
                    return false;
            }


        });// to implement on click event on items of menu
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.video_popup_menu, popup.getMenu());
        popup.show();
    }


    private void goToVideoPlayerActivity() {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(VIDEO_ID, video.getId());
        context.startActivity(intent);
    }

    private void downloadFile(ProgressBar progressBar,Button btnDownload) {

        if (video != null) {
            btnDownload.setEnabled(false);

            int downloadId = PRDownloader.download(Urls.VIDEOS_URL+video.getVideo_url(), UniversalMethods.getDefaultRootDirectory(context), video.getVideo_url())
                    .build()
                    .setOnStartOrResumeListener(() -> L.e("on start or resume"))
                    .setOnPauseListener(() -> L.e("on pause"))
                    .setOnCancelListener(() -> L.e("on pause"))
                    .setOnProgressListener(progress -> {
                        int new_progress = (int)((progress.currentBytes*100)/progress.totalBytes);
                        progressBar.setProgress(new_progress);
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            video.setDownloaded(true);
                            videoBox.put(video);
                            if (adapter != null) {
                                adapter.notifyItemChanged(position);
                            }
                            L.e("complete");

                        }

                        @Override
                        public void onError(Error error) {
                            L.e("error: "+error.toString());
                        }
                    });

        }

    }
}
