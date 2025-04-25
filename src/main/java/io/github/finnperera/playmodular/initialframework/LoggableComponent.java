package io.github.finnperera.playmodular.initialframework;

import java.util.List;
import java.util.Map;

public interface LoggableComponent {
    Map<String, Object> toLogMap();

    default List<String> toCSVRow() {
        return List.of();
    }
    // add headers, could be used when setting up logs?
    /*default List<String> getCSVHeaders() {
        return List.of();
    }*/
}
