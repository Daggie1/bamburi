package com.muva.bamburi.activities;

import android.os.Bundle;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.adapters.HomeAdapter;
import com.muva.bamburi.models.News;
import com.muva.bamburi.models.News_;
import com.muva.bamburi.utils.Settings;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;

import static com.muva.bamburi.utils.Constants.UPCOMING_EVENTS;
import static com.muva.bamburi.utils.UniversalMethods.trackScreen;

public class UpcomingEventsActivity extends BaseActivity {

    private TextView tvNoData;
    private RecyclerView recyclerView;
    private HomeAdapter homeAdapter;
    private Settings settings;

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

                return newDate.compareTo(oldDate) == 0 && oldItem.isDownloaded() == newItem.isDownloaded() && newItem.getOrder_index() == oldItem.getOrder_index() && oldItem.getDeleted_at().equalsIgnoreCase(newItem.getDeleted_at());
            } catch (Exception exception) {

                return false;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BoxStore boxStore = Bamburi.getInstance().getBoxStore();
        Box<News> newsBox = boxStore.boxFor(News.class);

        settings = new Settings(UpcomingEventsActivity.this);

        //by default setup news type
        tvNoData = findViewById(R.id.tv_no_data);
        recyclerView = findViewById(R.id.recyclerView);

        homeAdapter = new HomeAdapter(UpcomingEventsActivity.this, newsItemCallback);

        recyclerView.setLayoutManager(new LinearLayoutManager(UpcomingEventsActivity.this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(homeAdapter);


        Query<News> newsQuery = newsBox.query().equal(News_.news_type, UPCOMING_EVENTS).orderDesc(News_.id).build();

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

            tvNoData.setText(getString(R.string.no_data, "upcoming events"));
        }

    }

    @Override
    int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.bnav_upcoming_events;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //track screen
        trackScreen("Upcoming Event");
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
