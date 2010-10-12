package org.opentox.toxotis.benchmark.gauge;

/**
 * A Gauge is used to provide a desired measurement in any Job.
 * This class must be extended for beauty purposes. The setMeasurement() method
 * must be used in any implementation in order for the gauge to store a measurement.
 *
 * @author Charalampos Chomenides
 * @author Pantelis Sopasakis
 */
public abstract class Gauge implements Cloneable{

    private String title;

    private Number measurement;
    private Number stdev;

    public Gauge(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Number getMeasurement() {
        return measurement;
    }

    public void setMeasurement(Number measurement) {
        this.measurement = measurement;
    }

    public Number getStdev() {
        return stdev;
    }

    public void setStdev(Number stdev) {
        this.stdev = stdev;
    }
  
    @Override
    public Gauge clone() throws CloneNotSupportedException {
        Gauge newGauge = (Gauge)super.clone();
        newGauge.setTitle(this.title);        
        return newGauge;
    }
//
//    @Override
//    public boolean equals (Object obj) {
//        if(!(obj instanceof Gauge)){
//            return false;
//        }
//        Gauge other = (Gauge)obj;
//        if(this.getTitle().equals(other.getTitle())){
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 89 * hash + (this.title != null ? this.title.hashCode() : 0);
//        return hash;
//    }

    
    

}
