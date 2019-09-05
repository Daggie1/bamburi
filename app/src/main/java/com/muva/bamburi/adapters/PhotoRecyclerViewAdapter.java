package com.muva.bamburi.adapters;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.muva.bamburi.R;
import com.muva.bamburi.activities.PhotoFullActivity;
import com.muva.bamburi.models.Photo;
import com.muva.bamburi.network.Urls;
import com.muva.bamburi.utils.GlideApp;

import static com.muva.bamburi.utils.Constants.ARG_PHOTO_CORNER_ID_URL;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.muva.bamburi.models.Photo} and makes a call to the
 * specified .
 */
public class PhotoRecyclerViewAdapter extends ListAdapter<Photo, PhotoRecyclerViewAdapter.ViewHolder> {
    private Context context;

    public PhotoRecyclerViewAdapter(Context context, @NonNull DiffUtil.ItemCallback<Photo> diffCallback) {
        super(diffCallback);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Photo photo = getItem(position);
        GlideApp.with(context)
                .load(Urls.PHOTOS_URL+photo.getPhoto_server_url())
                .fitCenter()
                .into(holder.imageView);

//        holder.tv_time.setText(photo.getCreated_at());

        holder.imageView.setOnClickListener(v -> {
            goToPhotoFullActivity(photo.getId());
//            PhotoFullFragment photoFullFragment = PhotoFullFragment.newInstance(photo.getId());
//            FragmentHelper.openFragment(context,R.id.fragment_container,photoFullFragment);
        });
    }

    private void goToPhotoFullActivity(long photo_id) {
        Intent intent = new Intent(context, PhotoFullActivity.class);
        intent.putExtra(ARG_PHOTO_CORNER_ID_URL,photo_id);
        context.startActivity(intent);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView imageView;
        public final TextView tv_time;
        public Photo mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            imageView = view.findViewById(R.id.photo_image);
            tv_time = view.findViewById(R.id.tv_time);
        }

    }
}
