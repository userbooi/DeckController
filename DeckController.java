package controller;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// Necessary imports
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JPanel;

import model.*;
import view.*;

/**
 * 
 * @author Daniel
 * @author Edwin
 * @author Jayden
 *
 */
// Class that controls the entire game
public class DeckController implements ActionListener {

	// Fields
	// the players in the game
	private Player[] players = new Player[2];
	// Active player number
	private int activePlayerNumber = 0;
	// the game frame that will be created
	private BohnanzaFrame gameFrame;
	// the current active player
	private Player activePlayer;
	// the current phase
	private int phase = 0;

	// store if the player can plant or not plant (depending on the step in phase 2)
	private boolean canPlant = true;
	private boolean canDiscard = false;
	// store if the player has completed all the necessary steps to move to the next phase
	private boolean canNextPhase = true;
	// store if the player is planting right now
	private boolean planting = false;
	// discard pile (use a stack because you can only remove from the top
	private Stack<Card> discardPile = new Stack<Card>();
	// draw pile (use a stack because you can only remove from the top
	private Stack<Card> drawPile;
	// offer pile
	private Card[] offerPile;
	// active player number
	private int activePlayerNumber;
	
	// store the current card clicked
	private int cardClicked;
	// store the current field clicked
	private int fieldClicked;
	// store the current offer clicked
	private int offerClicked;
	
	// Constructor method called upon initialization
	public DeckController() {

		// call updatePhase
		updatePhase();

		// set first player to player1
		activePlayerNumber = 0;
		
		// initialize the two players
		initializePlayers();
		
		// Read image files using the file reader
		// read file must be called first since all the type information is dependent on the text file that needs to be read
		FileReader.readFile();
		
		// initialize the deck
		initializeDeck();

		// deal out the cards
		distributeCards();
		
		// Create the game frame
		gameFrame = new BohnanzaFrame(players[0].getHand(), players[1].getHand());
		
		// set the active player
		setActivePlayer(getPlayers()[0]);

		// add test cards to the piles
		TablePanel tp = gameFrame.getTablePanel();
		
		tp.addCard(tp.getDiscardPile(), new Card(Type.RED, 1), 0);
		getDiscardPile().push(new Card(Type.RED, 1));
		
		tp.addCard(tp.getOfferPile(), new Card(Type.BLUE, 2), 1);
		tp.addCard(tp.getOfferPile(), new Card(Type.BLACK_EYED, 2), 0);
		tp.addCard(tp.getOfferPile(), new Card(Type.CHILI, 2), 2);
		tp.addCard(tp.getFields()[0], new Card(Type.BLACK_EYED, 1), 0);
//						tp.addCard(tp.getFields()[0], new Card(Type.GREEN, 2), 1);
		getPlayers()[0].getFields().get(0).setCard(tp.getCard(tp.getFields()[0], 0).getCard());
		getPlayers()[0].getFields().get(1).setCard(tp.getCard(tp.getFields()[0], 1).getCard());
		
		tp.increaseFieldCount(1);
		tp.addCard(tp.getFields()[1], new Card(Type.SOY, 2), 1);
		tp.addCard(tp.getFields()[1], new Card(Type.STINK, 1), 0);
		tp.addCard(tp.getFields()[1], new Card(Type.CHILI, 2), 2);
		getPlayers()[1].addField();
		getPlayers()[1].getFields().get(0).setCard(tp.getCard(tp.getFields()[1], 0).getCard());
		getPlayers()[1].getFields().get(1).setCard(tp.getCard(tp.getFields()[1], 1).getCard());
		getPlayers()[1].getFields().get(2).setCard(tp.getCard(tp.getFields()[1], 2).getCard());
		
		// add the action listeners
		addActionListenerToCardsInHand();
		addActionListenerToControlButtons();
		addActionListenerToTable();
		
		// Play the sound and make it loop
//		playSound("assets/music/main_theme.wav", true);
		
	}
	
