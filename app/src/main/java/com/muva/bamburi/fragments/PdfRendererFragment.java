package com.muva.bamburi.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.muva.bamburi.R;
import com.muva.bamburi.utils.L;

import java.io.File;
import java.io.IOException;

import static com.muva.bamburi.utils.Constants.PDF_TITLE;
import static com.muva.bamburi.utils.Constants.PDF_URL;

/**
 * A simple {@link Fragment} subclass.
 */

public class PdfRendererFragment extends Fragment {

    private ParcelFileDescriptor fileDescriptor;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ImageView image;
    private Button btnPrevious;
    private Button btnNext;
    private String pdf_title,pdf_url;

    public PdfRendererFragment() {
        // Required empty public constructor
    }


    public static PdfRendererFragment getInstance(String pdf_url, String pdf_title) {
        PdfRendererFragment pdfRendererFragment = new PdfRendererFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PDF_URL, pdf_url);
        bundle.putString(PDF_TITLE, pdf_title);
        pdfRendererFragment.setArguments(bundle);

        return pdfRendererFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            pdf_title = bundle.getString(PDF_TITLE);
            pdf_url = bundle.getString(PDF_URL);
            L.d("title: " + pdf_title);
            L.d("url: " + pdf_url);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pdf_renderer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Retain view references.
        image = (ImageView) view.findViewById(R.id.image);
        btnPrevious = (Button) view.findViewById(R.id.btn_previous);
        btnNext = (Button) view.findViewById(R.id.btn_next);

        //set buttons event
        btnPrevious.setOnClickListener(onActionListener(-1)); //previous button clicked
        btnNext.setOnClickListener(onActionListener(1)); //next button clicked

        //
        if (pdfRenderer != null) {
            int count = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                count = pdfRenderer.getPageCount();
            }
            if (count <= 1) {
                btnNext.setEnabled(false);
                btnPrevious.setEnabled(false);
            }
        }


        int index = 0;
        // If there is a savedInstanceState (screen orientations, etc.), we restore the page index.
        if (null != savedInstanceState) {
            index = savedInstanceState.getInt("current_page", 0);
        }
        showPage(index);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (getArguments() != null) {
                Bundle bundle = getArguments();
                pdf_title = bundle.getString(PDF_TITLE);
                pdf_url = bundle.getString(PDF_URL);
                L.d("title: " + pdf_title);
                L.d("url: " + pdf_url);
            }

            openRenderer(context);
        } catch (IOException e) {
            e.printStackTrace();
            L.e("Fragment Error occurred!");
            L.e("Fragment"+e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
//        try {
//            closeRenderer();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        super.onDestroy();
    }

    @Override
    public void onStop() {
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
        L.e("here");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != currentPage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                outState.putInt("current_page", currentPage.getIndex());
            }
        }
    }

    /**
     * Create a PDF renderer
     * @param context
     * @throws IOException
     */

    private void openRenderer(Context context) throws IOException {


        File directory = new File(context.getFilesDir(), "/Bamburi");
        File pdfFile = new File(directory.getAbsolutePath(), "/"+ pdf_url);

//        File pdfFile = new File("/storage/emulated/0/icourtroom_16.pdf");

        if (pdfFile.exists()) {
            L.e("pdf file exists: "+pdfFile.getAbsolutePath());
        }
        else{

            L.e("pdf file does not exist at: "+pdfFile.getAbsolutePath());
        }

        fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        if (fileDescriptor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pdfRenderer = new PdfRenderer(fileDescriptor);
            }
        }
        else {
            L.e("file descriptor is null");
        }
    }

    /**
     * Closes PdfRenderer and related resources.
     */
    private void closeRenderer() throws IOException {
        if (null != currentPage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                currentPage.close();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pdfRenderer.close();
        }
        fileDescriptor.close();
    }

    /**
     * Shows the specified page of PDF file to screen
     * @param index The page index.
     */
    private void showPage(int index) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (pdfRenderer.getPageCount() <= index) {
                return;
            }
        }
        // Make sure to close the current page before opening another one.
        if (null != currentPage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                currentPage.close();
            }
        }
        //open a specific page in PDF file
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            currentPage = pdfRenderer.openPage(index);
        }
        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(),
                    Bitmap.Config.ARGB_8888);
        }
        // Here, we render the page onto the Bitmap.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        }
        // showing bitmap to an imageview
        image.setImageBitmap(bitmap);
        updateUIData();
    }

    /**
     * Updates the state of 2 control buttons in response to the current page index.
     */
    private void updateUIData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int index = currentPage.getIndex();
            int pageCount = pdfRenderer.getPageCount();
            btnPrevious.setEnabled(0 != index);
            btnNext.setEnabled(index + 1 < pageCount);
            if (getActivity() != null) {
                getActivity().setTitle(getString(R.string.pdf_title, pdf_title, index + 1, pageCount));
            }
        }
    }

    private View.OnClickListener onActionListener(final int i) {
        return v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (i < 0) {//go to previous page
                    showPage(currentPage.getIndex() - 1);
                } else {
                    showPage(currentPage.getIndex() + 1);
                }
            }
        };
    }

}
