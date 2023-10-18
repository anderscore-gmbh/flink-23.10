import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.random;

public class Aufgabe5 {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Generieren der Eingabedaten
        DataGeneratorSource<Double> source1 = newRandomSource();
        DataGeneratorSource<Double> source2 = newRandomSource();
        DataStream<Double> stream1 = env.fromSource(source1, ingestionTimeWatermarkStrategy(),"source1");
        DataStream<Double> stream2 = env.fromSource(source2, ingestionTimeWatermarkStrategy(),"source2");
        KeyedStream<AggregatingKeyedDouble, Integer> keyedStream1 = assignRandomKeysForAggregation(stream1);
        KeyedStream<KeyedDouble, Integer> keyedStream2 = assignRandomKeys(stream2);

        // Durchschnitt
        DataStream<KeyedDouble> averagesStream =
                keyedStream1.window(TumblingEventTimeWindows.of(Time.seconds(1)))
                            .reduce(new SumReduceFunction())
                            .map(element -> new KeyedDouble(element.getKey(), element.getValue() / Double.valueOf(element.getCount())));

        // Median
        DataStream<KeyedDouble> medianStream =
                keyedStream2.window(TumblingEventTimeWindows.of(Time.seconds(1)))
                            .process(new MedianProcessWindowFunction<>());

        // Join und Output
        averagesStream.coGroup(medianStream)
                        .where(KeyedDouble::getKey)
                        .equalTo(KeyedDouble::getKey)
                        .window(TumblingEventTimeWindows.of(Time.seconds(1)))
                        .apply(new WarningJoinFunction())
                        .writeAsText("C:\\flink\\aufgabe5\\output.txt", FileSystem.WriteMode.OVERWRITE);

        env.execute();
    }

    private static KeyedStream<AggregatingKeyedDouble, Integer> assignRandomKeysForAggregation(DataStream<Double> stream) {
        return stream.map(value -> new AggregatingKeyedDouble(
                        ThreadLocalRandom.current().nextInt(1, 6), value, 1))
                .keyBy(AggregatingKeyedDouble::getKey);
    }

    private static KeyedStream<KeyedDouble, Integer> assignRandomKeys(DataStream<Double> stream) {
        return stream.map(value -> new KeyedDouble(
                        ThreadLocalRandom.current().nextInt(1, 6), value))
                     .keyBy(KeyedDouble::getKey);
    }

     private static DataGeneratorSource<Double> newRandomSource() {
        return new DataGeneratorSource<Double>(
                    index -> random(),
                    1000,
                    RateLimiterStrategy.perSecond(100),
                    TypeInformation.of(Double.class)
        );
    }

    private static WatermarkStrategy<Double> ingestionTimeWatermarkStrategy() {
        return  WatermarkStrategy.<Double>forMonotonousTimestamps()
                           .withTimestampAssigner(new IngestionTimeTimestampAssigner<>());
    }

    private static class SumReduceFunction implements ReduceFunction<AggregatingKeyedDouble> {

        @Override
        public AggregatingKeyedDouble reduce(AggregatingKeyedDouble a1, AggregatingKeyedDouble a2)  {
            return new AggregatingKeyedDouble(a1.getKey(), a1.getValue() + a2.getValue(), a1.getCount() + a2.getCount());
        }
    }

    public static class MedianProcessWindowFunction<W extends Window>
            extends ProcessWindowFunction<KeyedDouble, KeyedDouble, Integer, W> {

        @Override
        public void process(Integer key, ProcessWindowFunction<KeyedDouble, KeyedDouble, Integer, W>.Context context,
                            Iterable<KeyedDouble> input, Collector<KeyedDouble> output)  {
            // Effizienz ist hier mal egal
            List<KeyedDouble> values = new ArrayList<>();
            input.forEach(values::add);
            int numberOfElements = values.size();
            if (numberOfElements == 0) {
                return;
            }
            if (numberOfElements == 1) {
                output.collect(values.get(0));
                return;
            }
            values.sort(Comparator.comparingDouble(KeyedDouble::getValue));
            int halfNumberOfElements = numberOfElements / 2;
            double middleRightValue = values.get(halfNumberOfElements).getValue();
            double middleLeftValue = values.get(halfNumberOfElements - 1).getValue();
            double median = numberOfElements % 2 == 1 ? middleRightValue
                  : (middleLeftValue + middleRightValue) / 2;
            output.collect(new KeyedDouble(key, median));
        }
    }

    public static class WarningJoinFunction
            implements CoGroupFunction<KeyedDouble, KeyedDouble, String> {

        @Override
        public void coGroup(Iterable<KeyedDouble> keyedDoubles1,
                            Iterable<KeyedDouble> keyedDoubles2,
                            Collector<String> output) {
            for (KeyedDouble keyedDouble1 : keyedDoubles1) {  // Iterables sollten hier tatsächlich nur bis zu 1 Element enthalten
                for (KeyedDouble keyedDouble2 : keyedDoubles2) {
                    double largerValue = Math.max(keyedDouble1.getValue(), keyedDouble2.getValue());
                    double smallerValue = Math.min(keyedDouble1.getValue(), keyedDouble2.getValue());
                    if (largerValue * 0.8 > smallerValue) {
                        output.collect(generateOutput(keyedDouble1, keyedDouble2));
                    }
                }
            }
        }

        private String generateOutput(KeyedDouble keyedDouble1, KeyedDouble keyedDouble2) {
            return String.format("Warning for key %d : values %.3f and %.3f are far apart",
                    keyedDouble1.getKey(), keyedDouble1.getValue(), keyedDouble2.getValue());
        }
    }
}
