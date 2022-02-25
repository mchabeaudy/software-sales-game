package com.codingame.game;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Company {

    public static final int MANAGER_COST = 15;
    public static final int DEV_COST = 10;
    public static final int SELLER_COST = 10;

    private static final double EX = 2.718281828;

    private int devs;
    private int sellers;
    private int managers = 1;
    private int cash = 1000;
    private int bugs;
    private int tests;
    private int newBugs;
    private int debugDevs;
    private int agressiveSellers;
    private int features;
    private int market;
    private int productQuality;
    private int resolvedBugs;
    private double score;
    private double availableFreeMarketShare;
    private double availableCompetitiveMarket;
    private Player player;


    public void addCash(int cash) {
        this.cash += cash;
    }


    public void addDevs(int devs) {
        this.devs += devs;
    }

    public void addBugs(int bugs) {
        this.bugs += bugs;
    }

    public void addManagers(int managers) {
        this.managers += managers;
    }

    public void addSellers(int sellers) {
        this.sellers += sellers;
    }

    public void payDay(int turn) {
        addCash((int) (market * Math.pow(1.0 / 0.95, turn)));

        while (costToPay() > cash) {
            decreaseEmployee();
        }
        addCash(-managers * MANAGER_COST);
        addCash(-sellers * SELLER_COST);
        addCash(-devs * DEV_COST);
    }

    private int costToPay() {
        return (devs + debugDevs) * DEV_COST + (sellers + agressiveSellers) * SELLER_COST + managers * MANAGER_COST;
    }

    private void decreaseEmployee() {
        if (devs > 0) {
            devs--;
        } else if (debugDevs > 0) {
            debugDevs--;
        } else if (sellers > 0) {
            sellers--;
        } else if (agressiveSellers > 0) {
            agressiveSellers--;
        } else {
            managers--;
        }
    }

    private double chanceToBug() {
        return Math.pow(EX, -1.0 * tests / features);
    }

    public void developFeatures(Random random) {
        // develop features
        features += devs;
        // resolve bugs
        int nbBugs = bugs;
        bugs = Math.max(0, bugs - debugDevs);
        resolvedBugs += nbBugs - bugs;
        if (features > 0) {
            tests += debugDevs;
        }
        // increase bugs
        newBugs = (int) Math.round(chanceToBug() * features * random.nextDouble());
        bugs += newBugs;
    }

    public void addMarket(int marketToAdd) {
        market += marketToAdd;
    }

    public void evaluateScore() {
        score = 1.0 * features / Math.max(1.0, 1.0 * (bugs * 3 + resolvedBugs));
    }

    public void resetAvailableMarket(double salesAverage) {
    }


    public double getRobustness() {
        return 0 == features ? 2.0 : Math.min((double) resolvedBugs / (double) features, 2.0) * 0.5;
    }


    public void takeFreeMarket(int remainingFreeMarket, AtomicInteger takenFreeMarket, double scoreAverage) {
        double sa = scoreAverage == 0 ? 1 : 1.0 / scoreAverage;
        int marketToAdd = Math.min(remainingFreeMarket - takenFreeMarket.get(),
                (int) (availableFreeMarketShare * score * sa));
        takenFreeMarket.getAndAdd(marketToAdd);
        market += marketToAdd;
    }

    public void takeCompetitiveMarket(Collection<Company> companies, double scoreAverage) {
        Company weakest = companies.stream()
                .filter(c -> c.getMarket() != 0 && !c.equals(this) && c.getMarket() > 10)
                .min(Comparator.comparingDouble(Company::getScore))
                .orElse(null);
        if (Objects.nonNull(weakest)) {
            double sa = scoreAverage == 0 ? 1 : 1.0 / scoreAverage;
            int marketToTake = Math.min(1, (int) (availableCompetitiveMarket * score * sa));
            market += marketToTake;
            weakest.setMarket(weakest.getMarket() - marketToTake);
        }
    }


    public int getTotalEmployees() {
        return devs + debugDevs + sellers + agressiveSellers + managers;
    }


    public int getTotalDevs() {
        return devs + debugDevs;
    }

    public int getTotalSellers() {
        return sellers + agressiveSellers;
    }

    public void applyManagerRule(Random random) {
        int k = getTotalDevs() + getTotalSellers();
        while (k > 4 * managers) {
            double d = random.nextDouble() * 4;
            if (d < 1 && devs > 0) {
                devs--;
                debugDevs++;
                k--;
            } else if (d < 2 && debugDevs > 0) {
                devs++;
                debugDevs--;
                k--;
            } else if (d < 3 && sellers > 0) {
                sellers--;
                agressiveSellers++;
                k--;
            } else if (agressiveSellers > 0) {
                sellers++;
                agressiveSellers--;
                k--;
            } else {
                // should never happen
                k--;
            }
        }
    }

    public void resetAvailableMarkets(int totalSales) {
        availableFreeMarketShare = 1.0 * sellers / totalSales;
        availableCompetitiveMarket = 1.0 * agressiveSellers / totalSales;
    }
}
