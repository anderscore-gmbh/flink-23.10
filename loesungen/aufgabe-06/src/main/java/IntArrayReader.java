import org.apache.flink.connector.file.src.reader.StreamFormat;
import org.apache.flink.core.fs.FSDataInputStream;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Arrays;

public class IntArrayReader implements StreamFormat.Reader<int[]> {

    private BufferedReader reader;

    public IntArrayReader(FSDataInputStream inputStream) throws UnsupportedEncodingException {
        this.reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
    }

    @Nullable
    @Override
    public int[] read() throws IOException {
        String readLine = reader.readLine();
        if (readLine == null) {
            return null;
        }
        return Arrays.stream(readLine.split(","))
                     .mapToInt(Integer::parseInt)
                     .toArray();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
