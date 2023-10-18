public class Order {

    private Long id;
    private Double value;

    public Order(Long id, Double value) {
        this.id = id;
        this.value = value;
    }

    public Order() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
