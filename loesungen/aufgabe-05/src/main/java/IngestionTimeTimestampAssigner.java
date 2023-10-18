import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;

public class IngestionTimeTimestampAssigner<T> implements SerializableTimestampAssigner<T> {

    @Override
    public long extractTimestamp(T record, long initialTimestamp) {
        return System.currentTimeMillis();
    }
}
