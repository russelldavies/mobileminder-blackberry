package com.mmtechco.mobileminder;


public class AddAction {
    protected int employerID;
    protected int deviceID;
    protected boolean error;
    protected int type;
    protected java.lang.String timeStamp;
    protected java.lang.String status;
    protected java.lang.String destinationAddress;
    
    public AddAction() {
    }
    
    public AddAction(int employerID, int deviceID, boolean error, int type, java.lang.String timeStamp, java.lang.String status, java.lang.String destinationAddress) {
        this.employerID = employerID;
        this.deviceID = deviceID;
        this.error = error;
        this.type = type;
        this.timeStamp = timeStamp;
        this.status = status;
        this.destinationAddress = destinationAddress;
    }
    
    public int getEmployerID() {
        return employerID;
    }
    
    public void setEmployerID(int employerID) {
        this.employerID = employerID;
    }
    
    public int getDeviceID() {
        return deviceID;
    }
    
    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }
    
    public boolean isError() {
        return error;
    }
    
    public void setError(boolean error) {
        this.error = error;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public java.lang.String getTimeStamp() {
        return timeStamp;
    }
    
    public void setTimeStamp(java.lang.String timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    public java.lang.String getStatus() {
        return status;
    }
    
    public void setStatus(java.lang.String status) {
        this.status = status;
    }
    
    public java.lang.String getDestinationAddress() {
        return destinationAddress;
    }
    
    public void setDestinationAddress(java.lang.String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }
}
