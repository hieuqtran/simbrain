/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2003 Jeff Yoshimi <www.jeffyoshimi.net>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.simbrain.world;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.simbrain.network.NetworkPanel;
import org.simbrain.util.SimbrainMath;


/**
 * <b>World</b> is the lowest-level environment panel which contains most
 * of the world's "logic". Creature and flower/food icons are drawn here.
 * Movement of the mouse in response to clicks and (very 
 * minimal) world-editing features are also handled here.  
 * Finally, the stimulus to the network is  calculated here, on the 
 * basis of the creature's distance from objects, as follows:
 *  
 *  <li> Get the vector of values, the "smell signature," associated with each object </li>
 *  <li> Scale this signature by the creature's distance fromm each object.</li>
 *  <li> Use the sum of these scaled smell signatures as input to the creature's network. </li>
 *  
 */
public class World extends JPanel implements MouseListener, MouseMotionListener, ActionListener, KeyListener {

	/** Color of the world background */
	public static final Color BACKGROUND_COLOR = Color.white;

	public static final int OBJECT_SIZE = 35;
	public static final int WORLD_WIDTH = 300; // X_Bounds
	public static final int WORLD_HEIGHT = 300; // Y_Bounds
	
	public static boolean inProgress = false; // TODO: CHANGE!


	//TODO straight_factor.  find better names
	
	private boolean followMode =  true;	
	private boolean objectInitiatesMovement = false;
	//TODO: Wraparound on/off
	private boolean useLocalBounds = false;
	private boolean updateWhileDragging = true; // Update network as objects are dragged
		
	private ArrayList objectList = new ArrayList();
		
	private Point selectedPoint; 
	private WorldEntity selectedEntity = null;
	
	private CreatureEntity theCreature = new CreatureEntity(this, "Mouse", 100, 100);
	private NetworkPanel theNetPanel = null;
	
	private JMenuItem deleteItem = new JMenuItem("Delete object");
	private JMenuItem addItem = new JMenuItem("Add new object");
	private JMenuItem objectPropsItem = new JMenuItem("Set object Properties");
	private JMenuItem propsItem = new JMenuItem("Set world properties");

	private double[] currentMotor = null;
	private double[] currentStimulus = null;
	private double[] currentStimulusL = null;
	private double[] currentStimulusR = null;


	private ArrayList input_list = new ArrayList();
	private ArrayList output_list = new ArrayList();

	/**
	 * Construct a world, set its background color
	 */
	public World() {

		setBackground(BACKGROUND_COLOR);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.setFocusable(true);
		
		init_popupMenu();
		init_outputs();
		init_inputs();

	}

	////////////////////
	// Initialization //
	////////////////////

	public void init_popupMenu() {
		deleteItem.addActionListener(this);
		objectPropsItem.addActionListener(this);
		addItem.addActionListener(this);
		propsItem.addActionListener(this);
	}
	/**
	 * Initialize list of motor commands
	 */
	public void init_outputs() {
		output_list.add("North");
		output_list.add("South");
		output_list.add("East");
		output_list.add("West");
		output_list.add("North-east");
		output_list.add("North-west");
		output_list.add("South-east");
		output_list.add("South-west");
		output_list.add("Straight");
		output_list.add("Left");
		output_list.add("Right");
		
	}

	/**
	 * Initialize list of stimuli labels
	 */
	public void init_inputs() {

		// Currently a fixed list,
		// later add names for objects and
		// ability to have different sized input vectors
		// depending on the world that has been loaded.

		input_list.add("1");
		input_list.add("2");
		input_list.add("3");
		input_list.add("4");
		input_list.add("5");
		input_list.add("6");
		input_list.add("7");
		input_list.add("8");
		input_list.add("L1");
		input_list.add("L2");
		input_list.add("R1");
		input_list.add("R2");

	}

	//////////////////////
	// Update methods   //
	//////////////////////

