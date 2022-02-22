package com.codingame.game.boss;

import static java.util.stream.IntStream.range;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Player {

    private int id;
    private int cash;
    private int devs;
    private int sales;
    private int managers;
    private int features;
    private int bugs;
    private final Map<Integer, Integer> marketShares = new HashMap<>();

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        Player player = new Player();

        while (true) {
            int playerId = in.nextInt();
            int playerCount = in.nextInt();
            int cash = in.nextInt();
            int devs = in.nextInt();
            int sales = in.nextInt();
            int managers = in.nextInt();
            int features = in.nextInt();
            int bugs = in.nextInt();

            player.setId(playerId);
            player.setCash(cash);
            player.setDevs(devs);
            player.setSales(sales);
            player.setManagers(managers);
            player.setFeatures(features);
            player.setBugs(bugs);

            range(0, playerCount).forEach(id -> {
                int marketShare = in.nextInt();
                player.getMarketShares().put(id, marketShare);
            });

            Instruction instruction = player.getInstruction();
            System.out.println(String.format("%d %d %d %d", instruction.getDevsToHire(), instruction.getSalesToHire(),
                    instruction.getManagersToHire(), instruction.getDebugRate()));
        }
    }

    public Instruction getInstruction() {
        Instruction instruction = new Instruction();

        instruction.setDevsToHire(0);
        instruction.setManagersToHire(0);
        instruction.setSalesToHire(0);
        instruction.setDebugRate(0);

        //TODO develop your algo there

        return instruction;
    }

    class Instruction {

        private int devsToHire;
        private int salesToHire;
        private int managersToHire;
        private int debugRate;

        public int getDevsToHire() {
            return devsToHire;
        }

        public void setDevsToHire(int devsToHire) {
            this.devsToHire = devsToHire;
        }

        public int getSalesToHire() {
            return salesToHire;
        }

        public void setSalesToHire(int salesToHire) {
            this.salesToHire = salesToHire;
        }

        public int getManagersToHire() {
            return managersToHire;
        }

        public void setManagersToHire(int managersToHire) {
            this.managersToHire = managersToHire;
        }

        public int getDebugRate() {
            return debugRate;
        }

        public void setDebugRate(int debugRate) {
            this.debugRate = debugRate;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCash() {
        return cash;
    }

    public void setCash(int cash) {
        this.cash = cash;
    }

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

    public int getFeatures() {
        return features;
    }

    public void setFeatures(int features) {
        this.features = features;
    }

    public int getBugs() {
        return bugs;
    }

    public void setBugs(int bugs) {
        this.bugs = bugs;
    }

    public Map<Integer, Integer> getMarketShares() {
        return marketShares;
    }
}
