import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class OrderProcessFunction extends KeyedCoProcessFunction<Long, Customer, Order, String> {

    private ValueState<String> customerName;
    private OutputTag<String> outputTag;

    public OrderProcessFunction(OutputTag<String> outputTag) {
        this.outputTag = outputTag;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        customerName = getRuntimeContext().getState(new ValueStateDescriptor<>("customers", String.class));
    }

    @Override
    public void processElement1(Customer customer, KeyedCoProcessFunction<Long, Customer, Order, String>.Context context, Collector<String> collector) throws Exception {
        if (customerName.value() == null) {
            customerName.update(customer.getName());
            System.out.println("Adding state for customer" + customer.getId() + " "  +customer.getName());
        }
    }

    @Override
    public void processElement2(Order order, KeyedCoProcessFunction<Long, Customer, Order, String>.Context context, Collector<String> collector) throws Exception {
        String nameRepresentation = customerName.value() == null ? "(name not found)" : customerName.value();
        collector.collect(nameRepresentation + ":" + order.getValue());
        context.output(outputTag, order.getId() + ":" + order.getValue());
    }
}
