public class WordWithCounter {
    private String word;
    private int counter;

    public WordWithCounter() {
    }

    public WordWithCounter(String word) {
        this.word = word;
        counter = 1;
    }

    public WordWithCounter(String word, int counter) {
        this.word = word;
        this.counter = counter;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}
