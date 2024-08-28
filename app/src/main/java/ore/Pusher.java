package ore;

import ch.aplu.jgamegrid.*;
import java.awt.*;

public class Pusher extends Vehicle {
    private static final String IMAGE_FILE = "sprites/pusher.png";

    //constructor used for this version of the game, only one possible pusher
    public Pusher(boolean isAutoMode) {
        super(IMAGE_FILE, "P", isAutoMode);
    }

    //constructor which allows a pre-defined manual controller (only one pusher only)
    public Pusher(Controller controller) {
        super(IMAGE_FILE, "P", controller);
    }

    //constructor which allows a machineID
    public Pusher(String machineID, boolean isAutoMode) {
        super(IMAGE_FILE, machineID, isAutoMode);
    }

    @Override
    public boolean canMove(Location location, GameGrid gamegrid, Color borderColour) {
        // Test if try to move into border, rock or clay
        Color c = gamegrid.getBg().getColor(location);
        Rock rock = (Rock) gamegrid.getOneActorAt(location, Rock.class);
        Clay clay = (Clay) gamegrid.getOneActorAt(location, Clay.class);
        Vehicle otherVehicles = (Vehicle) gamegrid.getOneActorAt(location, Vehicle.class);

        //these objects are present, we can't move there
        if (c.equals(borderColour) || rock != null || clay != null || otherVehicles != null) {
            return false;

        } else {
            // Test if there is an ore
            Ore ore = (Ore) gamegrid.getOneActorAt(location, Ore.class);
            if (ore != null)
            {
                // Check if the ore can move and if so, move the pusher and the ore together
                ore.setDirection(this.getDirection());
                if (ore.canMove(location, gamegrid, borderColour))
                    return true;
                else
                    return false;
            }
        }

        //we can move, return true
        return true;
    }

    @Override
    public String getStatistics() {
        //return a pre-formatted string containing statistics for this vehicle
        return "Pusher-1 Moves: " + getNumMoves() + "\n";
    }
}
