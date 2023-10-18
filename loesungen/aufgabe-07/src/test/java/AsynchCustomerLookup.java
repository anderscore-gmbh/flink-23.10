import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.util.Collections;

public class AsynchCustomerLookup extends RichAsyncFunction<Long, Customer> {

    private CustomerService customerService;

    @Override
    public void asyncInvoke(Long id, ResultFuture<Customer> resultFuture) throws Exception {
        resultFuture.complete(
                Collections.singleton(customerService.getCustomerById(id))
        );
    }

    @Override
    public void timeout(Long input, ResultFuture<Customer> resultFuture) throws Exception {
        super.timeout(input, resultFuture);
    }

    public AsynchCustomerLookup(String url, String username, String password) {
        DBConnectionProvider connectionProvider = new DBConnectionProvider(
                url,
                username,
                password
        );
        customerService = new CustomerService(connectionProvider);
    }

}
