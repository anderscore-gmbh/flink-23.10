import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;


public class Aufgabe2 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // Pfad relativ zum bin Verzeichnis im Installationsordner von Flink
        DataStream<String> inputLines = env.readTextFile("../aufgabe2/input.txt")
                                           .filter(line -> !line.startsWith("#"));
        DataStream<String> words = inputLines.flatMap(new Tokenizer());
        DataStream<WordWithCounter> wordsWithCounters = words.map(word -> new WordWithCounter(word));
        wordsWithCounters.keyBy(WordWithCounter::getWord)
                        .reduce((word1, word2) -> new WordWithCounter(word1.getWord(),
                                word1.getCounter() + word2.getCounter()))
                        .map(wordWithCounter -> wordWithCounter.getWord() + " : " + wordWithCounter.getCounter())
                        .writeAsText("../aufgabe2/output.txt", FileSystem.WriteMode.OVERWRITE);
        env.execute();
    }

    public static class Tokenizer implements FlatMapFunction<String, String> {
        @Override
        public void flatMap(String value, Collector<String> out) {
            for (String token : value.split("\\W")) {
                out.collect(token);
            }
        }
    }
}
