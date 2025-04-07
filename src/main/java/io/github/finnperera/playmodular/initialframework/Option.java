package io.github.finnperera.playmodular.initialframework;

public class Option<T> {

    private String name;
    private String description;
    private OptionType type;
    private Class<T> valueType;
    private T value;
    private T minValue;
    private T maxValue;

    public Option(String name, String description, OptionType type, Class<T> valueType, T value, T minValue, T maxValue) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.valueType = valueType;
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public T getOptionValue() {
        if (valueType.isInstance(value)) { // value.getClass().equals(type)
            return valueType.cast(value);
        } else {
            throw new ClassCastException("Cannot cast Option to" + valueType.getName()); // may want to catch in UI
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OptionType getType() {
        return type;
    }

    public void setType(OptionType type) {
        this.type = type;
    }

    public Class<T> getValueType() {
        return valueType;
    }

    public void setValueType(Class<T> valueType) {
        this.valueType = valueType;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getMinValue() {
        return minValue;
    }

    public void setMinValue(T minValue) {
        this.minValue = minValue;
    }

    public T getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(T maxValue) {
        this.maxValue = maxValue;
    }
}