	// setters and getters
	public Player[] getPlayers() {
		return players;
	}

	public void setPlayers(Player[] players) {
		this.players = players;
	}

	public BohnanzaFrame getGameFrame() {
		return gameFrame;
	}
	
	public void setGameFrame(BohnanzaFrame gameFrame) {
		this.gameFrame = gameFrame;
	}

	public Player getActivePlayer() {
		return activePlayer;
	}

	public void setActivePlayer(Player activePlayer) {
		this.activePlayer = activePlayer;
	}

	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	public Stack<Card> getDiscardPile() {
		return discardPile;
	}

	public void setDiscardPile(Stack<Card> discardPile) {
		this.discardPile = discardPile;
	}

	public Stack<Card> getDrawPile() {
		return drawPile;
	}

	public void setDrawPile(Stack<Card> drawPile) {
		this.drawPile = drawPile;
	}

	public Card[] getOfferPile() {
		return offerPile;
	}

	public void setOfferPile(Card[] offerPile) {
		this.offerPile = offerPile;
	}
	
	public boolean isCanPlant() {
		return canPlant;
	}

	public void setCanPlant(boolean canPlant) {
		this.canPlant = canPlant;
	}

	public boolean isCanDiscard() {
		return canDiscard;
	}

	public void setCanDiscard(boolean canDiscard) {
		this.canDiscard = canDiscard;
	}

	public int getCardClicked() {
		return cardClicked;
	}

	public void setCardClicked(int cardClicked) {
		this.cardClicked = cardClicked;
	}

	public int getFieldClicked() {
		return fieldClicked;
	}

	public void setFieldClicked(int fieldClicked) {
		this.fieldClicked = fieldClicked;
	}
	
	public int getOfferClicked() {
		return offerClicked;
	}

	public void setOfferClicked(int offerClicked) {
		this.offerClicked = offerClicked;
	}

	public boolean isCanNextPhase() {
		return canNextPhase;
	}

	public void setCanNextPhase(boolean canNextPhase) {
		this.canNextPhase = canNextPhase;
	}

	public boolean isPlanting() {
		return planting;
	}

	public void setPlanting(boolean planting) {
		this.planting = planting;
	}
	
	public int getActivePlayerNumber() {
		return activePlayerNumber;
	}

	public void setActivePlayerNumber(int currentPlayer) {
		this.activePlayerNumber = currentPlayer;
	}

	// Utility methods
	/**
     * @author Daniel
     * Method to initialize the deck
     */
    public void initializeDeck(){
        // Initialize the draw pile
        drawPile = new Stack<>();
       
        // Initialize the type of cards to draw from
        Type[] beanPool = new Type[]{
            Type.BLACK_EYED,
            Type.BLUE,
            Type.CHILI,
            Type.GREEN,
            Type.RED,
            Type.SOY,
            Type.STINK
        };
       
        // Loop through each type of beans in the drawing pool
        for (Type bean : beanPool){
            // Loop through all the bean counts in the drawing pool
            for (int beanNum=0; beanNum<bean.getCount(); beanNum++){
                // Add one card to the pile
                drawPile.push(new Card(bean, 1));
            }
        }

        // Shuffle the deck
        Collections.shuffle(drawPile);
        
    }
    
    /**
     * @author Daniel
     */
    // give the cards to the players from the shuffled deck
    public void distributeCards(){
        // Iterate through both players
        for (Player player : players){
           
            for (int card=0; card<5; card++){
               
                drawCard(player);
               
            }
        }
    }

	/**
	 * @author Jayden
	 */
	// method to add a card to a players hand from draw pile
	public void drawCard(Player player){
		// Draw a card from the draw pile and give it to the player
		// pop removes the element while returning it
		player.addCard(drawPile.pop());
		if (getGameFrame() != null){
			getGameFrame().getHandPanel()[activePlayerNumber].remakeTheHand(player.getHand());
			getGameFrame().getHandPanel()[activePlayerNumber].updateGridColumn();
		}
	}
	
