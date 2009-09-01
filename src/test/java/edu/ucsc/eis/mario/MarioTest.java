package edu.ucsc.eis.mario;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

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

public class MarioTest {
	LevelScene scene;
	Mario mario;
	
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
		
		mario = scene.mario;
		
		tickScene(2000);
	}
	
	@Test
	public void testSetup() {
		assertTrue(true);
	}
	
	@Test
	public void testFlower() {
		Mario.large = false;
		Mario.fire = false;
		assertFalse(Mario.large);
		assertFalse(Mario.fire);
				
		mario.getFlower();
		tickScene(1000);
		
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
	
	private void tickScene(int ticks) {
		for (int i = 0; i < ticks; i++) {
			scene.tick();
		}
	}
}
