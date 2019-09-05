package com.muva.bamburi.firebase; /**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.models.News;
import com.muva.bamburi.models.Newsletter;
import com.muva.bamburi.models.Option;
import com.muva.bamburi.models.Photo;
import com.muva.bamburi.models.Poll;
import com.muva.bamburi.models.Video;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;
import com.muva.bamburi.utils.UniversalMethods;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
//            L.d("Message data payload: " + remoteMessage.getData());
            handleNow(remoteMessage);
        }

    }


    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow(RemoteMessage remoteMessage) {


        if (!remoteMessage.getData().isEmpty() && remoteMessage.getData().containsKey("message") && remoteMessage.getData().containsKey("key") && remoteMessage.getData().containsKey("datum")) {

            try {
                BoxStore boxStore = Bamburi.getInstance().getBoxStore();
                Gson gson = new Gson();
                switch (remoteMessage.getData().get("key")) {
                    case "nw":
                    case "ue":
                    case "co":
                        /*
                        * allow for fetching of items when the related activity opens next,
                        * to cater for the reordering of the items via the order_index
                        *
                        */
                        new Settings(Bamburi.getInstance()).setNewsFetched(false);

                        Box<News> newsBox = boxStore.boxFor(News.class);

                        News news = gson.fromJson(remoteMessage.getData().get("datum"), News.class);
                        newsBox.put(news);

                        break;
                    case "nl":
                        /*
                         * allow for fetching of items when the related activity opens next,
                         * to cater for the reordering of the items via the order_index
                         *
                         */
                        new Settings(Bamburi.getInstance()).setNewslettersFetched(false);

                        Box<Newsletter> newsletterBox = boxStore.boxFor(Newsletter.class);

                        Newsletter newsletter = gson.fromJson(remoteMessage.getData().get("datum"), Newsletter.class);
                        newsletterBox.put(newsletter);
                        break;
                    case "vc":
                        /*
                         * allow for fetching of items when the related activity opens next,
                         * to cater for the reordering of the items via the order_index
                         *
                         */
                        new Settings(Bamburi.getInstance()).setVideosFetched(false);


                        Box<Video> videoBox = boxStore.boxFor(Video.class);

                        Video video = gson.fromJson(remoteMessage.getData().get("datum"), Video.class);
                        videoBox.put(video);
                        break;
                    case "pc":
                        /*
                         * allow for fetching of items when the related activity opens next,
                         * to cater for the reordering of the items via the order_index
                         *
                         */
                        new Settings(Bamburi.getInstance()).setPhotosFetched(false);

                        Box<Photo> photoBox = boxStore.boxFor(Photo.class);

                        Photo photo = gson.fromJson(remoteMessage.getData().get("datum"), Photo.class);
                        photoBox.put(photo);
                        break;
                    case "pl":
                        Box<Poll> pollBox = boxStore.boxFor(Poll.class);

                        Poll poll = gson.fromJson(remoteMessage.getData().get("datum"), Poll.class);

                        L.e("poll options: "+poll.getOptions().toString());
                        Box<Option> optionBox = boxStore.boxFor(Option.class);

                        pollBox.put(poll);
                        optionBox.put(poll.getOptions());

                        break;
                }


            } catch (Exception exception) {
                Crashlytics.logException(exception);
                L.e("error processing notification: "+exception.getMessage());
            }
            finally {
                UniversalMethods.sendNotification(getApplicationContext(), remoteMessage.getData().get("message"), remoteMessage.getData().get("key"));
            }
        }
    }


}