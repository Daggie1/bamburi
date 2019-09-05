package com.muva.bamburi.activities;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.adapters.PhotoRecyclerViewAdapter;
import com.muva.bamburi.databinding.ActivityPhotoListBinding;
import com.muva.bamburi.models.Photo;
import com.muva.bamburi.models.Photo_;
import com.muva.bamburi.utils.Settings;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.objectbox.Box;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;

import static com.muva.bamburi.utils.Constants.ARG_COLUMN_COUNT;
import static com.muva.bamburi.utils.Constants.ARG_PHOTO_CORNER_ID_URL;


public class PhotoListActivity extends AppCompatActivity {

    private int mColumnCount = 1;
    private long photo_corner_id;

    private DiffUtil.ItemCallback<Photo> photoItemCallback = new DiffUtil.ItemCallback<Photo>() {
        @Override
        public boolean areItemsTheSame(Photo oldItem, Photo newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Photo oldItem, Photo newItem) {
            try {
                //compare the dates
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date oldDate = format.parse(oldItem.getCreated_at());
                Date newDate = format.parse(newItem.getCreated_at());

                return newDate.compareTo(oldDate) == 0;
            } catch (Exception exception) {

                return false;
            }
        }
    };
    private Box<Photo> photoBox;
    private Query<Photo> photoQuery;
    private Settings settings;
    private PhotoRecyclerViewAdapter photoRecyclerViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPhotoListBinding binding = DataBindingUtil.setContentView(PhotoListActivity.this, R.layout.activity_photo_list);

        settings = new Settings(PhotoListActivity.this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mColumnCount = bundle.getInt(ARG_COLUMN_COUNT);
            photo_corner_id = bundle.getLong(ARG_PHOTO_CORNER_ID_URL);

            settings.setPhotoCornerCol(mColumnCount);
            settings.setPhotoCornerId(photo_corner_id);
        }

        photoRecyclerViewAdapter = new PhotoRecyclerViewAdapter(PhotoListActivity.this, photoItemCallback);

        photoBox = Bamburi.getInstance().getBoxStore().boxFor(Photo.class);
        photoQuery = photoBox.query()
                .isNull(Photo_.deleted_at)
                .and()
                .equal(Photo_.photo_corner_id, photo_corner_id)
                .orderDesc(Photo_.id)
                .build();

        photoQuery.subscribe()
                .on(AndroidScheduler.mainThread())
                .observer(photoRecyclerViewAdapter::submitList);



        RecyclerView recyclerView = binding.list;
        if (photoQuery.find().size() == 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(PhotoListActivity.this));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(PhotoListActivity.this,2, GridLayoutManager.VERTICAL,false));
        }
        recyclerView.setAdapter(photoRecyclerViewAdapter);
    }

    @Override
    protected void onResume() {
        if (photo_corner_id == 0) {
            photo_corner_id = settings.getPhotoCornerId();
            mColumnCount = settings.getPhotoCornerCol();

            photoQuery = photoBox.query()
                    .isNull(Photo_.deleted_at)
                    .and()
                    .equal(Photo_.photo_corner_id, photo_corner_id)
                    .orderDesc(Photo_.id)
                    .build();

            photoRecyclerViewAdapter.submitList(photoQuery.find());

            photoQuery.subscribe()
                    .on(AndroidScheduler.mainThread())
                    .observer(photoRecyclerViewAdapter::submitList);

        }

        super.onResume();
    }
}
