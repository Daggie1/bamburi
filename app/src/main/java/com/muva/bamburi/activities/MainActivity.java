package com.muva.bamburi.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.Repository;
import com.muva.bamburi.adapters.HomeAdapter;
import com.muva.bamburi.fragments.PollsPopupFragment;
import com.muva.bamburi.models.News;
import com.muva.bamburi.models.News_;
import com.muva.bamburi.models.Poll;
import com.muva.bamburi.models.Poll_;
import com.muva.bamburi.utils.Settings;
import com.muva.bamburi.utils.UniversalMethods;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;

import static com.muva.bamburi.utils.Constants.NEWS;
import static com.muva.bamburi.utils.UniversalMethods.trackScreen;

public class MainActivity extends BaseActivity {

    private Query<Poll> pollQuery;
    private Settings settings;
    private TextView tvNoData;
    private RecyclerView recyclerView;
    private HomeAdapter homeAdapter;

    private DiffUtil.ItemCallback<News> newsItemCallback = new DiffUtil.ItemCallback<News>() {
        @Override
        public boolean areItemsTheSame(News oldItem, News newItem) {
            return oldItem.getId() == newItem.getId() && oldItem.getNews_type() == newItem.getNews_type();
        }

        @Override
        public boolean areContentsTheSame(News oldItem, News newItem) {
            try {
                //compare the dates
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date oldDate = format.parse(oldItem.getCreated_at());
                Date newDate = format.parse(newItem.getCreated_at());

                return newDate.compareTo(oldDate) == 0 && oldItem.isDownloaded() == newItem.isDownloaded() && oldItem.getDeleted_at().equalsIgnoreCase(newItem.getDeleted_at()) && newItem.getOrder_index() == oldItem.getOrder_index();
            } catch (Exception exception) {

                return false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new Settings(MainActivity.this);
        BoxStore boxStore = Bamburi.getInstance().getBoxStore();
        Box<Poll> pollsBox = boxStore.boxFor(Poll.class);
        Box<News> newsBox = boxStore.boxFor(News.class);

        if (!settings.isFcmTokenUpdated()) {
            registerFCMTokenOnline();
        }

        //setup navigation channel for Oreo
        setupNotificationChannel();

        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);

                if (key.equalsIgnoreCase("message")) {
                    String message = getIntent().getExtras().getString("message");
                    String type = getIntent().getExtras().getString("key");
                    UniversalMethods.sendNotification(this, message, type);
                }
            }
        }

        tvNoData = findViewById(R.id.tv_no_data);
        recyclerView = findViewById(R.id.recyclerView);

        homeAdapter = new HomeAdapter(MainActivity.this, newsItemCallback);

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(homeAdapter);


        Query<News> newsQuery = newsBox.query().equal(News_.news_type, NEWS).and().isNull(News_.deleted_at).order(News_.order_index).build();
        pollQuery = pollsBox.query().equal(Poll_.dismissed, false).filter(Poll::isActive).build();


        newsQuery.subscribe().on(AndroidScheduler.mainThread()).observer(data -> {
            if (data.size() > 0 && tvNoData != null && recyclerView != null) {
                tvNoData.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            homeAdapter.submitList(data);
        });

        if (newsQuery.find().size() == 0) {
            tvNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            tvNoData.setText(getString(R.string.no_data, "news"));
        }

        setupPolls();
    }


    private void setupPolls() {
        pollsPopupFragment = PollsPopupFragment.newInstance();
        //if polls exist display alert dialog
        if (pollQuery.find().size() > 0) {
            loadPollsDialogFragment();
        }

        //listen for poll changes....
//        pollQuery.subscribe().on(AndroidScheduler.mainThread()).observer(data -> {
//            if (pollQuery.find().size() > 0) {
//                pollsPopupFragment.pollsAdapter.notifyDataSetChanged();
//            }
//        });

    }

    PollsPopupFragment pollsPopupFragment;

    private void loadPollsDialogFragment() {
//        FragmentHelper.openFragment(MainActivity.this, R.id.fragment_container, new PollsPopupFragment());
        gotoPage(pollsPopupFragment);
    }

    public void gotoPage(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack(fragment.getTag()).commit();
    }

    private void registerFCMTokenOnline() {
        String fcm_token = settings.getFcmToken();
        if (fcm_token.equalsIgnoreCase("")) {
            fcm_token = FirebaseInstanceId.getInstance().getToken();
            settings.setFcmToken(fcm_token);
        }
        String user_id = String.valueOf(settings.getUserLoggedinId());

        if (!TextUtils.isEmpty(fcm_token)) {
            Repository.updateFCMToken(MainActivity.this, user_id, fcm_token);
        }
    }

    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH));
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }


    @Override
    int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.bnav_whats_new;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //track screen
        trackScreen("News");
    }

    @Override
    public void onPause() {
        super.onPause();
        settings.setNewsFetched(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        settings.setNewsFetched(false);

    }
}
