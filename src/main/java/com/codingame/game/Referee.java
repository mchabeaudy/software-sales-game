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
import java.util.Random;
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
        if (turn == gameManager.getPlayerCount() * 100 - 1) {
            gameManager.getPlayers().forEach(p -> p.setScore(companies.get(p).getMarket()));
            endGame();
        }

        Player player = gameManager.getPlayer(turn % gameManager.getPlayerCount());

        sendInput(player, turn);

        player.execute();

        try {
            Company company = companies.get(player);
            Action action = player.getAction(company);
            gameManager.addToGameSummary(
                    String.format("Player %s played (%d %d %d %d %d)", action.getPlayer().getNicknameToken(),
                            action.getDevsToRecruit(), action.getSellersToRecruit(), action.getManagersToRecruit(),
                            action.getDebugDevs(), action.getAggressiveSellers()));
            // devs
            int newTotalDevs = company.getTotalDevs() + action.getDevsToRecruit();
            company.setFeatureDevs(newTotalDevs - action.getDebugDevs());
            company.setMaintenanceDevs(action.getDebugDevs());
            company.setInactiveDevs(0);
            // sellers
            int newTotalSellers = company.getTotalSellers() + action.getSellersToRecruit();
            company.setFreeMarketSellers(newTotalSellers - action.getAggressiveSellers());
            company.setCompetitiveMarketSellers(action.getAggressiveSellers());
            company.setInactiveSellers(0);
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
    }

    private void evolveMarket() {
        List<Company> comps = new ArrayList<>(companies.values());

        // market is growing
        comps.forEach(c -> c.setMarket((int) Math.round(0.95 * c.getMarket())));

        double featuresAverage = comps.stream().mapToInt(Company::getTotalFeatures).average().orElse(0);
        if (featuresAverage > 0) {
            double reputationAverage = comps.stream().mapToDouble(Company::getReputation).average().orElse(0);

            // Free market
            double freeMarketSellersAverage =
                    comps.stream().mapToInt(Company::getFreeMarketSellers).average().orElse(0);
            int takenMarket = comps.stream().mapToInt(Company::getMarket).sum();
            if (freeMarketSellersAverage > 0) {
                int freeMarketAvailableForSale = Math.min(50 * gameManager.getPlayerCount(), 1000 - takenMarket);
                double averages = featuresAverage * freeMarketSellersAverage * reputationAverage;
                comps.forEach(c -> c.takeFreeMarket(averages, freeMarketAvailableForSale));
            }

            // Competitive market
            double competitiveMarketSellersAverage =
                    comps.stream().mapToInt(Company::getCompetitiveMarketSellers).average().orElse(0);
            Collections.shuffle(comps);
            if (competitiveMarketSellersAverage > 0) {
                range(0, comps.size() - 1).forEach(compId1 ->
                        range(compId1 + 1, comps.size()).forEach(compId2 ->
                                fight(comps.get(compId1), comps.get(compId2)))
                );
            }
        }
    }

    private void fight(Company comp1, Company comp2) {
        if (comp1.getCompetitiveScore() > comp2.getCompetitiveScore() && comp1.getCompetitiveMarketSellers() > 0) {
            int marketToTake =
                    Math.min((int) Math.min(10, 5d * comp1.getCompetitiveScore() / comp2.getCompetitiveScore()),
                            comp2.getMarket());
            comp1.addMarket(marketToTake);
            comp2.addMarket(-marketToTake);
        } else if (comp2.getCompetitiveScore() > comp1.getCompetitiveScore()
                && comp2.getCompetitiveMarketSellers() > 0) {
            int marketToTake =
                    Math.min((int) Math.max(10, 5d * comp2.getCompetitiveScore() / comp1.getCompetitiveScore()),
                            comp1.getMarket());
            comp2.addMarket(marketToTake);
            comp1.addMarket(-marketToTake);
        }
    }

    private void sendInput(Player player, int turn) {
        int playerCount = gameManager.getPlayerCount();
        player.sendInputLine(Integer.toString(player.getPlayerId()));
        player.sendInputLine(Integer.toString(playerCount));
        player.sendInputLine(Integer.toString(turn / playerCount));

        Company company = companies.get(player);
        player.sendInputLine(Integer.toString(company.getMarket()));
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
            player.sendInputLine(Integer.toString(comp.getFeatureDevs() + comp.getTotalEmployees()));
        });
    }

    private void endGame() {
        gameManager.endGame();
    }
}
