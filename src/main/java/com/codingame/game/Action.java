package com.codingame.game;

import static java.lang.Integer.parseInt;

import java.util.Objects;

public class Action {

    private final int devsHired;
    private final int salesHired;
    private final int managersHired;
    private final int debugRate;
    private final Player player;

    public Action(int devsHired, int salesHired, int managersHired, int debugRate, Player player) {
        this.devsHired = devsHired;
        this.salesHired = salesHired;
        this.managersHired = managersHired;
        this.debugRate = debugRate;
        this.player = player;
    }

    public static Action fromInput(String[] args, Company company, Player player) throws InvalidAction {
        if (args.length != 4) {
            throw new InvalidAction("Input must contain 4 integer separated by space");
        }

        // devs
        if (isNotInteger(args[0])) {
            throw new InvalidAction("Devs hired number must be an integer");
        }
        int devs = parseInt(args[0]);
        if (devs + company.getDevs() < 0) {
            throw new InvalidAction("Impossible to fire more devs than you have");
        }

        // sales
        if (isNotInteger(args[1])) {
            throw new InvalidAction("Sales hired number must be an integer");
        }
        int sales = parseInt(args[1]);
        if (sales + company.getSales() < 0) {
            throw new InvalidAction("Impossible to fire more sales than you have");
        }

        // managers
        if (isNotInteger(args[2])) {
            throw new InvalidAction("Managers hired number must be an integer");
        }
        int managers = parseInt(args[2]);
        if (managers + company.getManagers() < 0) {
            throw new InvalidAction("Impossible to fire more manager than you have");
        }

        // debug rate
        if (isNotInteger(args[3])) {
            throw new InvalidAction("Debug rate be an integer");
        }
        int debug = parseInt(args[3]);
        if (debug < 0 || debug > 100) {
            throw new InvalidAction("Debug rate must be between 0 and 100");
        }

        return new Action(devs, sales, managers, debug, player);
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
}
