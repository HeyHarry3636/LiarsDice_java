/**
 * Write a description of class LiarsDice here.
 * 
 * @author Michael Harris
 * @version 12/13/16
 */

import java.util.Scanner; // Scanner library/module
import java.lang.Math; // Math library/module
import java.util.Random; // Random library/module
import java.util.*;

public class LiarsDice
{
    // Scanner input
    Scanner scnr = new Scanner(System.in);
    // Random number generator
    Random randGen = new Random();
    
    private int userNumDiceRemaining = 5; //Each player starts with 5 dice
    private int compNumDiceRemaining = 5; //Each player starts with 5 dice
    private int activeDiceValue = 0; // Die face value
    private int guessNumber = 0; // Guess of the num of dice on the table that are this number
    private String[] guessNumberList = new String[] {"ZERO", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", 
                                                     "SEVEN", "EIGHT", "NINE", "TEN", "ELEVEN", "TWELVE"};
    private int totalNumDiceOnTable = 0;
    private int totalPointsOnTable = 0;
    
    private boolean legalNumber = false; // Check if input is a real dice value (1 - 6)
    private boolean legalGuessNumber = false; // Check if input is less than or equal to total num dice on table
    private boolean playerWin = false; // Check if the player won vs. the computer
    private boolean computerWin = false;  // Check if the player won vs. the computer
    private boolean compTurnOver = false; // Check if the computers turn is over or not
    private boolean userTurnOver = false; // Check if the players turn is over or not
    private boolean backForth = false; // Check if the back and forth guessing between the player and computer is over
    private boolean exitCompLoop = false;  // Check to see if you need to exit this portion of the computers logic method loop
    
    private ArrayList<Integer> userDiceValues = new ArrayList<>();
    private ArrayList<Integer> compDiceValues = new ArrayList<>();
    
    
    public void playGame() {
        // Roll the first round for Liars Dice
        while ( compNumDiceRemaining > 0 && userNumDiceRemaining > 0 ) {
            
            // Empty the ArrayList's
            userDiceValues.clear();
            compDiceValues.clear();
            // Re-instantiate the appropriate boolean condition for these vars
            legalNumber = false; 
            legalGuessNumber = false; 
            playerWin = false;
            computerWin = false;
            compTurnOver = false;
            userTurnOver = false;
            backForth = false;
            exitCompLoop = false;
            
            // Run methods
            rollDice();
            selectPointValue();
            
            // This loop controls the back and forth nature of the computer vs player guessing
            while ( backForth == false ) {
                // run the player and computer logic loops and recalculate point totals after face changes on the dice etc...
                calcTotalPointsOnTable();
                computerLogic();
                calcTotalPointsOnTable();
                userTurn();
            }
        }
        
        // Print winning condition
        System.out.println("");
        if (userNumDiceRemaining > compNumDiceRemaining) {
            System.out.println("Player Wins!!");
        } else {
            System.out.println("Davy Jones Wins!");
        }
        System.out.println("GAME OVER");
        return;
    }
    
    public void rollDice() {
        // Populate empty ArrayList based on the number of available dice
        for (int i = 0; i < userNumDiceRemaining; ++i) {
            userDiceValues.add( randGen.nextInt(6) + 1 );
        }
        // Sort the ArrayList
        Collections.sort(userDiceValues);
        System.out.print("Your dice are... ");
        
        // Display the users dice
        for (int i = 0; i < userNumDiceRemaining; ++i) {
            System.out.print( userDiceValues.get(i) + " " );
        }
        System.out.println("");
        System.out.print("The computer has  ");
        
        // Add the dice values to the ArrayList based on the number of available dice
        for (int i = 0; i < compNumDiceRemaining; ++i) {
            compDiceValues.add( randGen.nextInt(6) + 1 );
            //System.out.print( compDiceValues.get(i) + " ");
        }
        System.out.println( guessNumberList[compDiceValues.size()] + "  dice remaining.");
        System.out.println("");
        
        // Get the total number of availale dice currently on the table
        totalNumDiceOnTable = userDiceValues.size() + compDiceValues.size();
    }
    
    public void selectPointValue() {
        // Let player select the active dice value
        while (legalNumber == false) {
            System.out.print("Which dice value would you like to select? ");
            activeDiceValue = scnr.nextInt();
            // Make sure the dice values are between 1 and 6
            if (activeDiceValue >= 1 && activeDiceValue <= 6) {
                legalNumber = true;
            } else {
                System.out.println("");
                System.out.println("!! Please enter a valid die value. !!");
            }
        }
        
        // Determine number of dice that you would like to guess
        while (legalGuessNumber == false) {
            // Get the number of dice that the player thinks remain on the table
            System.out.print("How many " + activeDiceValue + "'s would you like to guess? ");
            guessNumber = scnr.nextInt();
            // Make sure guess number is higher than the previous guess but lower than the total number of dice on the table
            if (guessNumber >= 1 && guessNumber <= totalNumDiceOnTable) {
                legalGuessNumber = true;
            } else {
                System.out.println("");
                System.out.println("!! Please enter a number between ONE and the total number of dice on the table(" + totalNumDiceOnTable + "). !!");
            }
        }
        
        System.out.println("Player thinks that there are " + guessNumberList[guessNumber] + " " + activeDiceValue + "'s on the table, out of " + 
                           guessNumberList[totalNumDiceOnTable] + " total dice.");
        System.out.println("");
        // Reset appropriate boolean variables
        legalNumber = false;
        legalGuessNumber = false;
    }
    
