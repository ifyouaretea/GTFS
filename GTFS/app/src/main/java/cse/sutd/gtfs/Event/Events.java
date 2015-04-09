package cse.sutd.gtfs.Event;

/**
 * Created by Francisco Furtado on 08/04/2015.
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import javax.security.auth.Subject;

public class Events implements Subject {
    private String EVENT_NAME;

    public static final String FIRSTNAME = "firstName";

    private List<Person> observers = new ArrayList<Person>();
    private List<PropertyChangeListener> listener = new ArrayList<PropertyChangeListener>();

    @Override
    public void addObserver(Observer o) {

    }

    @Override
    public void removeObserver(Observer o) {

    }

    public class Person {

        private String firstName;

        private String lastName;

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {

            return firstName;
        }

        public void setFirstName(String firstName) {
            notifyListeners(this,
                    FIRSTNAME,
                    this.firstName,
                    this.firstName = firstName);

        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            notifyListeners(this,
                    LASTNAME,
                    this.lastName,
                    this.lastName = lastName);
        }
    }

    public List<Person> getObservers() {
        return observers;
    }

    private void notifyListeners(Object object, String property, String oldValue, String newValue) {
        for (PropertyChangeListener name : listener) {
            name.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }

    public void addChangeListener(PropertyChangeListener newListener) {
        listener.add(newListener);
    }

}