import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sim {

    public static void run_sim(MBTA mbta, Log log) {
        List<Thread> trainThreads = new ArrayList<>();
        for (Train train : mbta.getTrains()) {
            Thread trainThread = new Thread(() -> runTrain(train, mbta, log));
            trainThread.start();
            trainThreads.add(trainThread);
        }

        for (Thread thread : trainThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void runTrain(Train train, MBTA mbta, Log log) {
        try {
            while (!mbta.allJourneyCompleted()) {
                mbta.boardPassenger(train, log);
                train.lock();
                try {
                    Station currentStation = train.getCurrentStationObject();
                    Station nextStation = train.getNextStationObject();

                    currentStation.lock(); 
                    try {
                        nextStation.lock(); 
                        try {
                            log.train_moves(train, currentStation, nextStation);
                            train.move(); 
                        } finally {
                            nextStation.unlock();
                        }
                    } finally {
                        currentStation.unlock();
                    }
                } finally {
                    train.unlock();
                }

                mbta.deboardPassenger(train, log);

                Thread.sleep(10); 
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        } finally {
            
            train.releaseAllLocks();
        }
    }


    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("usage: ./sim <config file>");
            System.exit(1);
        }

        MBTA mbta = new MBTA();
        mbta.loadConfig(args[0]);

        Log log = new Log();

        run_sim(mbta, log);

        String s = new LogJson(log).toJson();
        PrintWriter out = new PrintWriter("log.json");
        out.print(s);
        out.close();

        mbta.reset();
        mbta.loadConfig(args[0]);
        Verify.verify(mbta, log);

    }
}
