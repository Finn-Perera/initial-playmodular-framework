package io.github.finnperera.playmodular.initialframework;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GameLog {
    private String gameID;
    private Instant startTime;
    private Instant endTime;
    private Duration totalTime;
    private int totalTurns;
    private Map<Player, GameResult> playerResults;
    private ArrayList<MoveData> moveList;

    public GameLog(Instant startTime, Instant endTime, Map<Player, GameResult> playerResults, int totalTurns,
                   ArrayList<MoveData> moveList) {
        gameID = UUID.randomUUID().toString();
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalTime = Duration.between(startTime, endTime);
        this.playerResults = playerResults;
        this.totalTurns = totalTurns;
        this.moveList = new ArrayList<>(moveList);
    }

    public String getGameID() {
        return gameID;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Duration getTotalTime() {
        return totalTime;
    }

    public Map<Player, GameResult> getPlayerResults() {
        return playerResults;
    }

    public int getTotalTurns() {
        return totalTurns;
    }

    public String[] toStringArray() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String duration = formatDuration(totalTime);

        List<String> list = new ArrayList<>();
        list.add(gameID);
        list.add(startTime.atZone(ZoneId.systemDefault()).format(formatter));
        list.add(endTime.atZone(ZoneId.systemDefault()).format(formatter));
        list.add(duration);
        list.add(String.valueOf(totalTurns));
        for (Player player : playerResults.keySet()) {
            list.add(player.getPlayerID());
            list.add(playerResults.get(player).toString()); // could put it as a number?
        }

        return list.toArray(new String[0]);
    }

    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("gameID", gameID);
        Map<String, Object> playerMap = new HashMap<>();
        for (Player player : playerResults.keySet()) {
            if (player.isAI()) {
                if (player.getAIModel() instanceof LoggableComponent loggable) {
                    playerMap.put(player.getPlayerID(), loggable.toLogMap());
                } else {
                    playerMap.put(player.getPlayerID(), "logging not enabled");
                }
            } else {
                playerMap.put(player.getPlayerID(), "logging not enabled");
            }
        }
        map.put("players", playerMap);
        ArrayList<Map<String, Object>> moveMapList = new ArrayList<>();
        for (MoveData move : moveList) {
            Map<String, Object> moveMap = new LinkedHashMap<>();
            moveMap.put("turn length", formatDuration(move.timeTaken()));
            moveMap.put("move", move.move().toLogMap());
            moveMapList.add(moveMap);
        }
        map.put("moves", moveMapList);
        return map;
    }
}
