package edu.ucsc.eis.mario;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class MarioRulesFrameLauncher implements ActionListener
{
	JRadioButtonMenuItem rulesEnabledMenuItem;
	JRadioButtonMenuItem rulesDisabledMenuItem;
	
	public MarioRulesFrameLauncher() {
    	JMenuBar menuBar = new JMenuBar();
    	JMenu versionMenu = new JMenu("Version");
    	JMenu rulesMenu = new JMenu("Rules");
    	menuBar.add(versionMenu);
    	menuBar.add(rulesMenu);
    	
    	ButtonGroup group = new ButtonGroup();
    	JRadioButtonMenuItem goodMenuItem = new JRadioButtonMenuItem("Good Code");
    	goodMenuItem.setSelected(true);
    	group.add(goodMenuItem);
    	versionMenu.add(goodMenuItem);

    	JRadioButtonMenuItem badMenuItem = new JRadioButtonMenuItem("Bugged Code");
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

        MarioComponent mario = new MarioComponent(640, 500);
        JFrame frame = new JFrame("Mario Test");
        frame.setContentPane(mario);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);
        
        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
        
        mario.start();
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
	}
	
    public static void main(String[] args)
    {
    	new MarioRulesFrameLauncher();
    }
}