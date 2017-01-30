package com.pr0gramm.statistics.networking;

import com.google.gson.Gson;
import com.pr0gramm.statistics.Main;
import com.pr0gramm.statistics.api.User;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;

/**
 * Created by koray on 28/01/2017.
 */
public class UserDownloader {

    private boolean started = false;
    private boolean finished = false;

    private OkHttpClient client = new OkHttpClient();

    private ArrayList<User> users = new ArrayList<User>();
    private LinkedList<String> userNames = new LinkedList<String>();

    private Gson gson = new Gson();

    private UserDownloaderCallback callback;
    private int limit;

    private int enqueuedRequests = 0;
    private int finishedRequests = 0;

    public UserDownloader(ArrayList<String> userNames) {
        this(userNames, null, -1);
    }

    public UserDownloader(ArrayList<String> userNames, UserDownloaderCallback callback) {
        this(userNames, callback, -1);
    }

    public UserDownloader(ArrayList<String> userNames, UserDownloaderCallback callback, int limit) {
        this.userNames = new LinkedList<String>(userNames);
        this.callback = callback;
        this.limit = limit;

        this.enqueuedRequests = this.userNames.size();
    }

    public void startDownloading() throws UnsupportedOperationException {
        if (started || finished) {
            throw new UnsupportedOperationException("You can't start the downloader twice!");
        }
        started = true;

        Request.Builder requestBuilder = new Request.Builder();

        downloadNextUser();
    }

    private void downloadNextUser() {
        Request.Builder requestBuilder = new Request.Builder();

        String name = userNames.poll();

        if (name == null) {
            return;
        }

        /*
        if (name == null) {
            finishDownloader();
            return;
        }
        */

        final String url = "https://pr0gramm.com/api/profile/info?name=" + name;
        requestBuilder.url(url);

        requestBuilder.get();

        Request request = requestBuilder.build();

        newCall(request);

        new java.util.Timer().schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    downloadNextUser();
                }
            },
            20
        );
    }

    private void newCall(final Request request) {
        // enqueuedRequests++;

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Main.getLogger().log(Level.WARNING, "******* FAILURE *******");
                e.printStackTrace();

                // TODO: There should be a limit...
                newCall(request);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() != 200) {
                    Main.getLogger().log(Level.WARNING, "******* NON 200 STATUS CODE *******");
                    Main.getLogger().log(Level.WARNING, request.url().toString());
                    Main.getLogger().log(Level.WARNING, response.body().string());

                    addFinishedRequest();
                    finishIfFinished();
                } else {
                    addFinishedRequest();

                    parseResponse(response.body().string());
                    finishIfFinished();
                }
            }
        });
    }

    private synchronized void addFinishedRequest() {
        finishedRequests++;
    }

    private synchronized void finishIfFinished() {
        if (finishedRequests >= enqueuedRequests) {
            finishDownloader();
        }
    }

    private synchronized void parseResponse(String response) {
        User user = gson.fromJson(response, User.class);

        if (user != null && user.getUser() != null) {
            users.add(user);
            Main.getLogger().log(Level.INFO, "NEXT USER DOWNLOADED: " + user.getUser().getName());
        }
    }

    private synchronized void finishDownloader() {
        finished = true;
        if (callback != null) {
            callback.finishedLoading(users);
        }
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isFinished() {
        return finished;
    }
}
