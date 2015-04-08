package cse.sutd.gtfs.Event;

/**
 * Created by Francisco Furtado on 08/04/2015.
 */

import android.util.Log;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class EventsObserver implements PropertyChangeListener {
    public EventsObserver(Events model) {
        model.addChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        Log.d("change","Changed property: " + event.getPropertyName() + " [old -> "
                + event.getOldValue() + "] | [new -> " + event.getNewValue() + "]");
    }
}