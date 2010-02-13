package edu.ucsc.eis.mario;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import edu.ucsc.eis.mario.events.*;
import org.drools.runtime.rule.FactHandle;
import org.junit.*;
import static org.mockito.Mockito.*;

import edu.ucsc.eis.mario.level.Pit;
import edu.ucsc.eis.mario.sprites.BulletBill;
import edu.ucsc.eis.mario.sprites.Mario;

/**
 * Tests not valid for Infinite Mario:
 * - Script in invalid state: No script functions
 * - Invalid damage over time (Mario kills all enemies with one shot)
 * - Invalid resource accumulation over time (how many coins can Mario get a second?) 
 * @author cflewis
 *
 */
public class MarioRulesFileTest extends MarioRulesTest {
    private static final int CORRECT_JUMP_TIME = 7;

	@Ignore
	public void testSceneDetection() {
		ksession.insert(scene);
		assertFired("levelSceneFound");
		assertFired("levelFound");
		assertFired("pitFound");
	}
	
	@Ignore
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
	
	// Simple Test for Required Action Not Possible
	@Ignore
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
	
	/**
	 * Test for invalid position over time: Mario can't jump that high from
	 * the ground, but of course he could be that high position-wise.
	 * 
	 * Does this count if I'm not doing a position check?
	 */
    @Test
    public void testBrokenEventJumpHeight() {
        FactHandle jumpEvent = ksession.insert(new Jump(mario, 50));
        tickScene(1);
        assertFired("jumpEventFound");
        assertFired("marioJumpTooHigh");

        // When Mario lands, we can retract this fact to show that he landed
        ksession.retract(jumpEvent);
    }

	
	/**
	 * Test for position invalid
	 */
	@Ignore
	public void testEscapeYBoundary() {
		ksession.insert(scene);
		mario.y = -71;
		assertTrue(mario.getY() == -71);
		assertTrue(mario.deathTime == 0);
		assertFired("marioOutOfBounds");
	}
	
	@Test
	public void testEscapeYBoundaryWithDeath() {
		ksession.insert(scene);
		mario.die();
		mario.y = (scene.level.height * 16) + 21;
		tickScene(1);
		assertFalse(mario.deathTime == 0);
		// This shouldn't fire if Mario is set to be dead
		assertNotFired("marioOutOfBounds");
	}
	
	@Ignore
	public void testEscapeXBoundary() {
		ksession.insert(scene);
		mario.x = -21;
		assertTrue(mario.getX() == -21);
		assertTrue(mario.deathTime == 0);
		assertFired("marioOutOfBounds");
		mario.deathTime = 0;
		mario.x = (scene.level.height * 16) + 1;
		assertTrue(mario.getX() == (scene.level.height * 16) + 1);
		assertTrue(mario.deathTime == 0);
		assertFired("marioOutOfBounds");
	}
	
	@Test
	public void testEventJump() {
		FactHandle jumpEvent = ksession.insert(new Jump(mario, CORRECT_JUMP_TIME));
		tickScene(1);
		assertFired("jumpEventFound");
		
		FactHandle landingEvent = ksession.insert(new Landing(mario));
		tickScene(1);
		
		assertNotFired("marioJumpTooLong");
		// When Mario lands, we can retract this fact to show that he landed
		ksession.retract(jumpEvent);
		ksession.retract(landingEvent);
	}
	
	// Test for invalid position over time: Mario can't jump too long
	@Test
	public void testBrokenEventJump() {
		FactHandle jumpEvent = ksession.insert(new Jump(mario, CORRECT_JUMP_TIME));
		// Cause Mario to be able to jump for *ages*
		for (int i = 0; i < 100; i++) {
			mario.setJumpTime(7);
			tickScene(1);
		}

		assertFired("marioJumpTooLong");
		ksession.retract(jumpEvent);
	}
	
	// Tests for invalid animation context
	@Ignore
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
	
	@Ignore
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
	
	@Ignore
	public void testBrokenFireAnimationSheet() {
		Mario.large = true;
		Mario.fire = true;
		mario.sheet = Art.smallMario;
		assertFired("marioAnimationFire");
		assertTrue(mario.sheet == Art.fireMario);
	}
	
	// Test for invalid event occurrence over time
	// This one uses events (jumping without landing)...
	@Test
	public void testValidDoubleJump() {
		// Mario can't double jump ie. jump without landing
		ksession.insert(new Jump(mario, CORRECT_JUMP_TIME));
		tickScene(5);
		mario.setJumpTime(5);
		mario.setSliding(true);
		ksession.insert(new Jump(mario, CORRECT_JUMP_TIME));
		assertNotFired("marioDoubleJump");
	}
	
	@Test
	public void testBrokenDoubleJump() {
		// Mario can't double jump ie. jump without landing
		ksession.insert(new Jump(mario, CORRECT_JUMP_TIME));
		tickScene(5);
		mario.setJumpTime(5);
		mario.setSliding(false);
		ksession.insert(new Jump(mario, CORRECT_JUMP_TIME));
		assertFired("marioDoubleJump");
	}
	
	// Test for invalid event occurance over time
	// ...this one is purely temporal
	@Test
	public void testBulletBillFiring() {
		BulletBill bill = mock(BulletBill.class);
		when(bill.getWorld()).thenReturn(scene);
		ksession.insert(new BulletBillSpawn(bill, 1, getClockTime()));
		tickScene(1000);
		ksession.insert(new BulletBillSpawn(bill, 1, getClockTime()));
		assertNotFired("bulletBillSpawn");
		ksession.insert(new BulletBillSpawn(bill, 2, getClockTime()));
		ksession.insert(new BulletBillSpawn(bill, 3, getClockTime()));
		assertNotFired("bulletBillSpawn");
	}
	
	@Test
	public void testBrokenBulletBillFiring() {
		BulletBill bill = mock(BulletBill.class);
		when(bill.getWorld()).thenReturn(scene);
		ksession.insert(new BulletBillSpawn(bill, 1, getClockTime()));
		tickScene(10);
		ksession.insert(new BulletBillSpawn(bill, 1, getClockTime()));
		assertFired("bulletBillSpawn");
	}

	// Invalid value change
	@Test
	public void testCoinValue() {
		int oldCoin = Mario.coins;
		Mario.coins = Mario.coins + 2;
		ksession.insert(new ValueChange(mario, ValueChange.COIN_CHANGE, 
				oldCoin, Mario.coins));
		assertFired("coinValue");
	}
	
	/**
	 * Test for "Action when not allowed" bug.
	 * As this kills Mario, this should either be tested at the end,
	 * or I have to work out why the Mario instance variable
	 * isn't being set to the new Mario that is created once this one
	 * dies.
	 */
	@Test
	public void testDeathInteraction() {
		float oldX = mario.getX();
		mario.die();
        ksession.insert(new Death(mario));
		tickScene(1);
		mario.keys[Mario.KEY_RIGHT] = true;
		tickScene(1);
		assertTrue(oldX == mario.x);
		assertFired("stopMarioInteractionWhenDead");
        ksession.insert(new NewLife(mario));
        tickScene(1);
        assertFired("retractDeath");
	}
}
