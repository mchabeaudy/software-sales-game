import static java.util.stream.IntStream.range;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player2 {

    public static final int MANAGER_COST = 15;
    public static final int DEV_COST = 10;
    public static final int SELLER_COST = 10;

    private int id;
    private int cash;
    private int devs;
    private int sales;
    private int managers;
    private int features;
    private int bugs;
    private int turn = 0;
    private int market;
    private int test;

    private final Map<Integer, Integer> marketShares = new HashMap<>();

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        Player2 player = new Player2();

        while (true) {

            player.setId(in.nextInt());
            int playerCount = in.nextInt();
            player.setTurn(in.nextInt());

            player.setMarket(in.nextInt());
            player.setCash(in.nextInt());
            player.setDevs(in.nextInt());
            player.setSales(in.nextInt());
            player.setManagers(in.nextInt());
            player.setFeatures(in.nextInt());
            player.setTest(in.nextInt());
            player.setBugs(in.nextInt());

            System.err.println(player);

            range(0, playerCount).forEach(k -> {
                int startUpId = in.nextInt();
                int marketShare = in.nextInt();
                int employeesCount = in.nextInt();
                player.getMarketShares().put(startUpId, marketShare);
                System.err.println("id: " + startUpId + "   market share:" + marketShare);
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

        double cost = managers * MANAGER_COST + devs * DEV_COST + sales * SELLER_COST;
        int playerCount = marketShares.size();
        int market = marketShares.get(id);
        double revenue = market * Math.pow(1d / 0.95, turn * 1d / playerCount);

        int managerToHire;
        if (cost < 0.8 * revenue || managers < 3) {
            managerToHire = 1;
        } else {
            managerToHire = 0;
        }
        int available = Math.min(managers * 5 - devs - sales, managers * 2);
        int devsToHire, salesToHire;
        if (devs > sales) {
            salesToHire = Math.max(0, Math.min(50 - sales, available));
            devsToHire = Math.max(0, Math.min(50 - devs, available - salesToHire));
        } else {
            devsToHire = Math.max(0, Math.min(50 - devs, available));
            salesToHire = Math.max(0, Math.min(50 - sales, available - devsToHire));
        }

        instruction.setDevsToHire(devsToHire);
        instruction.setSalesToHire(salesToHire);
        instruction.setManagersToHire(managerToHire);
        instruction.setDebugRate(Math.min(devs, bugs + 1));

        if (marketShares.values().stream().mapToInt(Integer::valueOf).sum() > 90) {
            instruction.setSalesAggressivenessRate(sales - sales / 10);
        } else {
            instruction.setSalesAggressivenessRate(0);
        }

        return instruction;
    }

    @Getter
    @Setter
    public static class Instruction {

        private int devsToHire;
        private int salesToHire;
        private int managersToHire;
        private int debugRate;
        private int salesAggressivenessRate;

    }


    @Override
    public String toString() {
        return "Player{" +
                "cash=" + cash +
                ", devs=" + devs +
                ", sales=" + sales +
                ", managers=" + managers +
                ", features=" + features +
                ", bugs=" + bugs +
                ", market=" + market +
                '}';
    }
}
