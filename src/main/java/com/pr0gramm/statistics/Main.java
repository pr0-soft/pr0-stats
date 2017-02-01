package com.pr0gramm.statistics;

import com.google.gson.Gson;
import com.pr0gramm.statistics.api.Item;
import com.pr0gramm.statistics.api.User;
import com.pr0gramm.statistics.api.UserInfo;
import com.pr0gramm.statistics.helper.AbsoluteStatisticsHelper;
import com.pr0gramm.statistics.helper.DecimalHelper;
import com.pr0gramm.statistics.helper.StatisticsHelper;
import com.pr0gramm.statistics.networking.PostDownloader;
import com.pr0gramm.statistics.networking.PostDownloaderCallback;
import com.pr0gramm.statistics.networking.UserDownloader;
import com.pr0gramm.statistics.networking.UserDownloaderCallback;
import com.pr0gramm.statistics.toolbox.SimpleConsoleFormatter;
import com.pr0gramm.statistics.toolbox.SortType;
import com.pr0gramm.statistics.toolbox.StatsType;

import java.io.*;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The main class which handles user input.
 * <p>
 * Created by koray on 27/01/2017.
 */
public class Main {

    private static final String LOGGER_NAME = "com.pr0gramm.statistics";

    private static Logger logger = Logger.getLogger(Main.LOGGER_NAME);
    private static ConsoleHandler handler = new ConsoleHandler();

    private static Gson gson = new Gson();

    // ******* Global ArrayList of Items *******
    private static ArrayList<Item> items = new ArrayList<Item>();
    private static ArrayList<String> userNames = new ArrayList<String>();

    private static ArrayList<User> users = new ArrayList<User>();

    private static boolean executingCommandRightNow = false;

    public static void main(String[] args) {
        setupLogger();

        logger.log(Level.INFO, "******** Starting pr0gramm statistics ********");

        outputHandler();
    }

    private static void setupLogger() {
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleConsoleFormatter());
        try {
            handler.setEncoding("UTF-8");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        logger.addHandler(handler);
    }

