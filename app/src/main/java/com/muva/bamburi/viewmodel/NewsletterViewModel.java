package com.muva.bamburi.viewmodel;

import android.content.Context;
import android.content.Intent;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.appcompat.widget.PopupMenu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.activities.PdfViewerActivity;
import com.muva.bamburi.adapters.NewsletterAdapter;
import com.muva.bamburi.models.Newsletter;
import com.muva.bamburi.network.Urls;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.UniversalMethods;

import java.io.File;

import io.objectbox.Box;

import static com.muva.bamburi.utils.Constants.PDF_TITLE;
import static com.muva.bamburi.utils.Constants.PDF_URL;

/**
 * Created by Njoro on 5/7/18.
 */
public class  NewsletterViewModel extends BaseObservable {

    private Context context;
    private Newsletter newsletter;
    private Box<Newsletter> newsletterBox;
    private int position;
    private NewsletterAdapter adapter;

    public NewsletterViewModel(Context context, Newsletter newsletter, NewsletterAdapter adapter, int position) {
        this.context = context;
        this.newsletter = newsletter;
        this.position = position;
        this.adapter = adapter;

        newsletterBox = Bamburi.getInstance().getBoxStore().boxFor(Newsletter.class);
    }

    @Bindable
    public String getTitle() {
        return newsletter.getTitle();
    }

    @Bindable
    public String getBody() {
        return  newsletter.getBody();
    }

    @Bindable
    public String getTimeCreated() {
        return newsletter.getTimeCreated();
    }

    @Bindable
    public boolean getPdfDownloaded(){
        return newsletter.isDownloaded();
    }


    public void downloadMedia(ProgressBar progressBar, Button btnAction) {
        L.e("pdf downloaded: "+newsletter.isDownloaded());
        L.e("btn enabled: "+btnAction.isEnabled());
        L.e("progressBar: "+progressBar.getProgress());
        if (newsletter != null && !getPdfDownloaded()) {
            btnAction.setEnabled(false);
            L.T(context, context.getString(R.string.download_in_progress));


            int downloadId = PRDownloader.download(Urls.NEWSLETTERS_URL+newsletter.getPdf_url(), UniversalMethods.getDefaultRootDirectory(context), newsletter.getPdf_url())
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
                            newsletter.setDownloaded(true);
                            newsletterBox.put(newsletter);
                            if (adapter != null) {
                                adapter.notifyItemChanged(position);
                            }
                            L.e("complete");

                        }

                        @Override
                        public void onError(Error error) {
                            L.e("error: "+error.toString());
                            btnAction.setEnabled(true);
                        }
                    });
        }
    }

    public void viewPdf() {
        Intent intent = new Intent(context, PdfViewerActivity.class);
        intent.putExtra(PDF_URL,newsletter.getPdf_url());
        intent.putExtra(PDF_TITLE, newsletter.getTitle());
        context.startActivity(intent);

    }

    public void onOptionsMenuClick(View view) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.delete:
                    L.t(context, "Deleting newsletter pdf: "+newsletter.getTitle());
                    File directory = new File(context.getFilesDir(), "Bamburi");
                    File internalFile = new File(directory.getAbsolutePath(), newsletter.getPdf_url());

                    if (internalFile.exists()){
                        if(internalFile.delete()){
                            newsletter.setDownloaded(false);
                            newsletterBox.put(newsletter);
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
}
