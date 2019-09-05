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
import android.widget.TextView;

import com.muva.bamburi.R;
import com.muva.bamburi.activities.auth.AuthActivity;
import com.muva.bamburi.utils.UniversalMethods;

public class PinReset extends Fragment {
    private static final String ARG_PHONE_NUMBER = "phone_number";
    private static final String ARG_RESET_CODE = "reset_code";
    private static final String ARG_IS_A_PASSWORD_CHANGE = "ARG_IS_A_PASSWORD_CHANGE";
    private String phoneNumber;
    private String resetCode;
    private Boolean isAPasswordReset;


    public PinReset() {
    }

    public static PinReset newInstance(String phoneNumber, String resetCode, boolean isAPasswordReset) {
        PinReset fragment = new PinReset();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE_NUMBER, phoneNumber);
        args.putString(ARG_RESET_CODE, resetCode);
        args.putBoolean(ARG_IS_A_PASSWORD_CHANGE, isAPasswordReset);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phoneNumber = getArguments().getString(ARG_PHONE_NUMBER);
            resetCode = getArguments().getString(ARG_RESET_CODE);
            isAPasswordReset = getArguments().getBoolean(ARG_IS_A_PASSWORD_CHANGE);
        }
    }

    AuthActivity activityRef;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() != null) {
            activityRef = (AuthActivity) getActivity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pin_reset, container, false);
    }


    private EditText etPassword, etPhoneNumber, etNewPassword, etConfirmPin;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etPassword = view.findViewById(R.id.et_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPin = view.findViewById(R.id.et_confirm_new_password);
        etPhoneNumber = view.findViewById(R.id.et_phone_number);

        if (phoneNumber != null) {
            etPhoneNumber.setText(phoneNumber);
            etPhoneNumber.setVisibility(View.GONE);
            view.findViewById(R.id.im_phone).setVisibility(View.GONE);

            if (isAPasswordReset) {
                etPassword.setHint("Current Pin");
                etNewPassword.setHint("New Pin");
                ((TextView) view.findViewById(R.id.action_name)).setText(R.string.change_pin);
            }
        }

        if (resetCode != null) {
            etPassword.setText(resetCode);
            activityRef = (AuthActivity) getActivity();
            if (activityRef != null) {
                activityRef.showMessage("Please provide a new pin");
            }
        }

        view.findViewById(R.id.btn_proceed).setOnClickListener(v -> validate());
    }


    public void validate() {
        UniversalMethods.dismissKeyboard(activityRef);

        String newPin = etNewPassword.getText().toString();
        String password = etPassword.getText().toString();
        String confirmationPin = etConfirmPin.getText().toString();
        String phoneNumber = etPhoneNumber.getText().toString();

        if (TextUtils.isEmpty(newPin)) {
            activityRef.showMessage("Please provide a new pin");
        } else if (!UniversalMethods.validatePassword(newPin) || !UniversalMethods.validatePassword(password)) {
            activityRef.showMessage("Pin should be 4 characters");
        } else if (TextUtils.isEmpty(phoneNumber)) {
            activityRef.showMessage("Please provide a phone number");
        } else if (newPin.equals(password)) {
            if (isAPasswordReset)
                activityRef.showMessage("New Pin should not be same as the current one");
            else
                activityRef.showMessage("You cannot use the reset code as your new pin");
        } else if (!UniversalMethods.isPhoneNumberValid(phoneNumber)) {
            activityRef.showMessage("Please provide a valid phone number");
        } else if (!newPin.equals(confirmationPin)) {
            activityRef.showMessage("Pin mismatch");
        } else {
            activityRef.resetPassword(phoneNumber, password, newPin);
        }
    }
}
