package com.codingame.game;

import static com.codingame.game.Constants.DEV_COST;
import static com.codingame.game.Constants.MANAGER_COST;
import static com.codingame.game.Constants.SELLER_COST;
import static com.codingame.game.Constants.getProb;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Company {


    private int completedFeatures;
    private Map<Integer, Integer> featuresInProgress = new HashMap<>();

    private int featureDevs;
    private int maintenanceDevs;
    private int inactiveDevs;
    private int freeMarketSellers;
    private int competitiveMarketSellers;
    private int inactiveSellers;
    private int managers = 1;
    private int cash = 1000;
    private int bugs;
    private int tests;
    private int newBugs;
    private int market;
    private int resolvedBugs;
    private int reputation;
    private Integer targetId;
    private Player player;

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
        addCash((int) (market * Math.pow(1.0 / 0.95, turn)));

        while (costToPay() > cash) {
            decreaseEmployee();
        }
        addCash(-managers * MANAGER_COST);
        addCash(-freeMarketSellers * SELLER_COST);
        addCash(-featureDevs * DEV_COST);
    }

    private int costToPay() {
        return (featureDevs + maintenanceDevs) * DEV_COST + (freeMarketSellers + competitiveMarketSellers) * SELLER_COST
                + managers * MANAGER_COST;
    }

    private void decreaseEmployee() {
        if (featureDevs > 0) {
            featureDevs--;
        } else if (maintenanceDevs > 0) {
            maintenanceDevs--;
        } else if (freeMarketSellers > 0) {
            freeMarketSellers--;
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
        bugs = Math.max(0, bugs - maintenanceDevs);
        resolvedBugs += nbBugs - bugs;
        if (getTotalFeatures() > 0) {
            tests += maintenanceDevs;
        }

        // increase bugs
        int totFeatures = getTotalFeatures();
        if (totFeatures > 0) {
            double m = Math.max(0, 1d - 0.25 * tests / totFeatures);
            newBugs = 0;
            featuresInProgress.forEach((time, featuresCount) -> {
                double chanceToBug = getProb(time) * m;
                newBugs += (int) range(0, featuresCount)
                        .filter(i -> random.nextInt(1000) < chanceToBug)
                        .count();
            });
            bugs += newBugs;
        }
    }

    public int getTotalFeatures() {
        return completedFeatures + getFeaturesInProgressCount();
    }

    public void addMarket(int marketToAdd) {
        market += marketToAdd;
    }

    public void evaluateReputation() {
        reputation = Math.max(1, (int) (100 * (1d * getTotalFeatures() / Math.max(1d, 1d * bugs * 3 + resolvedBugs))));
    }


    public int getTotalEmployees() {
        return featureDevs + maintenanceDevs + freeMarketSellers + competitiveMarketSellers + managers;
    }


    public int getTotalDevs() {
        return featureDevs + maintenanceDevs + inactiveDevs;
    }

    public int getTotalSellers() {
        return freeMarketSellers + competitiveMarketSellers + inactiveSellers;
    }

    public void applyManagerRule(Random random) {
        int k = getTotalDevs() + getTotalSellers();
        while (k > 4 * managers) {
            k--;
            List<Consumer<Integer>> rules = new ArrayList<>();
            if (featureDevs > 0) {
                rules.add(this::devRule);
            }
            if (maintenanceDevs > 0) {
                rules.add(this::maintenanceDevRule);
            }
            if (freeMarketSellers > 0) {
                rules.add(this::sellerRule);
            }
            if (competitiveMarketSellers > 0) {
                rules.add(this::competitiveSellerRule);
            }
            int d = random.nextInt(rules.size());
            rules.get(d).accept(random.nextInt(3));
        }
    }


    private void devRule(int i) {
        if (i == 0) {
            featureDevs--;
            maintenanceDevs++;
        } else if (i == 1) {
            featureDevs--;
            inactiveDevs++;
        }
    }

    private void maintenanceDevRule(int i) {
        if (i == 0) {
            maintenanceDevs--;
            featureDevs++;
        } else if (i == 1) {
            maintenanceDevs--;
            inactiveDevs++;
        }
    }

    private void sellerRule(int i) {
        if (i == 0) {
            freeMarketSellers--;
            competitiveMarketSellers++;
        } else if (i == 1) {
            freeMarketSellers--;
            inactiveSellers++;
        }
    }

    private void competitiveSellerRule(int i) {
        if (i == 0) {
            competitiveMarketSellers--;
            freeMarketSellers++;
        } else if (i == 1) {
            competitiveMarketSellers--;
            inactiveSellers++;
        }
    }

    public int getFeaturesInProgressCount() {
        return featuresInProgress.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void takeUnfilledMarket(double totalScores, int freeMarketAvailableForSale) {
        addMarket(
                (int) (reputation * freeMarketSellers * getTotalFeatures() * freeMarketAvailableForSale / totalScores));
    }

    public int getCompetitiveScore() {
        return reputation * competitiveMarketSellers * getTotalFeatures();
    }

    public void takeMarketFrom(Company company, int factor) {
        if (getCompetitiveScore() > company.getCompetitiveScore()) {

            int marketToTake =
                    Math.min((int) Math.min(8d * factor,
                                    4d * factor * getCompetitiveScore() / company.getCompetitiveScore()),
                            company.getMarket());
            addMarket(marketToTake);
            company.addMarket(-marketToTake);
        }
    }


    public void fight(Company comp) {
        if (getCompetitiveScore() > comp.getCompetitiveScore() && getCompetitiveMarketSellers() > 0) {
            int marketToTake = Math.min((int) Math.min(8, 4d * getCompetitiveScore() / comp.getCompetitiveScore()),
                    comp.getMarket());
            addMarket(marketToTake);
            comp.addMarket(-marketToTake);
        } else if (comp.getCompetitiveScore() > getCompetitiveScore()
                && comp.getCompetitiveMarketSellers() > 0) {
            int marketToTake =
                    Math.min((int) Math.max(8, 4d * comp.getCompetitiveScore() / getCompetitiveScore()),
                            getMarket());
            comp.addMarket(marketToTake);
            addMarket(-marketToTake);
        }
    }
}
