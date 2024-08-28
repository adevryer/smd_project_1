package ore;

import ch.aplu.jgamegrid.*;
import java.awt.*;

public interface Movable {
    boolean canMove(Location location, GameGrid gamegrid, Color borderColour);
}
