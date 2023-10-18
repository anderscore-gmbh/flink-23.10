public class AggregatingKeyedDouble {

    private int key;
    private double value;
    private int count;


    public AggregatingKeyedDouble(int key, double value, int count) {
        this.key = key;
        this.value = value;
        this.count = count;
    }

    public AggregatingKeyedDouble() {
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
