package io.github.finnperera.playmodular.initialframework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* The logging manager to handle games to CSV and JSON
Aims:
Create unique IDs for any Game, version of game/ai
Create JSON output of the Game, could cover players, game itself and any rule adjustments(options)
Create a CSV logging:
 the winner/loser/draw
 turns taken
 time taken for game to run(?)
 (do i need a random seed?)
 move history? (JSON probs better)
 AI insights (num of nodes explored, could log alpha-beta pruning events)
 memory usage?
 avg move time per player
 ai computation time per move
 */
public class LoggingManager {
    private static final String LOG_DIR = "target/logs/";

    private void ensureLogDirectory() {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                throw new RuntimeException("Unable to create log directory");
            }
        }
    }

    public String setUpSessionLog(LoggableGameConfig loggableConfig) {
        ensureLogDirectory();

        String filePrefix = LOG_DIR + loggableConfig.getGameName() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        try {
            generateCSVFile(filePrefix, loggableConfig.getExpectedPlayers());
            generateJSONFile(filePrefix, loggableConfig);
        } catch (IOException e) {
            e.printStackTrace(); // maybe let it throw to the top?
        }

        return filePrefix;
    }

    private void generateCSVFile(String filePrefix, int expectedPlayers) throws IOException {
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(filePrefix + ".csv", true))) {
            // write header
            List<String> header = new ArrayList<>();
            header.add("Game ID");
            header.add("Start Time");
            header.add("End Time");
            header.add("Total Time");
            header.add("Turns");
            for (int i = 1; i <= expectedPlayers; i++) {
                header.add("Player " + (i));
                header.add("Result " + (i));
            }

            csvWriter.writeNext(header.toArray(new String[0]));
        }
    }

    private void generateJSONFile(String filePrefix, LoggableGameConfig loggableConfig) throws IOException {
        File jsonFile = new File(filePrefix + ".json");
        if (!jsonFile.exists()) {
            if (!jsonFile.createNewFile()) {
                throw new RuntimeException("Unable to create json file");
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("game config", loggableConfig.toLogMap());
        rootMap.put("games", new ArrayList<>());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootMap);
    }

    public synchronized void addResultToFiles(String filePrefix, GameLog log) {
        File csvFile = getCSVFile(filePrefix);
        File jsonFile = getJSONFile(filePrefix);
        addToCSVFile(csvFile, log);
        addToJSONFile(jsonFile, log);
    }

    private synchronized void addToCSVFile(File csv, GameLog log) {
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(csv, true))) {
            csvWriter.writeNext(log.toStringArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void addToJSONFile(File json, GameLog log) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(json);

            ArrayNode gamesArray;
            gamesArray = (ArrayNode) root.get("games");

            ObjectNode game = objectMapper.convertValue(log.toMap(), ObjectNode.class);

            gamesArray.add(game);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(json, root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getCSVFile(String filePrefix) {
        File csvFile = new File(filePrefix + ".csv");
        if (!csvFile.exists()) {
            throw new RuntimeException("CSV file does not exist"); // maybe change
        }
        return csvFile;
    }

    private File getJSONFile(String filePrefix) {
        File jsonFile = new File(filePrefix + ".json");
        if (!jsonFile.exists()) {
            throw new RuntimeException("JSON file does not exist");
        }
        return jsonFile;
    }
}
