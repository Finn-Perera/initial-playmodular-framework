package io.github.finnperera.playmodular.initialframework.AIModels;

import io.github.finnperera.playmodular.initialframework.AI;
import io.github.finnperera.playmodular.initialframework.Game;
import io.github.finnperera.playmodular.initialframework.Move;
import io.github.finnperera.playmodular.initialframework.Player;

import java.util.List;
import java.util.Random;

public class RandomModel<P, T> implements AI<P, T> {
    Random random = new Random();
    @Override
    public Move<P, T> getNextMove(Game<P, T> game, List<? extends Move<P, T>> moves) {
        return moves.get(random.nextInt(moves.size()));
    }

    @Override
    public AI<P, T> copy(Player newPlayer) {
        return new RandomModel<>();
    }
}
