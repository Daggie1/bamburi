package com.muva.bamburi.viewmodel;

import android.content.Context;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.RadioGroup;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.Repository;
import com.muva.bamburi.adapters.PollsAdapter;
import com.muva.bamburi.models.Poll;
import com.muva.bamburi.models.User;
import com.muva.bamburi.models.User_;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;

import io.objectbox.Box;
import io.objectbox.BoxStore;

/**
 * Created by Njoro on 5/9/18.
 */
public class PollViewModel extends BaseObservable {
    private Context context;
    private Poll poll;
    private BoxStore boxStore;
    private Box<User> userBox;
    private Box<Poll> pollBox;
    private long user_id;
    private User user;
    private final PollsAdapter.ActionCallback actionCallback;

    public PollViewModel(Context context, Poll poll, PollsAdapter.ActionCallback actionCallback) {
        this.context = context;
        this.poll = poll;
        this.boxStore = Bamburi.getInstance().getBoxStore();
        this.userBox = boxStore.boxFor(User.class);
        this.pollBox = boxStore.boxFor(Poll.class);
        this.user_id = new Settings(context).getUserLoggedinId();

        this.user = userBox.query().equal(User_.id, user_id).build().findFirst();
        this.actionCallback = actionCallback;
    }

    @Bindable
    public String getBody() {
        return poll.getBody();
    }

    public void dismissOrClosePoll() {
        //for dismissed poll, pass -1
        Repository.replyPoll(context, poll.getId(), user_id, -1);
        actionCallback.onPollDismissed();
    }


    public void replyPoll(RadioGroup optionsRadioGroup) {

        int poll_option_id = optionsRadioGroup.getCheckedRadioButtonId();
        if (poll_option_id == -1) {
            //no radio buttons are checked
            L.t(context, "You must select one option to vote");
        } else {
            Repository.replyPoll(context, poll.getId(), user_id, poll_option_id);
            actionCallback.onPollDismissed();
        }

    }

}
