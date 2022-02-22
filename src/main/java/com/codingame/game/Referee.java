package com.codingame.game;

import static java.util.stream.IntStream.range;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;

public class Referee extends AbstractReferee {

    @Inject
    private MultiplayerGameManager<Player> gameManager;
    private Map<Player, Company> companies;

    @Override
    public void init() {
        gameManager.setMaxTurns(400);
        gameManager.getPlayerCount();
        companies = new HashMap<>();
        range(0, gameManager.getPlayerCount())
                .forEach(i -> {
                    Player p = gameManager.getPlayer(i);
                    p.setPlayerId(i);
                    companies.put(p, new Company());
                });
    }

    @Override
    public void gameTurn(int turn) {
        if (turn == 399) {
            gameManager.getPlayers().forEach(p -> p.setScore(companies.get(p).getMarket()));
            endGame();
        }
        Player player = gameManager.getPlayer(turn % gameManager.getPlayerCount());

        sendInput(player);

        player.execute();

        try {
            Company company = companies.get(player);
            Action action = player.getAction(company);
            gameManager.addToGameSummary(
                    String.format("Player %s played (%d %d %d %d %d)", action.getPlayer().getNicknameToken(),
                            action.getDevsHired(), action.getSalesHired(), action.getManagersHired(),
                            action.getDebugRate(), action.getSalesAggressivenessRate()));
            company.addDevs(action.getDevsHired());
            company.addSales(action.getSalesHired());
            company.addManagers(action.getManagersHired());
            company.setDebugRate(action.getDebugRate());
            company.setSalesAggressivenessRate(action.getSalesAggressivenessRate());
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
            company.payEmployees(turn, gameManager.getPlayerCount());
            company.developFeatures();
            company.increaseBugs();
        });
        growMarket();
    }

    private void growMarket() {
        Collection<Company> comps = companies.values();

        // simulate new market
        comps.forEach(c -> c.setMarket((int) (0.95 * c.getMarket())));

        // set sales market
        double saleAverage = comps.stream().mapToInt(Company::getSales).average().orElse(0);
        comps.forEach(c -> c.resetAvailableMarket(saleAverage));

        // calculate feature average and companies score
        double featureAverage = comps.stream().mapToInt(Company::getFeatures).average().orElse(0);
        comps.forEach(c -> c.buildScore(featureAverage));
        double scoreAverage = comps.stream().mapToInt(Company::getScore).average().orElse(0);

        // Free market
        int remainingFreeMarket = 100 - comps.stream().mapToInt(Company::getMarket).sum();
        AtomicInteger takenFreeMarket = new AtomicInteger(0);
        comps.stream()
                .sorted(Comparator.comparingInt(Company::getScore).reversed())
                .forEach(c -> c.takeFreeMarket(remainingFreeMarket, takenFreeMarket, scoreAverage));

        // Competitive market
        comps.stream()
                .filter(c -> c.getAvailableCompetitiveMarket() > 0)
                .sorted(Comparator.comparingInt(Company::getScore).reversed())
                .forEach(c -> c.takeCompetitiveMarket(comps, scoreAverage));
    }


    private void sendInput(Player player) {
        player.sendInputLine(Integer.toString(player.getPlayerId()));
        player.sendInputLine(Integer.toString(gameManager.getPlayerCount()));
        Company company = companies.get(player);
        player.sendInputLine(Integer.toString(company.getCash()));
        player.sendInputLine(Integer.toString(company.getDevs()));
        player.sendInputLine(Integer.toString(company.getSales()));
        player.sendInputLine(Integer.toString(company.getManagers()));
        player.sendInputLine(Integer.toString(company.getFeatures()));
        player.sendInputLine(Integer.toString(company.getBugs()));
        range(0, gameManager.getPlayerCount()).forEach(
                i -> player.sendInputLine(Integer.toString(companies.get(gameManager.getPlayer(i)).getMarket())));
    }

    private void endGame() {
        gameManager.endGame();
    }
}
