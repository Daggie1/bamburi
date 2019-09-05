package com.muva.bamburi.activities;

import android.os.Bundle;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.adapters.HomeAdapter;
import com.muva.bamburi.models.News;
import com.muva.bamburi.models.News_;
import com.muva.bamburi.models.Poll;
import com.muva.bamburi.models.User;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;
import com.muva.bamburi.utils.customdrawer.NavDrawerItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;

import static com.muva.bamburi.utils.Constants.CAMPAIGNS_AND_OFFERS;
import static com.muva.bamburi.utils.UniversalMethods.trackScreen;

public class CampaignsAndOffersActivity extends BaseActivity {

    private Toolbar toolbar;
    private Settings settings;
    private Box<Poll> pollsBox;
    private Box<News> newsBox;
    private Box<User> userBox;
    private TextView tvNoData;
    private RecyclerView recyclerView;
    private HomeAdapter homeAdapter;
    private Query<News> newsQuery;
    private Query<Poll> pollQuery;
    private ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<>();

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
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        settings = new Settings(CampaignsAndOffersActivity.this);
        BoxStore boxStore = Bamburi.getInstance().getBoxStore();
        pollsBox = boxStore.boxFor(Poll.class);
        newsBox = boxStore.boxFor(News.class);
        userBox = boxStore.boxFor(User.class);

        L.e("number of users: "+userBox.count());

        //setup the news
        //by default setup news type
        tvNoData = findViewById(R.id.tv_no_data);
        recyclerView = findViewById(R.id.recyclerView);

        homeAdapter = new HomeAdapter(CampaignsAndOffersActivity.this, newsItemCallback);

        recyclerView.setLayoutManager(new LinearLayoutManager(CampaignsAndOffersActivity.this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(homeAdapter);


        newsQuery = newsBox.query().equal(News_.news_type, CAMPAIGNS_AND_OFFERS).orderDesc(News_.id).build();

        newsQuery.subscribe().on(AndroidScheduler.mainThread()).observer(data -> {
            if (data.size() >0 && tvNoData != null && recyclerView != null) {
                tvNoData.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            homeAdapter.submitList(data);
        });

        if (newsQuery.find().size() == 0) {
            tvNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            tvNoData.setText(getString(R.string.no_data,"campaigns and offers"));
        }
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    int getNavigationMenuItemId() {
        return -1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //track screen
        trackScreen("Campaigns and Offers");
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