    public void calcTotalPointsOnTable() {
        // Count the total number of dice that actually remain on the table, including ones.
        // Ones are wild
        totalPointsOnTable = 0; // clear points on table back to zero
        for (int i = 0; i < userNumDiceRemaining; ++i) {
            // Count the number of dice that are active, plus one's since they are wild
            if ( userDiceValues.get(i) == activeDiceValue || userDiceValues.get(i) == 1) {
                totalPointsOnTable = totalPointsOnTable + 1;
            }
        }
        
        for (int i = 0; i < compNumDiceRemaining; ++i) {
            // Count the number of dice that are active, plus one's since they are wild
            if ( compDiceValues.get(i) == activeDiceValue || compDiceValues.get(i) == 1) {
                totalPointsOnTable = totalPointsOnTable + 1;
            }
        }
    }
    
    public void computerLogic() {
        // Reset userTurn() method variables
        userTurnOver = true;
        legalNumber = true;
        legalGuessNumber = true;
        
        // Proportion variables for deciding which path to take.
        final double PLAYER_LYING = 0.6;
        final double COMP_NUM_DICE_SAME_AS_ACTIVE = 0.2;
        
        while( compTurnOver == false ) {
            System.out.println("Davy Jones, the computer, is thinking... ");
            // Sleep for X seconds to simulate the computer thinking
            try {
                Thread.sleep(1000); 
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            
            // Computer thinks that the player is lying
            // If players guessNumber is very high, currently if guess is greater than 60% of total remaining dice on table
            if ( guessNumber >= (totalNumDiceOnTable * PLAYER_LYING) ) {
                System.out.println("Davy Jones thinks that you are lying.");
                // If player or comp won, update player/comp score
                if ( guessNumber > totalNumDiceOnTable ) {
                    playerWin = true;
                    System.out.println("Player wins this round");
                    // Call the method to update the number of dice for player and computer
                    compNumberOfDiceControl(compNumDiceRemaining);
                } else {
                    System.out.println("\"Bootstrap Bill, you are a liar and will spend an eternity on this ship!\"");
                    computerWin = true;
                    System.out.println("Computer wins this round");
                    // Call the method to update the number of dice for player and computer
                    userNumberOfDiceControl(userNumDiceRemaining);
                }
                // Reset appropriate boolean vars
                compTurnOver = true;
                exitCompLoop = true;
                userTurnOver = true;
                backForth = true;
                System.out.println("");
            }
            
            // Keep the activeDiceValue the same
            while ( exitCompLoop == false ) {
                int numCompDieSameAsActive = 0;
                // iterate through computers dice to see how many dice are the same as the current guessNumber made by the player
                for (int i = 0; i < compNumDiceRemaining; ++i) {
                    if ( compDiceValues.get(i) == activeDiceValue ) {
                        numCompDieSameAsActive = numCompDieSameAsActive + 1;
                    }
                    else {
                        numCompDieSameAsActive = 1;
                    }
                }
                
                // Change the activeDiceValue to a die value in the computers favor
                // Use mode2 function which finds the mode of the dice set
                // Convert from IntegerSet to a regular array because I don't know how to use Sets.....
                Integer[] compDiceMode = mode2(compDiceValues).toArray(new Integer[0]);
                
                // If there isn't a definitive mode in for the computers dice; don't change the activeDiceValue
                for (int i = 0; i < compDiceMode.length; ++i) {
                    if (compDiceMode.length == 0) {
                        break;
                    } else {
                        activeDiceValue = compDiceMode[ randGen.nextInt( compDiceMode.length ) ];
                    }
                }
                
                /* If computer has a proportion of dice same as the active (Ex: active 4; if comp has two 4's out of FIVE dice
                 * remaining, that proportion is 0.4).  If this proportion is greater than the final COMP_NUM_ var
                 * then the computer will not change the value of the dice face and will guess on the same active number
                 */
                if (( ((double) numCompDieSameAsActive) / compNumDiceRemaining ) >= COMP_NUM_DICE_SAME_AS_ACTIVE) {
                     System.out.print("Davy Jones guesses... ");
                     if ( (numCompDieSameAsActive % 2) == 0) {
                         guessNumber = guessNumber + (numCompDieSameAsActive / 2);
                     } else {
                         guessNumber = guessNumber + (numCompDieSameAsActive / 2) + 1;
                     }
                }
                System.out.println(guessNumberList[guessNumber] + " " + activeDiceValue + "'s.");
                
                // Reset appropriate boolean vars
                userTurnOver = false;
                legalNumber = false;
                legalGuessNumber = false;
                exitCompLoop = true;
                compTurnOver = true;
                backForth = false;
            }
            System.out.println("");
            
            // Sleep for X seconds to simulate the computer thinking
            try {
                Thread.sleep(1000); 
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            return;
        }
    }
    
    public void userTurn() {
        // Control for the userTurn method
        while ( userTurnOver == false ) {
            while (legalNumber == false) {
                System.out.print("Which dice value would you like to select? (enter zero (0) if you think the computer is lying) ");
                int userEntry = scnr.nextInt();  // copy this var in case the user enters zero, we don't want that to be stored as the activeDiceValue
                if (userEntry == 0) {
                    System.out.println("Player thinks that the computer is lying");
                    // update scores and number of dice remaining if the player thinks that the computer is lying
                    if ( guessNumber > totalPointsOnTable ) {
                        playerWin = true;
                        System.out.println("Player wins this round");
                        // Call this method to update the number of dice for the player and comp
                        compNumberOfDiceControl(compNumDiceRemaining);
                    } else {
                        computerWin = true;
                        System.out.println("Computer wins this round");
                        // Call this method to update the number of dice for the player and comp
                        userNumberOfDiceControl(userNumDiceRemaining);
                    }
                    // Reset boolean chars
                    userTurnOver = true;
                    legalNumber = true;
                    legalGuessNumber = true;
                    compTurnOver = true;
                    backForth = true;
                    System.out.println("");
                } else if (userEntry >= 1 && userEntry <= 6) {
                    activeDiceValue = userEntry;
                    legalNumber = true;
                } else {
                    System.out.println("");
                    System.out.println("!! Please enter a valid die value. !!");
                }
            }
            
            while (legalGuessNumber == false) {
                // Get the number of dice that the player thinks remain on the table
                System.out.print("How many " + activeDiceValue + "'s would you like to guess? ");
                int updatedGuessNumber = scnr.nextInt();
                // Make sure guess number is higher than the last one but lower than the number of remaining dice
                if (updatedGuessNumber > guessNumber && updatedGuessNumber <= totalNumDiceOnTable) {
                    guessNumber = updatedGuessNumber;
                    legalGuessNumber = true;
                    
                    System.out.println("Player thinks that there are " + guessNumberList[guessNumber] + " " + activeDiceValue + 
                                       "'s on the table, out of " + guessNumberList[totalNumDiceOnTable] + " total dice.");
                    System.out.println("");
                } else {
                    System.out.println("");
                    System.out.println("!! Please enter a number greater than the last guess by the computer !!");
                }
            }
            // Reset appropriate boolean vars
            exitCompLoop = false;
            compTurnOver = false;
            return;
        }
    }
    
    // update the number of dice remaining for the computer
    public int compNumberOfDiceControl(int compNumDiceRemaining) {
        if ( playerWin == true ) {
            this.compNumDiceRemaining = compNumDiceRemaining - 1;
            playerWin = false;
        }
        return this.compNumDiceRemaining;
    }
    
    // update the number of dice remaining for the player
    public int userNumberOfDiceControl(int userNumDiceRemaining) {
        if ( computerWin == true ) {
            this.userNumDiceRemaining = userNumDiceRemaining - 1;
            computerWin = false;
        }
        return this.userNumDiceRemaining;
    }
    
    /*
     * Find mode of the computers dice so that it can make a logical decision on which dice to choose for its guess
     * 
     * This code was copied verbatim from this URL below on 12/14/16
     * http://stackoverflow.com/questions/36416048/java-program-to-find-mode-in-an-array-list
     * 
     * returns a integer set of the most common dice that the computer has
     */
    public static Set<Integer> mode2(List<Integer> list) {
        int maxFrequency = 0;
        boolean modeFound = false;
        Set<Integer> modeSet = new HashSet<>();
        Collections.sort(list);
        for (int i=0; i<list.size(); i++) {
            int number = list.get(i);
            int count = 1;
            for (; (i+count)<list.size() && list.get(i+count)==number; count++) {}
                i+=(count-1);
            if (maxFrequency!=0 && count!=maxFrequency) {
                modeFound = true;
            }
            if (count > maxFrequency) {
                modeSet.clear();
                modeSet.add (number);
                maxFrequency = count;
            }
            else if (count == maxFrequency) {
                modeSet.add(number);
            }
        }
        if (!modeFound) {
            modeSet.clear();
        }
        return modeSet;
    }
    
}