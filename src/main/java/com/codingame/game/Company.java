package com.codingame.game;

public class Company {

    public static final int MANAGER_COST = 10;
    public static final int DEV_COST = 5;
    public static final int SALE_COST = 5;

    private static final double EX = 2.718281828;

    private int devs;
    private int sales;
    private int managers;
    private int cash;
    private int bugs;
    private int debugRate;
    private int features;
    private int market;
    private int productQuality;
    private int resolvedBugs;
    private int score;
    private int availableMarket;
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
        this.debugRate = debugRate;
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

    public void payEmployees() {
        // Pays
        addCash(market * 100);

        // Pays managers
        while (cash - managers * MANAGER_COST < 0) {
            managers--;
        }
        addCash(-managers * MANAGER_COST);

        // Pays sales
        while (cash - sales * SALE_COST < 0) {
            sales--;
        }
        addCash(-sales * SALE_COST);

        // Pays devs
        while (cash - devs * DEV_COST < 0) {
            devs--;
        }
        addCash(-devs * DEV_COST);
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
        availableMarket -= marketToAdd;
    }

    public void buildScore(double salesAverage, double featuresAverage) {
        int saleScore = (int) Math.min(50.0 / salesAverage * sales, 100);
        int featureScore = (int) Math.min(50.0 / featuresAverage * features, 100);
        score = saleScore + featureScore + productQuality - bugs * 5;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void resetAvailableMarket() {
        availableMarket = 10;
    }

    public int getAvailableMarket() {
        return availableMarket;
    }

    public void setAvailableMarket(int availableMarket) {
        this.availableMarket = availableMarket;
    }

    public void increaseBugs() {
        bugs += (int) (Math.random() * (1.0 - productQuality * 0.01) * features);
    }
}
