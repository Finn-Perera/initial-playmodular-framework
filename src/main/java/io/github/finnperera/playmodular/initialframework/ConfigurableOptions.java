package io.github.finnperera.playmodular.initialframework;

import java.util.List;

public interface ConfigurableOptions {
    List<Option<?>> getOptions();
    void setOptions(List<Option<?>> options);
}
