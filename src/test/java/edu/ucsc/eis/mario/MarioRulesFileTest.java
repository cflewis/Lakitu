package edu.ucsc.eis.mario;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.drools.runtime.rule.FactHandle;
import org.junit.Test;

import edu.ucsc.eis.mario.events.Jump;
import edu.ucsc.eis.mario.events.Landing;
import edu.ucsc.eis.mario.level.Pit;
import edu.ucsc.eis.mario.sprites.Mario;

public class MarioRulesFileTest extends MarioRulesTest {
	@Test
	public void testSceneDetection() {
		ksession.insert(scene);
		assertFired("levelSceneFound");
		assertFired("levelFound");
		assertFired("pitFound");
	}
	
	@Test
	public void testPitDetection() {
		// Create a pit by hand
		for (int x = 20; x < 30; x++) {
			for (int y = 0; y < scene.getLevel().height; y++) {
				scene.level.setBlock(x, y, (byte) 0);
			}
		}
		
		scene.level.pits = new ArrayList<Pit>();
		scene.level.pits.add(new Pit(20, 29, false));
		
		ksession.insert(scene);
		assertFired("pitFound");
		assertFired("pitTooLong");
		assertFalse(scene.level.getBlock(29, scene.level.height - 1) == (byte) 0);
	}
	
	
	@Test
	public void brokenJump() {
		mario.setJumpTime(50);
		assertTrue("Mario jump time was " + mario.getJumpTime(),
				mario.getJumpTime() == 50);
		tickScene(1);
		// Rule engine should now kick in and stop the silly value
		assertFired("marioTooHigh");
		tickScene(1);
		assertTrue(mario.getJumpTime() <= 0);
		// Y is counted top to bottom, so higher Y is lower on screen
		assertTrue(mario.getYJumpSpeed() >= 0);
	}
	
	@Test
	public void eventJump() {
		FactHandle jumpEvent = ksession.insert(new Jump(mario));
		tickScene(1);
		assertFired("jumpEventFound");
		
		FactHandle landingEvent = ksession.insert(new Landing(mario));
		tickScene(1);
		
		assertNotFired("marioJumpTooLong");
		// When Mario lands, we can retract this fact to show that he landed
		ksession.retract(jumpEvent);
		ksession.retract(landingEvent);
	}
	
	@Test
	public void brokenEventJump() {
		FactHandle jumpEvent = ksession.insert(new Jump(mario));
		// Cause Mario to be able to jump for *ages*
		for (int i = 0; i < 100; i++) {
			mario.setJumpTime(7);
			tickScene(1);
		}

		assertFired("marioJumpTooLong");
		ksession.retract(jumpEvent);
	}
	
	@Test
	public void testDuck() {
		Mario.large = false;
		assertFalse(Mario.large);
		tickScene(1);
		
		mario.keys[Mario.KEY_DOWN] = true;
		tickScene(1);
		
		assertFalse(mario.isDucking());
		
		Mario.large = true;
		assertTrue(Mario.large);
		tickScene(1);
		assertTrue(mario.isDucking());
		assertFired("marioIsDucking");
	}
	
	
	@Test
	public void testBrokenSmallAnimationSheet() {
		Mario.large = false;
		Mario.fire = false;
		assertFalse(Mario.large);
		assertFalse(Mario.fire);
		mario.sheet = Art.fireMario;
		assertTrue(mario.sheet == Art.fireMario);
		assertFired("marioAnimationSmall");
		assertTrue(mario.sheet == Art.smallMario);
	}
	
	@Test
	public void testBrokenLargeAnimationSheet() {
		Mario.large = true;
		Mario.fire = false;
		assertTrue(Mario.large);
		assertFalse(Mario.fire);
		mario.sheet = Art.fireMario;
		assertTrue(mario.sheet == Art.fireMario);
		assertFired("marioAnimationLarge");
		assertTrue(mario.sheet == Art.mario);
	}
	
	@Test
	public void testBrokenFireAnimationSheet() {
		Mario.large = true;
		Mario.fire = true;
		mario.sheet = Art.smallMario;
		assertFired("marioAnimationFire");
		assertTrue(mario.sheet == Art.fireMario);
	}

	
	/**
	 * This satisfies the "Action when not allowed" bug.
	 * As this kills Mario, this should either be tested at the end,
	 * or I have to work out why the Mario instance variable
	 * isn't being set to the new Mario that is created once this one
	 * dies.
	 */
	@Test
	public void testDeathInteraction() {
		float oldX = mario.getX();
		mario.die();
		tickScene(1);
		mario.keys[Mario.KEY_RIGHT] = true;
		tickScene(1);
		assertTrue(oldX == mario.x);
		assertFired("stopMarioInteractionWhenDead");
	}
}
