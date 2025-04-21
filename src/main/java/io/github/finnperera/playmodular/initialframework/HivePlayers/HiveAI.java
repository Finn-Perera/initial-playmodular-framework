package io.github.finnperera.playmodular.initialframework.HivePlayers;

import io.github.finnperera.playmodular.initialframework.*;

import java.util.HashMap;
import java.util.List;

/*
    Non immutable?
 */
public class HiveAI extends HivePlayer {

    AI<Hex, HiveTile> model;

    public HiveAI(HiveColour colour, AI<Hex, HiveTile> model) {
        super(colour);
        this.model = model;
    }

    public HiveAI(HashMap<HiveTileType, Integer> tiles, HiveColour colour, AI<Hex, HiveTile> model, String playerID) {
        super(tiles, colour, playerID);
        this.model = model;
    }

    public Move<Hex, HiveTile> getNextMove(HiveGame game, List<HiveMove> possibleMoves) {
        return model.getNextMove(game, possibleMoves);
    }

    public void setModel(AI<Hex, HiveTile> model) {
        this.model = model;
    }

    public AI<Hex, HiveTile> getAIModel() {
        return model;
    }

    @Override
    public HiveAI copy() {
        AI<Hex, HiveTile> modelCopy = model.copy(this);
        return new HiveAI(getTiles(), getColour(), modelCopy, getPlayerID());
    }

    @Override
    public boolean isAI() {
        return true;
    }
}
