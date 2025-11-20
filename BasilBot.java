package controller;

import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.Timer;
import model.AI;
import model.Card;
import model.Field;
import model.Player;
import model.Type;

/**
 * 
 * @author Daniel
 * BasilBot v0.6.7
 * 
 * -Plays necessary moves
 * -Greedily play all moves
 * -Harvest when at max stack
 * -Discard irrelevant cards
 * -Scores cards based on many criteria to find which and whether or not a field should be harvested for an offered field
 * -Lazy card cycling (only works for counts of 2 or above) to remove offers for opponent
 * 
 */
public class BasilBot implements AI {

	private final int AI_MOVE_DELAY = 0;

    // Fields
    private DeckController deckController;
    private HashMap<Type, Double> beanTypeScore = new HashMap<>();

    // Constructor
    public BasilBot(){
        super();

    }

    // Returns a score based on how little more effort it takes to get to the next beanometer
    public int scoreBeanometer(Card card){

        int beansToNextCoin = 3; // Default value of 3
        for(int coins = 0; coins < 4; coins++){
            beansToNextCoin = card.getType().getBeanometer()[coins] - 1 - card.getCount();
            if (beansToNextCoin >= 0)
                break;
        }
        return beansToNextCoin;

    }

    public HashMap<Type,Double> evaluatePlayerBeanScore(Player player){

        // Score up how many card of a bean type are in the hand
        HashMap<Type, Double> ownedBeanScore = new HashMap<>();
        for(int hand=0; hand < player.getHand().size(); hand++){
            Card curCard = player.getHand().get(hand);
            ownedBeanScore.put(curCard.getType(), 
            // Score cards closer to the front of the hand with more points
            ownedBeanScore.getOrDefault(curCard.getType(),0.0) + 
            0.7 * (player.getHand().size() + 2 - hand) / player.getHand().size());
        }

        return ownedBeanScore;
    }

    public HashMap<Type,Double> evaluatePlayerBeanometerScore(Player player){

        // Score up the field cards based on how close it is to the next threshold
        HashMap<Type, Double> playerBeanometerScore = new HashMap<>();
        for(int field=0; field < player.getFields().size(); field++){
            Card curCard = player.getFields().get(field).getCard();
            if (curCard == null)
                continue;
            int beanometerScore = scoreBeanometer(curCard);
            playerBeanometerScore.put(curCard.getType(), playerBeanometerScore.getOrDefault(curCard.getType(), 0.0) + 3 - beanometerScore);
        }

        return playerBeanometerScore;

    }

    public void evaluateScore(){

        HashMap<Type, Double> ownedBeanScore = evaluatePlayerBeanScore(deckController.getActivePlayer());
        HashMap<Type, Double> ownedBeanometerScore = evaluatePlayerBeanometerScore(deckController.getActivePlayer());

        int opponentNumber = (deckController.getActivePlayerNumber() + 1) % 2;
        HashMap<Type, Double> opponentBeanScore = evaluatePlayerBeanScore(deckController.getPlayers()[opponentNumber]);
        HashMap<Type, Double> opponentBeanometerScore = evaluatePlayerBeanometerScore(deckController.getPlayers()[opponentNumber]);
        
        // Gives a value depending on the percentage of beans still in the deck and the other scores
        for(Entry<Type, Integer> beanCount : deckController.getBeanCount().entrySet()){
            
            double beanScore = ownedBeanScore.getOrDefault(beanCount.getKey(), 0.0);
            double beanometerScore = ownedBeanometerScore.getOrDefault(beanCount.getKey(), 0.0) * 0.25;
            double oppBeanScore = opponentBeanScore.getOrDefault(beanCount.getKey(), 0.0) * 0.5;
            double oppBeanometerScore = opponentBeanometerScore.getOrDefault(beanCount.getKey(), 0.0) * 0.5;
            double deckScore = (double)beanCount.getValue() / beanCount.getKey().getCount() * 0.7;
            double resultingScore = deckScore + beanScore + beanometerScore + oppBeanScore + oppBeanometerScore;
            beanTypeScore.put(beanCount.getKey(), resultingScore);
            // System.out.printf("Type: %s, beanScore: %.2f, beanometerScore: %.2f, oppBeanScore: %.2f, oppBeanometerScore: %.2f, deckScore: %.2f, resultingScore: %.2f\n", 
            //     beanCount.getKey().getName(), beanScore, beanometerScore, oppBeanScore, oppBeanometerScore, deckScore, resultingScore);
        }
        
    }

