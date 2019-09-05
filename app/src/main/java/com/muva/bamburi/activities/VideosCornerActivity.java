package com.muva.bamburi.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.adapters.VideoRecyclerViewAdapter;
import com.muva.bamburi.models.Video;
import com.muva.bamburi.models.Video_;
import com.muva.bamburi.network.RetrofitService;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.objectbox.Box;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.muva.bamburi.utils.UniversalMethods.trackScreen;

public class VideosCornerActivity extends AppCompatActivity {

    private Box<Video> videoBox;
    private Settings settings;
    private VideoRecyclerViewAdapter videoRecyclerViewAdapter;

    private DiffUtil.ItemCallback<Video> videoItemCallback = new DiffUtil.ItemCallback<Video>() {
        @Override
        public boolean areItemsTheSame(Video oldItem, Video newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Video oldItem, Video newItem) {
            try {
                //compare the dates
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date oldDate = format.parse(oldItem.getCreated_at());
                Date newDate = format.parse(newItem.getCreated_at());

                return newDate.compareTo(oldDate) == 0 && oldItem.isDownloaded() == newItem.isDownloaded() && oldItem.getDeleted_at().equalsIgnoreCase(newItem.getDeleted_at()) && oldItem.getOrder_index() == newItem.getOrder_index();
            } catch (Exception exception) {

                return false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoscorner);

        TextView tvNoData = findViewById(R.id.tv_no_data);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        videoRecyclerViewAdapter = new VideoRecyclerViewAdapter(this, videoItemCallback);
        recyclerView.setAdapter(videoRecyclerViewAdapter);


        videoBox = Bamburi.getInstance().getBoxStore().boxFor(Video.class);
        Query<Video> videoQuery = videoBox.query()
                .isNull(Video_.deleted_at)
                .order(Video_.order_index).build();

        videoQuery.subscribe()
                .on(AndroidScheduler.mainThread())
                .observer(videos-> {
                    if (videos.size() > 0 && tvNoData != null) {
                        tvNoData.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    videoRecyclerViewAdapter.submitList(videos);
                });

        settings = new Settings(getApplicationContext());


        if (!settings.isVideosFetched()) {
            fetchVideos();
            settings.setVideosFetched(true);
        }

        if (videoQuery.find().size() == 0) {
            tvNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvNoData.setText(getString(R.string.no_data,"videos"));
        }

    }

    private void fetchVideos() {
        Disposable disposable = RetrofitService.getInstance()
                .getVideos(settings.getUserLoggedinId())
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        data->videoBox.put(data.getData())
                        ,
                        throwable -> L.e("error fetching videos: "+throwable.getMessage())
                );
    }


    @Override
    protected void onResume() {
        super.onResume();
        //track screen
        trackScreen("Videos Corner");
    }

    @Override
    protected void onPause() {
        super.onPause();
        settings.setVideosFetched(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        settings.setVideosFetched(false);
    }
}
