package edu.ucsc.eis.mario;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.drools.base.RuleNameEqualsAgendaFilter;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.spi.AgendaFilter;
import org.drools.runtime.rule.FactHandle;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.time.SessionPseudoClock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mojang.sonar.FakeSoundEngine;
import com.mojang.sonar.SonarSoundEngine;
import com.mojang.sonar.SoundSource;
import com.mojang.sonar.sample.SonarSample;

import static org.junit.Assert.*;


import edu.ucsc.eis.mario.events.Jump;
import edu.ucsc.eis.mario.events.Landing;
import edu.ucsc.eis.mario.level.LevelGenerator;
import edu.ucsc.eis.mario.sprites.Enemy;
import edu.ucsc.eis.mario.sprites.Mario;
import edu.ucsc.eis.mario.sprites.Shell;
import static org.mockito.Mockito.*;

/**
 * These unit tests should fail when the rule engine is disabled.
 * When it is turned on, they should pass.
 * Probably need some qualitative evaluation (80K students? Web deployment?).
 * 
 * The Required Action not possible bug is the sort of thing we need
 * Drools Solver for... if there is no solution (eg. can't get over a jump),
 * then we fail.
 * @author cflewis
 *
 */
public class MarioTest {
	LevelScene scene;
	Mario mario;
	StatefulKnowledgeSession ksession;
	TrackingAgendaEventListener trackingAgendaEventListener;
	
	@Before
	public void setUp() {
		GraphicsConfiguration graphicsConfiguration = mock(GraphicsConfiguration.class);
		BufferedImage image = mock(BufferedImage.class);
		when(image.getWidth()).thenReturn(100);
		when(image.getHeight()).thenReturn(100);
		when(graphicsConfiguration.createCompatibleImage(anyInt(), anyInt(), anyInt())).thenReturn(image);

		Graphics2D g = mock(Graphics2D.class);
		when(g.drawImage(any(Image.class), anyInt(), anyInt(), any(ImageObserver.class))).thenReturn(true);
		when(image.getGraphics()).thenReturn(g);
		
		Art.init(null, new FakeSoundEngine());
		
		MarioComponent marioComponent = mock(MarioComponent.class);
		scene = spy(new LevelScene(graphicsConfiguration, marioComponent, 3581986689337283420l, 1, 
				LevelGenerator.TYPE_OVERGROUND));
		SonarSoundEngine sound = mock(SonarSoundEngine.class);
		scene.sound = sound;
		doNothing().when(sound).play(any(SonarSample.class), any(SoundSource.class), anyFloat(), anyFloat(), anyFloat());
		scene.init();
		Art.stopMusic();
		scene.paused = false;		
		
		mario = scene.mario;
		Mario.resetStatic();
		mario.deathTime = 0;
				
		try {
			// load up the knowledge base
			KnowledgeBase kbase = KnowledgeReader.getKnowledgeBase("Mario.drl", "Mario.rf");
			KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
			config.setOption(ClockTypeOption.get("pseudo"));
			ksession = kbase.newStatefulKnowledgeSession(config, null);
			KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
			trackingAgendaEventListener = new TrackingAgendaEventListener();
			ksession.addEventListener(trackingAgendaEventListener);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
		
		tickScene(500);
	}
	
	@After
	public void tearDown() {
		// Reset the knowledge base
		ksession.dispose();
	}
	
	@Test
	public void testSetup() {
		assertTrue(true);
	}
	
	@Test 
	public void testMushroom() {
		Mario.large = false;
		Mario.fire = false;
		assertFalse(Mario.large);
		assertFalse(Mario.fire);
				
		mario.getMushroom();
		tickScene(1000);
		
		assertTrue(Mario.large);
		assertFalse(Mario.fire);
	}
	
	@Test
	public void testFlower() {
		Mario.large = false;
		Mario.fire = false;
		assertFalse(Mario.large);
		assertFalse(Mario.fire);
				
		mario.getFlower();
		tickScene(1000);
		
		assertTrue(Mario.large);
		assertTrue(Mario.fire);
	}
	
	/**
	 * This is an invalid position over time test.
	 * Mario's normal jump lands after a second.
	 * The longest jump time I've managed is less than two seconds.
	 */
	@Test
	public void testJump() {
		mario.keys[Mario.KEY_JUMP] = true;
		tickScene(1);
		assertTrue(mario.getJumpTime() > 0);
		tickScene(2);
		assertTrue(mario.getJumpTime() > 0);
		tickScene(22);
		assertTrue(mario.getJumpTime() <= 0);
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
		tickScene(1);
		
		Mario.large = true;
		assertTrue(Mario.large);
		tickScene(1);
		assertTrue(mario.isDucking());
		tickScene(1);
		assertFired("marioIsDucking");
	}
	
	/**
	 * This satisfies the "Animation in wrong context" bug
	 */
	@Test
	public void testAnimationSheet() {
		assertTrue(mario.sheet == Art.smallMario);
		testMushroom();
		assertTrue(mario.sheet == Art.mario);
		testFlower();
		assertTrue(mario.sheet == Art.fireMario);
	}
	
	@Test
	public void testOutputLevel() {
		for(int i = 0; i < scene.level.map.length; i++) {
			for (int j = 0; j < scene.level.map[i].length; j++) {
				System.out.print(scene.level.map[i][j]);
			}
			
			System.out.print("\n");
		}
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
	
	private void tickScene(int ticks) {
		for (int i = 0; i < ticks; i++) {
			FactHandle marioFact = ksession.insert(mario);
			ksession.startProcess("Mario");
			ksession.fireAllRules();
			scene.tick();
			SessionPseudoClock clock = ksession.getSessionClock();
			clock.advanceTime(42, TimeUnit.MILLISECONDS);
			ksession.retract(marioFact);
		}
	}
	
	private void assertFired(String ruleName) {
		//ksession.fireAllRules(new RuleNameEqualsAgendaFilter(ruleName));
		ksession.startProcess("Mario");
		ksession.fireAllRules();
		assertTrue(trackingAgendaEventListener.isRuleFired(ruleName));
	}
	
	private void assertNotFired(String ruleName) {
		ksession.startProcess("Mario");
		ksession.fireAllRules();
		assertFalse(trackingAgendaEventListener.isRuleFired(ruleName));
	}
}
