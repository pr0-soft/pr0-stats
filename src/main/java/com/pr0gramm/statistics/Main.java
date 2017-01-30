package com.pr0gramm.statistics;

import com.google.gson.Gson;
import com.pr0gramm.statistics.api.Item;
import com.pr0gramm.statistics.api.User;
import com.pr0gramm.statistics.helper.StatisticsHelper;
import com.pr0gramm.statistics.networking.PostDownloader;
import com.pr0gramm.statistics.networking.PostDownloaderCallback;
import com.pr0gramm.statistics.networking.UserDownloader;
import com.pr0gramm.statistics.networking.UserDownloaderCallback;
import com.pr0gramm.statistics.predicate.ConditionalPredicateCombination;
import com.pr0gramm.statistics.predicate.GeneralPredicate;
import com.pr0gramm.statistics.predicate.OperatorPredicate;
import com.pr0gramm.statistics.toolbox.OperatorType;
import com.pr0gramm.statistics.toolbox.SimpleConsoleFormatter;
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
                String str = scanner.next().trim();

                String[] splitted = str.split("\\s+");

                if (splitted.length < 1) {
                    warnUsage();
                    continue;
                }

                switch (splitted[0].toLowerCase()) {
                case "readitems":
                    if (splitted.length != 2) {
                        warnUsage();
                        continue;
                    }
                    readItems(splitted[1]);
                    break;
                case "readusers":
                    if (splitted.length != 2) {
                        warnUsage();
                        continue;
                    }
                    readUsers(splitted[1]);
                    break;
                case "downloaditems":
                    if (splitted.length != 2) {
                        warnUsage();
                        continue;
                    }
                    if ((new File(splitted[1]).exists())) {
                        logger.log(Level.WARNING, "File " + splitted[1] + "already exists!");
                        logger.log(Level.WARNING, "Please type in a path to a file which does not exist yet.");
                        continue;
                    }
                    downloadItems(splitted[1]);
                    break;
                case "downloadusers":
                    if (splitted.length != 2) {
                        warnUsage();
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
                    downloadUsers(splitted[1]);
                    break;
                case "stats":
                    if (splitted.length < 3) {
                        warnUsage();
                        continue;
                    }

                    StatsType type;
                    if (splitted[1].equalsIgnoreCase("users")) {
                        type = StatsType.USERS;
                    } else if (splitted[1].equalsIgnoreCase("items")) {
                        type = StatsType.ITEMS;
                    } else if (splitted[1].equalsIgnoreCase("userinfo")) {
                        type = StatsType.USER_INFO;
                    } else {
                        warnUsage();
                        continue;
                    }

                    String[] predicate = Arrays.copyOfRange(splitted, 2, splitted.length);

                    parseStats(type, predicate);
                    break;
                default:
                    warnUsage();
                    continue;
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void downloadItems(final String fileName) {
        final long initialTime = System.currentTimeMillis();

        PostDownloader downloader = new PostDownloader(new PostDownloaderCallback() {

            @Override
            public void finishedLoading(ArrayList<Item> items, ArrayList<String> userNames) {
                System.out.println("******* WE ARE REALLY FINISHED!!! *******");
                System.out.println("FINAL ITEM SIZE: " + items.size());
                System.out.println("FINAL USER SIZE: " + userNames.size());

                System.out.println("TIME: " + (System.currentTimeMillis() - initialTime) + " millis");

                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(new FileOutputStream(fileName));
                    oos.writeObject(items);
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
            }
        });
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
            logger.log(Level.INFO, "There are now " + userNames.size() + " users in the list!");
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
            logger.log(Level.INFO, "There are now " + users.size() + " items in the list!");

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

    private static void downloadUsers(final String fileName) {
        final long initialTime = System.currentTimeMillis();

        UserDownloader downloader = new UserDownloader(userNames, new UserDownloaderCallback() {

            @Override
            public void finishedLoading(ArrayList<User> users) {
                System.out.println("******* WE ARE REALLY FINISHED!!! *******");
                System.out.println("FINAL DOWNLOADED USER SIZE: " + users.size());

                System.out.println("TIME: " + (System.currentTimeMillis() - initialTime) + " millis");

                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(new FileOutputStream(fileName));
                    oos.writeObject(users);
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

    private static void warnUsage() {
        logger.log(Level.WARNING,
            "\nUsage:\n" + "    readitems <path>\n" + "    readusers <path>\n" + "    downloaditems <outputPath>\n"
                + "    downloadusers <outputPath>\n" + "    stats (items | users | userinfo) <predicate>\n");
    }

    public static Logger getLogger() {
        return logger;
    }
}
