import static java.util.stream.IntStream.range;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Player1 {

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

        Player1 player = new Player1();

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

            System.err.println("features: " + features);
            System.err.println("bugs: " + bugs);
            System.err.println("cash: " + cash);
            range(0, playerCount).forEach(id -> {
                int marketShare = in.nextInt();
                //TODO taille de la company (nb employee)
                player.getMarketShares().put(id, marketShare);
                System.err.println("id: " + id + "   market share:" + marketShare);
            });

            Instruction instruction = player.getInstruction();
            System.out.println(
                    String.format("%d %d %d %d %d", instruction.getDevsToHire(), instruction.getSalesToHire(),
                            instruction.getManagersToHire(), instruction.getDebugRate(),
                            instruction.getSalesAggressivenessRate()));
        }
    }

    public Instruction getInstruction() {
        Instruction instruction = new Instruction();

        int available = Math.min(managers * 5 - devs - sales, managers * 2);
        int devsToHire = Math.min(10 - devs, available);
        int salesToHire = Math.min(6 - sales, available - devsToHire);
        int managerToHire = Math.min(3 - managers, 1);

        instruction.setDevsToHire(devsToHire);
        instruction.setSalesToHire(salesToHire);
        instruction.setManagersToHire(managerToHire);
        instruction.setDebugRate(20);
        if (marketShares.values().stream().mapToInt(Integer::valueOf).sum() > 90) {
            instruction.setSalesAggressivenessRate(100);
        } else {
            instruction.setSalesAggressivenessRate(0);
        }

        return instruction;
    }

    public static class Instruction {

        private int devsToHire;
        private int salesToHire;
        private int managersToHire;
        private int debugRate;
        private int salesAggressivenessRate;

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

        public int getSalesAggressivenessRate() {
            return salesAggressivenessRate;
        }

        public void setSalesAggressivenessRate(int salesAggressivenessRate) {
            this.salesAggressivenessRate = salesAggressivenessRate;
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
