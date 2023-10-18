import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;


public class Aufgabe2 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> inputLines = env.readTextFile("C:\\flink\\aufgabe2\\input.txt")
                                           .filter(line -> !line.startsWith("#"));
        DataStream<String> words = inputLines.flatMap(new Tokenizer())
               // .startNewChain()
                        ;
        DataStream<WordWithCounter> wordsWithCounters = words.map(word -> new WordWithCounter(word))
                    //    .setParallelism(2)
                    ;
        wordsWithCounters.keyBy(WordWithCounter::getWord)
                        .reduce((word1, word2) -> new WordWithCounter(word1.getWord(),
                                word1.getCounter() + word2.getCounter()))
                        .map(wordWithCounter -> wordWithCounter.getWord() + " : " + wordWithCounter.getCounter())
                .keyBy(word -> Character.toLowerCase(word.charAt(0)))
                .writeAsText("C:\\flink\\aufgabe2\\output.txt", FileSystem.WriteMode.OVERWRITE)
              //  .setParallelism(1)
                ;
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
