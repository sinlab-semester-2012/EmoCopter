package learning;

import java.io.File;
import java.io.FilenameFilter;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.awt.MediaTracker;
import java.awt.Container;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Teacher
 * learn perceptron to recognize numbers
 */
public class Teacher {

    private Perceptron perceptron;    

    /**
     * Constructor
     * @param perceptron
     */
    public Teacher(Perceptron perceptron)
    {
        this.perceptron = perceptron;        
    }

    /**
     * Learning of perceptron
     * @param path
     * @param n - # of learning cycles
     */
    public void teach(String path, int n)
    {
        
        class JPGFilter implements FilenameFilter {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg"));
            }
        }       

        // load all test images inside img[]
        String[] list = new File(path + "/").list(new JPGFilter());
        Image[] img = new Image[list.length];        
        MediaTracker mediaTracker = new MediaTracker(new Container());        
        int i = 0;
        for (String s: list) {
            img[i] = java.awt.Toolkit.getDefaultToolkit().createImage(path + "/" + s);

            mediaTracker.addImage(img[i], 0);
            try {
                mediaTracker.waitForAll();
            } catch (InterruptedException ex) {
                Logger.getLogger(Teacher.class.getName()).log(Level.SEVERE, null, ex);
            }

            i++;
        }

        // Weights initializing
        perceptron.initWeights();

        // Getting the pixel arrays of each image
        // and learning n times on each test case
        PixelGrabber pg;
        int[] pixels, x, y;
        int w, h, k = 0;
        while (n-- > 0) {
            for (int j = 0; j < img.length; j++) {
                w = img[j].getWidth(null);
                h = img[j].getHeight(null);

                if (w*h > perceptron.getM()) continue;

                pixels = new int[w*h];
                pg = new PixelGrabber(img[j], 0, 0, w, h, pixels, 0, w);
                try {
                    pg.grabPixels();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Teacher.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Getting vectors and teaching the perceptron
                x = getInVector(pixels);
                y = getOutVector(Integer.parseInt(String.valueOf(list[j].charAt(0))));
                perceptron.teach(x, y);
            }
        }
    }

    /**
     * Transformation des image pixels en vector de 0 et 1
     * 1 there, where we have color, 0 - there, where we ave white
     * @param p - image pixels
     * @return - perceptron enter vector
     */
    private int[] getInVector(int[] p)
    {
        int[] x = new int[p.length];
        for (int i = 0; i < p.length; i++) {
            if (p[i] == -1) x[i] = 0; else x[i] = 1;
        }
        return x;
    }

    /**
     * Right output vector generation
     * @param n - number, for wich we have to construct the vector, in other words:
     * in which place should be 1, others 0
     * @return - perceptron output vector
     */
    private int[] getOutVector(int n)
    {
        int[] y = new int[perceptron.getN()];
        for (int i = 0; i < y.length; i++) {
            if (i == n) y[i] = 1; else y[i] = 0;
        }
        return y;
    }
}
