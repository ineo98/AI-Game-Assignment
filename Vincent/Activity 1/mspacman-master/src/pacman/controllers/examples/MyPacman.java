/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman.controllers.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import pacman.controllers.Controller;
import pacman.game.Game;
import static pacman.game.Constants.*;

/**
 *
 * @author ASUS
 */
public class MyPacman extends Controller<MOVE> {
        Random random = new Random();
        private MOVE[] allMoves=MOVE.values();
	public MOVE getMove(Game game,long timeDue)
	{
            int[] pills = game.getActivePillsIndices();
            int[] powerPills = game.getActivePowerPillsIndices();
            int[] targetPills = new int[pills.length];
            int[] targetPowerPills = new int[powerPills.length];
            
            for(int i=0;i<pills.length;i++) {
                targetPills[i] = pills[i];
            }
            for(int i=0;i<powerPills.length;i++) {
                targetPowerPills[i] = powerPills[i];
            }
            
            int current=game.getPacmanCurrentNodeIndex();

            MOVE[] possibleMoves = game.getPossibleMoves(current);
            List<MOVE> possibleMoves2 = new ArrayList<MOVE>();
            for(GHOST ghost : GHOST.values()) {
                if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0) {
                    if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<50) {
                        if (game.getShortestPathDistance(current, game.getClosestNodeIndexFromNodeIndex(current, targetPowerPills, DM.PATH)) < 20) {
                            return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getClosestNodeIndexFromNodeIndex(current,targetPowerPills,DM.PATH),DM.PATH);
                        }
                        else {
                            possibleMoves2.add(game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost),DM.PATH));
                        }
                    }
                }
            }
            
            for (MOVE m: possibleMoves2) {
                for (MOVE m2: possibleMoves) {
                    if (m.compareTo(m2) == 0) {
                        return m;
                    }
                }
            }

            int minDistance=Integer.MAX_VALUE;
            GHOST minGhost=null;		

            for(GHOST ghost : GHOST.values()) {
                if(game.getGhostEdibleTime(ghost)>50)
                    {
                        int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));

                        if(distance<minDistance & game.isGhostEdible(ghost))
                        {
                                minDistance=distance;
                                minGhost=ghost;
                        }
                    }
            }
                    

            if(minGhost!=null) {
                return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(minGhost),DM.PATH);
            }
            
            return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getClosestNodeIndexFromNodeIndex(current,targetPills,DM.PATH),DM.PATH);
	}
    
}
