package com.codingame.game;

import static java.lang.Integer.parseInt;

import java.util.Collection;
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
    private final int maintenanceDevs;
    private final int competitiveSellers;
    private final Integer targetId;
    private final Player player;

    public static Action fromInput(String[] args, Company company, Collection<Integer> playerIds) throws InvalidAction {
        if (args.length != 5 && args.length != 6) {
            throw new InvalidAction("Input must contain 5 or 6 integers separated by space");
        }
        ActionBuilder builder = Action.builder();
        builder.devsToRecruit(getIntOrThrowWithMessage(args[0], "Devs to recruit must be an integer"));
        builder.sellersToRecruit(getIntOrThrowWithMessage(args[1],"Sellers to recruit must be an integer"));
        builder.managersToRecruit(getIntOrThrowWithMessage(args[2], "Managers to recruit must be an integer"));
        builder.maintenanceDevs(getIntOrThrowWithMessage(args[3], "Maintenance devs must be an integer"));
        builder.competitiveSellers(getIntOrThrowWithMessage(args[4], "Competitive sellers must be an integer"));
        if (args.length == 6) {
            builder.targetId(getIntOrThrowWithMessage(args[5],"Target id must be an integer"));
        }
        builder.player(company.getPlayer());

        Action action = builder.build();
        action.validate(company, playerIds);
        return builder.build();
    }

    public void validate(Company company, Collection<Integer> playerIds) throws InvalidAction {
        // count
        if (devsToRecruit + company.getFeatureDevs() < 0) {
            throw new InvalidAction("Impossible to fire more devs than you have");
        }
        if (sellersToRecruit + company.getFreeMarketSellers() < 0) {
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
        // maintenanceDevs
        int totalDevs = company.getTotalDevs() + devsToRecruit;
        if (maintenanceDevs < 0 || maintenanceDevs > totalDevs) {
            throw new InvalidAction("Maintenance devs must be between 0 and " + totalDevs);
        }
        // competitiveSellers
        int totalSellers = company.getTotalSellers() + sellersToRecruit;
        if (competitiveSellers < 0 || competitiveSellers > totalSellers) {
            throw new InvalidAction("Competitive sellers must be between 0 and " + totalSellers);
        }

        if (Objects.nonNull(targetId) && (targetId == player.getPlayerId() || !playerIds.contains(targetId))){
            throw new InvalidAction("Invalid target player id");
        }

    }

    private static int getIntOrThrowWithMessage(String value, String errorMessage) throws InvalidAction {
        if (Objects.isNull(value) || !value.matches("-?(0|[1-9]\\d*)")) {
            throw new InvalidAction(errorMessage);
        }
        return parseInt(value);
    }

}
