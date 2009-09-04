package edu.ucsc.eis.mario.events;

import edu.ucsc.eis.mario.sprites.Mario;

public class MarioEvent {
	private Mario mario;
	
	public MarioEvent(Mario mario) {
		this.mario = mario;
	}
	
	public Mario getMario() {
		return this.mario;
	}
}