	/**
	 *  Update the world (currently, just the creature), based on the motor
	 *  vector sent from the network.  How output vectors (sets of activation levels
	 *  at the output nodes of the network) are mapped to movements varies, and
	 *  can be set in the WorldDialog.
	 * 
	 * @param fromNet the output vector from the neural network
	 */
	public void update(double[] fromNet) {
		//System.out.println(" " + Utils.getVectorString(fromNet));

		//Move in the directions corresponding to nodes whose value is greater than the average value across
		//the output nodes
		double avg = SimbrainMath.getAverage(currentMotor);
		for (int i = 0; i < currentMotor.length; i++) {
			if (((int) currentMotor[i]) > avg) {
				// Each node is a direction.  
				theCreature.moveDirection(i);
			}
		}
		this.getParent().repaint();
	}


	/**
	 * Movement initiated by network, as opposed to by clicking the mouse
	 * 
	 * @param netOutput a single-value version of update, representing the most active output node
	 */
	public void moveCreatureNetwork(int netOutput) {

		theCreature.moveDirection(netOutput);
		this.getParent().repaint();

	}
	
	/**
	 * Used when the creature is directly moved in the world.
	 * 
	 * Used to update network from world, in a way which avoids iterating 
	 * the net more than once
	 */
	public void updateNetwork() {
		// When the creature is manually moved, the network is updated
		if ((theNetPanel.getInteractionMode() == NetworkPanel.BOTH_WAYS)
			|| (theNetPanel.getInteractionMode() == NetworkPanel.WORLD_TO_NET)) {
			theNetPanel.updateNetwork();
		} 
		if (theNetPanel != null) {
			theNetPanel.repaint();
		}
	}

	//////////////////////////////////////////
	// "Motor methods"						//
	//										//
	// Network output --> Creature Movement //
	//////////////////////////////////////////

	public void motorCommand(String name, double value) {

		// Must implement an actual rule  for dealing with intensity here!
		if (value < 1) {
			return;
		}

		if (name.equals("North")) {
			theCreature.moveDirection(CreatureEntity.NORTH);
		} else if (name.equals("South")) {
			theCreature.moveDirection(CreatureEntity.SOUTH);
		} else if (name.equals("West")) {
			theCreature.moveDirection(CreatureEntity.WEST);
		} else if (name.equals("East")) {
			theCreature.moveDirection(CreatureEntity.EAST);
		} else if (name.equals("North-west")) {
			theCreature.moveDirection(CreatureEntity.NORTH_WEST);
		} else if (name.equals("North-east")) {
			theCreature.moveDirection(CreatureEntity.NORTH_EAST);
		} else if (name.equals("South-west")) {
			theCreature.moveDirection(CreatureEntity.SOUTH_WEST);
		} else if (name.equals("South-east")) {
			theCreature.moveDirection(CreatureEntity.SOUTH_EAST);
		} else if (name.equals("Straight")) {
			theCreature.goStraight(value);
		} else if (name.equals("Left")) {
			theCreature.turnLeft(value);
		} else if (name.equals("Right")) {
			theCreature.turnRight(value);
		}
		
		repaint();
	}

	
	//TODO: Separate updates of world and network from movement types
		//	Different options: change world, change world and network, 
		//	Position is 
		//	Class WorldEntity, superclass of staticEntity and Creature.   Creature has whiskers
		//  Perhaps a second input parameter: (1) stimulus index, (2) receptor-location. 
		//  Make video-game world
		
	//////////////////////////////////////////
	// "Stimulus methods"					//
	//										//
	// Creature Movement --> Network Input  //
	//////////////////////////////////////////
	