    public double calculateFieldScore(Type fieldType){

        // Score for having the same card type as the one in the field
        double handTypeScore = 0;
        for(int hand=0; hand < deckController.getActivePlayer().getHand().size(); hand++){
            Card curCard = deckController.getActivePlayer().getHand().get(hand);
             
            if (curCard.getType() == fieldType)
                // Score cards closer to the front of the hand with more points
                handTypeScore += (deckController.getActivePlayer().getHand().size() + 2 - hand) / deckController.getActivePlayer().getHand().size();
        }

        // Score for having the same card type in the drawing pile
        double drawingScore = (double)(deckController.getBeanCount().get(fieldType) + 3) / fieldType.getCount();
        double resultingScore = handTypeScore + drawingScore;

        // System.out.printf("Field Type: %s, handTypeScore: %.2f, drawingScore: %.2f, Final Score: %.2f\n", fieldType.getName(), handTypeScore, drawingScore, resultingScore);

        return resultingScore;

    }

    // Lazy cycle: check if 2+ offers have 2+ count and if the best offer is better than the worst field
    public boolean canCycle(){

        int count=0;
        double bestOffer=0;
        for(int offer=0; offer<3; offer++){
            Card curOffer = deckController.getOfferPile()[offer];
            if (curOffer == null)
                continue;
            if (curOffer.getCount() >= 2){
                count++;
                double curScore = beanTypeScore.get(curOffer.getType()) + 
                                    beanTypeScore.get(curOffer.getType()) * (curOffer.getCount() - 1) * 12 / curOffer.getType().getCount();
                if (curScore > bestOffer){
                    bestOffer = curScore;
                }
            }
        }

        double worstFieldScore = 100000; // High default value to be easily overriden
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            Field curField = deckController.getActivePlayer().getFields().get(field);
            if (!deckController.getActivePlayer().canHarvest(field))
                continue;
            double curFieldScore = calculateFieldScore(curField.getCard().getType());
            if (curFieldScore < worstFieldScore){
                worstFieldScore = curFieldScore;
            }
        }

