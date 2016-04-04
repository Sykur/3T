package test;

import main.TripCombo;
import main.TripPlanner;
import org.junit.*;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

public class TripPlannerTest {

    private TripPlanner tp;

    @Before
    public void setup() {
        String depLoc = "KEF";
        String destLoc = "CPH";
        Date depTime = new Date(2016, 06, 10);
        Date returnTime = new Date(2016, 06, 24);
        boolean oneWay = false;
        int numPeople = 2;
        int priceLower = 50000;
        int priceHigher = 200000;
        int tourDuration = 3;
        
        tp = new TripPlanner(depLoc, destLoc, depTime, returnTime, numPeople, priceLower, priceHigher, tourDuration);
    }

    @After
    public void tearDown() {
        tp = null;
    }

    @Test
    public void testPriceSum() {
        List<TripCombo> combos = tp.suggestCombos();

        for(TripCombo combo: combos) {
            assertTrue(combo.getPrice() < tp.getPriceHigher());
        }
    }

    @Test
    public void testDates() {
        List<TripCombo> combos = tp.suggestCombos();

        for(TripCombo combo: combos) {
            assertTrue(tp.getDepTime().compareTo(combo.getFlight().getDepartureDate()) <= 0);
            assertTrue(tp.getReturnTime().compareTo(combo.getFlight().getReturnDate()) >= 0);
        }
    }

    @Test
    public void testHotelDates() {
        List<TripCombo> combos = tp.suggestCombos();

        for(TripCombo combo: combos) {
            assertTrue(tp.getDepTime().compareTo(combo.getHotel().getCheckinDate()) == 0);
            assertTrue(tp.getReturnTime().compareTo(combo.getHotel().getCheckoutDate()) == 0);
        }
    }

    @Test
    public void testTourDates() {
        List<TripCombo> combos = tp.suggestCombos();

        for(TripCombo combo: combos) {
            assertTrue(tp.getDepTime().compareTo(combo.getTour().getStartDate()) <= 0);
            assertTrue(tp.getReturnTime().compareTo(combo.getTour().getEndDate()) >= 0);
        }
    }

    @Test
    public void testHotelLocation() {
        List<TripCombo> combos = tp.suggestCombos();

        for(TripCombo combo: combos) {
            assertTrue(tp.getDestLocation().equals(combo.getTour().getLocation()));
        }
    }

    @Test
    public void testTourLocation() {
        List<TripCombo> combos = tp.suggestCombos();

        for(TripCombo combo: combos) {
            assertTrue(tp.getDestLocation().equals(combo.getHotel().getLocation()));
        }
    }
}
