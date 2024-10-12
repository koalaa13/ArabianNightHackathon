package api;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import model.IntPoint;
import model.Point;

public class IntPointSerializer extends JsonSerializer<Point> {
    @Override
    public void serialize(Point value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        IntPoint intPoint = new IntPoint((int) value.x, (int) value.y);
        gen.writeObject(intPoint);
    }
}
