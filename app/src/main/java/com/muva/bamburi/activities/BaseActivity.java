package com.muva.bamburi.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.crashlytics.android.Crashlytics;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.Repository;
import com.muva.bamburi.activities.auth.AuthActivity;
import com.muva.bamburi.models.User;
import com.muva.bamburi.models.User_;
import com.muva.bamburi.utils.GlideApp;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;
import com.muva.bamburi.utils.UniversalMethods;
import com.muva.bamburi.utils.customdrawer.DrawerAdapter;
import com.muva.bamburi.utils.customdrawer.NavDrawerDivider;
import com.muva.bamburi.utils.customdrawer.NavDrawerItem;

import java.util.ArrayList;

import io.objectbox.Box;
import io.objectbox.BoxStore;

import static com.muva.bamburi.utils.Constants.INTENTION_RESET_PASSWORD;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected BottomNavigationView navigationView;
    private User user;
    private Box<User> userBox;
    private Settings settings;
    private ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<>();
    private BoxStore boxStore;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());

        boxStore = Bamburi.getInstance().getBoxStore();
        userBox = boxStore.boxFor(User.class);
        settings = new Settings(BaseActivity.this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setup the navigation drawer
        setupNavigationDrawer();

        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navigationView.postDelayed(() -> {
            int itemId = item.getItemId();
            L.e("item id: " + itemId);
            if (itemId == R.id.bnav_whats_new) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (itemId == R.id.bnav_upcoming_events) {
                startActivity(new Intent(this, UpcomingEventsActivity.class));
            } else if (itemId == R.id.bnav_newsletters) {
                startActivity(new Intent(this, NewsletterActivity.class));
            }
//            finish();
        }, 300);
        return true;
    }

    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        if (itemId != -1) {//this is to cater for the CampaignAndOffers Activity
            MenuItem item = navigationView.getMenu().findItem(itemId);
            item.setChecked(true);
        }
    }

    private void setupNavigationDrawer() {

        //set the background image for the nav header
        RelativeLayout relativeLayout = findViewById(R.id.nav_header);
        GlideApp.with(this).load(R.drawable.bamburi_header_logo).centerCrop().into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    relativeLayout.setBackground(resource);
                }
            }
        });

        //setup drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        TextView tvVersionName = findViewById(R.id.version_name);

        //display app version
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            tvVersionName.setText(String.format(getString(R.string.version), versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        //display user_name
        TextView tvUsername = findViewById(R.id.name);

        user = userBox.query().equal(User_.id, settings.getUserLoggedinId()).build().findFirst();

        if (user != null) {
            String greeting = "Hello, " + user.getName();
            tvUsername.setText(greeting);
            //fetch app data
            fetchServerData();
        } else {
//            L.e("user is null for id " + settings.getUserLoggedinId() + " users stored: " + userBox.count());
            Crashlytics.log("objectbox failed to initialize: userbox has no items");
            L.t(BaseActivity.this, "Session has expired, Kindly emailLogin.");
            logout();
        }


        RecyclerView navDrawerRecyclerView = findViewById(R.id.custom_menu_list);
        navDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //get the icons and titles
        String[] titles = getResources().getStringArray(R.array.menu_items_titles);
        TypedArray icons = getResources().obtainTypedArray(R.array.menu_items_icons);

        for (int i = 0; i < titles.length; i++) {
            NavDrawerItem navDrawerItem = new NavDrawerItem();
            navDrawerItem.setId(i + 1);
            navDrawerItem.setTitle(titles[i]);
            navDrawerItem.setIcon(icons.getResourceId(i, 0));
            navDrawerItems.add(navDrawerItem);
        }
        icons.recycle();

        //setup the navigation drawer & adapter
        DrawerAdapter drawerAdapter = new DrawerAdapter(navDrawerItems);

        drawerAdapter.setOnNavDrawerItemClickListener((view, position) -> {
            switch (position) {
                case 0:
                    goToActivity(new MainActivity());
                    break;
                case 1:
                    goToActivity(new UpcomingEventsActivity());
                    break;
                case 2:
                    goToActivity(new CampaignsAndOffersActivity());
                    break;
                case 3:
                    goToActivity(new NewsletterActivity());
                    break;
                case 4:
                    goToActivity(new VideosCornerActivity());
                    break;
                case 5:
                    goToActivity(new PhotosCornerActivity());
                    break;
                case 6:
                    goToActivity(new ContactUsActivity());
                    break;
                case 7:
                    Intent intent = new Intent(BaseActivity.this, AuthActivity.class);
                    intent.putExtra(INTENTION_RESET_PASSWORD, "yeaaah");
                    BaseActivity.this.startActivity(intent);
                    break;
                case 8:
                    logout();
                    break;
            }

            drawerLayout.closeDrawer(GravityCompat.START);

        });

        navDrawerRecyclerView.setAdapter(drawerAdapter);
        navDrawerRecyclerView.addItemDecoration(new NavDrawerDivider(this));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void goToActivity(AppCompatActivity activity) {
        startActivity(new Intent(BaseActivity.this, activity.getClass()));
    }

    private void logout() {
        UniversalMethods.logout(BaseActivity.this);
        BaseActivity.this.finish();
        goToLoginActivity();
    }

    public void goToLoginActivity() {
//        startActivity(new Intent(BaseActivity.this, LoginActivity.class));
        startActivity(new Intent(BaseActivity.this, AuthActivity.class));
        finish();
    }

    private void fetchServerData() {
        if (!settings.isNewsFetched()) {
            Repository.fetchNews(user.getId());
            settings.setNewsFetched(true);
        }
        //always fetch news
        Repository.fetchPolls(user.getId());
    }

    abstract int getContentViewId();

    abstract int getNavigationMenuItemId();
}
