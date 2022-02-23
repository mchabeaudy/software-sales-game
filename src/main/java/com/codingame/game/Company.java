package com.codingame.game;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Company {

    public static final int MANAGER_COST = 10;
    public static final int DEV_COST = 5;
    public static final int SALE_COST = 5;

    private static final double EX = 2.718281828;

    private int devs;
    private int sales;
    private int managers = 1;
    private int cash = 1000;
    private int bugs;
    private int newBugs;
    private int debugRate;
    private int salesAggressivenessRate;
    private int features;
    private int market;
    private int productQuality;
    private int resolvedBugs;
    private int score;
    private int availableFreeMarketShare;
    private int availableCompetitiveMarket;
    private Player player;

    public int getDevs() {
        return devs;
    }

    public void setDevs(int devs) {
        this.devs = devs;
    }

    public int getSales() {
        return sales;
    }

    public void setSales(int sales) {
        this.sales = sales;
    }

    public int getManagers() {
        return managers;
    }

    public void setManagers(int managers) {
        this.managers = managers;
    }

    public int getCash() {
        return cash;
    }

    public void setCash(int cash) {
        this.cash = cash;
    }

    public void addCash(int cash) {
        this.cash += cash;
    }

    public int getBugs() {
        return bugs;
    }

    public void setBugs(int bugs) {
        this.bugs = bugs;
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

    public void addSales(int sales) {
        this.sales += sales;
    }

    public int getDebugRate() {
        return debugRate;
    }

    public void setDebugRate(int debugRate) {
        int variableRate = 0;
        if (getTotSubordinates() * 0.25 < managers) {
            variableRate = (int) (100 * Math.random() * 0.6 - 0.3);
        }
        this.debugRate = Math.min(Math.max(debugRate + variableRate, 0), 100);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getFeatures() {
        return features;
    }

    public void setFeatures(int features) {
        this.features = features;
    }

    public int getMarket() {
        return market;
    }

    public void setMarket(int market) {
        this.market = market;
    }

    public void payEmployees(int turn, int playerCount) {
        // Pays
        addCash((int) (market * (10 * Math.pow(1.05, turn * 1.0 / playerCount))));

        double factor = 0.03 * (managers + sales + devs) + 1;

        // Pays managers
        while (cash - managers * (MANAGER_COST * factor) < 0) {
            managers--;
        }
        addCash((int) (-managers * MANAGER_COST * factor));

        // Pays sales
        while (cash - sales * SALE_COST * factor < 0) {
            sales--;
        }
        addCash((int) (-sales * SALE_COST * factor));

        // Pays devs
        while (cash - devs * DEV_COST * factor < 0) {
            devs--;
        }
        addCash((int) (-devs * DEV_COST * factor));
    }

    public void developFeatures() {
        int featureDevs = (int) (devs * (0.01 * (100 - debugRate)));
        int debugDevs = devs - featureDevs;
        features += featureDevs;
        int nbBugs = bugs;
        bugs = Math.max(0, bugs - debugDevs);
        resolvedBugs += nbBugs - bugs;
        productQuality = (int) (100 * (1 - Math.pow(EX, -0.03 * resolvedBugs)));
    }

    public int getProductQuality() {
        return productQuality;
    }

    public void setProductQuality(int productQuality) {
        this.productQuality = productQuality;
    }

    public int getResolvedBugs() {
        return resolvedBugs;
    }

    public void setResolvedBugs(int resolvedBugs) {
        this.resolvedBugs = resolvedBugs;
    }

    public void addMarket(int marketToAdd) {
        market += marketToAdd;
    }

    public void buildScore(double featuresAverage) {
        int featureScore = (int) Math.min(50.0 / featuresAverage * features, 100);
        score = Math.max(0, featureScore + (int) (getRobustness() * 100) - (bugs - newBugs) * 3 - newBugs * 6);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void resetAvailableMarket(double salesAverage) {
        availableFreeMarketShare =
                salesAverage == 0 ? 0 : (int) ((100 - salesAggressivenessRate) * 0.1 * sales / salesAverage);
        availableCompetitiveMarket =
                salesAverage == 0 ? 0 : (int) ((salesAggressivenessRate) * 0.02 * sales / salesAverage);
    }


    public void increaseBugs(Random random) {
        newBugs = (int) (random.nextDouble() * getRobustness() * features);
        bugs += newBugs;
    }

    public double getRobustness() {
        return 0 == features ? 2.0 : Math.min((double) resolvedBugs / (double) features, 2.0) * 0.5;
    }

    public int getSalesAggressivenessRate() {
        return salesAggressivenessRate;
    }

    public void setSalesAggressivenessRate(int salesAggressivenessRate, Random random) {
        int variableRate = 0;
        if (getTotSubordinates() * 0.25 < managers) {
            variableRate = (int) (100 * random.nextDouble() * 0.6 - 0.3);
        }
        this.salesAggressivenessRate = Math.min(Math.max(salesAggressivenessRate + variableRate, 0), 100);
    }

    public int getTotSubordinates() {
        return devs + sales;
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
                .min(Comparator.comparingInt(Company::getScore))
                .orElse(null);
        if (Objects.nonNull(weakest)) {
            double sa = scoreAverage == 0 ? 1 : 1.0 / scoreAverage;
            int marketToTake = Math.min(1, (int) (availableCompetitiveMarket * score * sa));
            market += marketToTake;
            weakest.setMarket(weakest.getMarket() - marketToTake);
        }
    }

    public int getAvailableCompetitiveMarket() {
        return availableCompetitiveMarket;
    }

    public int getTotalEmployees() {
        return devs + sales + managers;
    }
}
