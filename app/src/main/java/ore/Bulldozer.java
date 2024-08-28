package ore;

import ch.aplu.jgamegrid.*;
import java.awt.*;

public class Bulldozer extends Vehicle {
    private static final String IMAGE_FILE = "sprites/bulldozer.png";

    //constructor used for this version of the game, only one possible bulldozer
    public Bulldozer(boolean isAutoMode) {
        super(IMAGE_FILE, "B", isAutoMode);
    }

    //constructor which allows a machineID
    public Bulldozer(String machineID, boolean isAutoMode) {
        super(IMAGE_FILE, machineID, isAutoMode);
    }

    @Override
    public boolean canMove(Location location, GameGrid gamegrid, Color borderColour) {
        // Test if we are trying to move into a border, ore, rock or another vehicle
        Color c = gamegrid.getBg().getColor(location);
        Ore ore = (Ore) gamegrid.getOneActorAt(location, Ore.class);
        Rock rock = (Rock)gamegrid.getOneActorAt(location, Rock.class);
        Vehicle otherVehicles = (Vehicle) gamegrid.getOneActorAt(location, Vehicle.class);

        //return false if we are trying to move into these objects
        if (c.equals(borderColour) || rock != null || ore != null || otherVehicles != null) {
            return false;

        } else {
            // Test if there is a clay
            Clay clay = (Clay) gamegrid.getOneActorAt(location, Clay.class);
            if (clay != null) {
                //there is a clay, destroy it
                clay.destroy();
                incrementNumActions();
            }
        }

        //we can move into the next space
        return true;
    }

    @Override
    public String getStatistics() {
        //return a pre-formatted string containing statistics for this vehicle
        return "Bulldozer-1 Moves: " + getNumMoves() + "\nBulldozer-1 Clay removed: " + getNumActions() + "\n";
    }
}
