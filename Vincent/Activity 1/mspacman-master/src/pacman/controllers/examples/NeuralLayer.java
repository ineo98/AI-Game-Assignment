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
import pacman.controllers.examples.Node;

public class NeuralLayer implements java.io.Serializable {
	int numNeurons;
	ArrayList<Node> nodeList = new ArrayList<Node>();
	
	//constructor
	public NeuralLayer(int numN, int inputsPerNeuron) {
		numNeurons = numN;
		for(int i = 0; i < numNeurons; ++i) {
			nodeList.add(new Node(inputsPerNeuron));
		}
	}
}
