import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.File;


public class Aufgabe6 {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        FileSource<int[]> source =
                FileSource.forRecordStreamFormat(new IntArrayStreamFormat(),
                                                 Path.fromLocalFile(new File("C:\\flink\\aufgabe6\\input.txt")))

                          .build();
        FileSink<int[]> sink =
                FileSink.forRowFormat(Path.fromLocalFile(new File("C:\\flink\\aufgabe6\\output")),
                                      new IntArrayEncoder())
                        .build();
       env.fromSource(source, WatermarkStrategy.noWatermarks(), "fileSource")
               .sinkTo(sink);
        env.execute();
    }
}
