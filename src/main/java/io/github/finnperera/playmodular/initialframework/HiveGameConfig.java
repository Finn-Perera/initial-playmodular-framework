package io.github.finnperera.playmodular.initialframework;


import io.github.finnperera.playmodular.initialframework.AIModels.Minimax.AlphaBetaMinimaxModel;
import io.github.finnperera.playmodular.initialframework.AIModels.Minimax.MinimaxModel;
import io.github.finnperera.playmodular.initialframework.AIModels.MonteCarloTreeSearch.MonteCarloModel;
import io.github.finnperera.playmodular.initialframework.AIModels.RandomModel;
import io.github.finnperera.playmodular.initialframework.HiveHeuristics.BasicHeuristic;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HiveAI;
import io.github.finnperera.playmodular.initialframework.HivePlayers.HivePlayer;

import java.util.*;

/*
acts as a sort of factory for hive games?
can be the configurable and loggable part
 */
public class HiveGameConfig implements LoggableGameConfig {
    private HivePlayer player1;
    private HivePlayer player2;
    private List<Option<?>> player1Options;
    private List<Option<?>> player2Options;
    private final ArrayList<Heuristic<?, ?>> heuristics;
    private Runnable updateHeuristicUI;

    public HiveGameConfig() {
        player1 = new HivePlayer(HiveColour.WHITE);
        player2 = new HivePlayer(HiveColour.BLACK);
        player1Options = player1.getOptions();
        player2Options = player2.getOptions();
        heuristics = new ArrayList<>();
        heuristics.add(new BasicHeuristic());
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

        if (optionMap.containsKey("Heuristic")) {
            optionMap.put("Heuristic", buildNewHeuristicOption(optionMap.get("Heuristic")));
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

    public HiveGame createGameFromGameState(HiveGame game) {
        HivePlayer predefinedPlayer1 = (HivePlayer) game.getPlayers().getFirst();
        HivePlayer predefinedPlayer2 = (HivePlayer) game.getPlayers().getLast();

        player1.setHand(predefinedPlayer1.getTiles());
        player2.setHand(predefinedPlayer2.getTiles());

        HivePlayer gamePlayer1 = player1.copy();
        HivePlayer gamePlayer2 = player2.copy();

        configurePlayerOptions(gamePlayer1, player1Options);
        configurePlayerOptions(gamePlayer2, player2Options);
        return new HiveGame(new HiveRuleEngine(), gamePlayer1, gamePlayer2,
                new HiveBoardState(game.getBoardState()), game.getTurn());
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
            case "Random Moves" -> {
                updatedPlayer = new HiveAI(colour, new RandomModel<>());
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

        if (player instanceof HiveAI hiveAI && hiveAI.getAIModel() instanceof ConfigurableOptions config) { // if the player is a hiveAI
            config.setOptions(localOptions);
        }
    }

    public List<Option<?>> getPlayerOptions(HiveColour colour) {
        if (colour == HiveColour.WHITE) {
            return player1Options;
        } else {
            return player2Options;
        }
    }

    public void addHeuristic(Heuristic<?, ?> heuristic) {
        heuristics.add(heuristic);
        Option<?> p1HeuristicOption = null;
        Option<?> p2HeuristicOption = null;

        for (Option<?> option : player1Options) {
            if ("Heuristic".equals(option.getName())) {
                p1HeuristicOption = option;
            }
        }

        for (Option<?> option : player2Options) {
            if ("Heuristic".equals(option.getName())) {
                p2HeuristicOption = option;
            }
        }

        if (p1HeuristicOption != null) {
            Option<?> newOption = buildNewHeuristicOption(p1HeuristicOption);
            player1Options.remove(p1HeuristicOption);
            player1Options.add(newOption);
        }

        if (p2HeuristicOption != null) {
            Option<?> newOption = buildNewHeuristicOption(p2HeuristicOption);
            player2Options.remove(p2HeuristicOption);
            player2Options.add(newOption);
        }
        configurePlayerOptions(player1, player1Options);
        configurePlayerOptions(player2, player2Options);

        updateHeuristicUI.run();
    }

    private Option<?> buildNewHeuristicOption(Option<?> oldHeuristicOption) {
        Option<Object> newOption = null;
        if (oldHeuristicOption != null) {
            List<ChoiceItem<Object>> newChoices = new ArrayList<>();
            for (Heuristic<?, ?> h : heuristics) {
                newChoices.add(new ChoiceItem<>(h.getHeuristicID(), h));
            }
             newOption = Option.builder()
                    .name(oldHeuristicOption.getName())
                    .description(oldHeuristicOption.getDescription())
                    .type(oldHeuristicOption.getType())
                    .valueType(Object.class)
                    .value(oldHeuristicOption.getValue())
                    .setMinValue(null)
                    .setMaxValue(null)
                    .setChoices(newChoices)
                    .build();
        }
        return newOption;
    }

    public Optional<Option<?>> getHeuristicOption(HiveColour colour) {
        List<Option<?>> options = getPlayerOptions(colour);
        return options.stream().filter(item -> "Heuristic".equals(item.getName())).findFirst();
    }

    public void setUpdateHeuristicUI(Runnable updateHeuristicUI) {
        this.updateHeuristicUI = updateHeuristicUI;
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

        return logMap;
    }

    private Map<String, Object> playerToMap(HivePlayer player, List<Option<?>> options) {
        Map<String, Object> playerConfigMap = new LinkedHashMap<>();
        playerConfigMap.put("type", player.getClass().getSimpleName());
        if (player instanceof HiveAI hiveAI) {
            playerConfigMap.put("model", hiveAI.getAIModel().getClass().getSimpleName());
        }

        playerConfigMap.put("colour", player.getColour());
        for (Option<?> option : options) {
            if (option.getValue() instanceof LoggableComponent loggableComponent) {
                playerConfigMap.put(option.getName(), loggableComponent.toLogMap());
            } else {
                playerConfigMap.put(option.getName(), option.getValue().toString());
            }
        }
        return playerConfigMap;
    }
}
