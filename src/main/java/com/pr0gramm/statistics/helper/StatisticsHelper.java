package com.pr0gramm.statistics.helper;

import com.pr0gramm.statistics.Main;
import com.pr0gramm.statistics.api.Item;
import com.pr0gramm.statistics.api.User;
import com.pr0gramm.statistics.api.UserInfo;
import com.pr0gramm.statistics.generator.StatisticsGenerator;
import com.pr0gramm.statistics.predicate.ConditionalPredicateCombination;
import com.pr0gramm.statistics.predicate.GeneralPredicate;
import com.pr0gramm.statistics.predicate.OperatorPredicate;
import com.pr0gramm.statistics.toolbox.ConditionalOperatorType;
import com.pr0gramm.statistics.toolbox.SortOption;
import com.pr0gramm.statistics.toolbox.SortType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

/**
 * Helper methods for statistics requests.
 * <p>
 * Created by koray on 30/01/2017.
 */
public class StatisticsHelper {

    public static void parseItemsStatsRequest(ArrayList<Item> items, String[] predicate) {
        if (items.size() < 1) {
            Main.getLogger().log(Level.WARNING, "There are no cached items to generate stats!");
            Main.getLogger().log(Level.WARNING, "Please use 'readitems <path>' to read items from the disk "
                + "or 'downloaditems <outputPath>' to download all items!");
            return;
        }
        SortOption sortOption = filterPredicates(predicate);

        ArrayList<GeneralPredicate<Item>> itemPredicates = generatePredicates(sortOption.getPredicate(), Item.class);
        if (itemPredicates == null) {
            Main.getLogger().log(Level.WARNING, "Statistics could not be generated!");
            return;
        }

        StatisticsGenerator<Item> itemGenerator = new StatisticsGenerator<Item>(items, itemPredicates);
        itemGenerator.generate();
        Main.getLogger().info("There are " + itemGenerator.getStatisticsList().size() + " out of " + items.size()
            + " items which satisfy all given predicates!");

        Main.getLogger().info("------- Showing the top " + sortOption.getTop() + " items -------");
        Main.getLogger().info("--- Sorted by field " + sortOption.getField() + "\n\n");

        ArrayList<Item> sorted = itemGenerator.sort(sortOption);

        if (sortOption.getSortType() == SortType.DESCENDING) {
            for (int i = sorted.size() - 1; i > sorted.size() - 1 - sortOption.getTop(); i--) {
                logPlacing(i + 1, sorted.get(i));
            }
        } else {
            for (int i = 0; i < sortOption.getTop(); i++) {
                if (sorted.size() > i) {
                    logPlacing(i + 1, sorted.get(i));
                }
            }
        }
    }

    public static void parseUsersStatsRequest(ArrayList<User> users, String[] predicate) {
        if (users.size() < 1) {
            Main.getLogger().log(Level.WARNING, "There are no cached users to generate stats.");
            Main.getLogger().log(Level.WARNING, "Please use 'readusers <path>' to read users from the disk "
                + "or 'downloadusers <outputPath>' to download all users!");
            return;
        }
        SortOption sortOption = filterPredicates(predicate);
        // Default field is user.id for users
        if (sortOption.getField().equals("id")) {
            sortOption.setField("user.id");
        }

        ArrayList<GeneralPredicate<User>> userPredicates = generatePredicates(sortOption.getPredicate(), User.class);
        if (userPredicates == null) {
            Main.getLogger().log(Level.WARNING, "Statistics could not be generated!");
            return;
        }

        StatisticsGenerator<User> userGenerator = new StatisticsGenerator<User>(users, userPredicates);
        userGenerator.generate();
        Main.getLogger().info("There are " + userGenerator.getStatisticsList().size() + " out of " + users.size()
            + " users which satisfy all given predicates!");

        Main.getLogger().info("------- Showing the top " + sortOption.getTop() + " users -------");
        Main.getLogger().info("--- Sorted by field " + sortOption.getField() + "\n\n");

        ArrayList<User> sorted = userGenerator.sort(sortOption);

        if (sortOption.getSortType() == SortType.DESCENDING) {
            for (int i = sorted.size() - 1; i > sorted.size() - 1 - sortOption.getTop(); i--) {
                logPlacing(i + 1, sorted.get(i));
            }
        } else {
            for (int i = 0; i < sortOption.getTop(); i++) {
                if (sorted.size() > i) {
                    logPlacing(i + 1, sorted.get(i));
                }
            }
        }
    }

