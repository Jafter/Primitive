package Functional;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.time.Duration;
import java.time.Instant;

/**
 * Primitive.java is a simple AI created to simulate a primitive living organism that has a symbiotic relationship with
 * its point of origin, or "Node". This Node serves as a nutrient storage device for the disease, allowing it to store
 * collected resources. Nodes are placed onto the map at random, and the map is generated randomly through the use of
 * "bgNoise()" in Terrain.java. Rain and environment changes occur after every X seconds giving the allure of a
 * changing environment.
 @author Jafter
 @author Notch --> Intro Screen and pixel map using a 2d array and decimal colors
 */

public class Primitive extends Applet implements Runnable {

    private boolean[] k = new boolean[32767];
    private int m;
    private Instant start;
    private Instant finish;
    private Duration timeElapsed;

    /** START
     * Initialize key inputs and Events
     */
    public void start() {
        enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        new Thread(this).start();
    }

    /** RUN
     * Runs code in a loop until finished
     */
    public void run() {
        BufferedImage fov = new BufferedImage(240, 240, BufferedImage.TYPE_INT_RGB);
        Graphics gr = fov.getGraphics();
        Terrain terra = new Terrain();
        Graphics sg = getGraphics();
        Disease adam = new Disease();
        Disease eve = new Disease();
        int[] pixels = ((DataBufferInt) fov.getRaster().getDataBuffer()).getData();
        boolean simulationStarted = false;

        while (true) {

            if (simulationStarted) {

                // Time Differential, currently used for "rain" --> see Terrain @func generateLakes
                finish = Instant.now();
                timeElapsed = Duration.between(start, finish);

                // Generate Terrain
                terra.mainBoard();

                // Rain every 4 seconds
                if(timeElapsed.getSeconds() % 4 == 0){
                    terra.generateLakes();
                }

                // Plants grow back every 6 seconds, removed for now
                //if(timeElapsed.getSeconds() % 6 == 0){
                    //terra.generateGrowth();
                //}

                // Permute Disease, biblical reference for comedic relief
                adam.infect(terra.getMap());
                eve.infect(terra.getMap());

                // Show Node Details
                int[] aNode = adam.getNodeDetails();
                int[] eNode = eve.getNodeDetails();

                // Set Pixel Mapping
                pixels = terra.terrainToPixels(pixels);

                // Draw Node Info
                gr.drawString(String.valueOf(aNode[2]), aNode[1] - 2, aNode[0] - 2);
                gr.drawString(String.valueOf(eNode[2]), eNode[1] - 2, eNode[0] - 2);

                gr.drawString(String.valueOf(", " + adam.getCarryingCapacity() ), 120, 10);
                //gr.drawString(String.valueOf(adam.getPositionX() + ", " + adam.getPositionY() ), 120, 20);

                // Restart on click
                if(k[1]){
                    simulationStarted = false;
                }

                // Stop simulation after 60 seconds, currently using click events to restart simulation on mouse click
                //if(timeElapsed.getSeconds() == 60){
                //    simulationStarted = false;
                //}
            }


            if (!simulationStarted) {

                // Intro animation & partial terrain mapping
                terra.bgNoise();

                // Set Pixel Mapping
                pixels = terra.terrainToPixels(pixels);

                gr.drawString("Primitive", 95, 100);

                // On mouse click, simulation starts
                if (k[1]) {
                    // Set text fonts for all text in simulation
                    Font nodeFont = new Font ("Courier New", Font.BOLD, 8);
                    gr.setFont (nodeFont);

                    // Populate map for each disease, and inject seed (create node for each disease, see Disease.java)
                    adam = new Disease(terra.getMap());
                    terra.setMap(adam.injectSeed());
                    eve = new Disease(terra.getMap());
                    terra.setMap(eve.injectSeed());

                    // Set timer for recurring events
                    start = Instant.now();

                    // Start Simulation
                    simulationStarted = true;

                    // Reset mouse click event
                    k[1] = false;
                }
            }

            sg.drawImage(fov, 0, 0, 480, 480, 0, 0, 240, 240, null);
        }
    }

    /** PROCESS EVENT
     * Processes Key Inputs needed to run the simulation
     * @param e
     */
    public void processEvent(AWTEvent e) {

        boolean down = false;
        switch (e.getID()) {
            case KeyEvent.KEY_PRESSED:
                down = true;
            case KeyEvent.KEY_RELEASED:
                k[((KeyEvent) e).getKeyCode()] = down;
                break;
            case MouseEvent.MOUSE_PRESSED:
                down = true;
            case MouseEvent.MOUSE_RELEASED:
                k[((MouseEvent) e).getButton()] = down;
            case MouseEvent.MOUSE_MOVED:
            case MouseEvent.MOUSE_DRAGGED:
                m = ((MouseEvent) e).getX() / 2 + ((MouseEvent) e).getY() / 2 * 240;
        }
    }

}
