package com.muva.bamburi.fragments.auth;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.muva.bamburi.R;
import com.muva.bamburi.activities.auth.AuthActivity;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.UniversalMethods;

import static com.muva.bamburi.utils.UniversalMethods.isPinValid;

public class Login extends Fragment {
    public Login() {
    }

    public static Login newInstance() {
        return new Login();
    }

    AuthActivity activityRef;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (this.getActivity() != null) {
            activityRef = (AuthActivity) getActivity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPhoneNumber = view.findViewById(R.id.et_phone_number);
        mPasswordView = view.findViewById(R.id.et_password);

        view.findViewById(R.id.btn_login).setOnClickListener(v -> {
            validateData();
        });

        view.findViewById(R.id.btn_forgot_pin).setOnClickListener(v -> {
            if (activityRef != null) {
                activityRef.gotoPage(ForgotPassword.newInstance(mPhoneNumber.getText().toString()));
            }
        });
    }

    private EditText mPhoneNumber, mPasswordView;

    private void validateData() {
        // dismiss keyboard
        UniversalMethods.dismissKeyboard(activityRef);
        String phone = mPhoneNumber.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
//            mPhoneNumber.setError(getString(R.string.error_field_required));
            activityRef.showMessage(getString(R.string.error_field_required));
        } else if (TextUtils.isEmpty(password)) {
//            mPasswordView.setError(getString(R.string.error_field_required));
            activityRef.showMessage(getString(R.string.error_field_required));
//        } else if (!isEmailValid(email)) {
        } else if (!UniversalMethods.isPhoneNumberValid(phone)) {
//            mPhoneNumber.setError(getString(R.string.error_invalid_email));
            activityRef.showMessage(getString(R.string.error_invalid_phone));
        } else if (!isPinValid(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
            activityRef.showMessage(getString(R.string.error_invalid_password));
        } else {
            if (activityRef != null) {
                activityRef.login(phone, password);
            } else {
                L.e("Auth activity is null");
            }
        }
    }


}
