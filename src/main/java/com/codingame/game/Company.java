package com.codingame.game;

import static com.codingame.game.Constants.DEV_COST;
import static com.codingame.game.Constants.MANAGER_COST;
import static com.codingame.game.Constants.SELLER_COST;
import static com.codingame.game.Constants.getNextInt;
import static com.codingame.game.Constants.getProb;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.IntStream.range;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Company {

    private static final int COMP_STEAL_MARKET = 8;

    private int completedFeatures;
    private Map<Integer, Integer> featuresInProgress = new HashMap<>();

    private final AtomicInteger counter = new AtomicInteger(0);
    private int featureDevs;
    private int maintenanceDevs;
    private int inactiveDevs;
    private int unfilledMarketSellers;
    private int competitiveMarketSellers;
    private int inactiveSellers;
    private int managers = 1;
    private int cash = 1500;
    private int bugs;
    private int tests;
    private int newBugs;
    private int market;
    private int turnUnfilledMarket;
    private int resolvedBugs;
    private int reputation;
    private int competitiveScore;
    private int unfilledMarketScore;
    private Integer targetId;
    private Player player;
    private boolean active = true;

    public Company(Player player) {
        this.player = player;
        featuresInProgress.put(1, 0);
        featuresInProgress.put(2, 0);
        featuresInProgress.put(3, 0);
        featuresInProgress.put(4, 0);
        featuresInProgress.put(5, 0);
        featuresInProgress.put(6, 0);
        featuresInProgress.put(7, 0);
        featuresInProgress.put(8, 0);
        featuresInProgress.put(9, 0);
        featuresInProgress.put(10, 0);
    }

    public void addCash(int cash) {
        this.cash += cash;
    }

    public void addManagers(int managers) {
        this.managers += managers;
    }

    public void payDay(int turn) {
        if (active) {
            addCash((int) (market * Math.pow(1.0 / 0.95, (turn - 1))));

            while (costToPay() > cash) {
                decreaseEmployee();
            }
            addCash(-managers * MANAGER_COST);
            addCash(-getTotalSellers() * SELLER_COST);
            addCash(-getTotalDevs() * DEV_COST);
        }
    }

    private int costToPay() {
        return getTotalDevs() * DEV_COST + getTotalSellers() * SELLER_COST + managers * MANAGER_COST;
    }

    private void decreaseEmployee() {
        if (featureDevs > 0) {
            featureDevs--;
        } else if (maintenanceDevs > 0) {
            maintenanceDevs--;
        } else if (unfilledMarketSellers > 0) {
            unfilledMarketSellers--;
        } else if (competitiveMarketSellers > 0) {
            competitiveMarketSellers--;
        } else {
            managers--;
        }
    }


    public void developFeatures(Random random) {
        // develop features
        Map<Integer, Integer> newFeatures = new HashMap<>();
        newFeatures.put(1, featureDevs);
        newFeatures.put(2, featuresInProgress.get(1));
        newFeatures.put(3, featuresInProgress.get(2));
        newFeatures.put(4, featuresInProgress.get(3));
        newFeatures.put(5, featuresInProgress.get(4));
        newFeatures.put(6, featuresInProgress.get(5));
        newFeatures.put(7, featuresInProgress.get(6));
        newFeatures.put(8, featuresInProgress.get(7));
        newFeatures.put(9, featuresInProgress.get(8));
        newFeatures.put(10, featuresInProgress.get(9));
        completedFeatures += featuresInProgress.get(10);
        featuresInProgress = newFeatures;

        // resolve bugs
        int nbBugs = bugs;
        bugs = max(0, bugs - maintenanceDevs);
        if (market > 0) {
            resolvedBugs += nbBugs - bugs;
        }
        if (getTotalFeatures() > 0) {
            tests += maintenanceDevs;
        }

        // increase bugs
        int totFeatures = getTotalFeatures();
        if (totFeatures > 0) {
            double m = max(0, 1d - 0.25 * tests / totFeatures);
            newBugs = 0;
            featuresInProgress.forEach((time, featuresCount) -> {
                double chanceToBug = getProb(time) * m;
                newBugs += (int) range(0, featuresCount)
                        .filter(i -> getNextInt(random, counter.getAndAdd(1)) < chanceToBug)
                        .count();
            });
            bugs += newBugs;
        }
    }


    public void addMarket(int marketToAdd) {
        market += marketToAdd;
    }

    public void evaluateReputation() {
        reputation = min(2000, max(1, (int) (100d * getTotalFeatures() / max(1d, 3d * bugs + resolvedBugs))));
    }

    public int getTotalDevs() {
        return featureDevs + maintenanceDevs + inactiveDevs;
    }

    public int getTotalSellers() {
        return unfilledMarketSellers + competitiveMarketSellers + inactiveSellers;
    }

    public void applyManagerRule(Random random) {
        int k = getTotalDevs() + getTotalSellers();
        while (k > 4 * managers) {
            int d = random.nextInt(k);
            if (d < featureDevs) {
                devRule(random.nextInt(4));
            } else if (d < featureDevs + maintenanceDevs) {
                maintenanceDevRule(random.nextInt(4));
            } else if (d < featureDevs + maintenanceDevs + unfilledMarketSellers) {
                sellerRule(random.nextInt(4));
            } else {
                competitiveSellerRule(random.nextInt(4));
            }
            k--;
        }
    }


    private void devRule(int i) {
        if (i == 0) {
            featureDevs--;
            maintenanceDevs++;
        } else if (i > 1) {
            featureDevs--;
            inactiveDevs++;
        }
    }

    private void maintenanceDevRule(int i) {
        if (i == 0) {
            maintenanceDevs--;
            featureDevs++;
        } else if (i > 1) {
            maintenanceDevs--;
            inactiveDevs++;
        }
    }

    private void sellerRule(int i) {
        if (i == 0) {
            unfilledMarketSellers--;
            competitiveMarketSellers++;
        } else if (i > 1) {
            unfilledMarketSellers--;
            inactiveSellers++;
        }
    }

    private void competitiveSellerRule(int i) {
        if (i == 0) {
            competitiveMarketSellers--;
            unfilledMarketSellers++;
        } else if (i > 1) {
            competitiveMarketSellers--;
            inactiveSellers++;
        }
    }

    public int getFeaturesInProgressCount() {
        return featuresInProgress.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void takeUnfilledMarket(double unfilledMarketScoreSum, int freeMarketAvailableForSale) {
        turnUnfilledMarket = (int) (unfilledMarketScore * freeMarketAvailableForSale / unfilledMarketScoreSum);
    }

    public void takeMarketFrom(Company competitor) {
        takeMarketFrom(competitor, 1);
    }

    public void takeMarketFrom(Company competitor, int factor) {
        if (reputation >= competitor.getReputation()) {
            int requestMarket = competitor.getCompetitiveScore() == 0 ? COMP_STEAL_MARKET * factor
                    : min(COMP_STEAL_MARKET * factor,
                            (COMP_STEAL_MARKET * factor * competitiveScore) / (2 * competitor.getCompetitiveScore()));
            int marketToTake = min(requestMarket, competitor.getMarket());
            addMarket(marketToTake);
            competitor.addMarket(-marketToTake);
        }
    }

    public void prepareForSales() {
        competitiveScore = getTotalFeatures() >= 10 ? reputation * competitiveMarketSellers * getTotalFeatures() : 0;
        unfilledMarketScore = getTotalFeatures() >= 10 ? reputation * unfilledMarketSellers * getTotalFeatures() : 0;
        market = (int) Math.round(0.95 * market);
        turnUnfilledMarket = 0;
        inactiveDevs = 0;
        inactiveSellers = 0;
    }

    public int getTotalFeatures() {
        return completedFeatures + getFeaturesInProgressCount();
    }

    public void processFreeMarket() {
        market += turnUnfilledMarket;
    }

    public void deactivate() {
        inactiveDevs = 0;
        featureDevs = 0;
        maintenanceDevs = 0;
        inactiveSellers = 0;
        unfilledMarketSellers = 0;
        competitiveMarketSellers = 0;
        managers = 0;
        active = false;
    }
}
