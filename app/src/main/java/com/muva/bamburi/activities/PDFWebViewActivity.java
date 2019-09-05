package com.muva.bamburi.activities;

import android.annotation.TargetApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.muva.bamburi.utils.L;

import static com.muva.bamburi.utils.Constants.PDF_TITLE;
import static com.muva.bamburi.utils.Constants.PDF_URL;

public class PDFWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView mWebview = new WebView(this);

        mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript

        final AppCompatActivity activity = this;

        mWebview.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                L.t(PDFWebViewActivity.this, description);
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });


        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(PDF_URL)) {
            String pdfUrl = getIntent().getStringExtra(PDF_URL);
            setTitle(getIntent().getStringExtra(PDF_TITLE));

            pdfUrl = "https://m-bamburi.com/uploads/newsletters/5bff8c105c5d8.pdf";

            String url = "http://docs.google.com/gview?embedded=true&url=" + pdfUrl;
            mWebview.getSettings().setJavaScriptEnabled(true);
            mWebview.loadUrl(url);
        } else {
            L.t(PDFWebViewActivity.this, "No pdf url provided");
            finish();
        }


        setContentView(mWebview);
    }
}
