package com.codingame.game;

import static java.lang.Integer.parseInt;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class Action {

    private final int devsToRecruit;
    private final int sellersToRecruit;
    private final int managersToRecruit;
    private final int debugDevs;
    private final int aggressiveSellers;
    private final Player player;

    public static Action fromInput(String[] args, Company company, Player player) throws InvalidAction {
        if (args.length != 5) {
            throw new InvalidAction("Input must contain 5 integer separated by space");
        }
        ActionBuilder builder = Action.builder();
        // parsing input
        // devs
        if (isNotInteger(args[0])) {
            throw new InvalidAction("Devs to recruit must be an integer");
        }
        builder.devsToRecruit(parseInt(args[0]));
        // sellers
        if (isNotInteger(args[1])) {
            throw new InvalidAction("Sellers to recruit must be an integer");
        }
        builder.sellersToRecruit(parseInt(args[1]));
        // managers
        if (isNotInteger(args[2])) {
            throw new InvalidAction("Managers to recruit must be an integer");
        }
        builder.managersToRecruit(parseInt(args[2]));
        // debugDevs
        if (isNotInteger(args[3])) {
            throw new InvalidAction("Debug devs must be an integer");
        }
        builder.debugDevs(parseInt(args[3]));
        // aggressiveSellers
        if (isNotInteger(args[4])) {
            throw new InvalidAction("Aggressive sellers must be an integer");
        }
        builder.aggressiveSellers(parseInt(args[4]));
        builder.player(player);

        Action action = builder.build();
        action.validate(company);
        return builder.build();
    }

    public void validate(Company company) throws InvalidAction {
        // count
        if (devsToRecruit + company.getDevs() < 0) {
            throw new InvalidAction("Impossible to fire more devs than you have");
        }
        if (sellersToRecruit + company.getSellers() < 0) {
            throw new InvalidAction("Impossible to fire more sellers than you have");
        }
        if (managersToRecruit + company.getManagers() < 0) {
            throw new InvalidAction("Impossible to fire more manager than you have");
        }
        // capacity
        if (Math.abs(devsToRecruit) + Math.abs(sellersToRecruit) > 2 * company.getManagers()) {
            throw new InvalidAction("Impossible to recruit more than your capacity: " + 2 * company.getManagers());
        }
        if (Math.abs(managersToRecruit) > 1) {
            throw new InvalidAction("Impossible to recruit or fire more than one manager");
        }
        // debugDevs
        int totalDevs = company.getTotalDevs() + devsToRecruit;
        if (debugDevs < 0 || debugDevs > totalDevs) {
            throw new InvalidAction("Debug devs must be between 0 and " + totalDevs);
        }
        // aggressiveSellers
        int totalSellers = company.getTotalSellers() + sellersToRecruit;
        if (aggressiveSellers < 0 || aggressiveSellers > totalSellers) {
            throw new InvalidAction("Agressive sellers must be between 0 and " + totalSellers);
        }

    }

    private static boolean isNotInteger(String value) {
        if (Objects.isNull(value)) {
            return true;
        }
        return !value.matches("-?(0|[1-9]\\d*)");
    }

}
