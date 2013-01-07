package neural_network;

import java.util.Random;

/**
 * Neuron with step function
 */
public class Neuron
{
    private int[] w;        // synapses weights    
    private int s = 50;     // separating value

    /**
     * constructor
     * @param m - # of input (input vector dimension)
     */
    Neuron(int m)
    {
        w = new int[m];
    }

    /**
     * transfer function
     * @param x - input vector
     * @return - neuron's output value
     */
    public int transfer(int[] x)
    {
        return activator(adder(x));
    }

    /**
     * Sinapse's weights initializing with random value
     * @param n - от 0 до n
     */
    public void initWeights(int n)
    {
        Random rand = new Random();
        for (int i = 0; i < w.length; i++) {
            w[i] = rand.nextInt(n);
        }
    }

    /**
     * Weights modification for learning
     * @param v - learning speed
     * @param d - difference between output vector and RIGHT output vector
     * @param x - input vetor
     */
    public void changeWeights(int v, int d, int[] x)
    {
        for (int i = 0; i < w.length; i++) {
            w[i] += v*d*x[i];
        }
    }

    /**
     * Adder
     * @param x - input vector
     * @return - Unweighted sum nec (no bias)
     */
    private int adder(int[] x)
    {
        int nec = 0;
        for (int i = 0; i < x.length; i++) {
            nec += x[i] * w[i];
        }
        return nec;
    }

    /**
     * Activation function
     * In this case - step function
     * Definition domain {0;1}
     * @param nec - adder output
     * @return
     */
    private int activator(int nec)
    {
        if (nec >= s) return 1;
        else return 0;
    }
}
