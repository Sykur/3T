package main;

import main.mock.*;
import main.service.FlightSearchService;
import main.service.HotelFinderService;
import main.service.TourSearchService;
import main.util.Util;

import java.lang.reflect.Array;
import java.util.*;

public class TripPlanner {

	/* General traveller parameters */
	private String depLocation;
	private String destLocation;
	private Date depTime;
	private Date returnTime;
	private int numPeople;
	private int priceLower;
	private int priceHigher;

	/* Flight specific parameters */
	private boolean oneWay;
	private String[] excludedAirlines;
	private String[] excludedHotels;

	/* Tour specific parameters */
	private int tourDurationLower;
	private int tourDurationHigher;
	private List<String> tourType;
	private int minTourRating;
	private boolean tourHotelPickup;

	private FlightSearchService flightSearch;
	private HotelFinderService hotelSearch;
	private TourSearchService tourSearch;

	public TripPlanner() {
		initServices();
	}

	public TripPlanner(String depLocation, String destLocation, Date depTime, Date returnTime, int numPeople,
					   int priceLower, int priceHigher, boolean oneWay, String[] excludedAirlines,
					   String[] excludedHotels, int tourDurationLower, int tourDurationHigher, List<String> tourType,
					   int minTourRating, boolean tourHotelPickup) {

		this.depLocation = depLocation;
		this.destLocation = destLocation;
		this.depTime = depTime;
		this.returnTime = returnTime;
		this.numPeople = numPeople;
		this.priceLower = priceLower;
		this.priceHigher = priceHigher;
		this.oneWay = oneWay;
		this.excludedAirlines = excludedAirlines;
		this.excludedHotels = excludedHotels;
		this.tourDurationLower = tourDurationLower;
		this.tourDurationHigher = tourDurationHigher;
		this.tourType = tourType;
		this.minTourRating = minTourRating;
		this.tourHotelPickup = tourHotelPickup;

		initServices();
	}

	public TripPlanner(String depLocation, String destLocation, Date depTime, Date returnTime, int numPeople,
					   int priceLower, int priceHigher, int tourDurationHigher) {

		this.depLocation = depLocation;
		this.destLocation = destLocation;
		this.depTime = depTime;
		this.returnTime = returnTime;
		this.numPeople = numPeople;
		this.priceLower = priceLower;
		this.priceHigher = priceHigher;
		this.tourDurationHigher = tourDurationHigher;

		initServices();
	}

	private void initServices() {
		flightSearch = new FlightSearchMock();
		hotelSearch = new HotelFinderMock(depTime.toString(), returnTime.toString());
		tourSearch = new TourSearchMock();
	}

	public Map<String, Collection> search() {
		// TODO: implement this
		return new HashMap<String, Collection>();
	}

	public List<TripCombo> suggestCombos(int numCombos) {
		// A list of combos to return
		List<TripCombo> combos = new ArrayList<>(numCombos);

		// Get a list of flights to and from the destination
		ArrayList<Flight> depFlightList = searchOutboundFlights();
		ArrayList<Flight> returnFlightList = searchInboundFlights();

		// Don't allow flight prices to be more than 1/3 of the max price
		depFlightList = flightSearch.filterFlightList(depFlightList, null, null, false, false, false, priceHigher/3);
		returnFlightList = flightSearch.filterFlightList(returnFlightList, null, null, false, false, false, priceHigher/3);

		// Get a list of hotels near the destination
		List<Hotel> hotelList = hotelSearch.getFreeRoomsFromHotelLocation(destLocation);

		for(int i=0; i<numCombos; i++) {
			// Pick arbitrary pairs of flights
			int depIndex = Util.randomIndex(depFlightList);
			int returnIndex = Util.randomIndex(returnFlightList);
			Flight depFlight = depFlightList.get(depIndex);
			Flight returnFlight = returnFlightList.get(returnIndex);

			int priceRemaining = priceHigher - depFlight.getPrice() - returnFlight.getPrice();

			// Pick an arbitrary hotel, given that it fits our price constraints
			int hotelIndex = Util.randomIndex(hotelList);
			while(hotelList.get(hotelIndex).getPrice() > priceRemaining * 2/3) {
				hotelIndex = Util.randomIndex(hotelList);
			}
			Hotel hotel = hotelList.get(hotelIndex);

			priceRemaining -= hotel.getPrice();

			// Pick a tour for the rest of the money
			String type = tourType.get(Util.randomIndex(tourType));
			List<Tour> tourList = tourSearch.createList(0, priceRemaining, tourDurationLower, tourDurationHigher,
					depTime, returnTime, numPeople, destLocation, null, type, minTourRating, tourHotelPickup, null);
			int tourIndex = Util.randomIndex(tourList);
			while(tourList.get(tourIndex).getPrice() > priceRemaining) {
				tourIndex = Util.randomIndex(tourList);
			}
			Tour tour = tourList.get(tourIndex);

			// Finally add our findings to the list of combos
			combos.add(new TripCombo(depFlight, returnFlight, hotel, tour, numPeople));
		}

		return combos;
	}

