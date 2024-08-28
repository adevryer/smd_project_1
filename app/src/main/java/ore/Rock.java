package ore;

public class Rock extends Element implements Destroyable {
    private static final String FILEPATH = "sprites/rock.png";

    public Rock(){
        super(FILEPATH);
    }

    //remove the rock from the game
    @Override
    public void destroy() {
        this.removeSelf();
    }
}
