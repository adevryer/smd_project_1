package ore;

import ch.aplu.jgamegrid.*;
import java.awt.*;
import java.util.List;

public class Ore extends Element implements Movable {
    private static final String FILEPATH = "sprites/ore.png";
    private static final int NUM_SPRITES = 2;

    public Ore() {
        super(FILEPATH, NUM_SPRITES);
    }

    @Override
    public boolean canMove(Location location, GameGrid gamegrid, Color borderColour) {
        Location next = this.getNextMoveLocation();
        // Test if the ore is moving into a border, rock, clay or another vehicle
        Color c = gamegrid.getBg().getColor(next);
        Element otherElements = (Element)gamegrid.getOneActorAt(next, Element.class);
        Vehicle otherVehicles = (Vehicle) gamegrid.getOneActorAt(next, Vehicle.class);

        if (c.equals(borderColour) || otherElements != null || otherVehicles != null) {
            return false;
        }

        // Reset the target if the ore is moved out of target
        Location currentLocation = this.getLocation();
        List<Actor> actors = gamegrid.getActorsAt(currentLocation);
        if (actors != null) {
            for (Actor actor : actors) {
                if (actor instanceof Target currentTarget) {
                    currentTarget.show();
                    this.show(0);
                }
            }
        }

        // Move the ore
        this.setLocation(next);

        // Check if we are at a target
        Target nextTarget = (Target)gamegrid.getOneActorAt(next, Target.class);
        if (nextTarget != null) {
            //hide the target and show the treasure image for the ore
            this.show(1);
            nextTarget.hide();
        } else {
            this.show(0);
        }

        return true;
    }
}
