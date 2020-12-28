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
import pacman.controllers.examples.NeuralNet;
import pacman.controllers.examples.GraphingData;

import pacman.Executor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import pacman.controllers.Controller;
import pacman.game.Constants;
import static pacman.game.Constants.DELAY;
import pacman.game.Game;

public class GenAlg {
	static ArrayList<NeuralNet> nets = new ArrayList<NeuralNet>();
	static ArrayList<Float> weights = new ArrayList<Float>();
        static ArrayList<Integer> fitnesses = new ArrayList<Integer>();
        static NeuralNet bestNN;
        static int bestNNFitness = Integer.MIN_VALUE;
        
        static int numInputs = 24;
        static int numOutputs = 4;
        static int numHiddenLay = 1;
        static int neuronsPerHidden = 8;
        
	public GenAlg(){
	}
	static int curGen = 0;
	static boolean stop = false;
	
	static ArrayList<JPanel> frameList = new ArrayList<JPanel>();
	
        static double runExp(Controller<Constants.MOVE> pacManController,Controller<EnumMap<Constants.GHOST,Constants.MOVE>> ghostController) {

            double score=0;
            Random rnd=new Random(0);
            Game game;


                game=new Game(rnd.nextLong());
                while(!game.gameOver())
                {
                game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
                                ghostController.getMove(game.copy(),System.currentTimeMillis()+DELAY));
                }
                score+=game.getScore();
            return score;
        }
        
        public static void serializeDataOut(NeuralNet nn) {
            String fileName= "bestNN.txt";
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(fileName);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(nn);
                oos.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GenAlg.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GenAlg.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public static NeuralNet serializeDataIn() {
           String fileName= "bestNN.txt";
           FileInputStream fin;
           ObjectInputStream ois;
           NeuralNet iHandler = null;
            try {
                fin = new FileInputStream(fileName);
                ois = new ObjectInputStream(fin);
                iHandler = (NeuralNet) ois.readObject();
                ois.close();
            } catch (IOException ex) {
                Logger.getLogger(GenAlg.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(GenAlg.class.getName()).log(Level.SEVERE, null, ex);
            }
           return iHandler;
        }

        
	static void doGeneration(int numGen, boolean doGen, final JFrame frameF, final JTextField txtField) {
		final int secNum = (curGen + numGen);
		for(int i = 0; i < numGen; i++) {
			ExecutorService es = Executors.newCachedThreadPool();
			fitnesses.clear();
			int highest = 0;
			//Find the fitnesses of the neural nets
			for(int n = 0; n<nets.size(); n++) {
				if(stop) {
					return;
				}
				class MyRunnable implements Runnable {
					NeuralNet ne;
					int q;
				    public MyRunnable(NeuralNet p, int l) {
				    	ne = p;
				    	q = l;
				    }
				    public void run() {
                                        runExp(new NNPacman(ne), new Legacy2TheReckoning());
				    }
				}
				stop = false;
				Thread t = new Thread(new MyRunnable(nets.get(n), n));
				es.execute(t);
			}
			//Wait for all the threads to finish, max time 3 minutes
			es.shutdown();
			try {
				es.awaitTermination(3, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//find the highest fitness neural net
			for(int k = 0; k < nets.size(); k++) {
                            int fitness = nets.get(k).fitness;
                            fitnesses.add(fitness);
                            if(fitness > highest) {
                                    highest = nets.get(k).fitness;
                                    highestSpot = k;
                            }
			}
                        
                        if (highest > bestNNFitness) {
                            bestNN = nets.get(highestSpot);
                            bestNNFitness = highest;
                        }
                        
			//find the average fitness
			int avg = 0;
			for(int n = 0; n<fitnesses.size(); n++) {
				avg += fitnesses.get(n);
			}
			average = 0;
			average = avg/fitnesses.size();
                        			//Copy the fitness array to use in the GUI
			final ArrayList<Integer> tmpFitness = new ArrayList<Integer>();
			for(int p = 0; p < fitnesses.size(); p++) {
				tmpFitness.add(fitnesses.get(p));
			}
			//update the GUI
			SwingUtilities.invokeLater(new Runnable() {
		         public void run() {
		        	 JPanel graph = new GraphingData(tmpFitness, curGen);
		        	 frameList.add(graph);
		        	 graph.setVisible(true);
		        	 graph.setBounds(300, 300, 400, 400);
		        	 frameF.add(graph);
		        	 txtField.setText("   Generation " + curGen + "/" + secNum);
		        	 frameF.repaint();
		        	 frameF.revalidate();
		        	 tmpFitness.clear();
		        	if(frameList.size() > 1) {
		         		JPanel g = frameList.get(0);
		         		g.setVisible(false);
		         		frameList.remove(0);
		         	}
		         }
			});

			curGen++;
			//Run the genetic algorithm
			if(doGen & i!=numGen-1) {
                            doGenetics(highestSpot);
			}
			System.out.println("Average Fitness = " + avg/fitnesses.size());
			System.out.println("Highest Fitness = " + highest);	
		}
                ArrayList<Float> bestWeight = bestNN.getWeights();
                System.out.println("Model Saved. Score :" + bestNNFitness);
                serializeDataOut(bestNN);
                double bestScore = 0;
                for (int i = 0; i < 20; i++) {
                    bestScore += runExp(new NNPacman(bestNN), new Legacy2TheReckoning());
                }
                 
                System.out.println("Best eval score : " + bestScore/20);

	}
	
	static void doGenetics(int hightSpot) {
		ArrayList<NeuralNet> newNets = new ArrayList<NeuralNet>();
		boolean doOnce = true;
		int totalFitness = 0;
		for(int j : fitnesses){
			totalFitness += j;
		}
		Random rand = new Random();
		//Breed the neural nets
		for(int i = 0; i < nets.size()/2; i++) {
			NeuralNet tmpNet1 = new NeuralNet();
			tmpNet1.numInputs = numInputs;
			tmpNet1.numOutputs = numOutputs;
			tmpNet1.numHiddenLay = numHiddenLay;
			tmpNet1.neuronsPerHidden = neuronsPerHidden;
			tmpNet1.createNet();
			NeuralNet tmpNet2= new NeuralNet();
			tmpNet2.numInputs = numInputs;
			tmpNet2.numOutputs = numOutputs;
			tmpNet2.numHiddenLay = numHiddenLay;
			tmpNet2.neuronsPerHidden = neuronsPerHidden;
			tmpNet2.createNet();
			int roulette = rand.nextInt(totalFitness-1)+1;
			int spot1 = -1;
			int spot2 = -1;
			int tmpX = 0;
			//Roulette selection, get the first net
			for(int k = 0; k < fitnesses.size(); k++) {
				if(roulette <= fitnesses.get(k)+tmpX && roulette >= tmpX) {
					spot1 = k;
				}
				tmpX += fitnesses.get(k);
			}
			tmpX = 0;
			//Roulette selection, get the second net
			roulette = rand.nextInt(totalFitness-1)+1;
			for(int k = 0; k < fitnesses.size(); k++) {
				if(roulette <= fitnesses.get(k)+tmpX && roulette >= tmpX) {
					spot2 = k;
				}
				tmpX += fitnesses.get(k);
			}
			ArrayList<Float> weights1 = nets.get(spot1).getWeights();
			ArrayList<Float> weights2 = nets.get(spot2).getWeights();
			if(rand.nextInt(10) > 3) {
				//Uniform Crossover to make the children
				for(int l = 0; l < weights1.size(); l++) {
					float corss = rand.nextFloat();
					int mutateR = rand.nextInt(5000);
					if(corss >= 0.5) {
						weights1.set(l, weights2.get(l));
					}
					if(corss < 0.5) {
						weights2.set(l, weights1.get(l));
					}
					if(mutateR <= 10) {
						float r = rand.nextFloat() - rand.nextFloat();
						weights1.set(l, r);
						//r = rand.nextFloat() - rand.nextFloat();
						//weights2.set(l, r);
					}
				}
			}
			//If one of those selected is the highest fitness of last generation, let it live
			if(spot1 == hightSpot && doOnce) {
				weights1.clear();
				weights1.addAll(nets.get(spot1).getWeights());
				doOnce = false;
			}
			if(spot2 == hightSpot && doOnce) {
				weights2.clear();
				weights2.addAll(nets.get(spot2).getWeights());
				doOnce = false;
			}
			
			tmpNet1.putWeights(weights1);
			tmpNet2.putWeights(weights2);
			tmpNet1.fitness = 0;
			tmpNet2.fitness = 0;
			newNets.add(tmpNet1);
			newNets.add(tmpNet2);
		}
		nets.clear();
		nets.addAll(newNets);
	}
	
	static int highestSpot = -1;
	static int average = 0;
	
	public static void main(String[] args) {
		final JFrame f = new JFrame();
		f.setSize(800,800);
                f.setLocation(200,200);
                f.setTitle("Mrs. Pacman Evolving Neural Networks");
		JButton firstGenB = new JButton("Make 100 Nets");
		final JTextField numNets = new JTextField();
		numNets.setText("   # Of Nets = 0");
		numNets.setEditable(false);
		numNets.setBounds(200, 10, 175, 50);
		numNets.setVisible(true);
		f.add(numNets);
		
		final JTextField generationNum = new JTextField();
		generationNum.setText("   Generation 0/0");
		generationNum.setEditable(false);
		generationNum.setBounds(200, 70, 175, 50);
		generationNum.setVisible(true);
		f.add(generationNum);
		
		firstGenB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for(int i = 0; i < 100; i++) {
					NeuralNet tmpNet = new NeuralNet();
					tmpNet.numInputs = numInputs;
					tmpNet.numOutputs = numOutputs;
					tmpNet.numHiddenLay = numHiddenLay;
					tmpNet.neuronsPerHidden = neuronsPerHidden;
					tmpNet.createNet();
					weights.addAll(tmpNet.getWeights());
					nets.add(tmpNet);
				}
				SwingUtilities.invokeLater(new Runnable() {
			         public void run() {
			        	 numNets.setText("   # Of Nets = " + nets.size());
			        	 f.repaint();
			        	 f.revalidate();
			         }
				});
			}
		});
		firstGenB.setBounds(10, 10, 175, 50);
		f.setLayout(null);
		f.add(firstGenB);
		JButton oneGen = new JButton("1 Generation");
		oneGen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				class MyRunnable implements Runnable {
				    public MyRunnable() {
				    }
				    public void run() {
				    	doGeneration(1, true, f, generationNum);
				    }
				}
				stop = false;
				Thread t = new Thread(new MyRunnable());
				t.start();

				f.repaint();
				f.revalidate();
			}
		});
		oneGen.setBounds(10, 70, 175, 50);
		f.add(oneGen);
		
		JButton tenGen = new JButton("10 Generations");
		tenGen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				class MyRunnable implements Runnable {
				    public MyRunnable() {
				    }
				    public void run() {
				    	doGeneration(10, true, f, generationNum);
				    }
				}
				stop = false;
				Thread t = new Thread(new MyRunnable());
				t.start();
				f.repaint();
				f.revalidate();
			}
		});
		tenGen.setBounds(10, 140, 175, 50);
		f.add(tenGen);
		
		JButton oneHGen = new JButton("100 Generations");
		oneHGen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				class MyRunnable implements Runnable {
				    public MyRunnable() {
				    }
				    public void run() {
				    	doGeneration(100, true, f, generationNum);
				    }
				}
				stop = false;
				Thread t = new Thread(new MyRunnable());
				t.start();
				f.repaint();
				f.revalidate();
			}
		});
		oneHGen.setBounds(10, 210, 175, 50);
		f.add(oneHGen);
		
		JButton oneTGen = new JButton("1000 Generations");
		oneTGen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				class MyRunnable implements Runnable {
				    public MyRunnable() {
				    }
				    public void run() {
				    	doGeneration(1000, true, f, generationNum);
				    }
				}
				stop = false;
				Thread t = new Thread(new MyRunnable());
				t.start();
				f.repaint();
				f.revalidate();
			}
		});
		oneTGen.setBounds(10, 280, 175, 50);
		f.add(oneTGen);
		
		JButton stopGen = new JButton("Stop Evolution");
		stopGen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				stop = true;
			}
		});
		stopGen.setBounds(10, 350, 175, 50);
		f.add(stopGen);
		
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
	}
	
}
