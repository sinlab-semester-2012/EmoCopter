package neural_network;

/**
 * one-layer n-neuron (dimensioned) perceptron
 */
public class Perceptron {

    Neuron[] neurons; // neurons layer
    int n, m;

    /**
     * Constructor
     * @param n - # of neurons
     * @param m - # of input of each neuron of cached layer
     */
    public Perceptron(int n, int m)
    {
        this.n = n;
        this.m = m;
        neurons = new Neuron[n];
        for (int j = 0; j < n; j++) {
            neurons[j] = new Neuron(m);
        }        
    }

    /**
     * Image recognition
     * @param x - enter vector
     * @return - out vector
     */
    public int[] recognize(int[] x)
    {
        int[] y = new int[neurons.length];
        
        for (int j = 0; j < neurons.length; j++) {
            y[j] = neurons[j].transfer(x);
        }
        
        return y;
    }

    /**
     * Weights initializing
     * with random value
     */
    public void initWeights() {        
        for (int j = 0; j < neurons.length; j++) {
            neurons[j].initWeights(10);
        }
    }

    /**
     * Learning perceptron
     * @param x - enter vector
     * @param y - good out vector
     */
    public void teach(int[] x, int[] y)
    {
        int d;
        int v = 1; // learning speed

        int[] t = recognize(x);
        while (!equal(t, y)) {

            // changing weights of each neuron
            for (int j = 0; j < neurons.length; j++) {
                d = y[j] - t[j];
                neurons[j].changeWeights(v, d, x);
            }
            t = recognize(x);
        }
    }

    /**
     * comparison betwen two vectors
     * @param a - first вектор
     * @param b - second вектор
     * @return boolean
     */
    private boolean equal(int[] a, int[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    public int getN() {
        return n;
    }

    public int getM() {
        return m;
    }

}