	public TripCombo suggestBestCombo() {
		ArrayList<Flight> depFlightList = searchOutboundFlights();
		ArrayList<Flight> returnFlightList = searchInboundFlights();
		Flight depCheapest = depFlightList.get(0);
		Flight returnCheapest = returnFlightList.get(0);	// Lists are ordered by price ascending

		List<Hotel> hotelList = hotelSearch.getFreeRoomsFromHotelLocation(destLocation);
		Hotel hotelCheapest = findCheapestHotel(hotelList);
		int roomID = 0; // TODO: Get a room ID somehow

		Tour tourCheapest = findCheapestTour();

		return new TripCombo(depCheapest, returnCheapest, hotelCheapest, tourCheapest, numPeople);
	}

	private ArrayList<Flight> searchOutboundFlights() {
		return flightSearch.searchFlightByCriteria(depTime.toString(), depLocation, destLocation, numPeople);
	}

	private ArrayList<Flight> searchInboundFlights() {
		return flightSearch.searchFlightByCriteria(returnTime.toString(), destLocation, depLocation, numPeople);
	}

	// May be unneccessary since the flight list is obtained in ascending order by price
	private Flight findCheapestFlight(List<Flight> flights) {
		Flight cheapest = null;

		int minPrice = Integer.MAX_VALUE;
		for(Flight flight: flights) {
			if(flight.getPrice() < minPrice) {
				cheapest = flight;
				minPrice = flight.getPrice();
			}
		}

		return cheapest;
	}

	private Hotel findCheapestHotel(List<Hotel> hotels) {
		Hotel cheapest = null;

		int minPrice = Integer.MAX_VALUE;
		for(Hotel hotel: hotels) {
			if(hotel.getPrice() < minPrice) {
				cheapest = hotel;
				minPrice = hotel.getPrice();
			}
		}

		return cheapest;
	}

	private Tour findCheapestTour(){
		Tour cheapest = null;

		int minPrice = Integer.MAX_VALUE;
		for(String type: tourType) {
			List<Tour> tours = tourSearch.createList(0, priceHigher, tourDurationLower, tourDurationHigher,
					depTime, returnTime, numPeople, destLocation, null, type, minTourRating, tourHotelPickup, null);

			for(Tour tour: tours) {
				if (tour.getPrice() < minPrice) {
					cheapest = tour;
					minPrice = tour.getPrice();
				}
			}
		}
		return cheapest;
	}



	// Getters and setters


	public String getDepLocation() {
		return depLocation;
	}

	public void setDepLocation(String depLocation) {
		this.depLocation = depLocation;
	}

	public String getDestLocation() {
		return destLocation;
	}

	public void setDestLocation(String destLocation) {
		this.destLocation = destLocation;
	}

	public Date getDepTime() {
		return depTime;
	}

	public void setDepTime(Date depTime) {
		this.depTime = depTime;
	}

	public Date getReturnTime() {
		return returnTime;
	}

	public void setReturnTime(Date returnTime) {
		this.returnTime = returnTime;
	}

	public int getNumPeople() {
		return numPeople;
	}

	public void setNumPeople(int numPeople) {
		this.numPeople = numPeople;
	}

	public int getPriceLower() {
		return priceLower;
	}

	public void setPriceLower(int priceLower) {
		this.priceLower = priceLower;
	}

	public int getPriceHigher() {
		return priceHigher;
	}

	public void setPriceHigher(int priceHigher) {
		this.priceHigher = priceHigher;
	}

	public boolean isOneWay() {
		return oneWay;
	}

	public void setOneWay(boolean oneWay) {
		this.oneWay = oneWay;
	}

	public String[] getExcludedAirlines() {
		return excludedAirlines;
	}

	public void setExcludedAirlines(String[] excludedAirlines) {
		this.excludedAirlines = excludedAirlines;
	}

	public String[] getExcludedHotels() {
		return excludedHotels;
	}

	public void setExcludedHotels(String[] excludedHotels) {
		this.excludedHotels = excludedHotels;
	}

	public int getTourDurationLower() {
		return tourDurationLower;
	}

	public void setTourDurationLower(int tourDurationLower) {
		this.tourDurationLower = tourDurationLower;
	}

	public int getTourDurationHigher() {
		return tourDurationHigher;
	}

	public void setTourDurationHigher(int tourDurationHigher) {
		this.tourDurationHigher = tourDurationHigher;
	}

	public List<String> getTourType() {
		return tourType;
	}

	public void setTourType(List<String> tourType) {
		this.tourType = tourType;
	}

	public int getMinTourRating() {
		return minTourRating;
	}

	public void setMinTourRating(int minTourRating) {
		this.minTourRating = minTourRating;
	}

	public boolean isTourHotelPickup() {
		return tourHotelPickup;
	}

	public void setTourHotelPickup(boolean tourHotelPickup) {
		this.tourHotelPickup = tourHotelPickup;
	}
}