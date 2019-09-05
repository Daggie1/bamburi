package com.muva.bamburi.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.adapters.PhotoCornerRecyclerViewAdapter;
import com.muva.bamburi.interfaces.PhotoCornerClickListener;
import com.muva.bamburi.models.Photo;
import com.muva.bamburi.models.PhotoCorner;
import com.muva.bamburi.models.PhotoCorner_;
import com.muva.bamburi.network.RetrofitService;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.muva.bamburi.utils.Constants.ARG_COLUMN_COUNT;
import static com.muva.bamburi.utils.Constants.ARG_PHOTO_CORNER_ID_URL;
import static com.muva.bamburi.utils.UniversalMethods.trackScreen;

public class PhotosCornerActivity extends AppCompatActivity implements PhotoCornerClickListener {

    //    private PhotoCornerFragment.OnListFragmentInteractionListener mListener;
    private List<PhotoCorner> photoCorners;
    private Settings settings;
    private Box<PhotoCorner> photoCornerBox;
    private Box<Photo> photoBox;
    private Query<PhotoCorner> photoCornerQuery;
    private PhotoCornerRecyclerViewAdapter photoCornerRecyclerViewAdapter;
    private FrameLayout frameLayout;
    private FragmentManager fragmentManager = null;
    private RecyclerView recyclerView;

    private DiffUtil.ItemCallback<PhotoCorner> photoCornerItemCallback = new DiffUtil.ItemCallback<PhotoCorner>() {
        @Override
        public boolean areItemsTheSame(PhotoCorner oldItem, PhotoCorner newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(PhotoCorner oldItem, PhotoCorner newItem) {
            try {
                //compare the dates
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date oldDate = format.parse(oldItem.getCreated_at());
                Date newDate = format.parse(newItem.getCreated_at());

                return newDate.compareTo(oldDate) == 0 && oldItem.getTotal_photos() == newItem.getTotal_photos() && newItem.getOrder_index() == oldItem.getOrder_index();
            } catch (Exception exception) {

                return false;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoscorner);

//        getActionBar().setDisplayHomeAsUpEnabled(true);

        frameLayout = findViewById(R.id.fragment_container);
        frameLayout.setVisibility(View.GONE);

        TextView tvNoData = findViewById(R.id.tv_no_data);

        recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        photoCornerRecyclerViewAdapter = new PhotoCornerRecyclerViewAdapter(PhotosCornerActivity.this, photoCornerItemCallback, this);

        recyclerView.setAdapter(photoCornerRecyclerViewAdapter);

        settings = new Settings(this);

        BoxStore boxStore = Bamburi.getInstance().getBoxStore();
        photoCornerBox = boxStore.boxFor(PhotoCorner.class);
        photoBox = boxStore.boxFor(Photo.class);

        photoCornerQuery = photoCornerBox.query()
                .greater(PhotoCorner_.total_photos, 0)
                .order(PhotoCorner_.order_index).build();
        photoCorners = photoCornerQuery.find();

        photoCornerQuery.subscribe().on(AndroidScheduler.mainThread()).observer(photos -> {
            if (photos.size() > 0 && tvNoData != null && recyclerView != null) {
                tvNoData.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            photoCornerRecyclerViewAdapter.submitList(photos);
        });

        if (!settings.isPhotosFetched()) {
            fetchPhotoCorners();
            settings.setPhotosFetched(true);
        }

        fragmentManager = getSupportFragmentManager();

        if (photoCornerQuery.find().size() == 0) {
            tvNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvNoData.setText(getString(R.string.no_data, "photos"));
        }

    }


    private void fetchPhotoCorners() {
        Disposable disposable = RetrofitService.getInstance()
                .getPhotoCorners(settings.getUserLoggedinId())
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    for (PhotoCorner photocorner : data.getData()) {
                        photoBox.put(photocorner.getPhotos());
                    }
                    photoCornerBox.put(data.getData());
                    L.d("fetch photocorners msg :" + data.getMessage());
                }, throwable -> {
                    L.e("fetch photo corners failed: " + throwable.getMessage());
                });
    }


    @Override
    public void onPhotoCornerClick(Context context, PhotoCorner item) {
        new MaterialDialog.Builder(context).content("Open this Photo album?").positiveText(R.string.ok).negativeText(R.string.cancel).onPositive((dialog, which) -> {

            goToPhotoListActivity(2, item.getId());
            dialog.dismiss();
        }).onNegative((dialog, which) -> dialog.dismiss()).show();
    }


    private void goToPhotoListActivity(int columnsCount, long photo_corner_id) {
        Intent intent = new Intent(PhotosCornerActivity.this, PhotoListActivity.class);
        intent.putExtra(ARG_COLUMN_COUNT, columnsCount);
        intent.putExtra(ARG_PHOTO_CORNER_ID_URL, photo_corner_id);

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //track screen
        trackScreen("Photos Corner");
    }

    @Override
    public void onPause() {
        super.onPause();
        settings.setPhotosFetched(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        settings.setPhotosFetched(false);

    }
}
