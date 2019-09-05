package com.muva.bamburi.adapters;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.muva.bamburi.R;
import com.muva.bamburi.databinding.CommentBinding;
import com.muva.bamburi.databinding.SingleNewsResponseBinding;
import com.muva.bamburi.models.Comment;
import com.muva.bamburi.viewmodel.NewsResponseViewModel;

import java.util.List;

/**
 * Created by Njoro on 5/7/18.
 */
public class NewsResponseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Comment> comments;
    private Context context;
    private static final int FROM_USER = 1;
    private static final int FROM_ADMIN = 2;

    public NewsResponseAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case FROM_USER:
                CommentBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.comment, parent, false);
                viewHolder = new CommentViewHolder(binding);
                break;
            case FROM_ADMIN:
                SingleNewsResponseBinding responseBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.single_news_response, parent, false);
                viewHolder = new ResponseViewHolder(responseBinding);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        switch (holder.getItemViewType()) {
            case FROM_USER:
                CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
                commentViewHolder.binding.setViewModel(new NewsResponseViewModel(context, comment));
                break;

            case FROM_ADMIN:
                ResponseViewHolder responseViewHolder = (ResponseViewHolder) holder;

                responseViewHolder.binding.setViewModel(new NewsResponseViewModel(context, comment));
                break;

        }

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    @Override
    public int getItemViewType(int position) {
        return comments.get(position).getFrom();
    }

    public void updateComments(List<Comment> commentList) {
        comments.clear();
        comments.addAll(commentList);
        notifyItemRangeChanged(0, commentList.size() - 1);
    }

    public class ResponseViewHolder extends RecyclerView.ViewHolder {
        SingleNewsResponseBinding binding;

        public ResponseViewHolder(SingleNewsResponseBinding itemView) {
            super(itemView.responseItem);
            this.binding = itemView;
        }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        CommentBinding binding;

        public CommentViewHolder(CommentBinding itemView) {
            super(itemView.commentItem);
            this.binding = itemView;
        }
    }
}
