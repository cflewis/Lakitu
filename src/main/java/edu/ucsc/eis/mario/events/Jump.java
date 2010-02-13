package edu.ucsc.eis.mario.events;

import edu.ucsc.eis.mario.sprites.Mario;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Jump extends MarioEvent {
	private final int jumpTime;

    public Jump(Mario mario, int jumpTime) {
		super(mario);
        this.jumpTime = jumpTime;

	}

    public int getJumpTime() {
        return jumpTime;
    }

    @Override
	public String toString() {
		return new ToStringBuilder(this).
			append("Jump Time", this.jumpTime).
			toString();
	}
}
