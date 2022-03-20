package com.codingame.game;

import static java.util.stream.IntStream.range;

import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.endscreen.EndScreenModule;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Text;
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
    @Inject
    private GraphicEntityModule graphicEntityModule;
    @Inject
    private EndScreenModule endScreenModule;
    private static final int[] COLORS = new int[] {0xc43bb4, 0x3b46c4, 0x3bc462, 0xbfc43b};


    private final List<Company> companies = new ArrayList<>();
    private final Map<Integer, Company> companiesById = new HashMap<>();
    private final Random random = new Random();
    private static final int MAX_TURN = 100;
    private final Map<Integer, Text> scores = new HashMap<>();


    @Override
    public void init() {
        gameManager.getPlayerCount();
        gameManager.setMaxTurns(MAX_TURN * gameManager.getPlayerCount());

        gameManager.setFrameDuration(600);

        range(0, gameManager.getPlayerCount())
                .forEach(i -> {
                    Player p = gameManager.getPlayer(i);
                    p.setPlayerId(i);
                    Company c = new Company(p);
                    companies.add(c);
                    companiesById.put(i, c);
                    Text text = graphicEntityModule.createText("Player " + p.getPlayerId())
                            .setFontFamily("Lato")
                            .setFontSize(100)
                            .setX(10)
                            .setY(20 + 110 * p.getPlayerId())
                            .setFillColor(COLORS[p.getPlayerId()]);
                    scores.put(p.getPlayerId(), text);
                });
        random.setSeed(gameManager.getSeed());
    }

    @Override
    public void gameTurn(int turn) {
        Player player = gameManager.getPlayer(turn % gameManager.getPlayerCount());

        sendInput(player, turn);

        player.execute();

        try {
            Company company = companiesById.get(player.getPlayerId());
            Action action = player.getAction(company, companiesById.keySet());
            gameManager.addToGameSummary("Player %s played " + action.displayValue());

            // devs
            int newTotalDevs = company.getTotalDevs() + action.getDevsToRecruit();
            company.setFeatureDevs(newTotalDevs - action.getMaintenanceDevs());
            company.setMaintenanceDevs(action.getMaintenanceDevs());
            company.setInactiveDevs(0);
            // sellers
            int newTotalSellers = company.getTotalSellers() + action.getSellersToRecruit();
            company.setUnfilledMarketSellers(newTotalSellers - action.getCompetitiveSellers());
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
        if (turn == gameManager.getPlayerCount() * MAX_TURN) {
            endGame();
        }

    }

    private void calculateState(int turn) {

        range(0, gameManager.getPlayerCount()).forEach(i -> {
            Company company = companies.get(i);
            company.payDay(turn / gameManager.getPlayerCount());
            company.developFeatures(random);
            company.evaluateReputation();
        });
        evolveMarket();
        printResult();
        if (companies.stream().anyMatch(c -> c.getMarket() >= 800)) {
            gameManager.getPlayers().forEach(p -> p.setScore(companiesById.get(p.getPlayerId()).getMarket()));
            endGame();
        }
    }

    private void printResult() {
        gameManager.getPlayers()
                .forEach(p ->{
                    Text text = scores.get(p.getPlayerId());
                    text.setVisible(false);
                    text.setText("Player " + p.getPlayerId() + " : " + p.getScore());
                    graphicEntityModule.commitEntityState(0, text);
                });
    }

    private void evolveMarket() {
        companies.forEach(Company::prepareForSales);

        double featuresAverage = companies.stream().mapToInt(Company::getTotalFeatures).average().orElse(0);
        if (featuresAverage > 0) {
            double reputationSum = companies.stream().mapToInt(Company::getReputation).sum();

            // Free market
            double freeMarketSellersAverage =
                    companies.stream().mapToInt(Company::getUnfilledMarketSellers).average().orElse(0);
            int takenMarket = companies.stream().mapToInt(Company::getMarket).sum();
            if (freeMarketSellersAverage > 0) {
                int freeMarketAvailableForSale = Math.min(40 * gameManager.getPlayerCount(), 1000 - takenMarket);
                double averages = featuresAverage * freeMarketSellersAverage * reputationSum;
                companies.forEach(c -> c.takeUnfilledMarket(averages, freeMarketAvailableForSale));
            }

            // Competitive market
            double competitiveMarketSellersAverage =
                    companies.stream().mapToInt(Company::getCompetitiveMarketSellers).average().orElse(0);
            if (competitiveMarketSellersAverage > 0) {
                // competitive market with target
                List<Company> compsWithTarget = companies.stream()
                        .filter(c -> c.getCompetitiveScore() > 0 && Objects.nonNull(c.getTargetId()))
                        .unordered()
                        .collect(Collectors.toList());
                Collections.shuffle(compsWithTarget);
                compsWithTarget.forEach(comp -> comp.takeMarketFrom(companies.stream()
                        .filter(c -> c.getTargetId().equals(c.getPlayer().getPlayerId()))
                        .findAny()
                        .get(), companies.size() - 1));

                // competitive market without target
                List<Company> compsWithoutTarget = companies.stream()
                        .filter(c -> c.getCompetitiveScore() > 0 && Objects.isNull(c.getTargetId()))
                        .collect(Collectors.toList());
                Collections.shuffle(compsWithoutTarget);
                compsWithoutTarget.forEach(comp -> companies.forEach(comp::takeMarketFrom));
            }
        }
    }

    private void sendInput(Player player, int turn) {
        int playerCount = gameManager.getPlayerCount();
        player.sendInputLine(Integer.toString(player.getPlayerId()));
        player.sendInputLine(Integer.toString(playerCount));
        player.sendInputLine(Integer.toString(turn / playerCount));
        player.sendInputLine(Float.toString((float) Math.pow(1d / 0.95, turn - 1d)));

        Company company = companiesById.get(player.getPlayerId());
        player.sendInputLine(Integer.toString(company.getCash()));
        player.sendInputLine(Integer.toString(company.getTotalDevs()));
        player.sendInputLine(Integer.toString(company.getTotalSellers()));
        player.sendInputLine(Integer.toString(company.getManagers()));
        player.sendInputLine(Integer.toString(company.getTotalFeatures()));
        player.sendInputLine(Integer.toString(company.getTests()));
        player.sendInputLine(Integer.toString(company.getBugs()));
        range(0, playerCount).forEach(i -> {
            Company comp = companiesById.get(i);
            player.sendInputLine(Integer.toString(i));
            player.sendInputLine(Integer.toString(comp.getMarket()));
            player.sendInputLine(Integer.toString(comp.getReputation()));
        });
    }

    private void endGame() {
        gameManager.getPlayers().forEach(p -> p.setScore(companiesById.get(p.getPlayerId()).getMarket()));
        gameManager.endGame();
    }

    @Override
    public void onEnd() {
        endScreenModule.setScores(
                gameManager.getPlayers().stream().mapToInt(AbstractMultiplayerPlayer::getScore).toArray());
    }
}
