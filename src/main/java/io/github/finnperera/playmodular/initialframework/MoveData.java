package io.github.finnperera.playmodular.initialframework;

import java.time.Duration;

public record MoveData(Duration timeTaken, Move<?, ?> move) {}
