import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.OutputTag;

public class Aufgabe4 {

    public static void main(String[] args) throws Exception {
            OutputTag<String> outputById = new OutputTag<String>("tag-1") {};
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            // Pfade für Linux ändern und Eingabedateien bereitstellen
            DataStream<String> ordersInput1 = env.readTextFile("C:\\flink\\aufgabe4\\inputOrders1.txt");
            DataStream<String> ordersInput2 = env.readTextFile("C:\\flink\\aufgabe4\\inputOrders2.txt");
            DataStream<String> customerInput = env.readTextFile("C:\\flink\\aufgabe4\\inputCustomers.txt");

            KeyedStream<Customer, Long> customers = customerInput
                    .map(line -> new Customer(Long.valueOf(line.substring(0, line.indexOf(":"))),
                                              line.substring(line.indexOf(":") + 1)))
                    .keyBy(Customer::getId);
            DataStream<Order> orders1 = ordersInput1
                    .map(line -> new Order(Long.valueOf(line.substring(0, line.indexOf(":"))),
                            Double.valueOf(line.substring(line.indexOf(":") + 1))));
            DataStream<Order> orders2 = ordersInput2
                    .map(line -> new Order(Long.valueOf(line.substring(0, line.indexOf(":"))),
                            Double.valueOf(line.substring(line.indexOf(":") + 1))));

            KeyedStream<Order, Long> orders = orders1.union(orders2)
                            .keyBy(Order::getId);
            SingleOutputStreamOperator<String> output = customers.connect(orders)
                            .process(new OrderProcessFunction(outputById));
            output.getSideOutput(outputById)
                    .writeAsText("C:\\flink\\aufgabe4\\outputById.txt", FileSystem.WriteMode.OVERWRITE);

            output.writeAsText("C:\\flink\\aufgabe4\\outputByName.txt", FileSystem.WriteMode.OVERWRITE);

            env.execute();
        }
}
