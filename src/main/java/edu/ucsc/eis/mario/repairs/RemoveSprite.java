package edu.ucsc.eis.mario.repairs;

/**
 * Created by IntelliJ IDEA.
 * User: cflewis
 * Date: Feb 20, 2010
 * Time: 11:30:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveSprite extends RepairEvent {
    public static final int MARIO = 0;
    public static final int BULLET_BILL = 1;

    private int sprite;

    public RemoveSprite(int sprite) {
        this.sprite = sprite;
    }

    public void execute() {
        switch (sprite) {
            case MARIO: this.mario.die(); break;
        }
    }
}
