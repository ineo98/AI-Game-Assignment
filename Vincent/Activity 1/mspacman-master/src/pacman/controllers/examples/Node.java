/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacman.controllers.examples;

/**
 *
 * @author ASUS
 */
import java.util.ArrayList;
import java.util.Random;

public class Node implements java.io.Serializable {
	int inputs;
	ArrayList<Float> weights = new ArrayList<Float>();
	
	//A step function will probably be better for this, but I kept this in just in case
	public double sigm(double d) {
		return 1 / (1 + Math.exp(-d));
	}
	
	//constructor
	public Node(int nInputs) {
		inputs = nInputs+1;
		Random rand = new Random();
		for(int i = 0; i < nInputs+1; i++) {
			weights.add(rand.nextFloat() - rand.nextFloat());
		}
	}
}
