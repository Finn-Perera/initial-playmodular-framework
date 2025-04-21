package io.github.finnperera.playmodular.initialframework;

public interface Player {
    String getPlayerID();
    boolean isAI();
    default AI<?, ?> getAIModel() {
        return null;
    }
    Player copy();
}
