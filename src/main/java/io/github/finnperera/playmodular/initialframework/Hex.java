package io.github.finnperera.playmodular.initialframework;

import java.util.*;

/**
 * This class was created mostly from this blog:
 * <a href="https://www.redblobgames.com/grids/hexagons/implementation.html">...</a>
 * Which outlines the majority of functions a hexagonal engine requires.
 */
public class Hex implements Position, LoggableComponent {
    private static final Hex[] hexDirections =
            new Hex[]{new Hex(1, 0, -1), new Hex(1, -1, 0), new Hex(0, -1, 1),
                    new Hex(-1, 0, 1), new Hex(-1, 1, 0), new Hex(0, 1, -1)};
    private int q, r, s;


    public Hex(int q, int r, int s) { // for cube
        assert (q + r + s == 0); // ensures valid coordinates
        this.q = q;
        this.r = r;
        this.s = s;
    }

    public static Hex hexAdd(Hex a, Hex b) {
        return new Hex(a.q + b.q, a.r + b.r, a.s + b.s);
    }

    public static Hex hexSubtract(Hex a, Hex b) {
        return new Hex(a.q - b.q, a.r - b.r, a.s - b.s);
    }

    public static Hex hexMultiply(Hex a, int k) {
        return new Hex(a.q * k, a.r * k, a.s * k);
    }

    public static int hexLength(Hex hex) {
        return (Math.abs(hex.q) + Math.abs(hex.r) + Math.abs(hex.s)) / 2;
    }

    public static int hexDistance(Hex a, Hex b) {
        return hexLength(hexSubtract(a, b));
    }

    public static int hexDirectionAsIndex(Hex hex) {
        for (int i = 0; i < hexDirections.length; i++) {
            if (hexDirections[i].equals(hex)) return i;
        }
        assert false : "Hex direction is invalid" + hex;
        return -1;
    }

    // must be 0 to 5
    public static Hex hexDirection(int direction) {
        assert (direction >= 0 && direction < 6);
        return hexDirections[direction];
    }

    public static Hex hexNeighbour(Hex hex, int direction) {
        return hexAdd(hex, hexDirection(direction));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Hex hex = (Hex) obj;
        return q == hex.q && r == hex.r && s == hex.s;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }

    public List<Hex> getNeighbours() {
        List<Hex> neighbours = new ArrayList<>();
        for (int i = 0; i < hexDirections.length; i++) {
            neighbours.add(hexNeighbour(this, i));
        }
        return neighbours;
    }

    @Override
    public boolean isAdjacent(Position other) {
        if (!(other instanceof Hex)) return false;
        for (Hex hexDirection : hexDirections) {
            if (hexDirection.equals(other)) return true;
        }
        return false;
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    public int getS() {
        return s;
    }

    @Override
    public String toString() {
        return "Hex{" +
                "q=" + q +
                ", r=" + r +
                ", s=" + s +
                '}';
    }

    @Override
    public Map<String, Object> toLogMap() {
        Map<String, Object> positionMap = new HashMap<>();
        positionMap.put("q", q);
        positionMap.put("r", r);
        positionMap.put("s", s);
        return positionMap;
    }
}
