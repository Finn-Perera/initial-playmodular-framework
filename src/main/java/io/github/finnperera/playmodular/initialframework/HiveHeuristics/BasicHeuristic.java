package io.github.finnperera.playmodular.initialframework.HiveHeuristics;

import io.github.finnperera.playmodular.initialframework.*;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BasicHeuristic extends Heuristic<Hex, HiveTile> implements ConfigurableOptions, LoggableComponent {
    // These variables could be adjusted in menus
    public Option<String> heuristicID = Option.<String>builder()
            .name("Heuristic ID")
            .description("Identifies heuristic for use, should be unique")
            .type(OptionType.TEXTBOX)
            .valueType(String.class)
            .value("BasicHeuristic")
            .setMinValue(null)
            .setMaxValue(null)
            .build();
    public Option<Integer> winScore =
            intOption("Win Score", "Score attributed to winning the game", Integer.MAX_VALUE / 2);
    public Option<Integer> loseScore =
            intOption("Lose Score", "Score attributed to losing the game", Integer.MIN_VALUE / 2);
    public Option<Integer> ownQueenSurroundScore =
            intOption("Own Queen Surround Score", "Score for each piece surrounding own queen", -50);
    public Option<Integer> ownQueenStuckScore =
            intOption("Own Queen Stuck Score", "Score for own queen being stuck", -300);
    public Option<Integer> oppQueenSurroundScore =
            intOption("Opponent Queen Surround Score", "Score for each piece surrounding the opponent queen", 50);
    public Option<Integer> oppQueenStuckScore =
            intOption("Opponent Queen Stuck Score", "Score for opponent queen being stuck", 300);

    public Option<Integer> queenTileVal =
            intOption("Queen Tile Value", "Score assigned to Queen tile", 70);
    public Option<Integer> antTileVal =
            intOption("Ant Tile Value", "Score assigned to Ant tile", 80);
    public Option<Integer> beetleTileVal =
            intOption("Beetle Tile Value", "Score assigned to Beetle tile", 60);
    public Option<Integer> grasshopperTileVal =
            intOption("Grasshopper Tile Value", "Score assigned to Grasshopper tile", 50);
    public Option<Integer> spiderTileVal =
            intOption("Spider Tile Value", "Score assigned to Spider tile", 50);

    @Override
    public int getEvaluation(Game<Hex, HiveTile> game) {
        HiveGame hiveGame = (HiveGame) game;

        HivePlayer maxPlayer = hiveGame.getCurrentPlayer();
        HivePlayer minPlayer = hiveGame.getCurrentOpponent();
        int evaluationScore = 0;
        evaluationScore += winOrLose(hiveGame, maxPlayer);
        evaluationScore += ownQueenSurrounded(hiveGame, maxPlayer);
        evaluationScore += opponentQueenSurrounded(hiveGame, minPlayer);
        evaluationScore += stuckPieces(hiveGame, maxPlayer);

        return evaluationScore;
    }

    @Override
    public int getEvaluation(Game<Hex, HiveTile> game, Player maxPlayer) {
        HiveGame hiveGame = (HiveGame) game;
        HivePlayer hiveMaxPlayer = (HivePlayer) maxPlayer;

        assert hiveGame.getPlayers().contains(hiveMaxPlayer);
        HivePlayer minPlayer = hiveGame.getCurrentPlayer() == hiveMaxPlayer ? hiveGame.getCurrentOpponent() : hiveGame.getCurrentPlayer();
        int evaluationScore = 0;
        evaluationScore += winOrLose(hiveGame, hiveMaxPlayer);
        evaluationScore += ownQueenSurrounded(hiveGame, hiveMaxPlayer);
        evaluationScore += opponentQueenSurrounded(hiveGame, minPlayer);
        evaluationScore += stuckPieces(hiveGame, hiveMaxPlayer);

        return evaluationScore;
    }

    private int winOrLose(HiveGame game, HivePlayer player) {
        if (!game.isTerminalState()) return 0;

        GameResult result = game.getGameResult(player);
        return result == GameResult.WIN ? winScore.getValue() : loseScore.getValue();
    }

    private int ownQueenSurrounded(HiveGame game, HivePlayer maxPlayer) {
        return queenSurround(game, maxPlayer, true);
    }

    private int opponentQueenSurrounded(HiveGame game, HivePlayer minPlayer) {
        return queenSurround(game, minPlayer, false);
    }

    private int queenSurround(HiveGame game, HivePlayer playerWithQueen, boolean isMaxPlayer) {
        int surroundScore;
        int stuckScore;
        if (isMaxPlayer) {
            // Queen is of own player
            surroundScore = ownQueenSurroundScore.getValue();
            stuckScore = ownQueenStuckScore.getValue();
        } else {
            // Queen is of opponent
            surroundScore = oppQueenSurroundScore.getValue();
            stuckScore = oppQueenStuckScore.getValue();
        }

        HiveTile queen = game.getBoardState().getQueenOfPlayer(playerWithQueen);
        if (queen == null) return 0;
        int scoreTotal = -surroundScore; // one piece touching queen doesn't affect score? (always will have one)
        for (Hex neighbour : queen.getHex().getNeighbours()) {
            if (game.getBoardState().hasPieceAt(neighbour)) {
                scoreTotal += surroundScore;
            }
        }

        // if queen cannot move
        if (game.getRuleEngine().generatePieceMoves(game.getBoardState(), queen).isEmpty()) {
            scoreTotal += stuckScore;
        }

        return scoreTotal;
    }

    private int stuckPieces(HiveGame game, HivePlayer maxPlayer) {
        // Get neighbours for each piece on the board if > 2 then do one hive rule check
        HiveColour playerColour = maxPlayer.getColour();
        AtomicInteger totalScore = new AtomicInteger();

        // This could probably all be refactored to one loop or put moveable pieces into hashmap?
        // maybe I hash move or the tile that is moving with the board state
        for (Stack<HiveTile> stack : game.getBoardState().getBoard().getAllPieces()) {
            // Pieces stuck under other pieces
            if (stack.size() > 1) {
                stack.subList(0, stack.size() - 1).forEach(stackTile -> {
                    if (stackTile.getColour() == playerColour) {
                        totalScore.addAndGet(-pieceValues(stackTile.getTileType()));
                    } else {
                        totalScore.addAndGet(pieceValues(stackTile.getTileType()));
                    }
                });
            }

            // Neighbouring pieces >= 2
            if (!canBeStuck(stack, game)) continue;

            // If one Hive Rule fails -> piece is stuck
            if (!game.getRuleEngine().isOneHiveWhileMoving(game.getBoardState(), stack.peek())) {
                HiveTile topTile = stack.peek();
                if (topTile.getColour() == playerColour) {
                    totalScore.addAndGet(-pieceValues(topTile.getTileType()));
                } else {
                    totalScore.addAndGet(pieceValues(topTile.getTileType()));
                }
            }
        }
        return totalScore.get();
    }

    private boolean canBeStuck(Stack<HiveTile> stack, HiveGame game) {
        int neighbourCount = 0;
        for (Hex neighbour: stack.peek().getHex().getNeighbours()) {
            if (game.getBoardState().hasPieceAt(neighbour)) {
                neighbourCount++;
            }
            if (neighbourCount > 1) {
                return true;
            }
        }
        return false;
    }

    private int pieceValues(HiveTileType tileType) {
        switch (tileType) {
            case QUEEN_BEE -> {
                return queenTileVal.getValue();
            }
            case ANT -> {
                return antTileVal.getValue();
            }
            case BEETLE -> {
                return beetleTileVal.getValue();
            }
            case GRASSHOPPER -> {
                return grasshopperTileVal.getValue();
            }
            case SPIDER -> {
                return spiderTileVal.getValue();
            }
            default -> throw new UnsupportedOperationException("Unknown tile type: " + tileType);
        }
    }

    @Override
    public String getHeuristicID() {
        return heuristicID.getValue();
    }

    private static Option<Integer> intOption(String name, String description, int value) {
        return Option.<Integer>builder()
                .name(name)
                .description(description)
                .type(OptionType.SPINNER)
                .valueType(Integer.class)
                .value(value)
                .setMinValue(Integer.MIN_VALUE / 2)
                .setMaxValue(Integer.MAX_VALUE / 2)
                .build();
    }

    @Override
    public List<Option<?>> getOptions() {
        return List.of(
                heuristicID,
                winScore,
                loseScore,
                ownQueenSurroundScore,
                ownQueenStuckScore,
                oppQueenSurroundScore,
                oppQueenStuckScore,
                queenTileVal,
                antTileVal,
                beetleTileVal,
                grasshopperTileVal,
                spiderTileVal
        );
    }

    @Override
    public void setOptions(List<Option<?>> options) {
        for (Option<?> option : options) {
            switch (option.getName()) {
                case "Heuristic ID" -> heuristicID.setValue((String) option.getValue());
                case "Win Score" -> winScore.setValue((Integer) option.getValue());
                case "Lose Score" -> loseScore.setValue((Integer) option.getValue());
                case "Own Queen Surround Score" -> ownQueenSurroundScore.setValue((Integer) option.getValue());
                case "Own Queen Stuck Score" -> ownQueenStuckScore.setValue((Integer) option.getValue());
                case "Opponent Queen Surround Score" -> oppQueenSurroundScore.setValue((Integer) option.getValue());
                case "Opponent Queen Stuck Score" -> oppQueenStuckScore.setValue((Integer) option.getValue());
                case "Queen Tile Value" -> queenTileVal.setValue((Integer) option.getValue());
                case "Ant Tile Value" -> antTileVal.setValue((Integer) option.getValue());
                case "Beetle Tile Value" -> beetleTileVal.setValue((Integer) option.getValue());
                case "Grasshopper Tile Value" -> grasshopperTileVal.setValue((Integer) option.getValue());
                case "Spider Tile Value" -> spiderTileVal.setValue((Integer) option.getValue());
            }
        }
    }

    @Override
    public Map<String, Object> toLogMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Option<?> option : getOptions()) {
            map.put(option.getName().toLowerCase(), option.getValue());
        }
        return map;
    }
}
