package com.muva.bamburi.adapters;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.muva.bamburi.R;
import com.muva.bamburi.databinding.SingleVideoBinding;
import com.muva.bamburi.models.Video;
import com.muva.bamburi.viewmodel.VideoViewModel;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.muva.bamburi.models.Video} and makes a call to the
 * specified {@link }.
 */
public class VideoRecyclerViewAdapter extends ListAdapter<Video, VideoRecyclerViewAdapter.ViewHolder> {

    private Context context;

    public VideoRecyclerViewAdapter(Context context, @NonNull DiffUtil.ItemCallback<Video> itemCallback) {
        super(itemCallback);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SingleVideoBinding view = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.single_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Video video = getItem(position);

        VideoViewModel videoViewModel = new VideoViewModel(context, video,this, position);
        holder.binding.setViewModel(videoViewModel);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public SingleVideoBinding binding;

        public ViewHolder(SingleVideoBinding binding) {
            super(binding.singleVideo);
            this.binding = binding;
        }

    }
}