	/**
	 * Updates the proximal stimulus to be sent to the network
	 */
	public void updateStimulus() {
		StaticEntity temp = null;
		currentStimulus = SimbrainMath.zeroVector(getHighestDimensionalStimulus());
		currentStimulusL = SimbrainMath.zeroVector(getHighestDimensionalStimulus());
		currentStimulusR = SimbrainMath.zeroVector(getHighestDimensionalStimulus());

		double distance = 0;
		
		//Sum proximal stimuli corresponding to each object
		for (int i = 0; i < objectList.size(); i++) {
				temp = (StaticEntity) objectList.get(i);
				distance = SimbrainMath.distance(temp.getLocation(), getCreature().getLocation());  
				currentStimulus = SimbrainMath.addVector(currentStimulus, temp.getStimulus(distance));
				
				distance = SimbrainMath.distance(temp.getLocation(), getCreature().getLeftWhisker()); 
				currentStimulusL = SimbrainMath.addVector(currentStimulusL, temp.getStimulus(distance));
				
				distance = SimbrainMath.distance(temp.getLocation(), getCreature().getRightWhisker());  
				currentStimulusR = SimbrainMath.addVector(currentStimulusR, temp.getStimulus(distance));
		}		
		
	}
	


	//TODO: Check that the label-index is within bounds of the currentStimulus array
	public double getStimulus(String in_label) {
		
		int max = this.getHighestDimensionalStimulus();
		
		if (in_label.startsWith("L")) {
			return currentStimulusL[(Integer.parseInt(in_label.substring(1))-1) % max];
		} else if (in_label.startsWith("R")) {
			return currentStimulusR[(Integer.parseInt(in_label.substring(1))-1) % max];
		} else {
			return currentStimulus[(Integer.parseInt(in_label)-1) % max];
		}
	
	}
	
	/**
	 * Go through entities in this world and find the one with the greatest number of dimensions.
	 * This will determine the dimensionality of the proximal stimulus sent to the network
	 * 
	 * @return the number of dimensions in the highest dimensional stimulus
	 */
	public int getHighestDimensionalStimulus() {
		StaticEntity temp = null;
		int max = 0;
		for (int i = 0; i < objectList.size(); i++) {
				temp = (StaticEntity) objectList.get(i);
				if(temp.getStimulusDimension() > max) max = temp.getStimulusDimension();
		}
		return max;
	}

	/**
	 * Calculate the stimulus to send to the neural network based on the locations
	 * and smell signatures of surrounding objects
	 * 
	 * @return an array of values to serve as input to the neural net.
	 */
	public double[] getStimulus() {
		return currentStimulus;
	}
	
	//////////////////////
	// Graphics Methods //
	//////////////////////

