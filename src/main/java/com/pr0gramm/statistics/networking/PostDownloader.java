package com.pr0gramm.statistics.networking;

import com.google.gson.Gson;
import com.pr0gramm.statistics.api.Item;
import com.pr0gramm.statistics.api.Result;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by koray on 27/01/2017.
 */
public class PostDownloader {

    private boolean started = false;
    private boolean finished = false;

    private OkHttpClient client = new OkHttpClient();

    private ArrayList<Item> items = new ArrayList<Item>();
    private ArrayList<String> userNames = new ArrayList<String>();

    private Gson gson = new Gson();

    private PostDownloaderCallback callback;
    private int limit = -1;
    private int olderThan = -1;

    private int newerThan = -1;

    public PostDownloader() {
        this(null, -1, -1);
    }

    public PostDownloader(PostDownloaderCallback callback) {
        this(callback, -1, -1);
    }

    public PostDownloader(PostDownloaderCallback callback, int limit, int olderThan) {
        this.callback = callback;
        this.limit = limit;
        this.olderThan = olderThan;
    }

    public PostDownloader(PostDownloaderCallback callback, int newerThan) {
        this.callback = callback;
        this.newerThan = newerThan;
    }

    public void startDownloading() throws UnsupportedOperationException {
        if (started || finished) {
            throw new UnsupportedOperationException("You can't start the downloader twice!");
        }
        started = true;

        if (olderThan > 0) {
            downloadNextStack(Integer.toString(olderThan));
            return;
        }
        if (newerThan > 0) {
            downloadNewerStack(Integer.toString(newerThan));
            return;
        }

        Request.Builder requestBuilder = new Request.Builder();

        final String url = "https://pr0gramm.com/api/items/get?flags=7";
        requestBuilder.url(url);

        requestBuilder.get();

        Request request = requestBuilder.build();

        newCall(request);
    }

    private void downloadNextStack(String lastId) {
        Request.Builder requestBuilder = new Request.Builder();

        final String url = "https://pr0gramm.com/api/items/get?flags=7&older=" + lastId;
        requestBuilder.url(url);

        requestBuilder.get();

        Request request = requestBuilder.build();

        newCall(request);
    }

    private void downloadNewerStack(String newerThan) {
        Request.Builder requestBuilder = new Request.Builder();

        final String url = "https://pr0gramm.com/api/items/get?flags=7&newer=" + newerThan;
        requestBuilder.url(url);

        requestBuilder.get();

        Request request = requestBuilder.build();

        newCall(request);
    }

    private void newCall(final Request request) {
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("******* FAILURE *******");
                e.printStackTrace();

                // TODO: There should be a limit...
                newCall(request);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() != 200) {
                    System.out.println("******* NON 200 STATUS CODE *******");
                    System.out.println(request.url().toString());
                    System.out.println(response.body().string());
                } else {
                    parseResponse(response.body().string());
                }
            }
        });
    }

    private synchronized void parseResponse(String response) {
        Result result = gson.fromJson(response, Result.class);

        Item[] newItems = result.getItems();
        if (newItems != null) {
            for (Item i : newItems) {
                if (!items.contains(i)) {
                    items.add(i);
                }

                if (i.getUser() != null && !userNames.contains(i.getUser())) {
                    userNames.add(i.getUser());
                }
            }

            items.addAll(Arrays.asList(newItems));
        }

        if (newItems != null && newItems.length > 0 && (newerThan > 0 ? !result.isAtStart() : !result.isAtEnd())) {
            Item lastItem = newItems[newItems.length - 1];

            int lastId = lastItem.getId();

            if (limit > 0 && items.size() >= limit) {
                finishDownloader();
                return;
            }

            System.out.println("LOADING NEXT STACK! LAST ID WAS: " + lastId);
            System.out.println("CURRENT ITEM SIZE: " + items.size());
            System.out.println("CURRENT USER SIZE: " + userNames.size());

            if (newerThan > 0) {
                // We are downloading into the opposite direction
                downloadNewerStack(Integer.toString(lastId));
            } else {
                downloadNextStack(Integer.toString(lastId));
            }
        } else {
            finishDownloader();
        }
    }

    private synchronized void finishDownloader() {
        finished = true;
        if (callback != null) {
            callback.finishedLoading(items, userNames);
        }
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isFinished() {
        return finished;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
