package com.codingame.game;

import static java.util.stream.IntStream.range;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class Referee extends AbstractReferee {

    @Inject
    private MultiplayerGameManager<Player> gameManager;
    private final Map<Player, Company> companies = new HashMap<>();
    private final Random random = new Random();

    @Override
    public void init() {
        gameManager.setMaxTurns(400);
        gameManager.getPlayerCount();
        range(0, gameManager.getPlayerCount())
                .forEach(i -> {
                    Player p = gameManager.getPlayer(i);
                    p.setPlayerId(i);
                    companies.put(p, new Company(p));
                });
        random.setSeed(gameManager.getSeed());
    }

    @Override
    public void gameTurn(int turn) {
        Player player = gameManager.getPlayer(turn % gameManager.getPlayerCount());

        sendInput(player, turn);

        player.execute();

        try {
            Company company = companies.get(player);
            Action action = player.getAction(company,
                    companies.keySet().stream().map(Player::getPlayerId).collect(Collectors.toSet()));
            gameManager.addToGameSummary(
                    String.format("Player %s played (%d %d %d %d %d)", action.getPlayer().getNicknameToken(),
                            action.getDevsToRecruit(), action.getSellersToRecruit(), action.getManagersToRecruit(),
                            action.getMaintenanceDevs(), action.getCompetitiveSellers()));
            // devs
            int newTotalDevs = company.getTotalDevs() + action.getDevsToRecruit();
            company.setFeatureDevs(newTotalDevs - action.getMaintenanceDevs());
            company.setMaintenanceDevs(action.getMaintenanceDevs());
            company.setInactiveDevs(0);
            // sellers
            int newTotalSellers = company.getTotalSellers() + action.getSellersToRecruit();
            company.setFreeMarketSellers(newTotalSellers - action.getCompetitiveSellers());
            company.setCompetitiveMarketSellers(action.getCompetitiveSellers());
            company.setInactiveSellers(0);
            // others
            company.setTargetId(action.getTargetId());
            company.addManagers(action.getManagersToRecruit());
            company.applyManagerRule(random);

            if (turn != 0 && turn % gameManager.getPlayerCount() == 0) {
                calculateState(turn);
            }

        } catch (TimeoutException e) {
            gameManager.addToGameSummary(GameManager.formatErrorMessage(player.getNicknameToken() + " timeout!"));
            player.deactivate(player.getNicknameToken() + " timeout!");
            player.setScore(-1);
            endGame();
        } catch (InvalidAction e) {
            player.deactivate(e.getMessage());
            player.setScore(-1);
            endGame();
        }
        if (turn == gameManager.getPlayerCount() * 100) {
            gameManager.getPlayers().forEach(p -> p.setScore(companies.get(p).getMarket()));
            endGame();
        }

    }

    private void calculateState(int turn) {

        range(0, gameManager.getPlayerCount()).forEach(i -> {
            Player player = gameManager.getPlayer(i);
            Company company = companies.get(player);
            company.payDay(turn / gameManager.getPlayerCount());
            company.developFeatures(random);
            company.evaluateReputation();
        });
        evolveMarket();
        if (companies.values().stream().anyMatch(c -> c.getMarket() >= 800)) {
            gameManager.getPlayers().forEach(p -> p.setScore(companies.get(p).getMarket()));
            endGame();
        }
    }

    private void evolveMarket() {
        List<Company> comps = new ArrayList<>(companies.values());

        // market is growing
        comps.forEach(c -> c.setMarket((int) Math.round(0.95 * c.getMarket())));

        double featuresAverage = comps.stream().mapToInt(Company::getTotalFeatures).average().orElse(0);
        if (featuresAverage > 0) {
            double reputationSum = comps.stream().mapToInt(Company::getReputation).sum();

            // Free market
            double freeMarketSellersAverage =
                    comps.stream().mapToInt(Company::getFreeMarketSellers).average().orElse(0);
            int takenMarket = comps.stream().mapToInt(Company::getMarket).sum();
            if (freeMarketSellersAverage > 0) {
                int freeMarketAvailableForSale = Math.min(40 * gameManager.getPlayerCount(), 1000 - takenMarket);
                double averages = featuresAverage * freeMarketSellersAverage * reputationSum;
                comps.forEach(c -> c.takeUnfilledMarket(averages, freeMarketAvailableForSale));
            }

            // Competitive market
            double competitiveMarketSellersAverage =
                    comps.stream().mapToInt(Company::getCompetitiveMarketSellers).average().orElse(0);
            if (competitiveMarketSellersAverage > 0) {
                // competitive market with target
                List<Company> compsWithTarget = comps.stream()
                        .filter(c -> c.getCompetitiveScore() > 0 && Objects.nonNull(c.getTargetId()))
                        .unordered()
                        .collect(Collectors.toList());
                Collections.shuffle(compsWithTarget);
                compsWithTarget.forEach(comp -> comp.takeMarketFrom(comps.stream()
                        .filter(c -> c.getTargetId().equals(c.getPlayer().getPlayerId()))
                        .findAny()
                        .get(), comps.size() - 1));

                // competitive market without target
                List<Company> compsWithoutTarget = comps.stream()
                        .filter(c -> c.getCompetitiveScore() > 0 && Objects.isNull(c.getTargetId()))
                        .collect(Collectors.toList());
                Collections.shuffle(compsWithoutTarget);
                compsWithoutTarget.forEach(comp -> comps.forEach(comp::fight));
            }
        }
    }

    private void sendInput(Player player, int turn) {
        int playerCount = gameManager.getPlayerCount();
        player.sendInputLine(Integer.toString(player.getPlayerId()));
        player.sendInputLine(Integer.toString(playerCount));
        player.sendInputLine(Integer.toString(turn / playerCount));
        player.sendInputLine(Float.toString((float) Math.pow(1.0 / 0.95, turn)));

        Company company = companies.get(player);
        player.sendInputLine(Integer.toString(company.getCash()));
        player.sendInputLine(Integer.toString(company.getTotalDevs()));
        player.sendInputLine(Integer.toString(company.getTotalSellers()));
        player.sendInputLine(Integer.toString(company.getManagers()));
        player.sendInputLine(Integer.toString(company.getTotalFeatures()));
        player.sendInputLine(Integer.toString(company.getTests()));
        player.sendInputLine(Integer.toString(company.getBugs()));
        range(0, playerCount).forEach(i -> {
            Company comp = companies.get(gameManager.getPlayer(i));
            player.sendInputLine(Integer.toString(i));
            player.sendInputLine(Integer.toString(comp.getMarket()));
            player.sendInputLine(Integer.toString(comp.getReputation()));
        });
    }

    private void endGame() {
        gameManager.endGame();
    }
}
