package io.github.finnperera.playmodular.initialframework;

import java.util.Map;

public interface LoggableGameConfig {
    String getGameName();
    int getExpectedPlayers();
    Map<String, Object> toLogMap();
}
