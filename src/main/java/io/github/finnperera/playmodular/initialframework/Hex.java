package io.github.finnperera.playmodular.initialframework;

/**
 * This class was created mostly from this blog:
 * <a href="https://www.redblobgames.com/grids/hexagons/implementation.html">...</a>
 * Which outlines the majority of functions a hexagonal engine requires.
 */
public class Hex {
    private int q, r, s;
    private static final Hex[] hexDirections =
            new Hex[]{new Hex(1, 0, -1), new Hex(1, -1,0), new Hex(0, -1, 1),
                    new Hex(-1, 0, 1), new Hex(-1, 1, 0), new Hex(0, 1, -1)};

    public Hex(int q, int r, int s) { // for cube
        assert(q + r + s == 0); // ensures valid coordinates
        this.q = q;
        this.r = r;
        this.s = s;
    }

    public boolean equals(Hex h) {
        return q == h.q && r == h.r && s == h.s;
    }

    public Hex hexAdd(Hex a, Hex b) {
        return new Hex(a.q + b.q, a.r + b.r, a.s + b.s);
    }

    public Hex hexSubtract(Hex a, Hex b) {
        return new Hex(a.q - b.q, a.r - b.r, a.s - b.s);
    }

    public Hex hexMultiply(Hex a, int k) {
        return new Hex(a.q * k, a.r * k, a.s * k);
    }

    public int hexLength(Hex hex) {
        return (Math.abs(hex.q) + Math.abs(hex.r) + Math.abs(hex.s)) / 2;
    }

    public int hexDistance(Hex a, Hex b) {
        return hexLength(hexSubtract(a, b));
    }

    // must be 0 to 5
    public Hex hexDirection(int direction) {
        assert(direction >= 0 && direction < 6);
        return hexDirections[direction];
    }

    public Hex hexNeighbour(Hex hex, int direction) {
        return hexAdd(hex, hexDirection(direction));
    }

}
