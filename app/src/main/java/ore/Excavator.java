package ore;

import ch.aplu.jgamegrid.*;
import java.awt.*;

public class Excavator extends Vehicle {
    private static final String IMAGE_FILE = "sprites/excavator.png";

    //constructor used for this version of the game, only one possible excavator
    public Excavator(boolean isAutoMode) {
        super(IMAGE_FILE, "E", isAutoMode);
    }

    //constructor which allows a machineID
    public Excavator(String machineID, boolean isAutoMode) {
        super(IMAGE_FILE, machineID, isAutoMode);
    }

    @Override
    public boolean canMove(Location location, GameGrid gamegrid, Color borderColour) {
        // Test if we are trying to move into a border, ore, clay or another vehicle
        Color c = gamegrid.getBg().getColor(location);
        Ore ore = (Ore) gamegrid.getOneActorAt(location, Ore.class);
        Clay clay = (Clay) gamegrid.getOneActorAt(location, Clay.class);
        Vehicle otherVehicles = (Vehicle) gamegrid.getOneActorAt(location, Vehicle.class);

        //if we are trying to move into these objects, return false and do not move
        if (c.equals(borderColour) || ore != null || clay != null || otherVehicles != null) {
            return false;

        } else {
            // Test if there is a rock to destroy
            Rock rock = (Rock) gamegrid.getOneActorAt(location, Rock.class);
            if (rock != null) {
                //destroy the rock before moving and keep track of the number of actions
                rock.destroy();
                incrementNumActions();
            }
        }

        //we can move into the next space
        return true;
    }

    @Override
    public String getStatistics() {
        //return a pre-formatted string containing statistics for this vehicle
        return "Excavator-1 Moves: " + getNumMoves() + "\nExcavator-1 Rock removed: " + getNumActions() + "\n";
    }
}
