package com.muva.bamburi.adapters;

import android.content.Context;
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
import com.muva.bamburi.interfaces.PhotoCornerClickListener;
import com.muva.bamburi.models.PhotoCorner;
import com.muva.bamburi.network.Urls;
import com.muva.bamburi.utils.GlideApp;



/**
 * {@link RecyclerView.Adapter} that can display a {@link com.muva.bamburi.models.PhotoCorner}
 *
 */
public class PhotoCornerRecyclerViewAdapter extends ListAdapter<PhotoCorner,PhotoCornerRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private PhotoCornerClickListener listener;


    public PhotoCornerRecyclerViewAdapter(Context context, DiffUtil.ItemCallback<PhotoCorner> itemCallback, PhotoCornerClickListener cornerClickListener) {
        super(itemCallback);
        this.context = context;
        this.listener = cornerClickListener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_photo_corner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        PhotoCorner photoCorner = getItem(position);
        holder.name.setText(photoCorner.getName());
        holder.date.setText(photoCorner.getCreated_at());
        holder.photos_count.setText(context.getString(R.string.photos_count,photoCorner.getTotal_photos()));
        GlideApp
                .with(holder.mView.getContext())

                .load(Urls.PHOTOS_URL+ photoCorner.getLatest_photo_url())
                .into(holder.imageView);


        holder.mView.setOnClickListener(v -> {
//            new PhotosCornerActivity().onPhotoCornerClicked(context,photoCorner);
            if (null != listener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                listener.onPhotoCornerClick(context,photoCorner);
            }
        });
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView name;
        public final TextView date;
        public final TextView photos_count;
        public final ImageView imageView;
        public PhotoCorner photoCorner;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            name = view.findViewById(R.id.tv_title);
            date = view.findViewById(R.id.tv_time);
            photos_count = view.findViewById(R.id.tv_photos_count);
            imageView = view.findViewById(R.id.photo_image);
        }

    }
}
