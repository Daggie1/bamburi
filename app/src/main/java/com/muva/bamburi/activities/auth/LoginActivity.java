package com.muva.bamburi.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.activities.MainActivity;
import com.muva.bamburi.models.User;
import com.muva.bamburi.network.RetrofitService;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;
import com.muva.bamburi.utils.UniversalMethods;

import io.objectbox.Box;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.muva.bamburi.utils.L.alert;
import static com.muva.bamburi.utils.UniversalMethods.isEmailValid;
import static com.muva.bamburi.utils.UniversalMethods.isPinValid;
import static com.muva.bamburi.utils.UniversalMethods.trackScreen;

/**
 * A emailLogin screen that offers emailLogin via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // UI references.
    private EditText mEmailView, mPasswordView;
    private Settings settings;
    private Box<User> userBox;
    private MaterialDialog progressDialog;
    private boolean userHasJustLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(LoginActivity.this);

        userBox = Bamburi.getInstance().getBoxStore().boxFor(User.class);

        determineNextStep();

        setContentView(R.layout.activity_login);
        // Set up the emailLogin form.
        mEmailView = findViewById(R.id.email);


        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                validateData();
                return true;
            }
            return false;
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(view -> validateData());

        TextView tv_forgotPassword = findViewById(R.id.forgot_password);
        tv_forgotPassword.setOnClickListener(v -> goToForgotPassword());

        //build progressbar
        progressDialog = new MaterialDialog.Builder(this).content(R.string.please_wait).progress(true, 0).build();

    }

    private void goToChangePasswordActivity() {
        startActivity(new Intent(LoginActivity.this, ChangePasswordActivity.class));
    }

    private void goToForgotPassword() {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }

    private void validateData() {
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
        } else if (!isPinValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
        } else {
            attemptLogin(email, password);
        }
    }

    private void goToTnCActivity() {
        Intent intent = new Intent(LoginActivity.this, TnCActivity.class);
        startActivity(intent);
    }

    private void goToMainActivity() {
        settings.resetDataFetch();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        LoginActivity.this.finish();
    }

    private void attemptLogin(String email, String password) {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            Disposable disposable = RetrofitService.getInstance()
                    .emailLogin(email, password)
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(consumer -> progressDialog.show())
                    .doOnComplete(() -> progressDialog.dismiss())
                    .doOnError(consumer2 -> progressDialog.dismiss())
                    .subscribe(userResponse -> {
                        if (userResponse.isSuccess()) {
                            User user = userResponse.getDatum();
                            userBox.put(user);
                            settings.setUserLoggedIn(true);
                            settings.setUserLoggedinId(user.getId());
                            settings.setUserFirstTimeLogin(user.isFirst_time_login());
                            settings.setUserTimeOut(false);
                            settings.setRequirePasswordReset(user.isRequire_password_reset());
                            settings.setUserExists(true);
                            userHasJustLoggedIn = true;
                            L.e("users stored: " + userBox.count());
                            determineNextStep();

                        } else {
                            switch (userResponse.getCode()) {
                                case 200:
                                    alert(LoginActivity.this,"Note", userResponse.getMessage());
                                    break;
                                case 401:
                                    //alert(TnCActivity.this, null, "Incorrect email or password. Try again!");
                                    L.T(LoginActivity.this, "Invalid email or password. Try again!");
                                    break;
                                case 500:
                                    alert(LoginActivity.this, null, getString(R.string.something_went_wrong));
                                    break;
                                case 403:
                                    alert(LoginActivity.this, null, userResponse.getMessage());
                                    break;
                            }
                        }
                    }, throwable -> {
                        UniversalMethods.handleErrors(throwable, LoginActivity.this);
                        L.e("error emailLogin user in: " + throwable.getMessage());
                    });
        }

    }

    private void goToChangePasswordActivty() {
        Intent intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void determineNextStep() {
        int userCount = (int) userBox.count();

        if (userCount > 0 && settings.isUserLoggedIn()) {
            //user is logged in
            if (settings.isUserFirstTimeLogin() || settings.isRequirePasswordReset()) {
                //requires to change password
                goToChangePasswordActivity();
            }
            else if(userHasJustLoggedIn){
                //show the Terms And Conditions
                goToTnCActivity();
            }
            else{
                //proceed to main
                goToMainActivity();
            }

        }
        // carry on with emailLogin
        if (settings.isUserTimedOut()) {
            UniversalMethods.logout(LoginActivity.this);
            L.T(LoginActivity.this, "Your Account is Suspended. Kindly Contact the System Admin");
        }

        if (!settings.isUserExists()) {
            UniversalMethods.logout(LoginActivity.this);
            L.T(LoginActivity.this,"You don't have an Account. Contact the system admin");
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        //track screen
        trackScreen("Login");
    }
}

