package controller;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// Necessary imports
import java.io.File;
import java.util.Arrays;
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
	private int phase = 1;

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
	private Card[] offerPile = new Card[3];

	// Store the number of times an offer card has been drawn from the draw pile (max 3)
	private int offersDrawn = 0;
	
	// store the current card clicked
	private int cardClicked;
	// store the current field clicked
	private int fieldClicked;
	// store the current offer clicked
	private int offerClicked;

	private int cardsDrawn;
	
	private int cardsPlanted;
	private int cardsDiscarded;
	
	private boolean ableDrawFromDiscard;

	
	// Constructor method called upon initialization
	public DeckController() {

		// Read image files using the file reader
		// read file must be called first since all the type information is dependent on the text file that needs to be read
		FileReader.readFile();

//		LinkedList<Card> testList1 = new LinkedList<Card>();
//		testList1.add(new Card(Type.BLACK_EYED, 1));
//		testList1.add(new Card(Type.BLUE, 1));
//		testList1.add(new Card(Type.SOY, 1));
//		testList1.add(new Card(Type.CHILI, 1));
//		testList1.add(new Card(Type.GREEN, 1));
//		testList1.add(new Card(Type.STINK, 1));
//		testList1.add(new Card(Type.BLACK_EYED, 1));
//		testList1.add(new Card(Type.CHILI, 1));
//		testList1.add(new Card(Type.STINK, 1));
//		testList1.add(new Card(Type.GREEN, 1));
//		testList1.add(new Card(Type.BLACK_EYED, 1));
//
//		LinkedList<Card> testList2 = new LinkedList<Card>();
//		testList2.add(new Card(Type.BLACK_EYED, 1));
//		testList2.add(new Card(Type.BLUE, 1));
//		testList2.add(new Card(Type.SOY, 1));
//		testList2.add(new Card(Type.CHILI, 1));
//		testList2.add(new Card(Type.GREEN, 1));
//		testList2.add(new Card(Type.STINK, 1));
//		testList2.add(new Card(Type.BLACK_EYED, 1));
//		testList2.add(new Card(Type.CHILI, 1));
//		testList2.add(new Card(Type.STINK, 1));
//		testList2.add(new Card(Type.GREEN, 1));
//		testList2.add(new Card(Type.BLACK_EYED, 1));
		
		// set first player to player1
		activePlayerNumber = 0;
		
		// initialize the two players
		initializePlayers();
//		initializePlayers(testList1, testList2);
		getPlayers()[0].addCoins(100);
		
		// initialize the deck
		initializeDeck();

		// deal out the cards
		distributeCards();
		
		// Create the game frame
		gameFrame = new BohnanzaFrame(players[0], players[1], getDrawPile().size());

		// call updatePhase
		
		// set the active player
		setActivePlayer(getPlayers()[0]);

		// add test cards to the piles
//		TablePanel tp = gameFrame.getTablePanel();
		
//		getGameFrame().getTablePanel().getDiscardPile().updateCard(new Card(Type.STINK, 1));
//		getDiscardPile().push(new Card(Type.STINK, 1));
		
		// tp.addCard(tp.getOfferPile(), new Card(Type.BLUE, 2), 1);
		// tp.addCard(tp.getOfferPile(), new Card(Type.BLACK_EYED, 2), 0);
		// tp.addCard(tp.getOfferPile(), new Card(Type.CHILI, 2), 2);
//		tp.addCard(tp.getFields()[0], new Card(Type.BLACK_EYED, 1), 0);
//		tp.addCard(tp.getFields()[0], new Card(Type.GREEN, 2), 1);
//		getPlayers()[0].getFields().get(0).setCard(tp.getCard(tp.getFields()[0], 0).getCard());
//		getPlayers()[0].getFields().get(1).setCard(tp.getCard(tp.getFields()[0], 1).getCard());
//		
//		tp.increaseFieldCount(1);
//		tp.addCard(tp.getFields()[1], new Card(Type.SOY, 2), 1);
//		tp.addCard(tp.getFields()[1], new Card(Type.STINK, 1), 0);
//		tp.addCard(tp.getFields()[1], new Card(Type.CHILI, 2), 2);
//		getPlayers()[1].addField();
//		getPlayers()[1].getFields().get(0).setCard(tp.getCard(tp.getFields()[1], 0).getCard());
//		getPlayers()[1].getFields().get(1).setCard(tp.getCard(tp.getFields()[1], 1).getCard());
//		getPlayers()[1].getFields().get(2).setCard(tp.getCard(tp.getFields()[1], 2).getCard());
		
		// add the action listeners
		addActionListenerToCardsInHand();
		addActionListenerToControlButtons();
		addActionListenerToTable();
		
		updatePhase();
		
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

	public int getOffersDrawn() {
		return offersDrawn;
	}

	public void setOffersDrawn(int offersDrawn) {
		this.offersDrawn = offersDrawn;
	}

	public int getCardsPlanted() {
		return cardsPlanted;
	}

	public void setCardsPlanted(int cardsPlanted) {
		this.cardsPlanted = cardsPlanted;
	}

	public int getCardsDrawn() {
		return cardsDrawn;
	}

	public void setCardsDrawn(int cardsDrawn) {
		this.cardsDrawn = cardsDrawn;
	}

	public int getCardsDiscarded() {
		return cardsDiscarded;
	}

	public void setCardsDiscarded(int cardsDiscarded) {
		this.cardsDiscarded = cardsDiscarded;
	}

	public boolean isAbleDrawFromDiscard() {
		return ableDrawFromDiscard;
	}

	public void setAbleDrawFromDiscard(boolean ableDrawFromDiscard) {
		this.ableDrawFromDiscard = ableDrawFromDiscard;
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
            Type.BLUE,
            Type.CHILI,
            Type.GREEN,
            Type.RED,
            Type.SOY,
            Type.STINK,
            Type.BLACK_EYED,
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
			getGameFrame().getHandPanel()[activePlayerNumber].remakeTheHand(player.getHand(), 1);
		}
	}

	/**
	 * @author Daniel
	 */
	// Method to draw a card to the offer pile, happens in phase 3
	public void drawToOffer(){

		// Increase counter
		offersDrawn++;

		// Draw a card from the draw pile
		Card drawnCard = drawPile.pop();

		// Loop through the offers, looking for cards of the same type to add count to
		for(int pile=0; pile<3; pile++){
			Card card = offerPile[pile];
			if(card == null) continue;
			if(card.getType() == drawnCard.getType()){
				card.setCount(card.getCount() + 1);
				gameFrame.getTablePanel().getCard(gameFrame.getTablePanel().getOfferPile(), pile).updateCard(card);
				return;
			}
		}

		// Loop through the piles searching for a place to put the card
		for(int pile=0; pile<3; pile++){

			// Check if there is a card there already
			if (offerPile[pile] == null){
				
				// Set the card
				offerPile[pile] = drawnCard;
				gameFrame.getTablePanel().getCard(gameFrame.getTablePanel().getOfferPile(), pile).updateCard(drawnCard);
				return;

			}

		}
		
	}

	/**
	 * @author Daniel
	 * @author Edwin
	 */
	// Method to clear all the offers
	public void clearOffers(){

		// Loop through the piles to remove the cards
		for(int pile=0; pile<3; pile++){

			// send the cards to the discard pile
			if (getOfferPile()[pile] != null) {
				addToDiscard(getOfferPile()[pile]);
			}
				
			// Set the card
			gameFrame.getTablePanel().getCard(gameFrame.getTablePanel().getOfferPile(), pile).updateCard(null);
			offerPile[pile] = null;

		}
			
	}

	/**
	 * @author Daniel
	 */
	// Method to draw a card from discard to offer
	public void drawDiscardToOffer(){
		
		// check if the player has already drawn from the discard
		if (!isAbleDrawFromDiscard()) {
			return ;
		}
		
		Card card = discardPile.peek();
		
		// Check if there is the same card type in the offer piles
		for (int pile=0; pile<3; pile++){
			
			Card offerCard = offerPile[pile];

			if (offerCard == null) continue;

			if (offerCard.getType() == card.getType()){
				offerCard.setCount(offerCard.getCount() + card.getCount());
				gameFrame.getTablePanel().getCard(gameFrame.getTablePanel().getOfferPile(), pile).updateCard(offerCard);
				discardPile.pop();
				if (discardPile.empty()) {
					gameFrame.getTablePanel().getDiscardPile().updateCard(null);
				} else {
					gameFrame.getTablePanel().getDiscardPile().updateCard(discardPile.peek());
				}
				
				canDrawFromDiscard();
				
				return;
			}

		}

	}

	/**
	 * @author Daniel
	 */
	public boolean canDrawFromDiscard(){

		if (discardPile.empty()) {
			setAbleDrawFromDiscard(false);
			return false;
		}
		
		Card card = discardPile.peek();

		// Check if there is the same card type in the offer piles
		for (int pile=0; pile<3; pile++){
			
			Card offerCard = offerPile[pile];

			if (offerCard == null) continue;

			// gameFrame.getTablePanel().getCard(gameFrame.getTablePanel().getOfferPile(), pile).getCard();
			if (offerCard.getType() == card.getType()){
				return true;
			}

		}
		
		setAbleDrawFromDiscard(false);
		return false;

	}
	
	// add a card to the discard pile
	/**
	 * @author Edwin
	 */
	public void addToDiscard(Card discardedCard) {
		
		// check if the top of the discard pile can be merged with the incoming discard card
		if (!getDiscardPile().empty() && getDiscardPile().peek().getType().equals(discardedCard.getType())) {
			Card topOfDiscard = getDiscardPile().pop();
			topOfDiscard.setCount(topOfDiscard.getCount() + discardedCard.getCount());
			getDiscardPile().push(topOfDiscard);
		} else {
			getDiscardPile().push(discardedCard);
		}
		
		// update the visual for the discard pile
		if (!getDiscardPile().empty()) {
			getGameFrame().getTablePanel().getDiscardPile().updateCard(getDiscardPile().peek());
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
	public void initializePlayers(LinkedList<Card> hand1, LinkedList<Card> hand2) { // for testing
		
		getPlayers()[0] = new Player(hand1);
		getPlayers()[1] = new Player(hand2);
		
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
	
	// add action listener to the third field when it has been purchased
	/**
	 * @author Jayden
	 */
	public void addActionListenerToThirdField(){
		JPanel field = getGameFrame().getTablePanel().getFields()[activePlayerNumber];
		getGameFrame().getTablePanel().getCard(field, 2).getCardButton().addActionListener(this);
	}

	
	// handles the outputs that are made when the player interacts with a card
	/**
	 * @author Edwin
	 * @author Jayden
	 * @author Daniel
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
				
				if (event.getSource() == cardPanel.getCardButton() && getPhase() == 2) {
					
					setCardClicked(component);

					// decide what can be done
					if (component == 0) {
						if (getCardsPlanted() == 0) {
							setCanPlant(true);
							setCanDiscard(false);
						} else if (getCardsPlanted() < 2) {
							setCanPlant(true);
							if (getCardsDiscarded() < 1) {
								setCanDiscard(true);
							} else {
								setCanDiscard(false);
								setCanPlant(false);
							}
						} else {
							setCanPlant(false);
							if (getCardsDiscarded() < 1) {
								setCanDiscard(true);
							} else {
								setCanDiscard(false);
							}
						}
					} else {
						setCanPlant(false);
						if (getCardsPlanted() == 0) {
							setCanDiscard(false);
						} else if (getCardsDiscarded() < 1) {
							setCanDiscard(true);
						} else {
							setCanDiscard(false);
						}
					}
					
					// set the buttons depending on what can be done
					updateButtonsForClickingOnHand(isCanDiscard(), isCanPlant());
					
				}
			}
			
			// loop through player 1 field
			for (int component=0; component<player1Field.getComponentCount(); component++) {
				if (event.getSource() == getGameFrame().getTablePanel().getCard(player1Field, component).getCardButton()) {
					
					// check if the user clicked this button because they are planting or wanting to harvest
					if (!isPlanting() && !isAbleDrawFromDiscard()) {
						
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
							getGameFrame().getTablePanel().getCard(player1Field, component).updateCard(getActivePlayer().getFields().get(component).getCard());
						
							if (phase != 2){
								beanToPlant.updateCard(null);
								offerPile[offerClicked] = null;
							} else {
								
								getActivePlayer().getHand().remove(0);
								getGameFrame().getHandPanel()[0].remakeTheHand(getActivePlayer().getHand(), -1);
								addActionListenerToCardsInHand();
								
							}
							
						}
						
						setCardsPlanted(getCardsPlanted()+1);
						setPlanting(false);

					}
				}
			}
			
		} else {
			JPanel player2Field = getGameFrame().getTablePanel().getFields()[1];
			
			// loop through player 2 hand
			for (int component=0; component<getGameFrame().getHandPanel()[1].getCardButtons().size(); component++) {
				CardPanel cardPanel = getGameFrame().getHandPanel()[1].getCardButtons().get(component);
				
				if (event.getSource() == cardPanel.getCardButton() && getPhase() == 2) {
					
					setCardClicked(component);
					
					// decide what can be done
					if (component == 0) {
						if (getCardsPlanted() == 0) {
							setCanPlant(true);
							setCanDiscard(false);
						} else if (getCardsPlanted() < 2) {
							setCanPlant(true);
							if (getCardsDiscarded() < 1) {
								setCanDiscard(true);
							} else {
								setCanDiscard(false);
								setCanPlant(false);
							}
						} else {
							setCanPlant(false);
							if (getCardsDiscarded() < 1) {
								setCanDiscard(true);
							} else {
								setCanDiscard(false);
							}
						}
					} else {
						setCanPlant(false);
						if (getCardsPlanted() == 0) {
							setCanDiscard(false);
						} else if (getCardsDiscarded() < 1) {
							setCanDiscard(true);
						} else {
							setCanDiscard(false);
						}
					}
					
					// set the buttons depending on what can be done
					updateButtonsForClickingOnHand(isCanDiscard(), isCanPlant());
				}
			}

			
			// loop through player 2 field
			for (int component=0; component<player2Field.getComponentCount(); component++) {
				if (event.getSource() == getGameFrame().getTablePanel().getCard(player2Field, component).getCardButton()) {
					
					// check if the user clicked this button because they are planting or wanting to harvest
					if (!isPlanting() && !isAbleDrawFromDiscard()) {
						
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
							// plant bean at the field
							getActivePlayer().plantBean(beanToPlant.getCard(), component);
							getGameFrame().getTablePanel().getCard(player2Field, component).updateCard(getActivePlayer().getFields().get(component).getCard());
							
							if (phase != 2){
								beanToPlant.updateCard(null);
								offerPile[offerClicked] = null;
							} else {

								getActivePlayer().getHand().remove(0);
								getGameFrame().getHandPanel()[1].remakeTheHand(getActivePlayer().getHand(), -1);
								addActionListenerToCardsInHand();
								
							}
							
						}
						
						setCardsPlanted(getCardsPlanted()+1);
						setPlanting(false);

					}
				}
			}
		}
		
		// check if the draw card button is clicked
		if (event.getSource() == getGameFrame().getTablePanel().getDrawPile().getCardButton()) {
			// draw from the drawPile to offer piles
			if (phase == 3 && offersDrawn < 3){
				drawToOffer();
				if (offersDrawn == 3) {
					if (canDrawFromDiscard()) {
						setAbleDrawFromDiscard(true);
					}
				}
			}
			// check if the phase is equal to 4 (Draw 2 cards)
			if (phase == 4 && cardsDrawn < 2){
				drawCard(activePlayer);
				cardsDrawn++;
			}
		}
		
		// check if the offer pile is clicked
		for (int component=0; component<3; component++) {
			CardPanel offerPile = getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getOfferPile(), component);
			if (event.getSource() == offerPile.getCardButton() && (getPhase() == 3 || getPhase() == 1)) {
//				System.out.println(getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getOfferPile(), component).getCard().getType());
				if (offerPile.getCard() != null && !isAbleDrawFromDiscard()){
					setOfferClicked(component);
					updateButtonsForClickingOnOffer();
				}
			}
		}
		
		// check if the discard pile is clicked
		if (event.getSource() == getGameFrame().getTablePanel().getDiscardPile().getCardButton()) {

			if (phase == 3 && offersDrawn == 3 && isAbleDrawFromDiscard()){

				drawDiscardToOffer();

			}
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
				// add the remaining cards to the discard pile
				addToDiscard(remainingCard);
				
				// remove the card pile from the field in the GUI
				if (getActivePlayer().equals(getPlayers()[0])) {
					getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getFields()[0], getFieldClicked()).updateCard(null);
				} else {
					getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getFields()[1], getFieldClicked()).updateCard(null);
				}
				
				// update the coin label
				getGameFrame().getHandPanel()[activePlayerNumber].getCoinsLabel().setText(Integer.toString(activePlayer.getCoins()));
				getGameFrame().getControlPanel().disableAllButtons();
				
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
				
				for (int field=0; field<getActivePlayer().getFields().size(); field++) {
					// highlight plantable fields
					if (getActivePlayer().getFields().get(field).canPlant(getGameFrame().getHandPanel()[currActivePlayer].getCardButtons().getFirst().getCard())) {
						getGameFrame().getTablePanel().getCard(getGameFrame().getTablePanel().getFields()[currActivePlayer], field).setHighlight(true);
						// set the planting to true to that clicking the beans in the fields will add to it instead of invoking harvesting
						setPlanting(true);
					}
				}
				
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
			
		} else if (event.getSource() == getGameFrame().getControlPanel().getDiscardButton()) {
			
			getGameFrame().getControlPanel().disableAllButtons();
			
			int currActivePlayer;
			
			if (getActivePlayer().equals(getPlayers()[0])) {
				currActivePlayer = 0;
			} else {
				currActivePlayer = 1;
			}
			
			Card discardedCard = getActivePlayer().getHand().get(getCardClicked());
			
			addToDiscard(discardedCard);
			
			getActivePlayer().getHand().remove(getCardClicked());
			getGameFrame().getHandPanel()[currActivePlayer].remakeTheHand(getActivePlayer().getHand(), -1);
			addActionListenerToCardsInHand();
			
			setCardsDiscarded(getCardsDiscarded()+1);

		} else if (event.getSource() == getGameFrame().getControlPanel().getBuyFieldButton()) { // if the player pressed “buy third field” button
	        // the player wants to purchase a 3rd field
	        if (activePlayer.buyField()){
	            // increase the field count
	            getGameFrame().getTablePanel().increaseFieldCount(activePlayerNumber);
	            getGameFrame().getHandPanel()[getActivePlayerNumber()].getCoinsLabel().setText(Integer.toString(activePlayer.getCoins()));;
	            // add an action listener to the third field
	            addActionListenerToThirdField();
	        }
	        
		}
		
	}
	
	// update the buttons that are available for clicking on a bean in the hand
	/**
	 * @author Edwin
	 */
	public void updateButtonsForClickingOnHand(boolean canDiscard, boolean canPlant) {
		
		getGameFrame().getControlPanel().enableButtonsClickingCardInHand(canDiscard, canPlant);
		
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
	 * @author Jayden
	 */
	public void updatePhase() {

		// Do not let the player end phase if they have yet to draw from the offers or can still draw from discard pile
		if ((getPhase() == 3 && (getOffersDrawn() < 3 || canDrawFromDiscard())) 
				|| (getPhase() == 2 && getCardsPlanted() == 0) 
				|| (getPhase() == 4 && getCardsDrawn() < 2))
			return;

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
			
//			System.out.println(getActivePlayer());
		}
		if (phase == 2) {
			clearOffers();
			
			setCanPlant(true);
			setCanDiscard(false);
			setCardsPlanted(0);
			setCardsDiscarded(0);
		} else if (phase == 1) {
			
			offersDrawn = 0; // Reset the number of offers drawn after the phase ends
			
		} else if (phase == 4) {
			
			cardsDrawn = 0;
			setAbleDrawFromDiscard(false);
		
		} else if (phase == 3) {
			
			setAbleDrawFromDiscard(false);
			
		}
		
//		System.out.println(phase);
//		System.out.println(Arrays.toString(offerPile));
		getGameFrame().getControlPanel().updatePhaseText(activePlayerNumber, phase);
		getGameFrame().getControlPanel().disableAllButtons();
	
	}
	
}