package com.muva.bamburi;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import com.google.gson.Gson;
import com.muva.bamburi.activities.MainActivity;
import com.muva.bamburi.models.Comment;
import com.muva.bamburi.models.News;
import com.muva.bamburi.models.NewsLetterCategory;
import com.muva.bamburi.models.Newsletter;
import com.muva.bamburi.models.Option;
import com.muva.bamburi.models.Poll;
import com.muva.bamburi.models.User;
import com.muva.bamburi.network.ApiEndpoints;
import com.muva.bamburi.network.RetrofitService;
import com.muva.bamburi.network.responses.Response;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;
import com.muva.bamburi.utils.UniversalMethods;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Njoro on 5/4/18.
 */
public class Repository {
    public static ApiEndpoints getRetrofitService() {
        return RetrofitService.getInstance();
    }

    private static BoxStore getBoxStore() {
        return Bamburi.getInstance().getBoxStore();
    }

    private static Box<User> getUserBox() {
        return getBoxStore().boxFor(User.class);
    }

    private static Box<News> getNewsBox() {
        return getBoxStore().boxFor(News.class);
    }

    private static Box<Comment> getCommentBox() {
        return getBoxStore().boxFor(Comment.class);
    }

    private static Box<Poll> getPollsBox() {
        return getBoxStore().boxFor(Poll.class);
    }

    private static Box<NewsLetterCategory> getNewsletterCategoryBox() {
        return getBoxStore().boxFor(NewsLetterCategory.class);
    }

    private static Box<Newsletter> getNewsletterBox() {
        return getBoxStore().boxFor(Newsletter.class);
    }

    private static Box<Option> getOptionBox() {
        return getBoxStore().boxFor(Option.class);
    }


    public static void fetchNews(long user_id) {
        Disposable disposable = getRetrofitService()
                .getNews(user_id)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::getData)
                .subscribe(
                        news -> {
//                            L.d("Incoming news :: "+(new Gson()).toJson(news));
                            for (News newsItem : news) {
                                List<Comment> commentList = newsItem.responses;

                                if (commentList != null && commentList.size() > 0) {
                                    L.e("number of responses: " + commentList.size());
                                    getCommentBox().put(commentList);
                                    getNewsBox().put(newsItem);
                                } else {
                                    getNewsBox().put(newsItem);
                                }
                            }


                        },
                        throwable -> L.e("error fetching news: " + throwable.getMessage())
                );
    }


    public static void fetchPolls(long user_id) {
        Disposable disposable = getRetrofitService()
                .getPolls(user_id)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::getData)
                .subscribe(
                        polls -> {
                            for (Poll poll : polls) {
                                List<Option> options = poll.getOptions();
                                if (options.size() > 0) {
                                    L.e("number of options: " + options.size());

                                    getPollsBox().put(poll);
                                    getOptionBox().put(options);
                                    getOptionBox().put(options);
                                } else {
                                    getPollsBox().put(poll);
                                }
                            }

                        },
                        throwable -> L.e("error fetching news: " + throwable.getMessage())
                );
    }

    public static void fetchNewsletterCategories(long user_id) {
        Disposable disposable = getRetrofitService()
                .getNewsletterCategories(user_id)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::getData)
                .subscribe(
                        data -> {
                            getNewsletterCategoryBox().put(data);

                        },
                        throwable -> {
                            L.e("error fetching newsletter categories: " + throwable.getMessage());
                        }
                );
    }

    public static void fetchNewsletters(long user_id) {
        Disposable disposable = getRetrofitService()
                .getNewsletters(user_id)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::getData)
                .subscribe(
                        data -> {
                            getNewsletterBox().put(data);

                        },
                        throwable -> {
                            L.e("error fetching newsletters: " + throwable.getMessage());
                        }
                );
    }

    public static void replyPoll(Context context, long poll_id, long user_id, long poll_option_id) {
        Disposable disposable = Repository
                .getRetrofitService()
                .pollReply(poll_id, user_id, poll_option_id)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::getData)
                .subscribe(data -> {
                            //remove that id
                            getPollsBox().remove(poll_id);

//                                HomeFragment homeFragment = HomeFragment.getInstance(NEWS);
//                                FragmentHelper.openFragment(context, R.id.fragment_container, homeFragment);
                            context.startActivity(new Intent(((MainActivity) context), MainActivity.class));

                            L.t(context, "Poll response submitted");
                        },
                        throwable -> {
                            UniversalMethods.handleErrors(throwable, context);
                            L.e("error replying to poll: " + throwable.getMessage());
                        }
                );
    }


    public static Disposable updateFCMToken(Context context, String user_id, String fcm_token) {
        Disposable disposable = getRetrofitService()
                .updateFCMToken(
                        user_id, fcm_token)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            L.d("status: " + response.isSuccess() + " message: " + response.getMessage());
                            if (response.isSuccess()) {
                                new Settings(context).setFcmTokenUpdated(true);
                                getUserBox().put(response.getDatum());
                            }
                        },
                        throwable -> {
                            L.e("error updating fcm token" + throwable.getMessage());
                        }
                );

        return disposable;
    }

    public static void replyNews(Context context, long user_id, long news_id, String comment, View sendButton) {
        Disposable disposable = Repository.getRetrofitService()
                .postNewsResponse(news_id, user_id, comment)
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .map(Response::getDatum)
                .subscribe(data -> {
                    List<Comment> commentList = data.responses;
                    if (commentList != null && commentList.size() > 0) {
                        L.t(context, "Comment Posted Successfully!");
                        getCommentBox().put(commentList);
                        getNewsBox().put(data);

                    } else {
                        getNewsBox().put(data);
                    }

                    if (sendButton != null)
                        sendButton.setVisibility(View.VISIBLE);
                }, throwable -> {
                    L.e("error posting news response: " + throwable.getMessage());
                    UniversalMethods.handleErrors(throwable, context);

                    if (sendButton != null)
                        sendButton.setVisibility(View.VISIBLE);
                });
    }

}
