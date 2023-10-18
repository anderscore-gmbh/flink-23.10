import org.apache.flink.api.common.typeinfo.PrimitiveArrayTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.src.reader.SimpleStreamFormat;
import org.apache.flink.core.fs.FSDataInputStream;

import java.io.IOException;

public class IntArrayStreamFormat extends SimpleStreamFormat<int[]> {
    private static final long serialVersionUID = 1L;
    @Override
    public Reader<int[]> createReader(Configuration configuration, FSDataInputStream fsDataInputStream) throws IOException {
        return new IntArrayReader(fsDataInputStream);
    }

    @Override
    public TypeInformation<int[]> getProducedType() {
        return PrimitiveArrayTypeInfo.INT_PRIMITIVE_ARRAY_TYPE_INFO;
    }
}
