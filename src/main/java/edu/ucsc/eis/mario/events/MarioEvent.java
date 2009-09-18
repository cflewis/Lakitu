package edu.ucsc.eis.mario.events;

import com.google.common.base.Preconditions;

import edu.ucsc.eis.mario.LevelScene;
import edu.ucsc.eis.mario.sprites.Mario;

public class MarioEvent {
	private Mario mario;
	
	public MarioEvent(Mario mario) {
		this.mario = Preconditions.checkNotNull(mario);
	}
	
	public Mario getMario() {
		return this.mario;
	}
	
	public LevelScene getLevelScene() {
		return this.mario.getWorld();
	}
}
