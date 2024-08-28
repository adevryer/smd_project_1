package ore;

public class ManualController <S> extends Controller {
    private final S left, right, up, down;
    private static final String LEFT = "L", RIGHT = "R", UP = "U", DOWN = "D";

    //define a manual controller which accepts any type of input for keys
    //allows for future extension with other controllers
    public ManualController(String vehicleID, S left, S right, S up, S down) {
        super(vehicleID);
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
    }

    @Override
    public <T> String getMove(T t) {
        //take the input and if it matches any of the keys, return the move
        if (t == left) {
            return LEFT;
        } else if (t == right) {
            return RIGHT;
        } else if (t == up) {
            return UP;
        } else if (t == down) {
            return DOWN;
        }

        return null;
    }
}
