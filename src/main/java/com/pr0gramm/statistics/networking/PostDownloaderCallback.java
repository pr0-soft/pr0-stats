package com.pr0gramm.statistics.networking;

import com.pr0gramm.statistics.api.Item;

import java.util.ArrayList;

/**
 * Created by koray on 27/01/2017.
 */
public interface PostDownloaderCallback {

    void finishedLoading(ArrayList<Item> items, ArrayList<String> userNames);
}
