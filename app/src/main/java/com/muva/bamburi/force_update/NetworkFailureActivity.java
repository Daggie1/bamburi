package com.muva.bamburi.force_update;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;

import com.muva.bamburi.R;

public class NetworkFailureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_failure);
        if(isNetworkAvailableAndConnected()){
         startNextActivity();
        }
        startNextActivity();
            findViewById(R.id.tryNetwoekBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isNetworkAvailableAndConnected()){
                     startNextActivity();
                    }
                }
            });



    }
    private boolean isNetworkAvailableAndConnected()
    {
        ConnectivityManager cm =
                (ConnectivityManager)
                        getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable =
                cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected =
                isNetworkAvailable &&
                        cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
    private void startNextActivity(){
        startActivity(new Intent(NetworkFailureActivity.this,ForceUpdateActivity.class));
        finish();
    }

}
