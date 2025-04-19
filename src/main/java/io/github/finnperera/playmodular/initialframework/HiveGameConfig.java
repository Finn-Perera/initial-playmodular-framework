package io.github.finnperera.playmodular.initialframework;


import io.github.finnperera.playmodular.initialframework.AIModels.Minimax.AlphaBetaMinimaxModel;
import io.github.finnperera.playmodular.initialframework.AIModels.Minimax.MinimaxModel;
import io.github.finnperera.playmodular.initialframework.AIModels.MonteCarloTreeSearch.MonteCarloModel;
import io.github.finnperera.playmodular.initialframework.HiveHeuristics.BasicHeuristic;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HiveAI;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
acts as a sort of factory for hive games?
can be the configurable and loggable part
 */
public class HiveGameConfig implements LoggableGameConfig {
    private HivePlayer player1;
    private HivePlayer player2;
    private List<Option<?>> player1Options;
    private List<Option<?>> player2Options;

    HiveGameConfig() {
        player1 = new HivePlayer(HiveColour.WHITE);
        player2 = new HivePlayer(HiveColour.BLACK);
        player1Options = player1.getOptions();
        player2Options = player2.getOptions();
    }

    public void setPlayer(HiveColour colour, HivePlayer player, List<Option<?>> options) {
        if (colour == HiveColour.WHITE) {
            this.player1 = player;
            if (options != null) {
                this.player1Options = combineOptions(options, player1Options);
            } else {
                this.player1Options = player1.getOptions();
            }
        } else {
            this.player2 = player;
            if (options != null) {
                this.player2Options = combineOptions(options, player2Options);
            } else {
                this.player2Options = player2.getOptions();
            }
        }
    }

    // may cause problems if not in place?
    private List<Option<?>> combineOptions(List<Option<?>> newOptions, List<Option<?>> oldOptions) {
        LinkedHashMap<String, Option<?>> optionMap = new LinkedHashMap<>();

        for (Option<?> old : oldOptions) {
            optionMap.put(old.getName(), old);
        }
        for (Option<?> newOption : newOptions) {
            optionMap.put(newOption.getName(), newOption);
        }

        return new ArrayList<>(optionMap.values());
    }

    public HiveGame createGame() {
        HivePlayer gamePlayer1 = player1.copy();
        HivePlayer gamePlayer2 = player2.copy();

        configurePlayerOptions(gamePlayer1, player1Options);
        configurePlayerOptions(gamePlayer2, player2Options);
        return new HiveGame(new HiveRuleEngine(), gamePlayer1, gamePlayer2, new HiveBoardState());
    }

    public HivePlayer configureHivePlayer(HiveColour colour, String playerType) {
        HivePlayer updatedPlayer = null;

        switch (playerType) {
            case "Monte Carlo" -> updatedPlayer = new HiveAI(colour, new MonteCarloModel<>());
            case "Minimax" -> {
                updatedPlayer = new HiveAI(colour, null);
                configureAIModel((HiveAI) updatedPlayer, new MinimaxModel<>(updatedPlayer, new BasicHeuristic()));
            }
            case "Alpha-Beta" -> {
                updatedPlayer = new HiveAI(colour, null);
                configureAIModel((HiveAI) updatedPlayer, new AlphaBetaMinimaxModel<>(updatedPlayer, new BasicHeuristic()));
            }
            case "Human" -> updatedPlayer = new HivePlayer(colour);
        }


        setPlayer(colour, updatedPlayer, null);

        return updatedPlayer;
    }

    public void configureAIModel(HiveAI aiPlayer, AI<Hex, HiveTile> aiModel) {
        aiPlayer.setModel(aiModel);
    }

    public void configurePlayerOptions(HivePlayer player, List<Option<?>> options) {
        List<Option<?>> localOptions = new ArrayList<>(options);
        Option<?> playerIDOption = null;

        for (Option<?> option : localOptions) {
            if ("Player ID".equals(option.getName())) {
                playerIDOption = option;
            }
        }

        if (playerIDOption != null) {
            player.setOptions(List.of(playerIDOption));
            localOptions.remove(playerIDOption);
        }


        if (player instanceof HiveAI hiveAI && hiveAI.getModel() instanceof ConfigurableOptions config) { // if the player is a hiveAI
            config.setOptions(localOptions); // set these options, which feels redundant?
        }
    }

    public List<Option<?>> getPlayerOptions(HiveColour colour) {
        if (colour == HiveColour.WHITE) {
            return player1Options;
        } else {
            return player2Options;
        }
    }

    public HivePlayer getPlayer1() {
        return player1;
    }

    public HivePlayer getPlayer2() {
        return player2;
    }

    public HivePlayer getPlayerByColour(HiveColour colour) {
        return colour == HiveColour.WHITE ? player1 : player2;
    }

    @Override
    public String getGameName() {
        return "Hive";
    }

    @Override
    public int getExpectedPlayers() {
        return 2;
    }

    @Override
    public Map<String, Object> toLogMap() {
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("player1", playerToMap(player1, player1Options));
        logMap.put("player2", playerToMap(player2, player2Options));
        logMap.put("ruleEngine", "standard");

        Map<String, Object> gameConfigMap = new LinkedHashMap<>();
        gameConfigMap.put("game config", logMap);
        return gameConfigMap;
    }

    private Map<String, Object> playerToMap(HivePlayer player, List<Option<?>> options) {
        Map<String, Object> playerMap = new LinkedHashMap<>();
        playerMap.put("type", player.getClass().getSimpleName());
        if (player instanceof HiveAI hiveAI) {
            playerMap.put("model", hiveAI.getModel().getClass().getSimpleName());
        }

        playerMap.put("colour", player.getColour());
        for (Option<?> option : options) {
            playerMap.put(option.getName(), option.getValue());
        }
        return playerMap;
    }
}