    public static void parseUserInfoStatsRequest(ArrayList<User> users, String[] predicate) {
        if (users.size() < 1) {
            Main.getLogger().log(Level.WARNING, "There are no cached users to generate stats.");
            Main.getLogger().log(Level.WARNING, "Please use 'readusers <path>' to read users from the disk "
                + "or 'downloadusers <outputPath>' to download all users!");
            return;
        }
        SortOption sortOption = filterPredicates(predicate);

        // Get user info elements
        ArrayList<UserInfo> userInfos = new ArrayList<UserInfo>();
        for (User u : users) {
            if (u.getUser() != null) {
                userInfos.add(u.getUser());
            }
        }

        ArrayList<GeneralPredicate<UserInfo>> userInfoPredicates = generatePredicates(sortOption.getPredicate(),
            UserInfo.class);
        if (userInfoPredicates == null) {
            Main.getLogger().log(Level.WARNING, "Statistics could not be generated!");
            return;
        }

        StatisticsGenerator<UserInfo> userInfoGenerator = new StatisticsGenerator<UserInfo>(userInfos,
            userInfoPredicates);
        userInfoGenerator.generate();
        Main.getLogger().info(
            "There are " + userInfoGenerator.getStatisticsList().size() + " out of " + userInfos.size()
                + " user info elements which satisfy all given predicates!");

        Main.getLogger().info("------- Showing the top " + sortOption.getTop() + " userinfo -------");
        Main.getLogger().info("--- Sorted by field " + sortOption.getField() + "\n\n");

        ArrayList<UserInfo> sorted = userInfoGenerator.sort(sortOption);

        if (sortOption.getSortType() == SortType.DESCENDING) {
            for (int i = sorted.size() - 1; i > sorted.size() - 1 - sortOption.getTop(); i--) {
                logPlacing(i + 1, sorted.get(i));
            }
        } else {
            for (int i = 0; i < sortOption.getTop(); i++) {
                if (sorted.size() > i) {
                    logPlacing(i + 1, sorted.get(i));
                }
            }
        }
    }

    private static <T> ArrayList<GeneralPredicate<T>> generatePredicates(String[] predicate, Class<T> predicateType) {
        ArrayList<GeneralPredicate<T>> predicates = new ArrayList<GeneralPredicate<T>>();

        LinkedList<String> list = new LinkedList<String>(Arrays.asList(predicate));

        boolean running = true;
        ConditionalOperatorType lastType = null;
        while (running) {
            String field = list.poll();
            String operator = list.poll();
            String value = list.poll();

            if (field == null && operator == null && value == null) {
                running = false;
                continue;
            } else if (field == null || operator == null || value == null) {
                warnPredicate();
                return null;
            }

            OperatorPredicate<T> operatorPredicate = new OperatorPredicate<T>(predicateType);
            try {
                operatorPredicate.parsePredicate(new String[] { field, operator, value });
            } catch (NoSuchFieldException e) {
                Main.getLogger().log(Level.WARNING, "The field with the name '" + field + "' could not be found!");
                return null;
            }

            if (lastType == null) {
                predicates.add(operatorPredicate);
            } else {
                if (predicates.size() < 1) {
                    Main.getLogger().log(Level.WARNING,
                        "Something went totally wrong and I bet the programmer doesn't know what the problem is...");
                    return null;
                }

                GeneralPredicate<T> oldPred = predicates.get(predicates.size() - 1);
                predicates.remove(predicates.size() - 1);

                ConditionalPredicateCombination<T, GeneralPredicate<T>> combinedPred = new ConditionalPredicateCombination<>(
                    oldPred, operatorPredicate, lastType);

                lastType = null;

                predicates.add(combinedPred);
            }

            // Getting next combinator or finishing the loop
            String combinator = list.poll();

            if (combinator == null) {
                running = false;
            } else {
                ConditionalOperatorType type = ConditionalOperatorType.fromString(combinator);
                if (type == null) {
                    Main.getLogger().log(Level.WARNING,
                        "The conditional operator " + combinator + " is not supported. Please use either && or ||");
                    return null;
                }

                lastType = type;
            }
        }

        return predicates;
    }

    private static SortOption filterPredicates(String[] predicate) {
        int top = 5;
        String sortField = "id";
        SortType sortType = SortType.ASCENDING;

        ArrayList<String> predicateList = new ArrayList<String>(Arrays.asList(predicate));
        Iterator<String> predicateIterator = predicateList.iterator();

        while (predicateIterator.hasNext()) {
            String s = predicateIterator.next();
            if (s == null) {
                continue;
            }

            if (s.startsWith("--top=")) {
                String number = s.replace("--top=", "");
                if (DecimalHelper.isInt(number)) {
                    top = Integer.parseInt(number);
                }
                predicateIterator.remove();
            } else if (s.startsWith("--sort_type=")) {
                SortType sort = SortType.fromString(s.replace("--sort_type=", ""));
                if (sort != null) {
                    sortType = sort;
                }
                predicateIterator.remove();
            } else if (s.startsWith("--sort_field=")) {
                sortField = s.replace("--sort_field=", "");
                predicateIterator.remove();
            }
        }

        String[] filteredPredicates = new String[predicateList.size()];
        filteredPredicates = predicateList.toArray(filteredPredicates);

        SortOption sortOption = new SortOption(sortType, sortField, top, filteredPredicates);

        return sortOption;
    }

    private static void warnPredicate() {
        Main.getLogger().log(Level.WARNING,
            "Predicates must follow the following rule: " + "<memberName> (== | != | > | < | >= | <=) <value>"
                + "\nand can be combined with && or ||");
    }

    private static void logPlacing(int number, Object object) {
        String info = "";
        info = info + "Number " + number + ":\n";
        info = info + object.toString();
        info = info.replace("\n", "\n    ");
        Main.getLogger().info(info);
    }
}
