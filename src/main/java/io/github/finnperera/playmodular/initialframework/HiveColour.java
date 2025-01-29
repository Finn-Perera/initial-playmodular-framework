package io.github.finnperera.playmodular.initialframework;

public enum HiveColour {
    WHITE,
    BLACK;

    public HiveColour oppositeColour() {
        return this == WHITE ? BLACK : WHITE;
    }
}




