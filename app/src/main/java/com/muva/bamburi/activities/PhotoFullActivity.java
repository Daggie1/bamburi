package com.muva.bamburi.activities;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.databinding.ActivityPhotoFullBinding;
import com.muva.bamburi.models.Photo;
import com.muva.bamburi.network.Urls;
import com.muva.bamburi.utils.GlideApp;
import com.muva.bamburi.utils.L;

import static com.muva.bamburi.utils.Constants.ARG_PHOTO_CORNER_ID_URL;
import static com.muva.bamburi.utils.Constants.ARG_PHOTO_URL;

public class PhotoFullActivity extends AppCompatActivity {

    private long photo_id;
    private Photo photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPhotoFullBinding binding = DataBindingUtil.setContentView(PhotoFullActivity.this, R.layout.activity_photo_full);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(ARG_PHOTO_CORNER_ID_URL)) {
                photo_id = bundle.getLong(ARG_PHOTO_CORNER_ID_URL);

                photo = Bamburi.getInstance().getBoxStore().boxFor(Photo.class).get(photo_id);

                ImageView imageView = binding.photo;
                GlideApp.with(PhotoFullActivity.this)
                        .load(Urls.PHOTOS_URL + photo.getPhoto_server_url())
                        .into(imageView);
            } else if (bundle.containsKey(ARG_PHOTO_URL)) {
                String url = bundle.getString(ARG_PHOTO_URL);
                GlideApp.with(PhotoFullActivity.this)
                        .load(url)
                        .into(binding.photo);
            } else {
                finish();
                L.t(this, "No image url provided");
            }
        }
    }

}
