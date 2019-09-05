package com.muva.bamburi.activities;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.Repository;
import com.muva.bamburi.adapters.NewsletterAdapter;
import com.muva.bamburi.databinding.ActivityNewsletterBinding;
import com.muva.bamburi.models.NewsLetterCategory;
import com.muva.bamburi.models.Newsletter;
import com.muva.bamburi.models.Newsletter_;
import com.muva.bamburi.utils.Settings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;

import static com.muva.bamburi.utils.UniversalMethods.trackScreen;

public class NewsletterActivity extends AppCompatActivity {

    private NewsletterAdapter newsletterAdapter;

    private Query<Newsletter> newsletterQuery;

    private Box<Newsletter> newsletterBox;

    private List<NewsLetterCategory> newsLetterCategories;
    private Settings settings;

    private DiffUtil.ItemCallback<Newsletter> newsletterItemCallback = new DiffUtil.ItemCallback<Newsletter>() {
        @Override
        public boolean areItemsTheSame(Newsletter oldItem, Newsletter newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Newsletter oldItem, Newsletter newItem) {
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

        ActivityNewsletterBinding binding = DataBindingUtil.setContentView(NewsletterActivity.this, R.layout.activity_newsletter);

        settings = new Settings(NewsletterActivity.this);

        RecyclerView recyclerView = binding.recyclerview;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        newsletterAdapter = new NewsletterAdapter(NewsletterActivity
                .this, newsletterItemCallback);

        recyclerView.setAdapter(newsletterAdapter);

        BoxStore boxStore = Bamburi.getInstance().getBoxStore();

        Box<NewsLetterCategory> newsLetterCategoryBox = boxStore.boxFor(NewsLetterCategory.class);
        newsletterBox = boxStore.boxFor(Newsletter.class);

        Query<NewsLetterCategory> newsLetterCategoryQuery = newsLetterCategoryBox.query().build();
        newsletterQuery = newsletterBox.query().order(Newsletter_.order_index).isNull(Newsletter_.deleted_at).build();

        newsLetterCategories = newsLetterCategoryQuery.find();


        //update the UI
        newsletterQuery.subscribe().on(AndroidScheduler.mainThread()).observer(newsletters -> {
            if (newsletters.size() > 0 && binding.tvNoData != null) {
                binding.tvNoData.setVisibility(View.GONE);
                binding.recyclerview.setVisibility(View.VISIBLE);
            }
            newsletterAdapter.submitList(newsletters);
        });

        newsLetterCategoryQuery.subscribe().on(AndroidScheduler.mainThread()).observer(categories -> {
            newsLetterCategories.clear();
            newsLetterCategories.addAll(categories);
            invalidateOptionsMenu();

        });

        Settings settings = new Settings(getApplicationContext());
        long user_id = settings.getUserLoggedinId();
        if (!settings.isNewslettersFetched()) {
            Repository.fetchNewsletterCategories(user_id);
            Repository.fetchNewsletters(user_id);
            settings.setNewslettersFetched(true);
        }

        if (newsletterQuery.find().size() == 0) {
            binding.tvNoData.setVisibility(View.VISIBLE);
            binding.recyclerview.setVisibility(View.GONE);
            binding.tvNoData.setText(getString(R.string.no_data, "newsletters"));
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (newsLetterCategories.size() > 0) {
            menu.clear();
            menu.add(0, 0, Menu.NONE, "All Categories").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            for (NewsLetterCategory newsletterCategory : newsLetterCategories) {
                menu.add(0, (int) newsletterCategory.getId(), Menu.NONE, newsletterCategory.getName()).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        for (NewsLetterCategory newsletterCategory : newsLetterCategories) {
            if (item.getItemId() == 0) {
                newsletterAdapter.submitList(newsletterQuery.find());
            } else if (newsletterCategory.getId() == item.getItemId()) {
                List<Newsletter> newsletterList = newsletterBox.query().equal(Newsletter_.category, newsletterCategory.getId()).build().find();
                newsletterAdapter.submitList(newsletterList);
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //track screen
        trackScreen("Newsletters");
    }

    @Override
    public void onPause() {
        super.onPause();
        settings.setNewslettersFetched(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        settings.setNewslettersFetched(false);

    }
}
