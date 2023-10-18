import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


@Testcontainers
public class Aufgabe7Test {

    @Container
    private static final KafkaContainer
            kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));

    @Container
    private static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine");

    CustomerService customerService;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        DBConnectionProvider connectionProvider = new DBConnectionProvider(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        customerService = new CustomerService(connectionProvider);
    }

    @Test
    public void kafkaIsRunningTest() {
         System.out.println(kafka.getBootstrapServers());
    }

    private void addTestDataToKafka() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers(kafka.getBootstrapServers())
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopicSelector(element -> "testTopic1")
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
        env.fromCollection(Arrays.asList("1", "3", "2", "3"))
           .sinkTo(sink);
        env.execute();
    }

    private void addTestDataToPostgres() {
        customerService.createCustomer(new Customer(1L, "George"));
        customerService.createCustomer(new Customer(2L, "Heinz"));
        customerService.createCustomer(new Customer(3L, "Minerva"));
    }

    @Test
    public void KafkaTest() throws Exception {
        System.out.println("Running Kafka Test");
        addTestDataToKafka();
        addTestDataToPostgres();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(kafka.getBootstrapServers())
                .setTopics("testTopic1")
                .setGroupId("my-group")
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setBounded(OffsetsInitializer.latest())
                .build();

        DataStream<Long> ids = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source")
                .map(Long::valueOf);
        AsynchCustomerLookup customerLookup = new AsynchCustomerLookup(postgres.getJdbcUrl(),
                postgres.getUsername(), postgres.getPassword());
        DataStream<Customer> customers =
             AsyncDataStream.unorderedWait(ids, customerLookup, 1000, TimeUnit.MILLISECONDS, 100);
        customers.print();
        env.execute();
    }
}
