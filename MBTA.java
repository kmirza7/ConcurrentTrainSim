import com.google.gson.Gson;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;
public class MBTA {

    
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private Map<String, Train> trains = new HashMap<>();
    private Map<String, Passenger> passengers = new HashMap<>();

    public MBTA() {
    }

    
    public void addLine(String name, List<String> stations) {
        Train train = Train.make(name);
        train.setStations(stations);
        trains.put(name, train);
    }

    
    public void addJourney(String name, List<String> stations) {
        Passenger passenger = Passenger.make(name);
        passenger.setStations(stations);
        passengers.put(name, passenger);
    }

    
    
    public void checkStart() {
        for (Train train : trains.values()) {
            if (!train.isFirstStation()) {
                throw new IllegalStateException("Train on line " + train.getName() + " is not at the first station.");
            }
        }

        for (Passenger passenger : passengers.values()) {
            if (passenger.isBoarded(null)) {
                throw new IllegalStateException("Passenger " + passenger.getName() + " is not at their initial station.");
            }
        }

    }

    
    
    public void checkEnd() {
        for (Passenger passenger : passengers.values()) {
            if (!passenger.journeyCompleted()) {
                throw new IllegalStateException("Passenger " + passenger.getName() + " has not completed their journey.");
            }
        }
    }

    
    public void reset() {
        trains = new HashMap<>();
        passengers = new HashMap<>();
        Passenger.reset();
        Train.reset();
        Station.reset();
    }

    
//    public void loadConfig(String filename) {
//        Gson gson = new Gson();
//        File file = new File(filename);
//        Scanner sc = null;
//        try {
//            sc = new Scanner(file);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        StringBuilder json = new StringBuilder();
//        while (sc.hasNextLine()) {
//            json.append(sc.nextLine());
//        }
//        JsonConfig config = gson.fromJson(json.toString(), JsonConfig.class);
//        for (String line : config.getLines().keySet()) {
//            addLine(line, config.getLines().get(line));
//        }
//        for (String journey : config.getTrips().keySet()) {
//            addJourney(journey, config.getTrips().get(journey));
//        }
//    }

//    public void loadConfig(String filename) {
//        Gson gson = new Gson();
//        File file = new File(filename);
//
//        // Check if the file exists before attempting to read it
//        if (!file.exists()) {
//            throw new RuntimeException("File not found: " + file.getAbsolutePath());
//        }
//
//        try (Scanner sc = new Scanner(file)) {
//            StringBuilder json = new StringBuilder();
//            while (sc.hasNextLine()) {
//                json.append(sc.nextLine());
//            }
//
//            JsonConfig config = gson.fromJson(json.toString(), JsonConfig.class);
//            for (String line : config.getLines().keySet()) {
//                addLine(line, config.getLines().get(line));
//            }
//            for (String journey : config.getTrips().keySet()) {
//                addJourney(journey, config.getTrips().get(journey));
//            }
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException("FileNotFoundException: " + e.getMessage());
//        }
//    }

    public void loadConfig(String filename) {
        Gson gson = new Gson();
        File file = new File(filename);

        // Check if the file exists before attempting to read it
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + file.getAbsolutePath());
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            JsonConfig config = gson.fromJson(bufferedReader, JsonConfig.class);
            for (String line : config.getLines().keySet()) {
                addLine(line, config.getLines().get(line));
            }
            for (String journey : config.getTrips().keySet()) {
                addJourney(journey, config.getTrips().get(journey));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage());
        }
    }


    public void moveTrainChecked(Train t, Station s1, Station s2) {
        if (t.lock.tryLock()) {
            try {
                if (s1.lock.tryLock()) {
                    try {
                        if (t.canMove(s1, s2)) {
                            t.move();
                        } else {
                            throw new IllegalStateException("Train " + t.getName() + " cannot move from " + s1.getName() + " to " + s2.getName());
                        }
                    } finally {
                        s1.lock.unlock();
                    }
                }
            } finally {
                t.lock.unlock();
            }
        }
    }

    public void boardPassengerChecked(Passenger p, Train t, Station s) {
        if (t.lock.tryLock()) {
            try {
                if (s.lock.tryLock()) {
                    try {
                        if (!p.isBoarded(t) && p.getCurrentStation().equals(s.getName()) && t.containsNextStation(p.getNextStation())) {
                            p.boardTrain(t);
                        } else if (!p.journeyCompleted()) {
                            throw new IllegalStateException("Passenger " + p.getName() + " cannot board train " + t.getName() + " at station " + s.getName());
                        }
                    } finally {
                        s.lock.unlock();
                    }
                }
            } finally {
                t.lock.unlock();
            }
        }
    }

    public void deboardPassengerChecked(Passenger p, Train t, Station s) {
        if (t.lock.tryLock()) {
            try {
                if (s.lock.tryLock()) {
                    try {
                        if (p.bordedTrain(t) && p.getCurrentStation().equals(t.getCurrentStation()) && !t.containsNextStation(p.getNextStation())) {
                            p.leaveTrain(t);
                        } else if (!p.journeyCompleted()) {
                            throw new IllegalStateException("Passenger " + p.getName() + " cannot deboard train " + t.getName() + " at station " + s.getName());
                        }
                    } finally {
                        s.lock.unlock();
                    }
                }
            } finally {
                t.lock.unlock();
            }
        }
    }


    public Train[] getTrains() {
        return trains.values().toArray(new Train[0]);
    }

    public void boardPassenger(Train train, Log log) {
        if (train.lock.tryLock()) {
            try {

                for (Passenger passenger : passengers.values()) {
                    if (passenger.journeyCompleted()) {
                        continue;
                    }
                    if (!passenger.isBoarded(train) && passenger.getCurrentStation().equals(train.getCurrentStation()) && train.containsNextStation(passenger.getNextStation())) {
                        log.passenger_boards(passenger, train, Station.make(passenger.getCurrentStation()));
                        passenger.boardTrain(train);
                    }
                }

            } finally {
                train.lock.unlock();
            }
        }
    }

    public void deboardPassenger(Train train, Log log) {
        if (train.lock.tryLock()) {
            try {

                for (Passenger passenger : passengers.values()) {
                    if (passenger.journeyCompleted()) {
                        continue;
                    }
                    if (passenger.isBoarded(train) && passenger.getCurrentStation().equals(train.getCurrentStation()) && !train.containsNextStation(passenger.getNextStation())) {
                        log.passenger_deboards(passenger, train, Station.make(passenger.getCurrentStation()));
                        passenger.leaveTrain(train);
                    }
                }

            } finally {
                train.lock.unlock();
            }
        }
    }

    public boolean allJourneyCompleted() {
        readLock.lock();
        try {
            for (Passenger passenger : passengers.values()) {
                if (!passenger.journeyCompleted()) {
                    return false;
                }
            }
            return true;
        } finally {
            readLock.unlock();
        }
    }

    public Map<String, Train> getTrainsMap() {
        return trains;
    }
}
