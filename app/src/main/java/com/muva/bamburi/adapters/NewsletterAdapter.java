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
import com.muva.bamburi.databinding.SingleNewsLetterItemBinding;
import com.muva.bamburi.models.Newsletter;
import com.muva.bamburi.viewmodel.NewsletterViewModel;

/**
 * Created by Njoro on 5/7/18.
 */
public class NewsletterAdapter  extends ListAdapter<Newsletter, NewsletterAdapter.NewsletterViewHolder>{
    private Context context;

    public NewsletterAdapter(Context context, @NonNull DiffUtil.ItemCallback<Newsletter> diffCallback) {
        super(diffCallback);
        this.context = context;
    }

    @NonNull
    @Override
    public NewsletterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SingleNewsLetterItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.single_news_letter_item, parent, false);

        return new NewsletterViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull NewsletterViewHolder holder, int position) {
        Newsletter newsletter = getItem(position);

        holder.binding.setViewModel(new NewsletterViewModel(context, newsletter, this, position));
    }

    class NewsletterViewHolder extends RecyclerView.ViewHolder{
        SingleNewsLetterItemBinding binding;
        public NewsletterViewHolder(SingleNewsLetterItemBinding itemView) {
            super(itemView.newsletterItem);
            this.binding = itemView;
        }
    }
}
