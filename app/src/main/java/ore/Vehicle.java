package ore;
import ch.aplu.jgamegrid.*;

import java.awt.*;

public abstract class Vehicle extends Actor implements Movable {
    private final String vehicleID;
    private int numMoves = 0, numActions = 0;
    private Controller controller = null;

    public Vehicle(String imageFile, String vehicleID, boolean isAutoMode) {
        //create a vehicle without a controller
        super(true, imageFile);
        this.vehicleID = vehicleID;

        //if the movement mode is auto, create an automatic controller for the vehicle
        if (isAutoMode) {
            controller = new AutoController(vehicleID);
        }
    }

    public Vehicle(String imageFile, String vehicleID, Controller controller) {
        //create a vehicle with a pre-defined manual controller
        super(true, imageFile);
        this.vehicleID = vehicleID;
        this.controller = controller;
    }

    public <T> void moveObject(T move, GameGrid gamegrid, Color borderColour) {
        if (controller != null) {
            //send move to the controller to verify if this object can move
            String nextLocation = controller.getMove(move);

            if (nextLocation != null) {
                Location next = null;

                //set the next object location according to the specified direction
                switch (nextLocation) {
                    case "L":
                        next = getLocation().getNeighbourLocation(Location.WEST);
                        setDirection(Location.WEST);
                        break;
                    case "U":
                        next = getLocation().getNeighbourLocation(Location.NORTH);
                        setDirection(Location.NORTH);
                        break;
                    case "R":
                        next = getLocation().getNeighbourLocation(Location.EAST);
                        setDirection(Location.EAST);
                        break;
                    case "D":
                        next = getLocation().getNeighbourLocation(Location.SOUTH);
                        setDirection(Location.SOUTH);
                        break;
                }

                //check if the object can actually move into the next space
                if (next != null && canMove(next, gamegrid, borderColour)) {
                    //if possible, move the object and keep track of the number of moves
                    setLocation(next);
                    incrementNumMoves();
                }
            }
        }
    }


    public abstract String getStatistics();

    //getters and setters for the number of moves and actions
    private void incrementNumMoves() {
        numMoves += 1;
    }

    protected void incrementNumActions() {
        numActions += 1;
    }

    protected int getNumMoves() {
        return numMoves;
    }

    protected int getNumActions() {
        return numActions;
    }
}