	/**
	 * @author Daniel
	 */
	// Method to play a sound
	public static Clip playSound(String soundFile, boolean isLooping) {
		
		// Declare the clip
		Clip clip = null;

		// Try/catch for getting the file
		// https://stackoverflow.com/questions/26305/how-can-i-play-sound-in-java
		try {

			File file = new File("./" + soundFile); // Create the sound file
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(file.toURI().toURL()); // Create the audo input
																							  // stream using the file
			clip = AudioSystem.getClip(); // Get the clip of the sound
			clip.open(audioIn); // Open the clip
			
			// Loop the clip depending on the boolean given
			if (isLooping) {
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			}
			
			clip.start(); // Start the clip

		// Catch any exceptions and print the message
		} catch (Exception e) {

			System.err.println(e.getMessage());

		}
		
		// Return the sound clip
		return clip;

	}
	
	// create the two players in the game
	public void initializePlayers() {
		
		getPlayers()[0] = new Player();
		getPlayers()[1] = new Player();
		
	}
	
	// add the action listeners to the buttons in the hand panels
	/**
	 * @author Edwin
	 */
	public void addActionListenerToCardsInHand() {
		
		// loop through all the cards and add the action listener
		for (CardPanel cardPanel: getGameFrame().getHandPanel()[0].getCardButtons()) {
			cardPanel.getCardButton().addActionListener(this);
		}
		
		for (CardPanel cardPanel: getGameFrame().getHandPanel()[1].getCardButtons()) {
			cardPanel.getCardButton().addActionListener(this);
		}
		
	}
	
	// add action listeners to the buttons in the control panels
	/**
	 * @author Edwin
	 */
	public void addActionListenerToControlButtons() {
		
		// get all the buttons and add the action listener
		getGameFrame().getControlPanel().getQuitButton().addActionListener(this);
		getGameFrame().getControlPanel().getHelpButton().addActionListener(this);
		getGameFrame().getControlPanel().getBuyFieldButton().addActionListener(this);
		getGameFrame().getControlPanel().getHarvestButton().addActionListener(this);
		getGameFrame().getControlPanel().getDiscardButton().addActionListener(this);
		getGameFrame().getControlPanel().getPlantButton().addActionListener(this);
		getGameFrame().getControlPanel().getEndPhaseButton().addActionListener(this);
		
	}
	
	// add action listeners to the buttons in the control panels
	/**
	 * @author Edwin
	 */
	public void addActionListenerToTable() {
		
		// get all the buttons and add the action listener
		getGameFrame().getTablePanel().getDrawPile().getCardButton().addActionListener(this);
		
		// loop through all the components, which will be buttons
		for (int component=0; component<3; component++) {
			getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getOfferPile(), component).getCardButton().addActionListener(this);
		}
		
		for (JPanel field: getGameFrame().getTablePanel().getFields()) {
			for (int component=0; component<field.getComponentCount(); component++) {
				getGameFrame().getTablePanel().getCard(field, component).getCardButton().addActionListener(this);
			}
		}
		
