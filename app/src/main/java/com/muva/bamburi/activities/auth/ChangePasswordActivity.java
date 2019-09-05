package com.muva.bamburi.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.activities.MainActivity;
import com.muva.bamburi.models.User;
import com.muva.bamburi.models.User_;
import com.muva.bamburi.network.RetrofitService;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;
import com.muva.bamburi.utils.UniversalMethods;

import io.objectbox.Box;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.muva.bamburi.utils.UniversalMethods.isPinValid;
import static com.muva.bamburi.utils.UniversalMethods.trackScreen;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Box<User> userBox;
    private Settings settings;
    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        settings = new Settings(this);

        etCurrentPassword = findViewById(R.id.currentpassword);
        etNewPassword = findViewById(R.id.newpassword);
        etConfirmPassword = findViewById(R.id.confirm_new_password);

        Button btnChangePassword = findViewById(R.id.password_change_button);

        btnChangePassword.setOnClickListener(v -> validateData());

        userBox = Bamburi.getInstance().getBoxStore().boxFor(User.class);

        progressDialog = new MaterialDialog.Builder(this)
                .content(getString(R.string.please_wait))
                .progress(true, 0)
                .build();

        L.T(ChangePasswordActivity.this, "You are required to change your password. Kindly do so. Thank you!");
    }

    private void validateData() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError(getString(R.string.error_field_required));
        } else if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError(getString(R.string.error_field_required));
        } else if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_field_required));
        } else if (!isPinValid(currentPassword)) {
            etCurrentPassword.setError(getString(R.string.error_invalid_password));
        } else if (!isPinValid(newPassword)) {
            etNewPassword.setError(getString(R.string.error_invalid_password));
        } else if (!isPinValid(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_invalid_password));
        } else if (currentPassword.equals(newPassword)) {
            etNewPassword.setError(getString(R.string.passwords_same));
        } else if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_passwords_not_matching));
        } else {
            attemptPasswordChange(currentPassword, newPassword);
        }

    }

    private void attemptPasswordChange(String currentPassword, String newPassword) {
        User user = userBox.query().equal(User_.id, settings.getUserLoggedinId()).build().findFirst();
        if (user != null) {
            Disposable disposable = RetrofitService.getInstance()
                    .resetPassword(user.getEmail(), currentPassword, newPassword)
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(consumer -> progressDialog.dismiss())
                    .doOnComplete(() -> progressDialog.dismiss())
                    .doOnSubscribe(consumer -> progressDialog.show())
                    .subscribe(userResponse -> {
                        if (userResponse.isSuccess()) {
                            User user1 = userResponse.getDatum();

                            userBox.put(user1);
                            settings.setUserFirstTimeLogin(user1.isFirst_time_login());
                            settings.setRequirePasswordReset(user1.isRequire_password_reset());
                            settings.resetDataFetch();

                            goToMainActivity();
                        } else {
                            L.e("message: " + userResponse.getMessage());
                            L.alert(ChangePasswordActivity.this, null, userResponse.getMessage());
                        }

                    }, throwable -> {
                        L.e("error changing password: " + throwable.getMessage());
                        UniversalMethods.handleErrors(throwable, ChangePasswordActivity.this);
                    });

        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //track screen
        trackScreen("Change Password");
    }
}
