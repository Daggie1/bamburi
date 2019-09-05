package com.muva.bamburi.utils;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Njoro on 3/23/18.
 */

public class FragmentHelper {
    public static FragmentManager getFragmentManager(Context context) {
        return ((AppCompatActivity) context).getSupportFragmentManager();
    }

    public static void openFragment(Context context, int frameId, Fragment fragment) {
        getFragmentManager(context).beginTransaction()
                .replace(frameId, fragment, fragment.getClass().toString())
                .addToBackStack(null).commitAllowingStateLoss();
    }

    public static void addFragment(Context context, int frameId, Fragment fragment) {
        getFragmentManager(context).beginTransaction()
                .add(frameId, fragment, fragment.getClass().toString())
                .addToBackStack(null).commitAllowingStateLoss();
//                fragmentTransaction.commitAllowingStateLoss();
    }
}
