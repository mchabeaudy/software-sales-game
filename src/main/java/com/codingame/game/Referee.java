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
import java.util.Objects;
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
                    String.format("Player %s played (%d %d %d %d)", action.getPlayer().getNicknameToken(),
                            action.getDevsHired(), action.getSalesHired(), action.getManagersHired(),
                            action.getDebugRate()));
            company.addDevs(action.getDevsHired());
            company.addSales(action.getSalesHired());
            company.addManagers(action.getManagersHired());
            company.setDebugRate(action.getDebugRate());
            if (turn != 0 && turn % gameManager.getPlayerCount() == 0) {
                calculateState();
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

    private void calculateState() {

        range(0, gameManager.getPlayerCount()).forEach(i -> {
            Player player = gameManager.getPlayer(i);
            Company company = companies.get(player);
            company.payEmployees();
            company.developFeatures();
            company.increaseBugs();
        });
        growMarket();
    }

    private void growMarket() {
        Collection<Company> comps = companies.values();
        comps.forEach(Company::resetAvailableMarket);
        int remainingEmptyMarket = 100 - comps.stream().mapToInt(Company::getMarket).sum();
        double saleAverage = comps.stream().mapToInt(Company::getSales).average().orElse(0);
        double featureAverage = comps.stream().mapToInt(Company::getFeatures).average().orElse(0);
        comps.forEach(c -> c.buildScore(saleAverage, featureAverage));
        int totalScore = comps.stream().mapToInt(Company::getScore).sum();
        int totalToTake = 5 * gameManager.getPlayerCount();
        if (remainingEmptyMarket >= 0) {
            AtomicInteger toTake = new AtomicInteger(Math.min(totalToTake, remainingEmptyMarket));
            comps.forEach(c -> c.addMarket(toTake.getAndAdd(-Math.min(toTake.get() * c.getScore() / totalScore, 10))));
        }
        comps.stream()
                .filter(c -> c.getAvailableMarket() > 0)
                .sorted(Comparator.comparingInt(Company::getScore).reversed())
                .forEach(comp -> {
                    Company weakest = getWeakest(comps, comp);
                    int compScore = comp.getScore();
                    if (Objects.nonNull(weakest)) {
                        int takenMarket = comp.getAvailableMarket() * compScore / (compScore + weakest.getMarket());
                        weakest.addMarket(-takenMarket);
                        comp.addMarket(takenMarket);
                    }
                });
    }

    private Company getWeakest(Collection<Company> comps, Company comp) {
        return comps.stream()
                .filter(c -> c.getMarket() > 0 && !c.equals(comp) && c.getScore() < comp.getScore()
                        && c.getMarket() > 10)
                .min(Comparator.comparingInt(Company::getScore))
                .orElse(null);
    }


    private void sendInput(Player player) {
        player.sendInputLine(Integer.toString(player.getPlayerId()));
        player.sendInputLine(Integer.toString(gameManager.getPlayerCount()));
        Company company = companies.get(player);
        player.sendInputLine(Integer.toString(company.getCash()));
        player.sendInputLine(Integer.toString(company.getDevs()));
        player.sendInputLine(Integer.toString(company.getSales()));
        player.sendInputLine(Integer.toString(company.getManagers()));
        player.sendInputLine(Integer.toString(company.getDebugRate()));
        player.sendInputLine(Integer.toString(company.getFeatures()));
        player.sendInputLine(Integer.toString(company.getBugs()));
        range(0, gameManager.getPlayerCount()).forEach(
                i -> player.sendInputLine(Integer.toString(companies.get(gameManager.getPlayer(i)).getMarket())));
    }

    private void endGame() {
        gameManager.endGame();
    }
}
