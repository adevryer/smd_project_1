package ore;

import ch.aplu.jgamegrid.Actor;

public abstract class Element extends Actor {
    //construct an element which has multiple sprites to display
    public Element(String filepath, int num_sprites){
        super(filepath, num_sprites);
    }

    //construct a normal element with only one sprite
    public Element(String filepath) {
        super(filepath);
    }
}
