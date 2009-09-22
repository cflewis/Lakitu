package edu.ucsc.eis.mario.rules;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.google.common.base.Preconditions;

import edu.ucsc.eis.mario.Art;
import edu.ucsc.eis.mario.MarioComponent;
import edu.ucsc.eis.mario.level.LevelGenerator;
import edu.ucsc.eis.mario.sprites.BulletBill;
import edu.ucsc.eis.mario.sprites.Mario;

public class MarioRulesFrameLauncher implements ActionListener
{
	JRadioButtonMenuItem rulesEnabledMenuItem;
	JRadioButtonMenuItem rulesDisabledMenuItem;
	JRadioButtonMenuItem goodMenuItem;
	JRadioButtonMenuItem badMenuItem;
	
	Mario mario;
		
	public MarioRulesFrameLauncher() {
    	JMenuBar menuBar = new JMenuBar();
    	JMenu versionMenu = new JMenu("Version");
    	versionMenu.getPopupMenu().setLightWeightPopupEnabled(false);
    	JMenu rulesMenu = new JMenu("Rules");
    	rulesMenu.getPopupMenu().setLightWeightPopupEnabled(false);
    	menuBar.add(versionMenu);
    	menuBar.add(rulesMenu);
    	
    	ButtonGroup group = new ButtonGroup();
    	goodMenuItem = new JRadioButtonMenuItem("Good Code");
    	goodMenuItem.setSelected(true);
    	goodMenuItem.addActionListener(this);
    	group.add(goodMenuItem);
    	versionMenu.add(goodMenuItem);

    	badMenuItem = new JRadioButtonMenuItem("Bugged Code");
    	badMenuItem.addActionListener(this);
    	group.add(badMenuItem);
    	versionMenu.add(badMenuItem);
    	
    	ButtonGroup group1 = new ButtonGroup();
    	rulesEnabledMenuItem = new JRadioButtonMenuItem("Rules Enabled");
    	rulesEnabledMenuItem.setSelected(true);
    	rulesEnabledMenuItem.addActionListener(this);
    	group1.add(rulesEnabledMenuItem);
    	rulesMenu.add(rulesEnabledMenuItem);

    	rulesDisabledMenuItem = new JRadioButtonMenuItem("Rules Disabled");
    	rulesDisabledMenuItem.addActionListener(this);
    	group1.add(rulesDisabledMenuItem);
    	rulesMenu.add(rulesDisabledMenuItem);

        MarioComponent marioComponent = new MarioComponent(640, 500, this);
        JFrame frame = new JFrame("Mario Test");
        frame.setContentPane(marioComponent);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);
        
        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
        
        marioComponent.start();
//        frame.addKeyListener(mario);
//        frame.addFocusListener(mario);
	}
	
	public void actionPerformed(ActionEvent e) {		
		JMenuItem source = (JMenuItem) e.getSource();
		
		System.err.println("Hello" + e);
		
		if (source == rulesEnabledMenuItem) {
			MarioComponent.rulesEnabled = true;
			System.err.println("ruled enabled");
		}
		
		if (source == rulesDisabledMenuItem) {
			MarioComponent.rulesEnabled = false;
			System.err.println("ruled disabled");
		}
		
		if (source == badMenuItem) {
			BulletBill.frequency = 50;
			LevelGenerator.jumpLength = 16;
			Mario.maxJumpTime = 50;
			Mario.dieOnFall = false;
			Mario.stopMovementOnDeath = false;
			Mario.coinValue = 2;
			if (mario != null) {mario.sheet = Art.fireMario;}

			System.err.println("Bad code enabled");
		}
		
		if (source == goodMenuItem) {
			BulletBill.frequency = 100;
			LevelGenerator.jumpLength = 2;
			Mario.maxJumpTime = 7;
			Mario.dieOnFall = true;
			Mario.stopMovementOnDeath = true;
			Mario.coinValue = 1;
			System.err.println("Good code enabled");
		}
	}
	
	public void setMario(Mario mario) {
		this.mario = mario;
	}
	
    public static void main(String[] args)
    {
    	new MarioRulesFrameLauncher();
    }
}