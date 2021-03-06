package main.mock;

import java.util.Date;

public class HotelMock implements Hotel {

    public HotelMock() {

    }

    @Override
    public int getPrice() {
        return (int) (Math.random()*100);
    }

    @Override
    public String getLocation() {
        return "";
    }

    @Override
    public Date getCheckinDate() {
        return new Date();
    }

    @Override
    public Date getCheckoutDate() {
        return new Date();
    }
}
