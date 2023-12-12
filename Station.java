import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Station extends Entity {
    private static Map<String, Station> stations = new HashMap<>();
    public final ReentrantLock lock = new ReentrantLock();

    public static Station make(String name) {
        if (stations.containsKey(name)) {
            return stations.get(name);
        }
        Station newStation = new Station(name);
        stations.put(name, newStation);
        return newStation;
    }

    private Train onBoardedTrain;

    private Station(String name) {
        super(name);
    }

    public static void reset() {
        stations = new HashMap<>();
    }

    public Train getCurrentTrain() {
        return onBoardedTrain;
    }

    public void setCurrentTrain(Train train) {
        this.onBoardedTrain = train;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
