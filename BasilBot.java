package controller;

import javax.swing.Timer;
import model.AI;
import model.Card;
import model.Field;

/**
 * 
 * @author Daniel
 * BasilBot v0.2
 * 
 * -Plays necessary moves
 * -Greedily play all moves
 * -Harvest when at max stack
 * -Discard irrelevant cards
 * 
 */
public class BasilBot implements AI {

	private final int AI_MOVE_DELAY = 1;

    // Fields
    private DeckController deckController;
    
    // Constructor
    public BasilBot(){
        super();

    }

    // Accept from offer
    public void phase1(){

        optionalHarvest();

        // Accept offers with the same type first
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            for (int offer=0; offer<3; offer++){
                Field curField = deckController.getActivePlayer().getFields().get(field);
                Card curOffer = deckController.getOfferPile()[offer];
                if (curField.getCard() == null || curOffer == null) continue;
                if (curField.getCard().getType() == curOffer.getType()){
                    deckController.clickButton("o" + offer);
                    deckController.clickButton("plant");
                    deckController.clickButton("f" + field);
                    break;
                }
            }
        }

        // Take cards to fill up fields
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            for (int offer=0; offer<3; offer++){
                Field curField = deckController.getActivePlayer().getFields().get(field);
                Card curOffer = deckController.getOfferPile()[offer];
                if (curOffer == null) continue;
                if (curField.canPlant(curOffer)){
                    deckController.clickButton("o" + offer);
                    deckController.clickButton("plant");
                    deckController.clickButton("f" + field);
                    break;
                }
            }
        }

    }

    // Method to search for and plant the first card if possible
    public boolean plantOnEmpty(Card planting){
        
        // Look for an empty space to plant on top of
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            Field curField = deckController.getActivePlayer().getFields().get(field);
            if (curField.canPlant(planting)){
                deckController.clickButton("h" + 0);
                deckController.clickButton("plant");
                deckController.clickButton("f" + field);
                return true;
            }
        }
        
        return false;

    }

    public boolean optionalPlant(){

        Card planting = deckController.getActivePlayer().getHand().getFirst();

        // Look for the same card to plant on top of
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            Field curField = deckController.getActivePlayer().getFields().get(field);
            if (curField.getCard() == null) continue;
            if (curField.getCard().getType() == planting.getType()){
                deckController.clickButton("h" + 0);
                deckController.clickButton("plant");
                deckController.clickButton("f" + field);
                return true;
            }
        }
        
        // Check if there is an empty slot to plant on
        return plantOnEmpty(planting);

    }

    public boolean optionalHarvest(){

        boolean hasHarvested = false;

        // Check if any of the beans are past their max yield
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            Field curField = deckController.getActivePlayer().getFields().get(field);
            if (curField.getCard() == null) continue;
            if (curField.getCard().getType().getBeanometer()[3] <= curField.getCard().getCount()){
                
                deckController.clickButton("f" + field);
                deckController.clickButton("harvest");
                deckController.clickButton("buy");

                hasHarvested = true;

            }
        }

        return hasHarvested;

    }

    public void mandatoryHarvest(){

        if (optionalHarvest())
            return;

        // Look for a card to harvest from
        int worstField = -1;
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            Field curField = deckController.getActivePlayer().getFields().get(field);
            if (curField.getCard() == null || !deckController.getActivePlayer().canHarvest(field)) continue;
            if (worstField == -1 || curField.getCard().getCount() > deckController.getActivePlayer().getFields().get(worstField).getCard().getCount()){
                worstField = field;
            }
        }
        
        deckController.clickButton("f" + worstField);
        deckController.clickButton("harvest");
        deckController.clickButton("buy");

    }

    // Method when plant is necessary
    public void mandatoryPlant(){

        Card planting = deckController.getActivePlayer().getHand().getFirst();

        if (optionalPlant()) return;

        mandatoryHarvest();

        if (!plantOnEmpty(planting))
            System.out.println("ERROR: CANNOT PLANT");
            
    }

    public void optionalDiscard(){

        // Check if any fields contain the next card
        for(int hand=0; hand<deckController.getActivePlayer().getHand().size(); hand++){

            // Get the current card it is checking
            Card curCard = deckController.getActivePlayer().getHand().get(hand);
            boolean sameCard = false;
            
            for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){

                Field curField = deckController.getActivePlayer().getFields().get(field);
                if (curField.getCard() == null) continue;
                if (curField.getCard().getType() == curCard.getType()){
                    sameCard = true;

                }

            }

            // If no cards in the fields are the same type as this card, then discard
            if (!sameCard){
                deckController.clickButton("h" + hand);
                deckController.clickButton("discard");
                return;
            }

        }
        // Discard from first slot

    }

    // Plant or discard
    public void phase2(){

        mandatoryPlant();
        optionalPlant();
        optionalDiscard();

    }

    // Draw from offer
    public void phase3(){

        for (int i=0;i<3;i++)
            deckController.clickButton("draw");

        while (deckController.isAbleDrawFromDiscard())
            deckController.clickButton("discard_pile");

        // Plant from offer pile
        phase1();

    }

    // Draw 2
    public void phase4(){

        // Draw 2
        for (int i=0;i<2;i++)
            deckController.clickButton("draw");

    }

    @Override
    public void makeMove() {
        
        int phase = deckController.getPhase();

        switch (phase){
            case 1:
                phase1();
                break;
            case 2:
                phase2();
                break;
            case 3:
                phase3();
                break;
            case 4:
                phase4();
                break;
        }

        Timer t = new Timer(AI_MOVE_DELAY, e->{
            deckController.clickButton("end");
        });

        t.setRepeats(false);
        t.start();
    }

    @Override
    public void assignDeckController(DeckController deckController) {
        this.deckController = deckController;
    }

}
