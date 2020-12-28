/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman.controllers.examples;
import java.util.ArrayList;
import java.util.Arrays;
import pacman.controllers.Controller;
import pacman.controllers.examples.NeuralNet;
import pacman.game.Game;
import pacman.game.Constants.*;

/**
 *
 * @author ASUS
 */
public class NNPacman extends Controller<MOVE> {
    	NeuralNet myNet = new NeuralNet();
	ArrayList<Float> inputs = new ArrayList<Float>();
	int q = 0;
	int fitness = 0;
	int curLev = 0;
	int numPPrev = 0;
	int tmpP = 0;
        private MOVE[] allMoves=MOVE.values();
	
	public NNPacman(NeuralNet nn) {
		myNet = nn;
	}
	public MOVE getMove(Game game, long timeDue) {
		makeInputs(game);
		ArrayList<Float> outputs = myNet.update(inputs);
                myNet.fitness = game.getScore();
		if(game.getCurrentLevel() > curLev) {
                    numPPrev += tmpP;
                    curLev = game.getCurrentLevel();
		}
                tmpP = game.getScore();
		
		int highestSpot = -1;
		float highest = -1;

		//Find the highest move
		for(int i = 0; i < outputs.size(); i++) {
			if(outputs.get(i) > highest) {
				highest = outputs.get(i);
				highestSpot = i;
			}
		}
		if(highestSpot == 0) {
                    return allMoves[0];
		}
		if(highestSpot == 1) {
                    return allMoves[1];
		}
		if(highestSpot == 2) {
                    return allMoves[2];
		}
		if(highestSpot == 3) {
                    return allMoves[3];
		}
		if(highestSpot == -1) {
                    return allMoves[4];
		}
            return allMoves[4];
	} 
	
	/*void makeNet() {
		myNet.numInputs = 11;
		myNet.numOutputs = 4;
		myNet.numHiddenLay = 4;
		myNet.neuronsPerHidden = 15;
		myNet.createNet();
	}*/
	
	public void makeInputs(Game game) {
		inputs.clear();
		
		//closest ghost
                int ghost0Loc = game.getGhostCurrentNodeIndex(GHOST.SUE);
		int ghost1Loc = game.getGhostCurrentNodeIndex(GHOST.INKY);
		int ghost2Loc = game.getGhostCurrentNodeIndex(GHOST.BLINKY);
		int ghost3Loc = game.getGhostCurrentNodeIndex(GHOST.PINKY);
		GHOST closestGhost = null;
                
                int current = game.getPacmanCurrentNodeIndex();
                float dist0 = (float) game.getDistance(current, ghost0Loc, DM.MANHATTAN);
                float dist1 = (float) game.getDistance(current, ghost1Loc, DM.MANHATTAN);
                float dist2 = (float) game.getDistance(current, ghost2Loc, DM.MANHATTAN);
                float dist3 = (float) game.getDistance(current, ghost3Loc, DM.MANHATTAN);
                
                inputs.add(1/dist0);
                inputs.add(1/dist1);
                inputs.add(1/dist2);
                inputs.add(1/dist3);

		double distGhost = 0;
		if(dist0 >= dist1 && dist0 >= dist2 && dist0 >= dist3) {
			closestGhost = GHOST.SUE;
			distGhost = dist0;
		}
		if(dist1 >= dist0 && dist1 >= dist2 && dist1 >= dist3) {
			closestGhost = GHOST.INKY;
			distGhost = dist1;
		}
		if(dist2 >= dist1 && dist2 >= dist0 && dist2 >= dist3) {
			closestGhost = GHOST.BLINKY;
			distGhost = dist2;
		}
		if(dist3 >= dist1 && dist3 >= dist2 && dist3 >= dist0) {
			closestGhost = GHOST.PINKY;
			distGhost = dist3;
		}
                
                if(game.isGhostEdible(closestGhost)) {
                    inputs.add(1f);
		}
                else {
                    inputs.add(0f);
                }
                
		if(distGhost <= 0) {
			inputs.add(1f);
		}
		if(distGhost > 0) {
			inputs.add(1/(float)distGhost);
		}
		
		//closest pill
		int[] pIndex1 = game.getActivePillsIndices();
                int closestActivePill = game.getClosestNodeIndexFromNodeIndex(current, pIndex1, DM.MANHATTAN);
                float pillDist = (float) game.getDistance(current, closestActivePill, DM.MANHATTAN);
                inputs.add(1 / pillDist);
                
		//all power pill
		int[] ppIndex1 = game.getPowerPillIndices();
		for(int i = 0; i < ppIndex1.length; i++) {
                    if (game.isPowerPillStillAvailable(ppIndex1[i])) {
                        float dist = (float) game.getDistance(current, ppIndex1[i], DM.MANHATTAN);
                        inputs.add(1/dist);
                    }
                    else {
                        inputs.add(0f);
                    }
		}	
                
		//pills collected 0 = 0%, 1 = 100%
		int totalNum = game.getNumberOfPills() + game.getNumberOfPowerPills();
		int numCollec = totalNum - (game.getNumberOfActivePills() + game.getNumberOfActivePowerPills());
		float percC = ((float)numCollec/(float)totalNum);
		inputs.add(percC);
                
		//get pacman neightbors, if its a legal move, 1, otherwise 0
                MOVE[] legalMoves = game.getPossibleMoves(current);
                for (MOVE move: allMoves) {
                    if (move == allMoves[4]) {
                        continue;
                    }
                    boolean in=false;
                    for (MOVE legalMove: legalMoves) {
                        if (legalMove == move) {
                            inputs.add(1f);
                            in=true;
                            break;
                        }
                    }
                    if (!in) {
                        inputs.add(0f);
                    }
                }   

                //If ghost is edible
		if(game.isGhostEdible(GHOST.BLINKY)) {
                    inputs.add(1f);
		}
                else {
                    inputs.add(0f);
                }
                
                if(game.isGhostEdible(GHOST.SUE)) {
                    inputs.add(1f);
		}
                else {
                    inputs.add(0f);
                }
                
                if(game.isGhostEdible(GHOST.PINKY)) {
                    inputs.add(1f);
		}
                else {
                    inputs.add(0f);
                }
                
                if(game.isGhostEdible(GHOST.INKY)) {
                    inputs.add(1f);
		}
                else {
                    inputs.add(0f);
                }
                
                inputs.add((float) 1 / game.getCurrentLevel());
                inputs.add((float) 1 / game.getPacmanNumberOfLivesRemaining());
                inputs.add((float) 1 / game.getGhostEdibleTime(closestGhost));
                inputs.add((float) 1 / game.getGhostLairTime(closestGhost));
//                inputs.add((float) 1 / game.getNumberOfActivePowerPills());
//                inputs.add((float) 1 / game.getNumberOfActivePills());
	}

}
