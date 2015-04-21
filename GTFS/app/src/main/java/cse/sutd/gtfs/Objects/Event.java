package cse.sutd.gtfs.Objects;

import java.util.ArrayList;

/**
 * Created by Francisco Furtado on 08/04/2015.
 */

public class Event {
    private String EVENT_NAME;
    private String EVENT_ID;
    private long EVENT_DATE;
    private ArrayList<String> voters;

    public Event(String name, String id){

    }

    public String getEVENT_NAME() {
        return EVENT_NAME;
    }

    public void setEVENT_NAME(String EVENT_NAME) {
        this.EVENT_NAME = EVENT_NAME;
    }

    public String getEVENT_ID() {
        return EVENT_ID;
    }

    public void setEVENT_ID(String EVENT_ID) {
        this.EVENT_ID = EVENT_ID;
    }

    public long getEVENT_DATE() {
        return EVENT_DATE;
    }

    public void setEVENT_DATE(long EVENT_DATE) {
        this.EVENT_DATE = EVENT_DATE;
    }

    public String getVoters() {
        return voters.toArray(new String[voters.size()]).toString();
    }

    public void setVoters(ArrayList<String> voters) {
        this.voters = voters;
    }
}