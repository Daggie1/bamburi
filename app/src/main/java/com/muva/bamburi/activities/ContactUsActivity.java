package com.muva.bamburi.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.muva.bamburi.R;
import com.muva.bamburi.network.RetrofitService;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ContactUsActivity extends AppCompatActivity {
    private CompositeDisposable compositeDisposable;
    private MaterialDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        compositeDisposable = new CompositeDisposable();

        //binding
        EditText etTitle = findViewById(R.id.et_title);
        EditText etDescription = findViewById(R.id.et_description);
        Button btnSubmit = findViewById(R.id.btnSubmit);


        progressBar = new MaterialDialog.Builder(ContactUsActivity.this)
                .title("Posting Feedback...")
                .content(getResources().getString(R.string.please_wait))
                .progress(true,0)
                .cancelable(false)
                .build();

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String comment = etDescription.getText().toString().trim();

            progressBar.show();

            sendFeedback(title, comment);
        });


    }

    private void sendFeedback(String title, String comment) {

        long user_id = new Settings(getApplicationContext()).getUserLoggedinId();
        Disposable disposable = RetrofitService.getInstance().sendFeedback(user_id,title,comment)
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response-> {
                            if (progressBar != null && progressBar.isShowing()){
                                progressBar.dismiss();
                            }
                            L.t(getApplicationContext(),response.getMessage());
                        },
                        throwable -> {
                            if (progressBar != null && progressBar.isShowing()){
                                progressBar.dismiss();
                            }
                            Answers.getInstance().logCustom(new CustomEvent("Error:: " + throwable.getMessage()));
                        }
                );
        compositeDisposable.add(disposable);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!compositeDisposable.isDisposed())
            compositeDisposable.dispose();
    }
}
