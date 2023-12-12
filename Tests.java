import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class Tests {
    @Test
    public void testPass() {
        assertTrue("true should be true", true);
    }

    @Test
    public void testPassengerJourneysLoadedCorrectly() {
        MBTA mbta = new MBTA();
        mbta.loadConfig("sample.json");
        System.out.println("Successful");
    }


}
