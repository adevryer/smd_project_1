package ore;

public class AutoController extends Controller {

    public AutoController(String vehicleID) {
        super(vehicleID);
    }

    @Override
    public <T> String getMove(T t) {
        //for this version of oresim, we are only accepting string inputs in the format listed
        //in the properties file
        //in future, this method allows for any type of input so it can be extended
        if (t instanceof String instruction) {
            String[] details = instruction.split("-");
            String vehicle = details[0], move = details[1];

            //if the movement string specified is for this vehicle, return the direction this vehicle can move in
            if (vehicle.equals(this.getVehicleID())) {
                return move;
            }
        }

        return null;
    }
}
