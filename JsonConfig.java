import java.util.List;
import java.util.Map;

public class JsonConfig {

    private Map<String, List<String>> lines;
    private Map<String, List<String>> trips;

    public Map<String, List<String>> getLines() {
        return lines;
    }

    public Map<String, List<String>> getTrips() {
        return trips;
    }
}
