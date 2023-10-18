import org.apache.flink.ml.api.Model;
import org.apache.flink.ml.builder.Pipeline;
import org.apache.flink.ml.classification.knn.Knn;
import org.apache.flink.ml.feature.hashingtf.HashingTF;
import org.apache.flink.ml.feature.tokenizer.Tokenizer;
import org.apache.flink.ml.linalg.Vector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.CloseableIterator;

import java.util.Arrays;
import java.util.stream.Collectors;


public class Aufgabe8 {

    public static void main(String[] args)  {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("text")
                .setOutputCol("tokenizedText");
        HashingTF hashingTF = new HashingTF()
                .setInputCol("tokenizedText")
                .setOutputCol("features")
                .setNumFeatures(200);
        Knn knn = new Knn()
                .setFeaturesCol("features")
                .setLabelCol("label")
                .setPredictionCol("prediction")
                .setK(1);
        Pipeline pipeline = new Pipeline(Arrays.asList(tokenizer, hashingTF, knn));

        DataStream<Row> trainingData = env.fromElements(
                Row.of("A B C", 0),
                Row.of("A B D", 0),
                Row.of("E F B", 1),
                Row.of("E G H", 2),
                Row.of("P Q B", 1)
        );

        DataStream<Row> testData = env.fromElements(
                Row.of("A B E"),  // erwartet Label 0
                Row.of("Y X A"), // erwartet Label 0
                Row.of("E M B"), // erwartet Label 1
                Row.of("E Q F"), // erwartet Label 1
                Row.of("G A H") // erwartet Label 2
        );


        Table trainTable = tEnv.fromDataStream(trainingData).as("text", "label");
        Table testTable = tEnv.fromDataStream(testData).as("text");

        Model model = pipeline.fit(trainTable);
        Table output = model.transform(testTable)[0];

        for (CloseableIterator<Row> it = output.execute().collect(); it.hasNext(); ) {
            Row row = it.next();
            String text = (String) row.getField("text");
            String[] token = (String[]) row.getField("tokenizedText");
            Vector features = (Vector) row.getField("features");
            Double prediction = (Double) row.getField("prediction");

            System.out.println(
                    "Text: " + text
                   + "\ttokens: " + Arrays.stream(token).collect(Collectors.joining(","))
                    + "\t vec" + features
                    + "\t pred: " + prediction
            );
        }
    }
}
