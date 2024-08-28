package ore;

public abstract class Controller {
    private final String vehicleID;

    public Controller(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public String getVehicleID() {
        return vehicleID;
    }

    //define a generic method to accept any input
    public abstract <T> String getMove(T move);
}
