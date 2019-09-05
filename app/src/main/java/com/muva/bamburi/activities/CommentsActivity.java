package com.muva.bamburi.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.Repository;
import com.muva.bamburi.adapters.NewsResponseAdapter;
import com.muva.bamburi.models.Comment;
import com.muva.bamburi.models.Comment_;
import com.muva.bamburi.models.News;
import com.muva.bamburi.models.News_;
import com.muva.bamburi.utils.Settings;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;

import static com.muva.bamburi.utils.Constants.NEWS_ID;

public class CommentsActivity extends AppCompatActivity {

    private long news_id;
    private Box<Comment> commentBox;
    private Query<Comment> commentQuery;
    private Box<News> newsBox;
    private Query<News> newsQuery;
    private News news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            news_id = bundle.getLong(NEWS_ID);
        }

        BoxStore boxStore = Bamburi.getInstance().getBoxStore();
        newsBox = boxStore.boxFor(News.class);
        commentBox = boxStore.boxFor(Comment.class);

        newsQuery = newsBox.query().equal(News_.id, news_id).build();
        news = newsQuery.findFirst();

        if (news != null) {
            setTitle(news.getTitle());
        }


        RecyclerView recyclerView = findViewById(R.id.comments_recyclerview);
        EditText etComment = findViewById(R.id.comment_text);
        ImageButton postComment = findViewById(R.id.sendButton);


        commentQuery = commentBox.query().equal(Comment_.news_id, news_id).order(Comment_.created_at).build();
        List<Comment> comments = commentQuery.find();


        recyclerView.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));
        NewsResponseAdapter adapter = new NewsResponseAdapter(CommentsActivity.this, comments);
        recyclerView.setAdapter(adapter);


        postComment.setOnClickListener(view -> {
            //update a comment
            String comment = etComment.getText().toString().trim();
            if (TextUtils.isEmpty(comment)) {
                etComment.setError("You cannot post an empty comment");
            } else {

                Repository.replyNews(CommentsActivity.this, new Settings(CommentsActivity.this).getUserLoggedinId(), news_id, comment, null);
            }

        });


        commentQuery.subscribe().on(AndroidScheduler.mainThread()).observer(data -> {
            if (data.size() > 0) {
                //clear the edittext
                etComment.setText("");
                etComment.clearFocus();

                //hide the keyboard
                View focusedView = getCurrentFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (focusedView != null && inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                }

                adapter.updateComments(data);
                //go to the latest comment
                recyclerView.scrollToPosition(data.size()-1);
            }
        });

    }
}
