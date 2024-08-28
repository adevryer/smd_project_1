package ore;

public class Clay extends Element implements Destroyable {
    private static final String FILEPATH = "sprites/clay.png";

    public Clay(){
        super(FILEPATH);
    }

    //remove the clay from the game
    @Override
    public void destroy() {
        this.removeSelf();
    }
}
