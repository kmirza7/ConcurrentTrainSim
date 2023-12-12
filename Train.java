import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Train extends Entity {

    private static Map<String, Train> trains = new HashMap<>();
    public final ReentrantLock lock = new ReentrantLock();

    public static Train make(String name) {
        if (trains.containsKey(name)) {
            return trains.get(name);
        }
        Train newTrain = new Train(name);
        trains.put(name, newTrain);
        return newTrain;
    }


    private List<String> stations;
    private int currentStation = 0;
    private int increment = 1;

    private Train(String name) {
        super(name);
    }

    public static void reset() {
        trains = new HashMap<>();
    }


    public void setStations(List<String> stations) {
        this.stations = stations;
    }

    public String getCurrentStation() {
        return stations.get(currentStation);
    }

    public Station getCurrentStationObject() {
        return Station.make(stations.get(currentStation));
    }


    public boolean isFirstStation() {
        return currentStation == 0;
    }

    public void move() {
        if (currentStation + increment == stations.size() || currentStation + increment == -1) {
            increment = -increment;
        }
        Station st = Station.make(stations.get(currentStation + increment));
        if (st.getCurrentTrain() == null) {
            Station.make(stations.get(currentStation)).setCurrentTrain(null);
            st.setCurrentTrain(this);
            currentStation += increment;
        }

    }

    public boolean containsNextStation(String station) {
        return stations.contains(station);
    }

    public String getNextStation() {
        int inc = increment;
        if (currentStation + inc == stations.size() || currentStation + inc == -1) {
            inc = -inc;
        }
        return stations.get(currentStation + inc);
    }

    public Station getNextStationObject() {
        int inc = increment;
        if (currentStation + inc == stations.size() || currentStation + inc == -1) {
            inc = -inc;
        }
        return Station.make(stations.get(currentStation + inc));
    }

    public boolean canMove(Station s1, Station s2) {
        return s1.equals(getCurrentStationObject()) && s2.equals(getNextStationObject());
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void releaseAllLocks() {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
        for (String stat : stations) {
            Station station = Station.make(stat);
            if (station.lock.isHeldByCurrentThread()) {
                station.lock.unlock();
            }
        }
    }

    public List<String> getStations() {
        return stations;
    }
}
