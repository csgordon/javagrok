package vfm;

import java.util.LinkedList;
import java.util.List;

public class VehicleFleetMgmt {

	private final List<Vehicle> list;
   
    
    public VehicleFleetMgmt() {
        this.list = new LinkedList<Vehicle>();
    }
   
    
                                       
    public void add(Vehicle v) {
        if (v.getNumberOfWheels() < 2) {
            throw new IllegalArgumentException("No Unicycles");
        }
       
        list.add(v);
    }
   
    
    public List<Vehicle> getAllVehicles() {
        return list;
    }
}
