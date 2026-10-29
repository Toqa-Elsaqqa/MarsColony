package Core;

public class Resource {
    private final String type;
    private int quantity;

    public Resource(String type, int quantity) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Invalid resource type");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Invalid quantity");
        }

        this.type = type;
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void add(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        quantity += amount;
    }

    public boolean remove(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        if (amount > quantity) {
            return false;
        }

        quantity -= amount;
        return true;
    }
}