	public void mouseEntered(MouseEvent mouseEvent) {
	}
	public void mouseExited(MouseEvent mouseEvent) {
	}
	public void mouseMoved(MouseEvent e) {
	}
	public void mouseClicked(MouseEvent mouseEvent) {
	}
	public void mouseReleased(MouseEvent mouseEvent) {
	}
	public void mouseDragged(MouseEvent e) {
		if(selectedEntity != null) {
			selectedEntity.setLocation(e.getPoint());
			repaint();
			if ((theNetPanel.getInteractionMode() == NetworkPanel.BOTH_WAYS) || (theNetPanel.getInteractionMode() == NetworkPanel.WORLD_TO_NET)) {
				if(updateWhileDragging == true) { 
					if (selectedEntity == theCreature) {
						theNetPanel.updateNetwork();
					} else {
						if (objectInitiatesMovement == true) {
							theNetPanel.updateNetworkAndWorld(); 
						} else {
							theNetPanel.updateNetwork();
						}
					}
					theNetPanel.repaint();
				}
			}
		}	
	
	} 

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent mouseEvent) {

		selectedPoint = mouseEvent.getPoint();
		selectedEntity = findClosestEntity(selectedPoint, OBJECT_SIZE/2);
		
		//Show popupmenu for right click
		if(mouseEvent.isControlDown() || (mouseEvent.getButton() == 3)) {
			JPopupMenu menu  = buildPopupMenu(selectedEntity);
			menu.show(this, (int)selectedPoint.getX(), (int)selectedPoint.getY());
		} 	
		//open dialogue for that world-item		
		else if (mouseEvent.getClickCount() == 2) {
			showEntityDialog((StaticEntity)selectedEntity);
		} 	

		// Move an entity in the world
		else if (selectedEntity == null) {
			if (SimbrainMath.distance(theCreature.getLocation(), selectedPoint) < 15) {
				selectedEntity = theCreature;
			}
		}
						  
		// move mouse in direction of left-click						  
		if(followMode == true) {
			if (selectedPoint.x < theCreature.getLocation().x) {
				theCreature.moveDirection(CreatureEntity.WEST);
			}
			if (selectedPoint.x > theCreature.getLocation().x) {
				theCreature.moveDirection(CreatureEntity.EAST);
			}
			if (selectedPoint.y < theCreature.getLocation().y) {
				theCreature.moveDirection(CreatureEntity.NORTH);
			}
			if (selectedPoint.y > theCreature.getLocation().y) {
				theCreature.moveDirection(CreatureEntity.SOUTH);
			}
		}
		updateNetwork();

		java.awt.Container container = this.getParent().getParent();
		container.repaint();
	}

	public void actionPerformed(ActionEvent e) {

		Object e1 = e.getSource();

		// Handle pop-up menu events
		Object o = e.getSource();
		if (o instanceof JMenuItem) {
			if (o == deleteItem ) {
				deleteEntity(selectedEntity);
			} else if (o == addItem) {
				addEntity(selectedPoint);
			} else if (o == propsItem) { 
				showGeneralDialog();	
			} else if (o == objectPropsItem){
				showEntityDialog((StaticEntity)selectedEntity);
			}
			return;
		}
	}
	
	 public void keyReleased(KeyEvent k)
	 {
	 }
	 public void keyTyped(KeyEvent k)
	 {
	 }
	 public void keyPressed(KeyEvent k)
	 {
	 	if(k.getKeyCode() == KeyEvent.VK_UP) {
	 		theCreature.goStraight(1);
	 	} else if(k.getKeyCode() == KeyEvent.VK_DOWN) {
	 		theCreature.goStraight(-1);
	 	} else if(k.getKeyCode() == KeyEvent.VK_RIGHT) {
	 		theCreature.turnRight(4);
	 	} else if(k.getKeyCode() == KeyEvent.VK_LEFT) {
	 		theCreature.turnLeft(4);
	 	}
	 	updateNetwork();
	 	repaint();
	 }
	              
	
	/**
	 * Delete the specified world entity
	 * 
	 * @param e world entity to delete
	 */
	public void deleteEntity(WorldEntity e) {
		if ((e != null) || (e != theCreature)) {
			objectList.remove(e);
			repaint();
		}
		e = null;
	}
	
	/**
	 * Add a world object at point p.  Note that it currently has a set of default values specified within the code.
	 * 
	 * @param p the location where the object should be added
	 */
	public void addEntity(Point p) {
	    StaticEntity we = new StaticEntity();
		we.setLocation(p);
		we.setImageName("Swiss.gif");
		we.setObjectVector(new double[] {10,10,0,0,0,0,0,0});
		objectList.add(we);
		repaint();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintWorld(g);
	}

	/**
	 * Paint all the objects in the world
	 * 
	 * @param g Reference to the world's graphics object
	 */
	public void paintWorld(Graphics g) {

		for (int i = 0; i < objectList.size(); i++) {
			WorldEntity theEntity = (WorldEntity) objectList.get(i);
			paintEntity(theEntity, g);
		}

		g.setColor(Color.black);
		g.setColor(Color.white);

		// Paint main (moving) creature
		paintEntity(theCreature, g);

	}

	/**
	 * Paint the  creature (the mouse)
	 */
	public void paintCreature() {
		paintEntity(theCreature, getGraphics());
	}

	/**
	 * Erase the creature (the mouse)
	 */
	public void eraseCreature() {
		eraseEntity(theCreature, getGraphics());
	}

	/**
	 * Paint the entity
	 * 
	 * @param theEntity the entity to paint
	 * @param g reference to the World's graphics object
	 */
	public void paintEntity(WorldEntity theEntity, Graphics g) {

		theEntity.paintIcon(
			this,
			g,
			theEntity.getLocation().x - 20,
			theEntity.getLocation().y - 20);
	}

	/**
	 * Erase an entity in the world
	 * 
	 * @param theEntity the entity to erase
	 * @param g reference to the World's graphics object
	 */
	public void eraseEntity(WorldEntity theEntity, Graphics g) {
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(
			theEntity.getLocation().x,
			theEntity.getLocation().y,
			theEntity.getIconWidth(),
			theEntity.getIconHeight());
	}
	/**
	 * Call up a {@link DialogWorldEntity} for a world object nearest to a specified point
	 * 
	 * @param theEntity the non-creature entity closest to this point will have a dialog called up
	 */
	public void showEntityDialog(StaticEntity theEntity) {
		DialogWorldEntity theDialog = null;
		
		if(theEntity != null) {
			theDialog = new DialogWorldEntity(theEntity);
			theDialog.pack();
			theDialog.show();
			if(!theDialog.hasUserCancelled())
			{
				theDialog.getValues();
			}
			repaint();			
		}
		
	}
	
	public void showGeneralDialog() {
		DialogWorld theDialog = new DialogWorld(this);
		theDialog.pack();
		theDialog.show();
		if(!theDialog.hasUserCancelled())
		{
			theDialog.getValues();
		}
		repaint();
	}
	
	public void showScriptDialog() {
		DialogScript theDialog = new DialogScript(this);
		theDialog.show();
		theDialog.pack();
		repaint();
	}
	

	//TODO: This returns the first entity found within a distance of radius; make it return the closest 
	//			entity within that radius
	private WorldEntity findClosestEntity(Point thePoint, double radius) {
		for (int i = 0; i < objectList.size(); i++) {
			WorldEntity temp = (WorldEntity) objectList.get(i);
			int distance = SimbrainMath.distance(thePoint, temp.getLocation());
			if (distance < radius) {
				return temp;
			}
		}
		return null;
	}

	/////////////////////////
	// Getters and Setters //
	/////////////////////////

	//This will be part of a world interface
	public ArrayList get_outputs() {
		return output_list;
	}
	public ArrayList get_inputs() {
		return input_list;
	}
	public ArrayList getObjectList() {
		return objectList;
	}

	public void setBounds(boolean val) {
		useLocalBounds = val;
	}
	public  boolean getLocalBounds() {
		return useLocalBounds;
	}


	public ArrayList getEntityRef() {
		return objectList;
	}
	public CreatureEntity getCreature() {
		return theCreature;
	}

	public NetworkPanel getNetworkPanel() {
		return theNetPanel;
	}
	public void setNetworkPanel(NetworkPanel netPanelRef) {
		theNetPanel = netPanelRef;
	}
	
	public void setObjectList(ArrayList theList) {
		objectList = theList;
	}
	
	public void setCreature(CreatureEntity creature) {
		creature.setParentWorld(this);
		theCreature = creature;
	}

	/**
	 * @return true if the network should be updated as the creature is dragged, false otherwise
	 */
	public boolean isUpdateWhileDragging() {
		return updateWhileDragging;
	}

	/**
	 * @param b true if the network should be updated as the creature is dragged, false otherwise
	 */
	public void setUpdateWhileDragging(boolean b) {
		updateWhileDragging = b;
	}
	
	
	public String getRandomMovementCommand() {
		return((String)(output_list.get((int)(Math.random() * 8))));
	}
	
	/**
	 * Create a popup menu based on location of mouse click
	 * 
	 * @return the popup menu
	 */	
	public JPopupMenu buildPopupMenu(WorldEntity theEntity) {
		
		JPopupMenu ret = new JPopupMenu();

		if (theEntity == theCreature) {			
		} else if (theEntity instanceof WorldEntity){
			ret.add(objectPropsItem);
			ret.add(deleteItem);
		} else {
			ret.add(addItem);
		}
		ret.add(propsItem);
		return ret;
	}
	
	/**
	 * @return true if the creature should follow mouse-clicks, false otherwise
	 */
	public boolean isFollowMode() {
		return followMode;
	}

	/**
	 * @param b true if the creature should follow mouse-clicks, false otherwise
	 */
	public void setFollowMode(boolean b) {
		followMode = b;
	}

}
