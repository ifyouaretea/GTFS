package cse.sutd.gtfs.Objects;

/**
 * Created by Francisco Furtado on 16/04/2015.
 */
public class Contact {
    private String number;
    private String name;
    private String status;


    public Contact(String number){
        this.number = number;
    }

    public String getNumber() {return number; }

    public void setNumber(String number) {this.number = number;    }

    public String getName() {return name;    }

    public void setName(String name) {this.name = name;    }

    public String getStatus() {return status;    }

    public void setStatus(String status) {this.status = status;    }
}
