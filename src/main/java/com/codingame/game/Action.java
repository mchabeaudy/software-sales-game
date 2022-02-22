package com.codingame.game;

import static java.lang.Integer.parseInt;

import java.util.Objects;

public class Action {

    private final int devsHired;
    private final int salesHired;
    private final int managersHired;
    private final int debugRate;
    private final int salesAggressivenessRate;
    private final Player player;

    public Action(int devsHired, int salesHired, int managersHired, int debugRate, int salesAggressivenessRate, Player player) {
        this.devsHired = devsHired;
        this.salesHired = salesHired;
        this.managersHired = managersHired;
        this.debugRate = debugRate;
        this.player = player;
        this.salesAggressivenessRate = salesAggressivenessRate;
    }

    public static Action fromInput(String[] args, Company company, Player player) throws InvalidAction {
        if (args.length != 5) {
            throw new InvalidAction("Input must contain 4 integer separated by space");
        }

        // devs
        if (isNotInteger(args[0])) {
            throw new InvalidAction("Devs to recruit must be an integer");
        }
        int devs = parseInt(args[0]);
        if (devs + company.getDevs() < 0) {
            throw new InvalidAction("Impossible to fire more devs than you have");
        }
        if (devs > 2 * company.getManagers()) {
            throw new InvalidAction("Impossible to recruit more developers than twice the number of your managers");
        }
        if (devs < -2 * company.getManagers()) {
            throw new InvalidAction("Impossible to fire more developers than twice the number of your managers");
        }

        // sales
        if (isNotInteger(args[1])) {
            throw new InvalidAction("Sales to recruit must be an integer");
        }
        int sales = parseInt(args[1]);
        if (sales + company.getSales() < 0) {
            throw new InvalidAction("Impossible to fire more sales than you have");
        }
        if (sales > 2 * company.getManagers()) {
            throw new InvalidAction("Impossible to recruit more sales than twice the number of your managers");
        }
        if (sales < -2 * company.getManagers()) {
            throw new InvalidAction("Impossible to fire more sales than twice the number of your managers");
        }
        if (company.getDevs() + company.getSales() + devs + sales > 5 * company.getManagers()) {
            throw new InvalidAction("Impossible to recruit more resources than five times the number of your managers");
        }

        // managers
        if (isNotInteger(args[2])) {
            throw new InvalidAction("Managers hired number must be an integer");
        }
        int managers = parseInt(args[2]);
        if (managers + company.getManagers() < 0) {
            throw new InvalidAction("Impossible to fire more manager than you have");
        }
        if (managers > 1) {
            throw new InvalidAction("Impossible to recruit more than one manager");
        }
        if (managers < -1) {
            throw new InvalidAction("Impossible to fire more than one manager");
        }

        // debug rate
        if (isNotInteger(args[3])) {
            throw new InvalidAction("Debug rate be an integer");
        }
        int debug = parseInt(args[3]);
        if (debug < 0 || debug > 100) {
            throw new InvalidAction("Debug rate must be between 0 and 100");
        }

        // sales aggressiveness rate
        if (isNotInteger(args[4])) {
            throw new InvalidAction("Sales aggressiveness rate rate be an integer");
        }
        int salesAggressivenessRate = parseInt(args[4]);
        if (salesAggressivenessRate < 0 || salesAggressivenessRate > 100) {
            throw new InvalidAction("Sales aggressiveness rate must be between 0 and 100");
        }

        return new Action(devs, sales, managers, debug, salesAggressivenessRate, player);
    }

    private static boolean isNotInteger(String value) {
        if (Objects.isNull(value)) {
            return true;
        }
        return !value.matches("-?(0|[1-9]\\d*)");
    }

    public int getDevsHired() {
        return devsHired;
    }

    public int getSalesHired() {
        return salesHired;
    }

    public int getManagersHired() {
        return managersHired;
    }

    public int getDebugRate() {
        return debugRate;
    }

    public Player getPlayer() {
        return player;
    }

    public int getSalesAggressivenessRate() {
        return salesAggressivenessRate;
    }
}
