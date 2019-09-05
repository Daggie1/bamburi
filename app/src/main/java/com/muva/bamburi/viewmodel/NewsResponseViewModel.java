package com.muva.bamburi.viewmodel;

import android.content.Context;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.models.Comment;
import com.muva.bamburi.models.User;
import com.muva.bamburi.models.User_;
import com.muva.bamburi.utils.Settings;


/**
 * Created by Njoro on 5/7/18.
 */
public class NewsResponseViewModel extends BaseObservable {

    private Context context;
    private Comment comment;

    public NewsResponseViewModel(Context context, Comment comment) {
        this.context = context;
        this.comment = comment;
    }

    @Bindable
    public String getName() {
        if (comment.getFrom() == 1) {
            User user = Bamburi.getInstance().getBoxStore().boxFor(User.class).query().equal(User_.id, new Settings(context).getUserLoggedinId()).build().findFirst();

            return user != null ? user.getName() : "ME";
        } else {
            return "Admin";
        }
    }

    @Bindable
    public String getFirstLetter() {
        if (comment.getFrom() == 1) {
            User user = Bamburi.getInstance().getBoxStore().boxFor(User.class).query().equal(User_.id, new Settings(context).getUserLoggedinId()).build().findFirst();
            return user != null ? " " + user.getName().substring(0, 1) + " " : " M ";
        } else {
            return " A ";
        }
    }

    @Bindable
    public String getResponse() {
        return comment.getResponse();
    }

    @Bindable
    public String getTimeCreated() {
        return comment.getTime_created();
    }
}

