package com.codingame.game;

import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import java.util.Collection;

public class Player extends AbstractMultiplayerPlayer {

    private int playerId;

    @Override
    public int getExpectedOutputLines() {
        return 1;
    }

    public Action getAction(Company company, Collection<Integer> playerIds) throws TimeoutException, InvalidAction {
        return Action.fromInput(getOutputs().get(0).split(" "), company, playerIds);
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
}
