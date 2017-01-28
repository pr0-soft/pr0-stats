package com.pr0gramm.statistics.networking;

import com.pr0gramm.statistics.api.Item;
import com.pr0gramm.statistics.api.User;

import java.util.ArrayList;

/**
 * Created by koray on 28/01/2017.
 */
public interface UserDownloaderCallback {

    void finishedLoading(ArrayList<User> users);
}
