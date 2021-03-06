package com.codingame.game;

import static java.util.stream.IntStream.range;

import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.endscreen.EndScreenModule;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Rectangle;
import com.codingame.gameengine.module.entities.Text;
import java.math.BigDecimal;
import java.math.BigInteger;
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
    private static final int[] COLORS = new int[] {0xc43bb4, 0x10b0b3, 0x3bc462, 0xbfc43b};

    private static final int[] X_ORIGINS = new int[] {10, 960, 10, 960};
    private static final int[] Y_ORIGINS = new int[] {10, 10, 550, 550};
    private static final int GAP = 110;
    private static final int HEIGHT = 100;

    private final List<Company> companies = new ArrayList<>();
    private final Map<Integer, Company> companiesById = new HashMap<>();
    private final Random random = new Random();
    private static final int MAX_TURN = 100;
    private final Map<Integer, Text> scores = new HashMap<>();
    private final Map<Integer, Text> reputations = new HashMap<>();
    private final Map<Integer, Rectangle> bars = new HashMap<>();

    private int playerCount;
    private int turn;


    @Override
    public void init() {
        playerCount = gameManager.getPlayerCount();
        gameManager.setMaxTurns(MAX_TURN * playerCount);
        Constants.resetProb(random);
        range(0, playerCount)
                .forEach(i -> {
                    Player p = gameManager.getPlayer(i);
                    p.setPlayerId(i);
                    Company c = new Company(p);
                    companies.add(c);
                    companiesById.put(i, c);
                    graphicEntityModule.createText(p.getNicknameToken())
                            .setFontFamily("Lato")
                            .setFontSize(HEIGHT)
                            .setX(X_ORIGINS[p.getPlayerId()])
                            .setY(Y_ORIGINS[p.getPlayerId()])
                            .setFillColor(COLORS[p.getPlayerId()]);
                    Text reputation = graphicEntityModule.createText("Reputation : 0")
                            .setFontFamily("Lato")
                            .setFontSize(HEIGHT)
                            .setX(X_ORIGINS[p.getPlayerId()])
                            .setY(Y_ORIGINS[p.getPlayerId()] + GAP)
                            .setFillColor(COLORS[p.getPlayerId()]);
                    Text score = graphicEntityModule.createText("Market share : 0")
                            .setFontFamily("Lato")
                            .setFontSize(HEIGHT)
                            .setX(X_ORIGINS[p.getPlayerId()])
                            .setY(Y_ORIGINS[p.getPlayerId()] + 2 * GAP)
                            .setFillColor(COLORS[p.getPlayerId()]);
                    Rectangle rectangle = graphicEntityModule.createRectangle()
                            .setX(X_ORIGINS[p.getPlayerId()])
                            .setY(Y_ORIGINS[p.getPlayerId()] + 3 * GAP)
                            .setHeight(HEIGHT)
                            .setWidth(0)
                            .setFillColor(COLORS[p.getPlayerId()]);
                    bars.put(p.getPlayerId(), rectangle);
                    scores.put(p.getPlayerId(), score);
                    reputations.put(p.getPlayerId(), reputation);
                });
        random.setSeed(gameManager.getSeed());
    }

    @Override
    public void gameTurn(int turn) {
        for (Player p : gameManager.getActivePlayers()) {
            sendInput(p, turn);
            p.execute();
        }
        this.turn=turn;

        for (Player player : gameManager.getActivePlayers()) {
            try {
                Company company = companiesById.get(player.getPlayerId());
                Action action = player.getAction(company, companiesById.keySet());
                gameManager.addToGameSummary(
                        String.format("Player %s played %s", player.getNicknameToken(), action.displayValue()));

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


            } catch (TimeoutException e) {
                deactivatePlayer(player, "timeout!");
            } catch (InvalidAction e) {
                deactivatePlayer(player, e.getMessage());
            }
        }
        calculateState(turn);
        if (turn == MAX_TURN) {
            endGame();
        }

    }

    private void calculateState(int turn) {
        range(0, playerCount).forEach(i -> {
            Company company = companies.get(i);
            company.payDay(turn);
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
                .forEach(p -> {
                    Company c = companies.get(p.getPlayerId());
                    scores.get(p.getPlayerId())
                            .setText("Market share : " + new BigDecimal(BigInteger.valueOf(c.getMarket()), 1));
                    reputations.get(p.getPlayerId()).setText("Reputation : " + c.getReputation());
                    bars.get(p.getPlayerId()).setWidth(c.getMarket() * 95 / 100);
                });
    }

    private void evolveMarket() {
        companies.forEach(Company::prepareForSales);

        List<Company> activeCompanies = gameManager.getActivePlayers().stream()
                .map(p -> companiesById.get(p.getPlayerId()))
                .collect(Collectors.toList());

        // Free market
        int unfilledMarketScoreSum = activeCompanies.stream()
                .mapToInt(Company::getUnfilledMarketScore)
                .sum();
        int takenMarket = companies.stream().mapToInt(Company::getMarket).sum();
        if (unfilledMarketScoreSum > 0) {
            int freeMarketAvailableForSale = Math.min(30 * playerCount, 1000 - takenMarket);
            activeCompanies.forEach(c -> c.takeUnfilledMarket(unfilledMarketScoreSum, freeMarketAvailableForSale));
        }

        // Competitive market
        // With target
        List<Company> compsWithTarget = activeCompanies.stream()
                .filter(c -> c.getCompetitiveScore() > 0 && Objects.nonNull(c.getTargetId()))
                .collect(Collectors.toList());
        Collections.shuffle(compsWithTarget);
        compsWithTarget.forEach(comp -> companies.stream()
                .filter(c -> Objects.equals(comp.getTargetId(), c.getPlayer().getPlayerId()))
                .findAny()
                .ifPresent(c -> comp.takeMarketFrom(c, companies.size() - 1)));

        // Without target
        List<Company> compsWithoutTarget = activeCompanies.stream()
                .filter(c -> c.getCompetitiveScore() > 0 && Objects.isNull(c.getTargetId()))
                .collect(Collectors.toList());
        Collections.shuffle(compsWithoutTarget);
        compsWithoutTarget.forEach(comp -> companies.forEach(comp::takeMarketFrom));

        activeCompanies.forEach(Company::processFreeMarket);
    }

    private void sendInput(Player player, int turn) {
        player.sendInputLine(Integer.toString(player.getPlayerId()));
        player.sendInputLine(Integer.toString(playerCount));
        player.sendInputLine(Integer.toString(turn));

        Company company = companiesById.get(player.getPlayerId());
        player.sendInputLine(Integer.toString((int) (company.getMarket() * Math.pow(1.0 / 0.95, (turn - 1)))));
        player.sendInputLine(Integer.toString(company.getCash()));
        player.sendInputLine(Integer.toString(company.getTotalDevs()));
        player.sendInputLine(Integer.toString(company.getTotalSellers()));
        player.sendInputLine(Integer.toString(company.getManagers()));
        player.sendInputLine(Integer.toString(company.getTotalFeatures()));
        player.sendInputLine(Integer.toString(company.getTests()));
        player.sendInputLine(Integer.toString(company.getBugs()));
        range(0, playerCount).forEach(i -> {
            Company comp = companiesById.get(i);
            player.sendInputLine(i + " " + comp.getMarket() + " " + comp.getReputation());
        });
    }

    private void endGame() {
        gameManager.getActivePlayers().forEach(p -> p.setScore(companiesById.get(p.getPlayerId()).getMarket()));
        gameManager.endGame();
    }

    @Override
    public void onEnd() {
        endScreenModule.setScores(
                gameManager.getPlayers().stream().mapToInt(AbstractMultiplayerPlayer::getScore).toArray());
    }

    private void deactivatePlayer(Player player, String message) {
        gameManager.addToGameSummary(GameManager.formatErrorMessage(player.getNicknameToken() + " - " + message));
        player.deactivate(message);
        companiesById.get(player.getPlayerId()).deactivate();
        player.setScore(-1);
    }
}
