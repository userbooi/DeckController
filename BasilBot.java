package controller;

import javax.swing.Timer;
import model.AI;
import model.Card;
import model.Field;

/**
 * 
 * @author Daniel
 * BasilBot v0.1
 */
public class BasilBot implements AI {

	private final int AI_MOVE_DELAY = 100;

    // Fields
    private DeckController deckController;
    
    // Constructor
    public BasilBot(){
        super();

    }

    // Accept from offer
    public void phase1(){

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
                System.out.println("Planted on empty");
                return true;
            }
        }
        System.out.println("No empty found");
        return false;

    }

    // Method when plant is necessary
    public void mandatoryPlant(){

        Card planting = deckController.getActivePlayer().getHand().getFirst();

        // Look for the same card to plant on top of
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            Field curField = deckController.getActivePlayer().getFields().get(field);
            if (curField.getCard() == null) continue;
            if (curField.getCard().getType() == planting.getType()){
                deckController.clickButton("h" + 0);
                deckController.clickButton("plant");
                deckController.clickButton("f" + field);
                return;
            }
        }
        
        if (plantOnEmpty(planting)) return;

        // Look for a card to harvest from
        int bestField = -1;
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            Field curField = deckController.getActivePlayer().getFields().get(field);
            if (curField.getCard() == null) continue;
            if (bestField == -1 || curField.getCard().getCount() > deckController.getActivePlayer().getFields().get(bestField).getCard().getCount()){
                bestField = field;
            }
        }
        
        deckController.clickButton("f" + bestField);
        deckController.clickButton("harvest");
        deckController.clickButton("buy");

        plantOnEmpty(planting);
            
    }

    // Plant or discard
    public void phase2(){

        mandatoryPlant();

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

        if (phase == 1){ // Accept from offer piles

            phase1();

        } else if (phase == 2){ // Plant and discard

            phase2();

        } else if (phase == 3){ // Draw from offer

            phase3();

        } else if (phase == 4){

            phase4();
            
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
