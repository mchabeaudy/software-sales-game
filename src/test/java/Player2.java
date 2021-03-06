import static java.util.stream.IntStream.range;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player2 {

    public static final int MANAGER_COST = 20;
    public static final int DEV_COST = 10;
    public static final int SELLER_COST = 10;

    private int id;
    private int cash;
    private int devs;
    private int sellers;
    private int managers;
    private int features;
    private int bugs;
    private int tests;
    private int turn = 0;
    private int market;
    private int income;

    private final Map<Integer, Integer> marketShares = new HashMap<>();

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        Player2 player = new Player2();

        while (true) {

            player.setId(in.nextInt());
            int playerCount = in.nextInt();
            player.setTurn(in.nextInt());
            player.setIncome(in.nextInt());

            player.setCash(in.nextInt());
            player.setDevs(in.nextInt());
            player.setSellers(in.nextInt());
            player.setManagers(in.nextInt());
            player.setFeatures(in.nextInt());
            player.setTests(in.nextInt());
            player.setBugs(in.nextInt());

            System.err.println(player);

            range(0, playerCount).forEach(k -> {
                int startUpId = in.nextInt();
                int marketShare = in.nextInt();
                int reputation = in.nextInt();
                player.getMarketShares().put(startUpId, marketShare);
                System.err.println("id: " + startUpId + "   market share:" + marketShare);
            });

            Instruction instruction = player.getInstruction();
            System.out.println(
                    String.format("%d %d %d %d %d", instruction.getDevsToRecruit(), instruction.getSellersToRecruit(),
                            instruction.getManagersToRecruit(), instruction.getMaintenanceDevs(),
                            instruction.getCompetitiveSellers()));
        }
    }

    public Instruction getInstruction() {
        Instruction instruction = new Instruction();

        int devsToRecruit = 0;
        int sellersToRecruit = 0;
        int managersToRecruit = 0;
        double cost = getCost(managersToRecruit, devsToRecruit, sellersToRecruit);
        int playerCount = marketShares.size();
        int market = marketShares.get(id);

        if (managers < 5 || ((devs + sellers) / 4 > managers)) {
            managersToRecruit = 1;
        }
        int available = Math.min(managers * 10 - devs - sellers, managers * 2);

        while ((getCost(managersToRecruit, devsToRecruit, sellersToRecruit) < income - DEV_COST || devs + sellers < 20)
                && available != 0) {
            available--;
            if (devsToRecruit + devs > sellersToRecruit + sellers) {
                sellersToRecruit++;
            } else {
                devsToRecruit++;
            }
        }

        instruction.setDevsToRecruit(devsToRecruit);
        instruction.setSellersToRecruit(sellersToRecruit);
        instruction.setManagersToRecruit(managersToRecruit);
        int md = Math.min(bugs, devs + devsToRecruit);
        int maintenanceDevs = Math.max(md, (devs + devsToRecruit) / 2);
        int competitiveSellers = (sellers + sellersToRecruit) / 2;
        instruction.setMaintenanceDevs(maintenanceDevs);
        instruction.setCompetitiveSellers(competitiveSellers);

        return instruction;
    }

    private int getCost(int managersToRecruit, int devsToRecruit, int sellersToRecruit) {
        return (managers + managersToRecruit) * MANAGER_COST + (devs + devsToRecruit) * DEV_COST
                + (sellers + sellersToRecruit) * SELLER_COST;
    }

    public static class Instruction {

        private int devsToRecruit;
        private int sellersToRecruit;
        private int managersToRecruit;
        private int maintenanceDevs;
        private int competitiveSellers;

        public int getDevsToRecruit() {
            return devsToRecruit;
        }

        public void setDevsToRecruit(int devsToRecruit) {
            this.devsToRecruit = devsToRecruit;
        }

        public int getSellersToRecruit() {
            return sellersToRecruit;
        }

        public void setSellersToRecruit(int sellersToRecruit) {
            this.sellersToRecruit = sellersToRecruit;
        }

        public int getManagersToRecruit() {
            return managersToRecruit;
        }

        public void setManagersToRecruit(int managersToRecruit) {
            this.managersToRecruit = managersToRecruit;
        }

        public int getMaintenanceDevs() {
            return maintenanceDevs;
        }

        public void setMaintenanceDevs(int maintenanceDevs) {
            this.maintenanceDevs = maintenanceDevs;
        }

        public int getCompetitiveSellers() {
            return competitiveSellers;
        }

        public void setCompetitiveSellers(int competitiveSellers) {
            this.competitiveSellers = competitiveSellers;
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

    public int getSellers() {
        return sellers;
    }

    public void setSellers(int sellers) {
        this.sellers = sellers;
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

    public int getTests() {
        return tests;
    }

    public void setTests(int tests) {
        this.tests = tests;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getMarket() {
        return market;
    }

    public void setMarket(int market) {
        this.market = market;
    }

    public Map<Integer, Integer> getMarketShares() {
        return marketShares;
    }

    @Override
    public String toString() {
        return "Player{" +
                "cash=" + cash +
                ", devs=" + devs +
                ", sellers=" + sellers +
                ", managers=" + managers +
                ", features=" + features +
                ", bugs=" + bugs +
                ", market=" + market +
                '}';
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }
}
