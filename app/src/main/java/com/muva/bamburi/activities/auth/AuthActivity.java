package com.muva.bamburi.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.activities.MainActivity;
import com.muva.bamburi.fragments.auth.Login;
import com.muva.bamburi.fragments.auth.PinReset;
import com.muva.bamburi.models.User;
import com.muva.bamburi.network.RetrofitService;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;
import com.muva.bamburi.utils.UniversalMethods;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.muva.bamburi.utils.Constants.INTENTION_RESET_PASSWORD;
import static com.muva.bamburi.utils.L.alert;
import static com.muva.bamburi.utils.UniversalMethods.handleErrors;

public class AuthActivity extends AppCompatActivity {
    private Box<User> userBox;
    private Settings settings;
    private MaterialDialog progressDialog;
    private boolean userHasJustLoggedIn = false;
    private boolean shouldGoToResetPassword;
    private List<Disposable> disposableList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shouldGoToResetPassword = getIntent().hasExtra(INTENTION_RESET_PASSWORD);
        settings = new Settings(AuthActivity.this);
        userBox = Bamburi.getInstance().getBoxStore().boxFor(User.class);
        disposableList = new ArrayList<>();

        if (!shouldGoToResetPassword)
            determineNextStep();

        setContentView(R.layout.activity_auth);
        progressDialog = new MaterialDialog.Builder(this).content(R.string.please_wait).progress(true, 0).build();

        if (shouldGoToResetPassword) {
            if (settings.getUserLoggedinId() != 0) {
                User user = userBox.get(settings.getUserLoggedinId());
                String phone = "0" + user.getPhone_number().substring(3);
                gotoChangePassword(phone, true);
            } else {
                L.t(this, "You  need to update your password first");
                gotoChangePassword(null, true);
            }
        } else {
            gotoPage(Login.newInstance());
        }
    }


    private void gotoChangePassword(String phoneNumber, boolean isAPasswordReset) {
        gotoPage(PinReset.newInstance(phoneNumber, null, isAPasswordReset));
    }

    private void goToTnCActivity() {
        Intent intent = new Intent(AuthActivity.this, TnCActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToMainActivity() {
        settings.resetDataFetch();
        startActivity(new Intent(AuthActivity.this, MainActivity.class));
        AuthActivity.this.finish();
    }

    public void login(String phone, String password) {
        if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(password)) {
            phone = UniversalMethods.makeValidPhoneNumber(phone);

            disposableList.add(
                    RetrofitService.getInstance()
                            .phoneLogin(phone, password)
                            .toObservable()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe(consumer -> progressDialog.show())
                            .doOnComplete(() -> progressDialog.dismiss())
                            .doOnError(consumer2 -> progressDialog.dismiss())
                            .subscribe(userResponse -> {
                                if (userResponse.isSuccess()) {
                                    User user = userResponse.getDatum();
                                    onSuccessfulLogin(user);
                                } else {
                                    switch (userResponse.getCode()) {
                                        case 200:
                                            alert(AuthActivity.this, "Note", userResponse.getMessage());
                                            break;
                                        case 401:
                                            //alert(TnCActivity.this, null, "Incorrect email or password. Try again!");
                                            showMessage("Invalid email or password. Try again!");
                                            break;
                                        case 500:
                                            alert(AuthActivity.this, null, getString(R.string.something_went_wrong));
                                            break;
                                        case 403:
                                            alert(AuthActivity.this, null, userResponse.getMessage());
                                            break;
                                    }
                                }
                            }, throwable -> {
                                UniversalMethods.handleErrors(throwable, AuthActivity.this);
                                L.e("error emailLogin user in: " + throwable.getMessage());
                            }));
        }
    }


    private void determineNextStep() {
        int userCount = (int) userBox.count();

        if (userCount > 0 && settings.isUserLoggedIn()) {
            //user is logged in
            if (settings.isUserFirstTimeLogin() || settings.isRequirePasswordReset()) {
                //requires to change password
                gotoChangePassword(null, false);
            } else if (userHasJustLoggedIn) {
                //show the Terms And Conditions
                goToTnCActivity();
            } else {
                //proceed to main
                goToMainActivity();
            }

        }
        // carry on with emailLogin
        if (settings.isUserTimedOut()) {
            UniversalMethods.logout(AuthActivity.this);
            showMessage("Your Account is Suspended. Kindly Contact the System Admin");
        }

        if (!settings.isUserExists()) {
            UniversalMethods.logout(AuthActivity.this);
            showMessage("You don't have an Account. Contact the system admin");
        }
    }


    public void gotoPage(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finish();
        }
    }

    public void showMessage(String message) {
        Snackbar.make(findViewById(R.id.content_main), message, 10000)
                .setAction("OK", view -> {

                })
                .setActionTextColor(getResources().getColor(R.color.colorAccent))
                .show();
    }


    public void sendResetLink(String phone) {
        phone = UniversalMethods.makeValidPhoneNumber(phone);
        disposableList.add(RetrofitService.getInstance()
                .phoneForgotPassword(phone)
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
                                showMessage(userResponse.getMessage());
                            } else {
                                alert(AuthActivity.this, null, userResponse.getMessage());
                            }
                        },
                        throwable -> {
                            progressDialog.dismiss();
                            L.e("error sending reset link: " + throwable.getMessage());
                            handleErrors(throwable, AuthActivity.this);
                        }
                ));
    }


    private GoogleApiClient apiClient = null;

    public GoogleApiClient getGoogleApiClient() throws Exception {
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */,
                            connectionResult -> showMessage(connectionResult.getErrorMessage()) /* OnConnectionFailedListener */)
                    .addApi(Auth.CREDENTIALS_API)
                    .build();
        } else {
            throw new Exception("Could not initialize google client api");
        }

        return apiClient;
    }

    public void resetPassword(String phoneNumber, String password, String newPassword) {
        phoneNumber = UniversalMethods.makeValidPhoneNumber(phoneNumber);
        disposableList.add(RetrofitService.getInstance()
                .resetPasswordPhone(phoneNumber, password, newPassword)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(consumer -> progressDialog.dismiss())
                .doOnComplete(() -> progressDialog.dismiss())
                .doOnSubscribe(consumer -> progressDialog.show())
                .subscribe(userResponse -> {

                    if (userResponse.isSuccess()) {
                        if (shouldGoToResetPassword) {
                            new MaterialDialog.Builder(AuthActivity.this)
                                    .content(userResponse.getMessage())
                                    .positiveText(R.string.ok)
                                    .onPositive((dialog, which) -> {
                                        dialog.dismiss();
                                        AuthActivity.this.finish();
                                    })
                                    .show();
                        } else {
                            User user = userResponse.getDatum();
                            onSuccessfulLogin(user);
                        }
                    } else {
                        showMessage(userResponse.getMessage());
                    }

                }, throwable -> {
                    L.e("error changing password: " + throwable.getMessage());
                    UniversalMethods.handleErrors(throwable, AuthActivity.this);
                }));
    }


    public void onSuccessfulLogin(User user) {
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

//        userBox.put(user1);
//        settings.setUserFirstTimeLogin(user1.isFirst_time_login());
//        settings.setRequirePasswordReset(user1.isRequire_password_reset());
//        settings.resetDataFetch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (Disposable disposable : disposableList) {
            if (disposable != null)
                disposable.dispose();
        }
    }
}
