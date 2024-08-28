package ore;

import ch.aplu.jgamegrid.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Properties;

public class OreSim extends GameGrid implements GGKeyListener
{
    public enum ElementType{
        OUTSIDE("OS"), EMPTY("ET"), BORDER("BD"),
        PUSHER("P"), BULLDOZER("B"), EXCAVATOR("E"), ORE("O"),
        ROCK("R"), CLAY("C"), TARGET("T");
        private String shortType;

        ElementType(String shortType) {
            this.shortType = shortType;
        }

        public String getShortType() {
            return shortType;
        }

        public static ElementType getElementByShortType(String shortType) {
            ElementType[] types = ElementType.values();
            for (ElementType type: types) {
                if (type.getShortType().equals(shortType)) {
                    return type;
                }
            }

            return ElementType.EMPTY;
        }
    }

    //local variables used during the playing of oresim
    private static final double ONE_SECOND = 1000.0;
    private MapGrid grid;
    private final int nbHorzCells, nbVertCells;
    private final Color borderColor = new Color(100, 100, 100),
            defaultColor = new Color(230, 230, 230), outsideColor = Color.lightGray;

    private ArrayList<Ore> ores = new ArrayList<>();
    private ArrayList<Target> targets = new ArrayList<>();
    private ArrayList<Vehicle> vehicles = new ArrayList<>();
    private boolean isFinished = false;
    private final Properties properties;
    private final boolean isAutoMode;
    private double gameDuration;
    private final List<String> controls;
    private int movementIndex;
    private StringBuilder logResult = new StringBuilder();

    //define a manual controller using arrow keys for the pusher
    private final ManualController<Integer> pusherController = new ManualController<>("P", KeyEvent.VK_LEFT,
            KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN);

    public OreSim(Properties properties, MapGrid grid) {
        //create the board and set up the game environment
        super(grid.getNbHorzCells(), grid.getNbVertCells(), 30, false);
        this.grid = grid;
        nbHorzCells = grid.getNbHorzCells();
        nbVertCells = grid.getNbVertCells();
        this.properties = properties;

        isAutoMode = properties.getProperty("movement.mode").equals("auto");
        gameDuration = Integer.parseInt(properties.getProperty("duration"));
        setSimulationPeriod(Integer.parseInt(properties.getProperty("simulationPeriod")));
        controls = Arrays.asList(properties.getProperty("machines.movements").split(","));
    }

    /**
     * Check the number of ores that are collected and see if it matches the number of targets on the board
     * @return number of targets currently filled
     */
    private int checkOresDone() {
        int nbTarget = 0;
        for (Ore ore : ores) {
            if (ore.getIdVisible() == 1)
                nbTarget++;
        }

        return nbTarget;
    }

