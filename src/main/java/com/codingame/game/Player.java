package com.codingame.game;

import com.codingame.gameengine.core.AbstractMultiplayerPlayer;

public class Player extends AbstractMultiplayerPlayer {

    private int playerId;

    @Override
    public int getExpectedOutputLines() {
        return 1;
    }

    public Action getAction(Company company) throws TimeoutException, InvalidAction
    {
        return Action.fromInput(getOutputs().get(0).split(" "), company, this);
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
}