		getGameFrame().getTablePanel().getDiscardPile().getCardButton().addActionListener(this);
		
	}
	
	// handles the outputs that are made when the player interacts with a card
	/**
	 * @author Edwin
	 * @author Jayden
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
	
		// check which what invoked this method
		// check which player is active
		if (getActivePlayer().equals(getPlayers()[0])) {
			JPanel player1Field = getGameFrame().getTablePanel().getFields()[0];
			
			// loop through player 1 hand
			for (int component=0; component<getGameFrame().getHandPanel()[0].getCardButtons().size(); component++) {
				CardPanel cardPanel = getGameFrame().getHandPanel()[0].getCardButtons().get(component);
				
				if (event.getSource() == cardPanel.getCardButton()) {
					
					setCardClicked(component);
					updateButtonsForClickingOnHand();
					
				}
			}
			
			// loop through player 1 field
			for (int component=0; component<player1Field.getComponentCount(); component++) {
				if (event.getSource() == getGameFrame().getTablePanel().getCard(player1Field, component).getCardButton()) {
					
					// check if the user clicked this button because they are planting or wanting to harvest
					if (!isPlanting()) {
						
						setFieldClicked(component);
						updateButtonsForClickingOnField();
						
					} else {
						
						CardPanel beanToPlant;
						
						// check if the phase is planting or planting from harvest
						if (getPhase() == 2) {
							beanToPlant = getGameFrame().getHandPanel()[0].getCardButtons().getFirst();
						} else {
							beanToPlant = getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getOfferPile(), getOfferClicked());
						}
						
						// toggle highlight off
						getGameFrame().getTablePanel().setHighlightOff(0);
						
						if (getActivePlayer().getFields().get(component).canPlant(beanToPlant.getCard())) {
							
							// plant bean at the field
							getActivePlayer().plantBean(beanToPlant.getCard(), component);
							getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getFields()[0], component).updateCard(getActivePlayer().getFields().get(component).getCard());
							beanToPlant.updateCard(null);
							
						}
						
						setPlanting(false);

					}
				}
			}
			
		} else {
			JPanel player2Field = getGameFrame().getTablePanel().getFields()[1];
			
			// loop through player 2 hand
			for (int component=0; component<getGameFrame().getHandPanel()[1].getCardButtons().size(); component++) {
				CardPanel cardPanel = getGameFrame().getHandPanel()[1].getCardButtons().get(component);
				
				if (event.getSource() == cardPanel.getCardButton()) {
					
					setCardClicked(component);
					updateButtonsForClickingOnHand();
				}
			}

			
			// loop through player 2 field
			for (int component=0; component<player2Field.getComponentCount(); component++) {
				if (event.getSource() == getGameFrame().getTablePanel().getCard(player2Field, component).getCardButton()) {
					
					// check if the user clicked this button because they are planting or wanting to harvest
					if (!isPlanting()) {
						
						setFieldClicked(component);
						updateButtonsForClickingOnField();
						
					} else {
						
						CardPanel beanToPlant;
						
						// check if the phase is planting or planting from harvest
						if (getPhase() == 2) {
							beanToPlant = getGameFrame().getHandPanel()[1].getCardButtons().getFirst();
						} else {
							beanToPlant = getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getOfferPile(), getOfferClicked());
						}
						
						// toggle highlight off
						getGameFrame().getTablePanel().setHighlightOff(1);
						
						if (getActivePlayer().getFields().get(component).canPlant(beanToPlant.getCard())) {
							System.out.println("HI");
							// plant bean at the field
							getActivePlayer().plantBean(beanToPlant.getCard(), component);
							getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getFields()[1], component).updateCard(getActivePlayer().getFields().get(component).getCard());
							beanToPlant.updateCard(null);
							
						}
						
						setPlanting(false);

					}
				}
			}
		}
		
		// check if the draw card button is clicked
		if (event.getSource() == getGameFrame().getTablePanel().getDrawPile().getCardButton()) {
			System.out.println("Draw Card");
			// draw from the drawPile to offer piles
			if (phase == 3){}
			// check if the phase is equal to 4 (Draw 2 cards)
			if (phase == 4){
				drawCard(activePlayer);
			}
		}
		
		// check if the offer pile is clicked
		for (int component=0; component<3; component++) {
			CardPanel offerPile = getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getOfferPile(), component);
			if (event.getSource() == offerPile.getCardButton()) {
//				System.out.println(getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getOfferPile(), component).getCard().getType());
				if (offerPile.getCard() != null){
					setOfferClicked(component);
					updateButtonsForClickingOnOffer();
				}
			}
		}
		
		// check if the discard pile is clicked
		if (event.getSource() == getGameFrame().getTablePanel().getDiscardPile().getCardButton()) {
			System.out.println("DRAW FROM DISCARD");
		}
		
		// check if one of the control buttons are clicked
		if (event.getSource() == getGameFrame().getControlPanel().getEndPhaseButton()) {
			
			// change the phase if the 
			updatePhase();
			
		} else if (event.getSource() == getGameFrame().getControlPanel().getHarvestButton()) {
			
			// harvest the field if the field can be harvested
			if (getActivePlayer().canHarvest(getFieldClicked())) {
				
				// harvest it from the player to add to the coin count
				Card remainingCard = getActivePlayer().harvestField(getFieldClicked());
				// change the discard pile GUI
				getGameFrame().getTablePanel().getDiscardPile().updateCard(remainingCard);
				// add to the discard pile in the this class
				getDiscardPile().push(remainingCard);
				
				// remove the card pile from the field in the GUI
				if (getActivePlayer().equals(getPlayers()[0])) {
					getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getFields()[0], getFieldClicked()).updateCard(null);
				} else {
					getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getFields()[1], getFieldClicked()).updateCard(null);
				}
				
			} else {
				// if can't harvest, then output error message
				System.out.println("CAN'T HARVEST");
			}
			
		} else if (event.getSource() == getGameFrame().getControlPanel().getPlantButton()) {
			
			getGameFrame().getControlPanel().disableAllButtons();
			
			int currActivePlayer;
			
			if (getActivePlayer().equals(getPlayers()[0])) {
				currActivePlayer = 0;
			} else {
				currActivePlayer = 1;
			}
			
			// check if the player is in the plant phase or offer card phase
			if (getPhase() == 2) {
				
				System.out.println("PLANT BEAN FROM HAND");
				
			} else { // use an else because only phase 2 and phase 3 can invoke the plant button
				
				for (int field=0; field<getActivePlayer().getFields().size(); field++) {
					// highlight plantable fields
					if (getActivePlayer().getFields().get(field).canPlant(getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getOfferPile(), getOfferClicked()).getCard())) {
						getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getFields()[currActivePlayer], field).setHighlight(true);
						// set the planting to true to that clicking the beans in the fields will add to it instead of invoking harvesting
						setPlanting(true);
					}
				}
				
			}
			
		}
		
	}
	
	// update the buttons that are available for clicking on a bean in the hand
	/**
	 * @author Edwin
	 */
	public void updateButtonsForClickingOnHand() {
		
		getGameFrame().getControlPanel().enableButtonsClickingCardInHand(isCanDiscard(), isCanPlant());
		
	}
	
	// update the buttons that are available for clicking on a pile of beans on a field
	/**
	 * @author Edwin
	 */
	public void updateButtonsForClickingOnField() {
		if (getActivePlayer().getFields().get(getFieldClicked()).getCard() != null) {
			getGameFrame().getControlPanel().enableButtonsClickingFieldCards();
		}
		
	}
	
	// update the buttons that are available for clicking on a pile of beans in the offer piles
	/**
	 * @author Edwin
	 */
	public void updateButtonsForClickingOnOffer() {
		
		getGameFrame().getControlPanel().enableButtonsClickingOfferedCards();
		
	}

	
	// update the phase for the current active player
	/**
	 * @author Edwin
	 */
	public void updatePhase() {
		System.out.println(getPhase());
		if (getPhase() < 4) {
			setPhase(getPhase() + 1);
		} else {
			setPhase(1);
			activePlayerNumber++;
			activePlayerNumber %= 2;
			if (getActivePlayer().equals(getPlayers()[0])) {
				setActivePlayer(getPlayers()[1]);
			} else {
				setActivePlayer(getPlayers()[0]);
			}
		}
		getGameFrame().getControlPanel().updatePhaseText(activePlayerNumber, phase);
	}
	
}