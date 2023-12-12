import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Passenger extends Entity {

  private static Map<String, Passenger> passengers = new HashMap<>();
  private List<String> stations;

  private Train boardedTrain;

  private Passenger(String name) { super(name); }


  public static Passenger make(String name) {
    if (passengers.containsKey(name)) {
      return passengers.get(name);
    }
    Passenger newPassenger= new Passenger(name);
    passengers.put(name, newPassenger);
    return newPassenger;
  }

  public static void reset() {
    passengers = new HashMap<>();
  }

  public void setStations(List<String> stations) {
    this.stations = stations;
  }

  public boolean isBoarded(Train train) {
    return boardedTrain != null && train != null && boardedTrain.getName().equals(train.getName());
  }

  public boolean journeyCompleted() {
    return stations.isEmpty();
  }

  public void boardTrain(Train train) {
    if (train.getCurrentStation().equals(stations.get(0)) && train.containsNextStation(stations.get(1))) {
      boardedTrain = train;
      stations.remove(0);
    }
  }

  public void leaveTrain(Train train){
    if(stations.size() == 1){
      stations.remove(0);
    }
  }

  public String getCurrentStation() {
    return stations.get(0);
  }

  public String getNextStation() {
    if(stations.size() == 1){
      return null;
    }
    return stations.get(1);
  }

  public boolean bordedTrain(Train t) {
    return boardedTrain.equals(t);
  }
}
