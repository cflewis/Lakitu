package edu.ucsc.eis.mario;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatelessKnowledgeSession;
import org.junit.Before;
import org.junit.Test;

import com.mojang.sonar.FakeSoundEngine;
import com.mojang.sonar.SonarSoundEngine;
import com.mojang.sonar.SoundSource;
import com.mojang.sonar.sample.SonarSample;

import static org.junit.Assert.*;


import edu.ucsc.eis.mario.level.LevelGenerator;
import edu.ucsc.eis.mario.sprites.Mario;
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
	StatelessKnowledgeSession ksession;
	OutputStream os;
	OutputStream eos;
	
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
		scene.paused = false;		
		
		mario = scene.mario;
		Mario.resetStatic();
		mario.deathTime = 0;
				
		try {
			// load up the knowledge base
			KnowledgeBase kbase = KnowledgeReader.getKnowledgeBase("Mario.drl");
			ksession = kbase.newStatelessKnowledgeSession();
			//knowledgeLogger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
				
		eos = new ByteArrayOutputStream();
		PrintStream eps = new PrintStream(eos);
		System.setErr(eps);
		
		tickScene(500);
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
	
	@Test
	public void testJump() {
		mario.keys[Mario.KEY_JUMP] = true;
		tickScene(5);
		assertTrue(mario.getJumpTime() > 0);
		tickScene(100);
		assertTrue(mario.getJumpTime() <= 0);
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
		assertTrue(eos.toString().contains("Mario is ducking"));
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
		assertTrue(eos.toString().contains("Mario is dead"));
	}
	
	private void tickScene(int ticks) {
		for (int i = 0; i < ticks; i++) {
			ksession.execute(mario);
			scene.tick();
		}
	}
}
