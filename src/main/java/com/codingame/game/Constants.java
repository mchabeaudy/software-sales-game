package com.codingame.game;

import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class Constants {

    private static final int P1 = 740; // 1000*e^-0.3
    private static final int P2 = 549; // 1000*e^-0.6
    private static final int P3 = 407; // 1000*e^-0.9
    private static final int P4 = 301; // 1000*e^-1.2
    private static final int P5 = 223; // 1000*e^-1.5
    private static final int P6 = 165; // 1000*e^-1.8
    private static final int P7 = 122; // 1000*e^-2.1
    private static final int P8 = 91; // 1000*e^-2.4
    private static final int P9 = 67; // 1000*e^-2.7

    public static final int MANAGER_COST = 20;
    public static final int DEV_COST = 10;
    public static final int SELLER_COST = 10;

    private static final Map<Integer, Integer> PROB_TABLE = new HashMap<>();
    private static final List<Integer> FIRST_PROB = new ArrayList<>(5000);

    static {
        PROB_TABLE.put(1, P1);
        PROB_TABLE.put(2, P2);
        PROB_TABLE.put(3, P3);
        PROB_TABLE.put(4, P4);
        PROB_TABLE.put(5, P5);
        PROB_TABLE.put(6, P6);
        PROB_TABLE.put(7, P7);
        PROB_TABLE.put(8, P8);
        PROB_TABLE.put(9, P9);
    }

    private Constants() {
    }

    public static void resetProb(Random random) {
        FIRST_PROB.clear();
        range(0, 5000).forEach(i -> FIRST_PROB.add(random.nextInt(1000)));
    }

    public static int getProb(int time) {
        return PROB_TABLE.getOrDefault(time, 0);
    }

    public static int getNextInt(Random random, int index) {
        if (index < 5000) {
            return FIRST_PROB.get(index);
        }
        return random.nextInt(1000);
    }
}
