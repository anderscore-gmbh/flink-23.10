import org.apache.flink.api.common.serialization.Encoder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class IntArrayEncoder implements Encoder<int[]> {
    @Override
    public void encode(int[] ints, OutputStream outputStream) throws IOException {
        outputStream.write(
            Arrays.stream(ints)
                  .mapToObj(String::valueOf)
                  .collect(Collectors.joining("+"))
                  .getBytes(StandardCharsets.UTF_8)
        );
        outputStream.write(10);
    }
}
