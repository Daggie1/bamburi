package com.muva.bamburi.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.muva.bamburi.R;
import com.muva.bamburi.network.RetrofitService;
import com.muva.bamburi.utils.L;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.muva.bamburi.utils.L.alert;
import static com.muva.bamburi.utils.UniversalMethods.handleErrors;
import static com.muva.bamburi.utils.UniversalMethods.isEmailValid;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.email);
        Button btnSendResetLink = findViewById(R.id.email_send_link_button);
        Button btnBack = findViewById(R.id.back_button);

        btnSendResetLink.setOnClickListener(v -> validateData());

        btnBack.setOnClickListener(v -> goToLoginActivity());

        progressDialog = new MaterialDialog.Builder(this)
                .content(getString(R.string.please_wait))
                .progress(true, 0)
                .build();
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(new Intent(intent));
        finish();
    }

    private void validateData() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_field_required));
        } else if (!isEmailValid(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
        } else {
            //send reset link
            sendResetLink(email);
        }

    }

    private void sendResetLink(String email) {
        Disposable disposable = RetrofitService.getInstance().forgotPassword(email)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(consumer -> progressDialog.show())
                .doOnComplete(() -> progressDialog.dismiss())
                .doOnError(consumer2 -> progressDialog.dismiss())
                .subscribe(
                        userResponse -> {
                            progressDialog.dismiss();
                            if (userResponse.isSuccess()) {
//                                alert(ForgotPasswordActivity.this, null, userResponse.getMessage());
                                new MaterialDialog.Builder(ForgotPasswordActivity.this)
                                        .content(userResponse.getMessage())
                                        .positiveText(R.string.ok)
                                        .onPositive((dialog, which) -> {
                                            dialog.dismiss();

                                            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                                        })
                                        .show();

                            } else {
//                                L.e("error sending reset link: " + userResponse.getMessage());
                                alert(ForgotPasswordActivity.this, null, userResponse.getMessage());
                            }
                        },
                        throwable -> {
                            progressDialog.dismiss();
                            L.e("error sending reset link: " + throwable.getMessage());
                            handleErrors(throwable, ForgotPasswordActivity.this);
                        }
                );
    }
}
