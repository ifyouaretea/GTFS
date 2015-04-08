package cse.sutd.gtfs.Event;

/**
 * Created by Francisco Furtado on 08/04/2015.
 */
public interface Subject {
    public void addObserver( Observer o );
    public void removeObserver( Observer o );
}
