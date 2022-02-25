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
import java.util.Random;
import javax.inject.Inject;

public class Referee extends AbstractReferee {

    @Inject
    private MultiplayerGameManager<Player> gameManager;
    private Map<Player, Company> companies;
    private Random random = new Random();

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
            company.setDevs(newTotalDevs - action.getDebugDevs());
            company.setDebugDevs(action.getDebugDevs());
            // sellers
            int newTotalSellers = company.getTotalSellers() + action.getSellersToRecruit();
            company.setSellers(newTotalSellers - action.getAggressiveSellers());
            company.setAgressiveSellers(action.getAggressiveSellers());
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
            company.evaluateScore();
        });
        evolveMarket();
    }

    private void evolveMarket() {
        Collection<Company> comps = companies.values();

        // market is growing
        comps.forEach(c -> c.setMarket((int) Math.round(0.95 * c.getMarket())));

        // set available sales market
        int totalSellers = comps.stream().mapToInt(Company::getTotalSellers).sum();
        comps.forEach(c -> c.resetAvailableMarkets(totalSellers));

        // Free market
        double totalFreeMarketScore = comps.stream().mapToDouble(c -> c.getScore() * c.getSellers()).sum();
        int totalFreeMarketSellers = comps.stream().mapToInt(Company::getSellers).sum();
        double freeMarketAvailable = 1000d - comps.stream().mapToInt(Company::getMarket).sum();
        int freeMarketAvailableForSale = (int) Math.round(
                Math.min(50.0 * gameManager.getPlayerCount() * totalFreeMarketSellers / totalSellers,
                        freeMarketAvailable / gameManager.getPlayerCount()));
        comps.stream()
                .sorted(Comparator.comparingDouble(Company::getScore).reversed())
                .forEach(c -> c.addMarket(
                        (int) (freeMarketAvailableForSale * c.getSellers() * c.getScore() / totalFreeMarketScore)));

        double totalCompetitiveMarketScore =
                comps.stream().mapToDouble(c -> c.getScore() * c.getAgressiveSellers()).sum();
        int totalCompetitiveMarketSellers = comps.stream().mapToInt(Company::getAgressiveSellers).sum();
        // Competitive market
        comps.stream()
                .sorted(Comparator.comparingDouble(Company::getScore).reversed())
                .forEach(comp -> {
                    Company toAttack = comps.stream()
                            .filter(c -> c.getMarket() > 0 && c.getScore() < comp.getScore())
                            .max(Comparator.comparingInt(Company::getMarket))
                            .orElse(null);
                    if (toAttack != null) {
                        int takenMarket = (int) Math.round(Math.min(toAttack.getMarket(),
                                50.0 * comp.getAgressiveSellers() * comp.getScore() / totalCompetitiveMarketScore
                                        * totalCompetitiveMarketSellers / totalSellers));
                        toAttack.addMarket(-takenMarket);
                        comp.addMarket(takenMarket);
                    }
                });
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
        player.sendInputLine(Integer.toString(company.getFeatures()));
        player.sendInputLine(Integer.toString(company.getTests()));
        player.sendInputLine(Integer.toString(company.getBugs()));
        range(0, playerCount).forEach(i -> {
            Company comp = companies.get(gameManager.getPlayer(i));
            player.sendInputLine(Integer.toString(i));
            player.sendInputLine(Integer.toString(comp.getMarket()));
            player.sendInputLine(Integer.toString(comp.getDevs() + comp.getTotalEmployees()));
        });
    }

    private void endGame() {
        gameManager.endGame();
    }
}
