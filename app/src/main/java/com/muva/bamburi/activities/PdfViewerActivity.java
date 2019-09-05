package com.muva.bamburi.activities;

import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.muva.bamburi.R;
import com.muva.bamburi.utils.L;

import java.io.File;

import static com.muva.bamburi.utils.Constants.PDF_TITLE;
import static com.muva.bamburi.utils.Constants.PDF_URL;

public class PdfViewerActivity extends AppCompatActivity implements OnPageChangeListener {

    private String pdfUrl;
    private String pdfTitle;
    Integer pageNumber = 0;

//    private Integer pageNumber = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        PDFView pdfView = findViewById(R.id.pdfView);
        FrameLayout frameLayout = findViewById(R.id.fragment_container);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            pdfUrl = bundle.getString(PDF_URL);
            pdfTitle = bundle.getString(PDF_TITLE);

        }


        File directory = new File(getFilesDir(), "/Bamburi");
        File pdfFile = new File(directory.getAbsolutePath(), "/"+ pdfUrl);

        if (pdfFile.exists()) {
            L.e("pdf file exists at: "+pdfFile.getAbsolutePath());
        }

        Uri path = FileProvider.getUriForFile(this, "com.muva.bamburi.fileprovider", pdfFile);


//        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // less than lollipop
//            L.d("less than lollipop");
            frameLayout.setVisibility(View.GONE);
            pdfView.setVisibility(View.VISIBLE);
            pdfView.fromUri(path)
                    .defaultPage(0)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .onPageChange(this)
                    .enableAnnotationRendering(true)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .onError(throwable -> L.e("error loading page: " + throwable.getMessage()))
                    .load();
//        }
//        else{
//            //lollipop and above
//            L.d("lollipop and above");
//            frameLayout.setVisibility(View.VISIBLE);
//            pdfView.setVisibility(View.GONE);
//
//            PdfRendererFragment pdfRendererFragment = PdfRendererFragment.getInstance(pdfUrl, pdfTitle);
//
//            L.e("activity, pdf title: " + pdfTitle + " url: " + pdfUrl);
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.fragment_container, pdfRendererFragment);
//            transaction.commit();
//        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfTitle, page + 1, pageCount));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