    /**
     * The main method to run the game
     * @param isDisplayingUI
     * @return logresult of the overall game play
     */
    public String runApp(boolean isDisplayingUI) {
        //draw the board on the screen
        GGBackground bg = getBg();
        int autoMovementIndex = 0;
        drawBoard(bg);
        drawActors();
        addKeyListener(this);

        if (isDisplayingUI) {
            show();
        }

        if (isAutoMode) {
            doRun();
        }

        //run the simulation until all ores have been collected or until the timer runs out
        int oresDone = checkOresDone();
        while(oresDone < grid.getNbOres() && gameDuration >= 0) {
            try {
                Thread.sleep(simulationPeriod);
                double minusDuration = (simulationPeriod / ONE_SECOND);
                gameDuration -= minusDuration;
                String title = String.format("# Ores at Target: %d. Time left: %.2f seconds", oresDone, gameDuration);
                setTitle(title);

                //send automatic movements from the controls string to each vehicle
                //vehicle determines if it can move
                if (isAutoMode && (autoMovementIndex < controls.size())) {
                    for (Vehicle vehicle : vehicles) {
                        vehicle.moveObject(controls.get(autoMovementIndex), this, borderColor);
                    }

                    autoMovementIndex += 1;
                    refresh();
                    updateLogResult();
                }

                oresDone = checkOresDone();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //game has now ended, check if the player has been successful
        doPause();
        if (oresDone == grid.getNbOres()) {
            setTitle("Mission Complete. Well done!");
        } else if (gameDuration < 0) {
            setTitle("Mission Failed. You ran out of time");
        }

        updateStatistics();
        isFinished = true;
        return logResult.toString();
    }

    /**
     * Transform the list of actors to a string of location for a specific kind of actor. Used to generate
     * log results.
     * @param actors takes the list of actors on the board
     * @return string of actor locations
     */
    private String actorLocations(List<Actor> actors) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean hasAddedColon = false;
        boolean hasAddedLastComma = false;

        for (Actor actor : actors) {
            if (actor.isVisible()) {
                if (!hasAddedColon) {
                    stringBuilder.append(":");
                    hasAddedColon = true;
                }
                stringBuilder.append(actor.getX() + "-" + actor.getY());
                stringBuilder.append(",");
                hasAddedLastComma = true;
            }
        }

        if (hasAddedLastComma) {
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");
        }

        return stringBuilder.toString();
    }


    /**
     * Writes statistics from the gameplay at the end of the game for all the vehicles on the board
     */
    private void updateStatistics() {
        File statisticsFile = new File("statistics.txt");
        FileWriter fileWriter = null;

        try {
            //write statistics in the order of: Pusher, Excavator then Bulldozer
            fileWriter = new FileWriter(statisticsFile);
            for (Vehicle vehicle : vehicles) {
                if (vehicle instanceof Pusher) {
                    fileWriter.write(vehicle.getStatistics());
                }
            }

            for (Vehicle vehicle : vehicles) {
                if (vehicle instanceof Excavator) {
                    fileWriter.write(vehicle.getStatistics());
                }
            }

            for (Vehicle vehicle : vehicles) {
                if (vehicle instanceof Bulldozer) {
                    fileWriter.write(vehicle.getStatistics());
                }
            }

        } catch (IOException ex) {
            System.out.println("Cannot write to file - e: " + ex.getLocalizedMessage());

        } finally {
            try {
                assert fileWriter != null;
                fileWriter.close();

            } catch (IOException ex) {
                System.out.println("Cannot close file - e: " + ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Draw all different actors on the board: pusher, ore, target, rock, clay, bulldozer, excavator
     * Also add the actors to an ArrayList if we are keeping track of them
     */
    private void drawActors() {
        //loop through every cell on the board
        for (int y = 0; y < nbVertCells; y++) {
            for (int x = 0; x < nbHorzCells; x++) {
                //get the type of element which is currently on the board at the specified location
                Location location = new Location(x, y);
                OreSim.ElementType a = grid.getCell(location);

                switch (a) {
                    case PUSHER:
                        //create a new pusher and save it to the arraylist
                        Pusher pusher;
                        if (isAutoMode) {
                            //automatic mode, let pusher create an autocontroller
                            pusher = new Pusher(isAutoMode);
                        } else {
                            //assign a manual controller to the pusher
                            pusher = new Pusher(pusherController);
                        }
                        addActor(pusher, location);
                        vehicles.add(pusher);
                        break;

                    case BULLDOZER:
                        //create a new bulldozer and save to the arraylist
                        Bulldozer bulldozer = new Bulldozer(isAutoMode);
                        addActor(bulldozer, location);
                        vehicles.add(bulldozer);
                        break;

                    case EXCAVATOR:
                        //create a new excavator and save to the arraylist
                        Excavator excavator = new Excavator(isAutoMode);
                        addActor(excavator, location);
                        vehicles.add(excavator);
                        break;

                    case ORE:
                        //create a new ore and save to the arraylist
                        Ore ore = new Ore();
                        addActor(ore, location);
                        ores.add(ore);
                        break;

                    case TARGET:
                        //create a new target and save to the arraylist
                        Target target = new Target();
                        addActor(target, location);
                        targets.add(target);
                        break;

                    case ROCK:
                        //add a new rock to the map
                        addActor(new Rock(), location);
                        break;

                    case CLAY:
                        //add a new clay to the map
                        addActor(new Clay(), location);
                        break;
                }
            }
        }
    }

    /**
     * Draw the basic board with outside color and border color
     * @param bg background for the current game grid
     */
    private void drawBoard(GGBackground bg)
    {
        //colour the entire background of the board in the pre-defined background colour
        bg.clear(defaultColor);
        bg.setPaintColor(outsideColor);

        //navigate through all cells
        for (int y = 0; y < nbVertCells; y++) {
            for (int x = 0; x < nbHorzCells; x++) {
                Location location = new Location(x, y);
                ElementType a = grid.getCell(location);

                //colour the background cells according to their board type
                if (a != ElementType.OUTSIDE) {
                    bg.fillCell(location, outsideColor);
                } if (a == ElementType.BORDER)  // Border
                    bg.fillCell(location, borderColor);
            }
        }
    }

    /**
     * The method is automatically called by the framework when a key is pressed. OreSim will send this input to
     * all vehicles if it is in manual mode so the vehicle which uses that input key can move.
     * @param evt the id of the key which has just been pressed
     * @return true if the input has been accepted
     */
    public boolean keyPressed(KeyEvent evt)
    {
        if (isFinished)
            return true;

        if (!isAutoMode) {
            //send the keycode to all vehicles to see if any can move
            int keyCode = evt.getKeyCode();
            for (Vehicle vehicle : vehicles) {
                vehicle.moveObject(keyCode, this, borderColor);
            }

            refresh();
            return true;
        }

        return true;
    }

    //default method for key being released, not being used in this version
    public boolean keyReleased(KeyEvent evt) {
        return true;
    }

    /**
     * The method will generate a log result for all the movements of all actors
     * This is updated at the end of each move whilst the game is running
     */
    private void updateLogResult() {
        movementIndex++;
        List<Actor> pushers = getActors(Pusher.class);
        List<Actor> ores = getActors(Ore.class);
        List<Actor> targets = getActors(Target.class);
        List<Actor> rocks = getActors(Rock.class);
        List<Actor> clays = getActors(Clay.class);
        List<Actor> bulldozers = getActors(Bulldozer.class);
        List<Actor> excavators = getActors(Excavator.class);

        logResult.append(movementIndex + "#");
        logResult.append(ElementType.PUSHER.getShortType()).append(actorLocations(pushers)).append("#");
        logResult.append(ElementType.ORE.getShortType()).append(actorLocations(ores)).append("#");
        logResult.append(ElementType.TARGET.getShortType()).append(actorLocations(targets)).append("#");
        logResult.append(ElementType.ROCK.getShortType()).append(actorLocations(rocks)).append("#");
        logResult.append(ElementType.CLAY.getShortType()).append(actorLocations(clays)).append("#");
        logResult.append(ElementType.BULLDOZER.getShortType()).append(actorLocations(bulldozers)).append("#");
        logResult.append(ElementType.EXCAVATOR.getShortType()).append(actorLocations(excavators));

        logResult.append("\n");
    }
}