        // System.out.printf("Count: %d, BestOffer: %.2f, WorstFieldScore: %.2f\n", count, bestOffer, worstFieldScore);
        return (count >= 2 && bestOffer > worstFieldScore);

    }

    // 'cycling' is when you take from offer and harvest it immediately for another offer, preventing your opponents from getting the one you harvested
    public void cycleOffer(){

        // Find the worst offer
        int worstOffer = -1;
        double worstScore = 10000;
        for(int offer=0; offer<3; offer++){
            Card curOffer = deckController.getOfferPile()[offer];
            if (curOffer == null || curOffer.getCount() < 2)
                continue;
            double curScore = beanTypeScore.get(curOffer.getType()) + 
                                beanTypeScore.get(curOffer.getType()) * (curOffer.getCount() - 1) * 12 / curOffer.getType().getCount();
            if (curOffer.getCount() >= 2 && curScore < worstScore){
                worstOffer = offer;
                worstScore = curScore;
            }
        }

        // Check if there is a low score field to replace
        // Check if there is an empty field to plant on
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            Field curField = deckController.getActivePlayer().getFields().get(field);
            if (curField.canPlant(deckController.getOfferPile()[worstOffer])){

                // System.out.printf("Planted: %s\n", deckController.getOfferPile()[worstOffer].getType().toString());

                deckController.clickButton("o" + worstOffer);
                deckController.clickButton("plant");
                deckController.clickButton("f" + field);
                
                return;

            }
        }
        
        int worstFieldIndex = -1;
        double worstFieldScore = 100000; // High default value to be easily overriden
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            Field curField = deckController.getActivePlayer().getFields().get(field);
            if (!deckController.getActivePlayer().canHarvest(field))
                continue;
            double curFieldScore = calculateFieldScore(curField.getCard().getType());
            if (curFieldScore < worstFieldScore){
                worstFieldIndex = field;
                worstFieldScore = curFieldScore;
            }
            
        }

        // System.out.printf("Harvested: %s, Planted: %s\n", deckController.getActivePlayer().getFields().get(worstFieldIndex).getCard().getType().toString(), deckController.getOfferPile()[worstOffer].getType().toString());
        deckController.clickButton("f" + worstFieldIndex);
        deckController.clickButton("harvest");
        deckController.clickButton("buy");

        deckController.clickButton("o" + worstOffer);
        deckController.clickButton("plant");
        deckController.clickButton("f" + worstFieldIndex);

    }

    public void optionalAcceptFromOffer(){

        boolean accepted;

        do {

            evaluateScore();

            accepted = false;

            // Check if you can cycle
            while (canCycle()){
                cycleOffer();
                accepted = true;
            }
            if (accepted)
                continue;

            // Search for the offer with the best score
            int bestOffer = -1;
            double bestScore = 0.5;
            for (int offer=0; offer<3; offer++){
                Card curOffer = deckController.getOfferPile()[offer];
                if (curOffer == null)
                    continue;

                // Give more score to offers with more than 1 count, but make bonus score increase less
                double curScore = beanTypeScore.get(curOffer.getType()) + 
                                beanTypeScore.get(curOffer.getType()) * (curOffer.getCount() - 1) * 12 / curOffer.getType().getCount();
                if (curScore > bestScore){
                    bestOffer = offer;
                    bestScore = curScore;
                }
            }

            if (bestScore <= 0.1 || bestOffer == -1)
                return;

            // Check if there is an empty field to plant on
            for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
                Field curField = deckController.getActivePlayer().getFields().get(field);
                if (curField.canPlant(deckController.getOfferPile()[bestOffer])){

                    deckController.clickButton("o" + bestOffer);
                    deckController.clickButton("plant");
                    deckController.clickButton("f" + field);

                    accepted = true;
                    
                    break;

                }
            }

            if (accepted)
                continue;

            // Check if there is a low score field to replace
            int worstFieldIndex = -1;
            double worstFieldScore = 100000; // High default value to be easily overriden
            for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
                Field curField = deckController.getActivePlayer().getFields().get(field);
                if (!deckController.getActivePlayer().canHarvest(field))
                    continue;
                double curFieldScore = calculateFieldScore(curField.getCard().getType());
                if (curFieldScore < worstFieldScore){
                    worstFieldIndex = field;
                    worstFieldScore = curFieldScore;
                }
                
            }
            if (bestScore > worstFieldScore){

                deckController.clickButton("f" + worstFieldIndex);
                deckController.clickButton("harvest");
                deckController.clickButton("buy");

                deckController.clickButton("o" + bestOffer);
                deckController.clickButton("plant");
                deckController.clickButton("f" + worstFieldIndex);

                accepted = true;

            }

        } while(accepted);
        
    }

    // Accept from offer
    public void phase1(){

        // Keep trying optionally harvest until it doesnt harvest anymore
        optionalHarvest();

        // Accept offers with the same type first
        for(int field=0; field<deckController.getActivePlayer().getFields().size(); field++){
            for (int offer=0; offer<3; offer++){
                Field curField = deckController.getActivePlayer().getFields().get(field);
                Card curOffer = deckController.getOfferPile()[offer];
                if (curField.getCard() == null || curOffer == null) 
                    continue;
                if (curField.getCard().getType() == curOffer.getType()){
                    deckController.clickButton("o" + offer);
                    deckController.clickButton("plant");
                    deckController.clickButton("f" + field);
                    break;
                }
            }
        }

        optionalAcceptFromOffer();

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
            if (curField.getCard() == null) 
                continue;
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
            if (curField.getCard() == null) 
                continue;
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
            if (curField.getCard() == null || !deckController.getActivePlayer().canHarvest(field)) 
                continue;
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

        if (optionalPlant()) 
            return;

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
                if (curField.getCard() == null) 
                    continue;
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

        // if (phase == 2 || phase == 3) return;
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
