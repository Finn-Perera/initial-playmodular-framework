package io.github.finnperera.playmodular.initialframework;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class OptionFactory {

    public static <T> Node createOptionControl(Option<T> option) {
        Label label = createLabel(option);

        Control control = switch (option.getType()) {
            case SLIDER -> createSlider(option);
            case SPINNER -> createSpinner(option);
            case TOGGLE -> createCheckbox(option);
            case DROPDOWN -> createDropdown(option);
            case TEXTBOX -> createTextBox(option);
            case null -> throw new IllegalArgumentException("Unsupported option type: " + option.getType());
        };

        return new VBox(label, control);
    }

    private static <T> Control createSlider(Option<T> option) {
        if (!Number.class.isAssignableFrom(option.getValueType())) {
            throw new IllegalArgumentException("Slider requires numeric type");
        }

        double min = toDouble(option.getMinValue());
        double max = toDouble(option.getMaxValue());
        double current = toDouble(option.getOptionValue());

        Slider slider = new Slider(min, max, current);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit((max - min) / 4);
        slider.setMinorTickCount(1);
        slider.setSnapToTicks(true);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            T newValue = fromDouble(option.getValueType(), newVal.doubleValue());
            option.setValue(newValue);
        });

        return slider;
    }

    private static <T> Control createSpinner(Option<T> option) {
        if (!Number.class.isAssignableFrom(option.getValueType())) {
            throw new IllegalArgumentException("Spinner requires numeric type");
        }

        double min = toDouble(option.getMinValue());
        double max = toDouble(option.getMaxValue());
        double value = toDouble(option.getValue());

        Spinner<Double> spinner = new Spinner<>(min ,max, value);
        spinner.setEditable(true);

        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            T newValue = fromDouble(option.getValueType(), newVal);
            option.setValue(newValue);
        });

        return spinner;
    }

    private static <T> Control createDropdown(Option<T> option) {
        if (option.getChoices() == null || option.getChoices().isEmpty()) {
            throw new IllegalArgumentException("Dropdown requires choices");
        }

        List<ChoiceItem<T>> choices = option.getChoices();
        ChoiceBox<ChoiceItem<T>> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().setAll(choices);

        for (ChoiceItem<T> item : choices) {
            if (item.getItem().equals(option.getValue())) {
                choiceBox.setValue(item);
                break;
            }
        }

        choiceBox.valueProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                option.setValue(newItem.getItem());
            }
        });

        return choiceBox;
    }

    private static <T> Control createCheckbox(Option<T> option) {
        if (!Boolean.class.isAssignableFrom(option.getValueType())) {
            throw new IllegalArgumentException("Checkbox requires Boolean type");
        }

        CheckBox checkbox = new CheckBox();
        checkbox.setSelected((Boolean) option.getValue());

        checkbox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            option.setValue(option.getValueType().cast(isSelected)); // not sure about this
        });

        return checkbox;
    }

    private static <T> Label createLabel(Option<T> option) {
        Label label = new Label();
        label.setText(option.getName());
        Tooltip tooltip = new Tooltip(option.getDescription());
        tooltip.setShowDelay(Duration.seconds(0.25));
        Tooltip.install(label, tooltip);
        return label;
    }

    private static <T> TextField createTextBox(Option<T> option) {
        if (!String.class.isAssignableFrom(option.getValueType())) {
            throw new IllegalArgumentException("Textbox requires String type");
        }

        TextField textField = new TextField();
        textField.setPromptText(option.getName());

        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            option.setValue(option.getValueType().cast(newVal));
        });

        return textField;
    }

    private static double toDouble(Object num) {
        return ((Number) num).doubleValue();
    }

    @SuppressWarnings("unchecked")
    private static <T> T fromDouble(Class<?> type, double value) {
        if (type == Integer.class || type == int.class) return (T) Integer.valueOf((int) value);
        if (type == Double.class || type == double.class) return (T) Double.valueOf(value);
        if (type == Long.class || type == long.class) return (T) Long.valueOf((long) value);
        if (type == Float.class || type == float.class) return (T) Float.valueOf((float) value);
        throw new IllegalArgumentException("Unsupported number type: " + type.getName());
    }
}
