package io.github.finnperera.playmodular.initialframework;

public class ChoiceItem<T> {
    private String label;
    private T item;

    public ChoiceItem(String label, T item) {
        this.label = label;
        this.item = item;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return label;
    }
}
