package com.muva.bamburi.activities.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.activities.MainActivity;
import com.muva.bamburi.models.User;
import com.muva.bamburi.utils.Settings;

import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.objectbox.Box;
import io.objectbox.BoxStore;

import static com.muva.bamburi.utils.Constants.EXTRA_PASS;
import static com.muva.bamburi.utils.L.alert;

public class TnCActivity extends AppCompatActivity {

    @BindView(R.id.textView)
    TextView tocTextView;
    @BindView(R.id.checkBox)
    CheckBox cb_acceptTerms;
    @BindView(R.id.btn_proceed)
    Button btnProceed;

    private String email, password;
    private MaterialDialog progressDialog;
    private Box<User> userBox;
    private Settings settings;

    private FirebaseAnalytics mFirebaseAnalytics;

    private static String readFromFile(Context context, String file) {

        try {
            InputStream is = context.getResources().openRawResource(R.raw.tnc);
            int size = is.available();
            byte buffer[] = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer);
        } catch (Exception e) {
            Log.d("APP", "error reading file....: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_c);

        ButterKnife.bind(this);

        settings = new Settings(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                email = bundle.getString(Intent.EXTRA_EMAIL);
                password = bundle.getString(EXTRA_PASS);
            }
        }
        //read the tnc doc from the assets folder
        String toc_text = "no data";
        try {
            toc_text = readFromFile(getApplicationContext(), "tnc.txt");
        } catch (Exception exception) {
            Log.d("APP", "error reading file: " + exception.getMessage());
        }
        tocTextView.setText(toc_text);


        BoxStore boxStore = Bamburi.getInstance().getBoxStore();
        userBox = boxStore.boxFor(User.class);
    }

    @OnClick(R.id.btn_proceed)
    void confirmTermsAccepted(View view) {
        if (cb_acceptTerms.isChecked()) {
            //proceed to MainActivity
            goToMainActivity();
        } else {
            alert(TnCActivity.this, null, "You MUST Accept the Terms to Continue");
        }
    }



    private void goToMainActivity() {
        Intent intent = new Intent(TnCActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
//        TnCActivity.this.finish();
    }

}
