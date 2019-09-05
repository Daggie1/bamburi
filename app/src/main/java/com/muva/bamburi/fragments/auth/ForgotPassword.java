package com.muva.bamburi.fragments.auth;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.muva.bamburi.R;
import com.muva.bamburi.activities.auth.AuthActivity;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.UniversalMethods;

import static android.app.Activity.RESULT_OK;

public class ForgotPassword extends Fragment {
    private static final String ARG_PHONE_NUMBER = "phone_number";
    private static final int RESOLVE_HINT = 192;
    private String phoneNumber;

    public ForgotPassword() {
    }

    public static ForgotPassword newInstance(String phoneNumber) {
        ForgotPassword fragment = new ForgotPassword();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE_NUMBER, phoneNumber);
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phoneNumber = getArguments().getString(ARG_PHONE_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etPhoneNumber = view.findViewById(R.id.et_phone_number);
        etPhoneNumber.setText(phoneNumber);

        view.findViewById(R.id.btn_proceed).setOnClickListener(v -> validateData());
        view.findViewById(R.id.btn_resend_reset_code).setOnClickListener(v -> validateData());

        view.findViewById(R.id.btn_have_reset_code).setOnClickListener(v ->
                activityRef.gotoPage(PinReset.newInstance(etPhoneNumber.getText().toString(), null, false)));

    }

    @Override
    public void onStart() {
        super.onStart();

        activityRef = (AuthActivity) getActivity();
        requestHint();
    }

    private EditText etPhoneNumber;

    private void validateData() {
        String phone = etPhoneNumber.getText().toString().trim();
        UniversalMethods.dismissKeyboard(activityRef);

        if (TextUtils.isEmpty(phone)) {
//            etPhoneNumber.setError(getString(R.string.error_field_required));
            activityRef.showMessage(getString(R.string.error_field_required));
        } else if (!UniversalMethods.isPhoneNumberValid(phone)) {
//            etPhoneNumber.setError(getString(R.string.error_invalid_email));
            activityRef.showMessage(getString(R.string.error_invalid_phone));
        } else {
            prepareListener();

            //send reset link
            if (activityRef != null) {
                activityRef.sendResetLink(phone);
            } else {
                L.e("AuthActivity is null");
            }
        }
    }


    // Construct a request for phone numbers and show the picker
    private void requestHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        try {
            if (activityRef == null) {
                L.e("Could not access parent activity");
                return;
            }


            GoogleApiClient apiClient = activityRef.getGoogleApiClient();

            if (apiClient != null) {
                PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(apiClient, hintRequest);
                startIntentSenderForResult(intent.getIntentSender(), RESOLVE_HINT, null, 0, 0, 0, null);
            }
        } catch (Exception e) {
            L.e(e.getMessage());
        }
    }

    // Obtain the phone number from the result


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId();  <-- will need to process phone number string

                etPhoneNumber.setText(credential.getId().replace("+2547", "07"));
                validateData();
            }
        }
    }


    private void prepareListener() {
        // Get an instance of SmsRetrieverClient, used to start listening for a matching
        // SMS message.
        SmsRetrieverClient client = SmsRetriever.getClient(getContext());

        // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
        // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
        // action SmsRetriever#SMS_RETRIEVED_ACTION.
        Task<Void> task = client.startSmsRetriever();

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener(aVoid -> activityRef.showMessage("You will receive a verification code in a few minutes..."));

        task.addOnFailureListener(e -> L.t(getContext(), "Failed to initate retrieval of verification code"));
    }


    public BroadcastReceiver verificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);



                switch (status.getStatusCode()) {
                    case CommonStatusCodes.SUCCESS:
                        // Get SMS message contents
                        String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                        // Extract one-time code from the message and complete verification
                        // by sending the code back to your server.

                        String[] incomingStringArray = message.split("\\s+");

                        if (incomingStringArray.length > 5) {
                            activityRef.showMessage("retriever :: Reset code is " + incomingStringArray[5]);
                            L.d("retriever :: Result :: " + incomingStringArray[5]);
                            activityRef.gotoPage(PinReset.newInstance(etPhoneNumber.getText().toString(), incomingStringArray[5], false));
                        }
                        break;
                    case CommonStatusCodes.TIMEOUT:
                        // Waiting for SMS timed out (5 minutes)
                        // Handle the error ...
                        activityRef.showMessage("Getting SMS delayed, press on 'I have a reset code' once you get it");
                        break;
                }
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null)
            getContext().registerReceiver(verificationReceiver, new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null)
            getContext().unregisterReceiver(verificationReceiver);
    }
}
