package com.muva.bamburi.adapters;

import android.content.Context;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.muva.bamburi.R;
import com.muva.bamburi.activities.PhotoFullActivity;
import com.muva.bamburi.databinding.NewsSnippetBinding;
import com.muva.bamburi.models.News;
import com.muva.bamburi.network.Urls;
import com.muva.bamburi.viewmodel.NewsViewModel;

import static com.muva.bamburi.utils.Constants.ARG_PHOTO_URL;

/**
 * {@link RecyclerView.Adapter} that can display a {@link } and makes a call to the
 * specified {@link }.
 */
public class HomeAdapter extends ListAdapter<News, HomeAdapter.NewsSnippetViewHolder> {

    private Context context;

    public HomeAdapter(Context context, @NonNull DiffUtil.ItemCallback<News> diffCallback) {
        super(diffCallback);
        this.context = context;
    }

    @NonNull
    @Override
    public NewsSnippetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        NewsSnippetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.news_snippet, parent, false);
        return new NewsSnippetViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull final NewsSnippetViewHolder holder, int position) {

        News news = getItem(position);

        if (news != null) {
            NewsViewModel newsViewModel = new NewsViewModel(context, news, this, position);

            holder.newsSnippetBinding.setViewModel(newsViewModel);
            //make links clickable
            Linkify.addLinks(holder.newsSnippetBinding.tvMessage, Linkify.ALL);


            holder.newsSnippetBinding.newsMedia.setOnClickListener(v -> {
                Intent intent = new Intent(context, PhotoFullActivity.class);
                intent.putExtra(ARG_PHOTO_URL, Urls.NEWS_URL + newsViewModel.getMediaUrl());
                context.startActivity(intent);
            });
        }
    }


    public class NewsSnippetViewHolder extends RecyclerView.ViewHolder {
        NewsSnippetBinding newsSnippetBinding;

        public NewsSnippetViewHolder(NewsSnippetBinding binding) {
            super(binding.newsSnippet);
            this.newsSnippetBinding = binding;
        }
    }
}
