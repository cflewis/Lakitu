package edu.ucsc.eis.mario.rules;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;

import javax.swing.*;

import edu.ucsc.eis.mario.Art;
import edu.ucsc.eis.mario.MarioComponent;
import edu.ucsc.eis.mario.level.LevelGenerator;
import edu.ucsc.eis.mario.sprites.BulletBill;
import edu.ucsc.eis.mario.sprites.Mario;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class LakituFrameLauncher implements ActionListener
{
	JRadioButtonMenuItem rulesEnabledMenuItem;
	JRadioButtonMenuItem rulesDisabledMenuItem;
	JCheckBoxMenuItem showConsoleMenuItem;
	JRadioButtonMenuItem goodMenuItem;
	JRadioButtonMenuItem badMenuItem;
	Console console;

    Logger logger;
	
	Mario mario;

	public LakituFrameLauncher() {
        Logger activeMqLogger = Logger.getLogger("org.apache.activemq");
        activeMqLogger.setLevel(Level.INFO);

        logger = Logger.getLogger("edu.ucsc.eis.mario");

        PatternLayout layout = new PatternLayout("%C{1} - %m\n");
        ConsoleAppender appender = new ConsoleAppender(layout);
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

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
    	
    	showConsoleMenuItem = new JCheckBoxMenuItem("Show Rules Output");
    	showConsoleMenuItem.addActionListener(this);
    	//rulesMenu.add(showConsoleMenuItem);

        MarioComponent marioComponent = new MarioComponent(640, 500, this);
        JFrame frame = new JFrame("Lakitu");
        frame.setContentPane(marioComponent);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);
        
        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
        
        try {
            console = new Console();
        } catch (IOException e) {
        	System.err.println("Couldn't output console text: " + e);
        }
        
        marioComponent.start();
//        frame.addKeyListener(mario);
//        frame.addFocusListener(mario);
	}

    public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) e.getSource();

		if (source == rulesEnabledMenuItem) {
			MarioComponent.rulesEnabled = true;
			logger.info("Rules enabled");
		}
		
		if (source == rulesDisabledMenuItem) {
			MarioComponent.rulesEnabled = false;
			logger.info("Rules disabled");
		}
		
		if (source == badMenuItem) {
			BulletBill.frequency = 50;
			LevelGenerator.jumpLength = 16;
			Mario.maxJumpTime = 30;
			Mario.dieOnFall = false;
			Mario.stopMovementOnDeath = false;
			Mario.coinValue = 2;

			logger.info("Bad code enabled");
		}
		
		if (source == goodMenuItem) {
			BulletBill.frequency = 100;
			LevelGenerator.jumpLength = 2;
			Mario.maxJumpTime = 7;
			Mario.dieOnFall = true;
			Mario.stopMovementOnDeath = true;
			Mario.coinValue = 1;
			logger.info("Good code enabled");
		}
		
		if (source == showConsoleMenuItem) {
			if (console.isVisible()) { 
				console.setVisible(false); 
			} else {
				console.setVisible(true);
			}
		}
	}
	
	public void setMario(Mario mario) {
		this.mario = mario;
	}
	
    public static void main(String[] args)
    {
    	new LakituFrameLauncher();
    }
}
