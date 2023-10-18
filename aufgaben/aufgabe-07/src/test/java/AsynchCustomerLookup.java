import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.util.Collections;

public class AsynchCustomerLookup extends RichAsyncFunction<Long, Customer> {

    @Override
    public void asyncInvoke(Long id, ResultFuture<Customer> resultFuture) throws Exception {
        // bitte implementieren
    }

    @Override
    public void timeout(Long input, ResultFuture<Customer> resultFuture) throws Exception {
        super.timeout(input, resultFuture);
    }

    public AsynchCustomerLookup(String url, String username, String password) {
        // bitte implementieren
    }

}