    private static void outputHandler() {
        try {
            Console console = System.console();

            Scanner scanner = console != null ? new Scanner(console.reader()) : new Scanner(System.in);
            scanner.useDelimiter(Pattern.compile("[\r\n]+"));

            while (scanner.hasNext()) {
                if (executingCommandRightNow) {
                    // Remove the next element from scanner to prevent infinite loop
                    scanner.next();
                    logger.log(Level.WARNING, "A command is currently being executed. Please try again later.");
                    continue;
                }
                executingCommandRightNow = true;

                String str = scanner.next().trim();

                String[] splitted = str.split("\\s+");

                if (splitted.length < 1) {
                    warnUsage();
                    executingCommandRightNow = false;
                    continue;
                }

                switch (splitted[0].toLowerCase()) {
                case "readitems":
                    if (splitted.length != 2) {
                        warnUsage();
                        executingCommandRightNow = false;
                        continue;
                    }
                    readItems(splitted[1]);

                    executingCommandRightNow = false;
                    break;
                case "readusers":
                    if (splitted.length != 2) {
                        warnUsage();
                        executingCommandRightNow = false;
                        continue;
                    }
                    readUsers(splitted[1]);

                    executingCommandRightNow = false;
                    break;
                case "downloaditems":
                    if (splitted.length != 2) {
                        warnUsage();
                        executingCommandRightNow = false;
                        continue;
                    }
                    if ((new File(splitted[1]).exists())) {
                        logger.log(Level.WARNING, "File " + splitted[1] + "already exists!");
                        logger.log(Level.WARNING, "Please type in a path to a file which does not exist yet.");
                        continue;
                    }
                    downloadItems(splitted[1], new Runnable() {

                        @Override
                        public void run() {
                            executingCommandRightNow = false;
                        }
                    });
                    break;
                case "downloadusers":
                    if (splitted.length != 2) {
                        warnUsage();
                        executingCommandRightNow = false;
                        continue;
                    }
                    if ((new File(splitted[1]).exists())) {
                        logger.log(Level.WARNING, "File " + splitted[1] + "already exists!");
                        logger.log(Level.WARNING, "Please type in a path to a file which does not exist yet.");
                        continue;
                    }
                    if (userNames.size() < 1) {
                        logger.log(Level.WARNING, "There are no cached usernames to download.");
                        logger.log(Level.WARNING, "Please use 'readitems <path>' to read items from the disk "
                            + "or 'downloaditems <outputPath>' to download all items!");
                        continue;
                    }
                    downloadUsers(splitted[1], new Runnable() {

                        @Override
                        public void run() {
                            executingCommandRightNow = false;
                        }
                    });
                    break;
                case "stats":
                    if (splitted.length < 3) {
                        warnUsage();
                        executingCommandRightNow = false;
                        continue;
                    }

                    StatsType type;
                    if (splitted[1].equalsIgnoreCase("users")) {
                        type = StatsType.USERS;
                    } else if (splitted[1].equalsIgnoreCase("items")) {
                        type = StatsType.ITEMS;
                    } else if (splitted[1].equalsIgnoreCase("userinfo")) {
                        type = StatsType.USER_INFO;
                    } else if (splitted[1].equalsIgnoreCase("absolute")) {
                        parseAbsoluteStats(Arrays.copyOfRange(splitted, 2, splitted.length), false);
                        executingCommandRightNow = false;
                        continue;
                    } else if (splitted[1].equalsIgnoreCase("average")) {
                        parseAbsoluteStats(Arrays.copyOfRange(splitted, 2, splitted.length), true);
                        executingCommandRightNow = false;
                        continue;
                    } else {
                        warnUsage();
                        continue;
                    }

                    String[] predicate = Arrays.copyOfRange(splitted, 2, splitted.length);

                    parseStats(type, predicate);

                    executingCommandRightNow = false;
                    break;
                default:
                    warnUsage();
                    executingCommandRightNow = false;
                    continue;
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void downloadItems(final String fileName, final Runnable completion) {
        final long initialTime = System.currentTimeMillis();

        int newerThan = -1;
        if (items.size() > 0) {
            Collections.sort(items, new Comparator<Item>() {

                @Override
                public int compare(Item o1, Item o2) {
                    if (o1 == null && o2 == null) {
                        return 0;
                    } else if (o1 == null) {
                        return 1;
                    } else if (o2 == null) {
                        return -1;
                    }

                    if (o1.getId() > o2.getId()) {
                        return 1;
                    }
                    if (o1.getId() < o2.getId()) {
                        return -1;
                    }

                    return 0;
                }
            });
            newerThan = items.get(items.size() - 1).getId();
        }

        PostDownloader downloader = new PostDownloader(new PostDownloaderCallback() {

            @Override
            public void finishedLoading(ArrayList<Item> items, ArrayList<String> userNames) {
                System.out.println("******* WE ARE REALLY FINISHED!!! *******");
                System.out.println("NEW DOWNLOADED ITEM SIZE: " + items.size());
                System.out.println("TOTAL ITEM SIZE: " + (items.size() + Main.items.size()));
                System.out.println("FINAL USER SIZE: " + userNames.size());

                System.out.println("TIME: " + (System.currentTimeMillis() - initialTime) + " millis");

                System.out.println("WRITING ITEMS TO FILE " + fileName);

                Main.items.addAll(items);
                // FILTER ITEMS BY ID
                HashMap<Long, Item> ids = new HashMap<Long, Item>();
                for (Item i : Main.items) {
                    if (!ids.containsKey((long) i.getId())) {
                        ids.put((long) i.getId(), i);
                    }
                }
                Main.items = new ArrayList<Item>(ids.values());
                // END FILTERING

                // ******* Get unique usernames *******
                HashMap<String, Item> names = new HashMap<String, Item>();
                for (Item i : Main.items) {
                    if (i.getUser() != null && !names.containsKey(i.getUser())) {
                        names.put(i.getUser(), i);
                    }
                }
                for (String s : names.keySet()) {
                    if (!Main.userNames.contains(s)) {
                        Main.userNames.add(s);
                    }
                }
                // ******* End Get unique usernames *******

                // DELETE FILE IF IT EXISTS, BUT IT SHOULD NOT EXIST BECAUSE WE ARE CHECKING THIS BEFORE STARTING
                // DOWNLOAD ITEMS!!!
                File file = new File(fileName);
                if (file.exists()) {
                    file.delete();
                }

                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(new FileOutputStream(fileName));
                    oos.writeObject(Main.items);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Writing to " + fileName + " finished");

                try {
                    if (oos != null) {
                        oos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                completion.run();
            }
        }, newerThan);
        downloader.startDownloading();
    }

    private static void readItems(String path) {
        ObjectInputStream ois = null;
        try {
            logger.log(Level.INFO, "******* Reading items from " + path + " *******");
            ois = new ObjectInputStream(new FileInputStream(path));
            ArrayList<Item> itemList = (ArrayList<Item>) ois.readObject();
            items.addAll(itemList);
            logger.log(Level.INFO, "Finished reading items from " + path);
            logger.log(Level.INFO, "There are now " + items.size() + " items in the list!");

            logger.log(Level.INFO, "******* Parsing users *******");

            // ******* FILTER ITEMS BY ID: e.g.: Just one item per id should be in the ArrayList *******
            HashMap<Long, Item> ids = new HashMap<Long, Item>();
            for (Item i : items) {
                if (!ids.containsKey((long) i.getId())) {
                    ids.put((long) i.getId(), i);
                }
            }
            System.out.println("NEW COUNT: " + ids.size());
            items = new ArrayList<Item>(ids.values());
            System.out.println("FILTERED: " + items.size());

            // ******* Get unique usernames *******
            HashMap<String, Item> names = new HashMap<String, Item>();
            for (Item i : items) {
                if (i.getUser() != null && !names.containsKey(i.getUser())) {
                    names.put(i.getUser(), i);
                }
            }
            userNames = new ArrayList<String>(names.keySet());

            logger.log(Level.INFO, "Finished parsing users");
            logger.log(Level.INFO, "There are now " + userNames.size() + " usernames in the list!");
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
        }

        try {
            if (ois != null) {
                ois.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readUsers(String path) {
        ObjectInputStream ois = null;
        try {
            logger.log(Level.INFO, "******* Reading users from " + path + " *******");
            ois = new ObjectInputStream(new FileInputStream(path));
            ArrayList<User> userList = (ArrayList<User>) ois.readObject();
            users.addAll(userList);
            logger.log(Level.INFO, "Finished reading users from " + path);
            logger.log(Level.INFO, "There are now " + users.size() + " users in the list!");

            logger.log(Level.INFO, "******* Filtering users *******");

            // ******* FILTER USERS BY NAME: e.g.: Just one user per name should be in the ArrayList *******
            HashMap<String, User> names = new HashMap<String, User>();
            for (User u : users) {
                if (u.getUser() == null) {
                    continue;
                }
                if (!names.containsKey(u.getUser().getName())) {
                    names.put(u.getUser().getName(), u);
                }
            }
            System.out.println("NEW COUNT: " + names.size());
            users = new ArrayList<User>(names.values());
            System.out.println("FILTERED: " + users.size());

            System.out.println("FINISHED PARSING USERS");
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
        }

        try {
            if (ois != null) {
                ois.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadUsers(final String fileName, final Runnable completion) {
        final long initialTime = System.currentTimeMillis();

        ArrayList<String> namesToDownload = new ArrayList<String>(userNames);
        if (users.size() > 0) {
            for (User u : users) {
                if (namesToDownload.contains(u.getUser().getName())) {
                    namesToDownload.remove(u.getUser().getName());
                }
            }
            System.out.println("DOWNLOADING " + namesToDownload.size() + " new users!");
        }

        UserDownloader downloader = new UserDownloader(namesToDownload, new UserDownloaderCallback() {

            @Override
            public void finishedLoading(ArrayList<User> users) {
                System.out.println("******* WE ARE REALLY FINISHED!!! *******");
                System.out.println("FINAL NEWLY DOWNLOADED USERS SIZE: " + users.size());
                System.out.println("TOTAL USERS SIZE: " + (Main.users.size() + users.size()));

                System.out.println("TIME: " + (System.currentTimeMillis() - initialTime) + " millis");

                System.out.println("WRITING USERS TO FILE " + fileName);

                Main.users.addAll(users);

                // ******* Get unique users *******
                HashMap<String, User> uniqueUsers = new HashMap<String, User>();
                for (User u : Main.users) {
                    if (u.getUser() != null && u.getUser().getName() != null && !uniqueUsers
                        .containsKey(u.getUser().getName())) {
                        uniqueUsers.put(u.getUser().getName(), u);
                    }
                }

                Main.users = new ArrayList<User>(uniqueUsers.values());
                // ******* End Get unique usernames *******

                // DELETE FILE IF IT EXISTS, BUT IT SHOULD NOT EXIST BECAUSE WE ARE CHECKING THIS BEFORE STARTING
                // DOWNLOAD ITEMS!!!
                File file = new File(fileName);
                if (file.exists()) {
                    file.delete();
                }

                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(new FileOutputStream(fileName));
                    oos.writeObject(Main.users);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Writing to " + fileName + " finished");

                try {
                    if (oos != null) {
                        oos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                completion.run();
            }
        });

        downloader.startDownloading();
    }

    private static void parseStats(StatsType type, String[] predicate) {
        switch (type) {
        case ITEMS:
            StatisticsHelper.parseItemsStatsRequest(items, predicate);
            break;
        case USERS:
            StatisticsHelper.parseUsersStatsRequest(users, predicate);
            break;
        case USER_INFO:
            StatisticsHelper.parseUserInfoStatsRequest(users, predicate);
            break;
        }
    }

    private static void parseAbsoluteStats(String[] options, boolean average) {
        if (options.length != 2) {
            warnUsage();
            return;
        }

        switch (options[0].toLowerCase()) {
        case "items":
            if (items.size() < 1) {
                warnNoItems();
                return;
            }
            AbsoluteStatisticsHelper.parseAbsoluteStatistics(items, options[1], Item.class, average);
            break;
        case "users":
            if (users.size() < 1) {
                warnNoUsers();
                return;
            }
            AbsoluteStatisticsHelper.parseAbsoluteStatistics(users, options[1], User.class, average);
            break;
        case "userinfo":
            if (users.size() < 1) {
                warnNoUsers();
                return;
            }
            ArrayList<UserInfo> userInfos = new ArrayList<UserInfo>();
            for (User u : users) {
                if (u.getUser() != null) {
                    userInfos.add(u.getUser());
                }
            }
            AbsoluteStatisticsHelper.parseAbsoluteStatistics(userInfos, options[1], UserInfo.class, average);
            break;
        }
    }

    private static void warnUsage() {
        StringBuilder builder = new StringBuilder();

        builder.append("\nUsage:\n");
        builder.append("    readitems <path>\n");
        builder.append("    readusers <path>\n");
        builder.append("    downloaditems <outputPath>\n");
        builder.append("    downloadusers <outputPath>\n");
        builder.append("    stats (items | users | userinfo) <predicate> "
            + "[--sort_type=(acs | desc)] [--sort_field=<field>] [--top=<number>]\n");
        builder.append("    stats (absolute | average) (items | users | userinfo) <memberName>\n");

        builder.append("\n");

        builder.append("Options:\n");
        builder.append("    --sort_type=(asc | desc)  Sort ascending or descending [default: asc].\n");
        builder.append("    --sort_field=<field>      The field to base the sorting on [default: id].\n");
        builder.append("    --top=<number>            The number of elements which should be printed [default: 5].\n");

        logger.log(Level.WARNING, builder.toString());
    }

    private static void warnNoItems() {
        logger.log(Level.WARNING, "There are no cached items to generate stats!");
        logger.log(Level.WARNING, "Please use 'readitems <path>' to read items from the disk "
            + "or 'downloaditems <outputPath>' to download all items!");
    }

    private static void warnNoUsers() {
        logger.log(Level.WARNING, "There are no cached users to generate stats.");
        logger.log(Level.WARNING, "Please use 'readusers <path>' to read users from the disk "
            + "or 'downloadusers <outputPath>' to download all users!");
    }

    public static Logger getLogger() {
        return logger;
    }
}
