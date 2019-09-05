package com.muva.bamburi.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.muva.bamburi.R;

/**
 * Created by Njoro on 4/24/18.
 */
public class L {
    private static final String TAG = "riven::";

    public static void d(String message) {
        Log.d(TAG, message + "");
    }

    public static void e(String message) {
        Log.e(TAG, message+"");
    }

    public static void t(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void T(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void alert(Context context, String title, String message) {
        if (TextUtils.isEmpty(title)) {
            new MaterialDialog.Builder(context)
                    .content(message)
                    .positiveText(R.string.ok)
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            new MaterialDialog.Builder(context)
                    .title(title)
                    .content(message)
                    .positiveText(R.string.ok)
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .show();

        }
    }

}

