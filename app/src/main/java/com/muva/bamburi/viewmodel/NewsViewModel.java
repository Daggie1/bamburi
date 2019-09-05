package com.muva.bamburi.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.Repository;
import com.muva.bamburi.activities.CommentsActivity;
import com.muva.bamburi.activities.NewsDetailsActivity;
import com.muva.bamburi.adapters.HomeAdapter;
import com.muva.bamburi.models.News;
import com.muva.bamburi.models.User;
import com.muva.bamburi.models.User_;
import com.muva.bamburi.network.Urls;
import com.muva.bamburi.utils.GlideApp;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;
import com.muva.bamburi.utils.UniversalMethods;

import io.objectbox.Box;
import io.objectbox.BoxStore;

import static com.muva.bamburi.utils.Constants.AUDIO_MEDIA;
import static com.muva.bamburi.utils.Constants.IMAGE_MEDIA;
import static com.muva.bamburi.utils.Constants.NEWS_ID;
import static com.muva.bamburi.utils.Constants.NO_MEDIA;
import static com.muva.bamburi.utils.Constants.VIDEO_MEDIA;

/**
 * Created by Njoro on 5/3/18.
 */
public class NewsViewModel extends BaseObservable {

    private Context context;
    private News news;
    private HomeAdapter homeAdapter;
    private Box<News> newsBox;
    private int position;

    String comment;


    public NewsViewModel(Context context, News news, HomeAdapter homeAdapter,int position) {
        this.context = context;
        this.news = news;
        this.homeAdapter = homeAdapter;
        this.position = position;
        newsBox = Bamburi.getInstance().getBoxStore().boxFor(News.class);
    }

    @Bindable
    public News getNews() {
        return news;
    }

    @Bindable
    public int getMediaType() {
        return news.getMedia_type();
    }

    @Bindable
    public String getMediaUrl() {
        return news.getMedia_url();
    }

    @Bindable
    public String getTitle() {
        return news.getTitle();
    }


    @Bindable
    public String getMessage() {
        return news.getMessage();
    }

    public boolean hasMedia() {
        return news.getMedia_type() != NO_MEDIA;
    }

    public float getGuidelinePercent() {
        return (hasMedia() ? 0.6f : 1.0f); // Of course, this default could be anything you want.
    }

    @BindingAdapter("layout_constraintGuide_begin")
    public static void setLayoutConstraintGuideBegin(Guideline guideline, float percent) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        params.guidePercent = percent;
        guideline.setLayoutParams(params);
    }


    @BindingAdapter({"media_url"})
    public static void loadImage(ImageView view, String url) {
        if (!TextUtils.isEmpty(url)) {
            if (url.endsWith(".mp4"))
                GlideApp.with(view.getContext()).load(R.drawable.ic_video_library_black_24dp).into(view);
            else if (url.endsWith(".mp3")){
                GlideApp.with(view.getContext()).load(R.drawable.ic_library_music_black_24dp).into(view);
            }
            else {
                GlideApp.with(view.getContext()).load(Urls.NEWS_URL + url).into(view);
            }
        }
    }

    @Bindable
    public String getMediaSize() {
        return news.getMedia_size();
    }

    @Bindable
    public String getTimeCreated() {
        return news.getTime_created();
    }

    public boolean isMediaDownloaded() {
        return news.isDownloaded();
    }

    public boolean isRepliable() {
        return news.isRepliable();
    }

    public boolean hasComments() {
        return news.hasComments();
    }

    public boolean isImage() {
        return news.getMedia_type() == IMAGE_MEDIA;
    }

    public boolean isVideo() {
        return news.getMedia_type() == VIDEO_MEDIA;
    }
    public boolean isAudio() {
        return news.getMedia_type() == AUDIO_MEDIA;
    }

    public void onNewsCardClick(ProgressBar progressBar, ImageButton adsDownloadButton) {
        //check whether the media is downloaded
        if (isMediaDownloaded()) {
            Intent intent = new Intent(context, NewsDetailsActivity.class);
            intent.putExtra(NEWS_ID, news.getId());
            context.startActivity(intent);
        }else{
            new MaterialDialog.Builder(context)
                    .content("Kindly download the media to view the News details in full. OK?")
                    .positiveText("Download")
                    .negativeText("Cancel")
                    .onPositive((dialog, which) -> {
                        downloadMedia(progressBar,adsDownloadButton);
                        dialog.dismiss();
                    })
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .build()
                    .show();
        }
    }

    public void submitComment(EditText commentText, View view) {

        if (commentText != null) {
            String userComment = commentText.getText().toString().trim();

            if (TextUtils.isEmpty(userComment)) {
                commentText.setError(context.getString(R.string.error_field_required));
            } else {
                //update the server with the details

                //clear the edittext
                commentText.setText("");
                commentText.clearFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                BoxStore boxStore = Bamburi.getInstance().getBoxStore();

                User user = boxStore.boxFor(User.class).query().equal(User_.id, new Settings(context).getUserLoggedinId()).build().findFirst();

                if (user != null) {
                    Repository.replyNews(context,user.getId(),news.getId(),userComment, null);
                }
            }
        } else {
            L.e("etComment is null");
        }

//        } else {
//            L.e("view is null");
//        }
    }

    public void launchCommentsActivity() {
        Intent intent = new Intent(context, CommentsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong(NEWS_ID,news.getId());
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public void downloadMedia(ProgressBar progressBar, ImageButton adsDownloadButton) {
        try{
            //disable download button
            adsDownloadButton.setEnabled(false);

            L.T(context,context.getString(R.string.download_in_progress));
            downloadFile(progressBar,adsDownloadButton);
        }catch(Exception e){
        L.e("error launching the download manager: "+e.getMessage());
        }
    }


    public boolean showProgressBar() {
        return hasMedia() && !isMediaDownloaded();
    }

    private void downloadFile(ProgressBar progressBar, ImageButton adsDownloadButton) {

        if (news != null) {
            int downloadId = PRDownloader.download(Urls.NEWS_URL+news.getMedia_url(), UniversalMethods.getDefaultRootDirectory(context), news.getMedia_url())
                    .build()
                    .setOnStartOrResumeListener(() -> L.e("on start or resume"))
                    .setOnPauseListener(() -> L.e("on pause"))
                    .setOnCancelListener(() -> L.e("on pause"))
                    .setOnProgressListener(progress -> {
                        int new_progress = (int)((progress.currentBytes*100)/progress.totalBytes);
                        progressBar.setProgress(new_progress);
//                            L.e("on progress: "+progress.currentBytes+" of "+progress.totalBytes+" = "+new_progress);
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            news.setDownloaded(true);
                            newsBox.put(news);
                            if (homeAdapter != null) {
                                homeAdapter.notifyItemChanged(position);
                            }
                            L.e("complete");

                        }

                        @Override
                        public void onError(Error error) {
                            L.e("error: "+error.toString());
                            adsDownloadButton.setEnabled(true);
                        }
                    });

        }

    }
}
