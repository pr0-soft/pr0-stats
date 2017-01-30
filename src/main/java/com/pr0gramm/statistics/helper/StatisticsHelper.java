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

import java.util.ArrayList;
import java.util.Arrays;
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
        ArrayList<GeneralPredicate<Item>> itemPredicates = generatePredicates(predicate, Item.class);
        if (itemPredicates == null) {
            Main.getLogger().log(Level.WARNING, "Statistics could not be generated!");
            return;
        }

        StatisticsGenerator<Item> itemGenerator = new StatisticsGenerator<Item>(items, itemPredicates);
        itemGenerator.generate();
        Main.getLogger().info("There are " + itemGenerator.getStatisticsList().size() + " out of " + items.size()
            + " items which satisfy all given predicates!");
    }

    public static void parseUsersStatsRequest(ArrayList<User> users, String[] predicate) {
        if (users.size() < 1) {
            Main.getLogger().log(Level.WARNING, "There are no cached users to generate stats.");
            Main.getLogger().log(Level.WARNING, "Please use 'readusers <path>' to read users from the disk "
                + "or 'downloadusers <outputPath>' to download all users!");
            return;
        }
        ArrayList<GeneralPredicate<User>> userPredicates = generatePredicates(predicate, User.class);
        if (userPredicates == null) {
            Main.getLogger().log(Level.WARNING, "Statistics could not be generated!");
            return;
        }

        StatisticsGenerator<User> userGenerator = new StatisticsGenerator<User>(users, userPredicates);
        userGenerator.generate();
        Main.getLogger().info("There are " + userGenerator.getStatisticsList().size() + " out of " + users.size()
            + " users which satisfy all given predicates!");
    }

    public static void parseUserInfoStatsRequest(ArrayList<User> users, String[] predicate) {
        if (users.size() < 1) {
            Main.getLogger().log(Level.WARNING, "There are no cached users to generate stats.");
            Main.getLogger().log(Level.WARNING, "Please use 'readusers <path>' to read users from the disk "
                + "or 'downloadusers <outputPath>' to download all users!");
            return;
        }

        // Get user info elements
        ArrayList<UserInfo> userInfos = new ArrayList<UserInfo>();
        for (User u : users) {
            if (u.getUser() != null) {
                userInfos.add(u.getUser());
            }
        }

        ArrayList<GeneralPredicate<UserInfo>> userInfoPredicates = generatePredicates(predicate, UserInfo.class);
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

    private static void warnPredicate() {
        Main.getLogger().log(Level.WARNING,
            "Predicates must follow the following rule: " + "<memberName> (== | != | > | < | >= | <=) <value>"
                + "\nand can be combined with && or ||");
    }
}
