package Functional;

import java.util.Random;

/** Terrain.java allows for random terrain generation within the confinement of the main BufferedImage, or Map for short.
 * This terrain is sprinkled with different ground types, giving the allure of a forest or greenland style environment.
 * See Color.java for ground information, as each ground type is designated a decimal based color.
    @author Jafter
    @author Notch --> Intro Screen and pixel map using a 2d array and decimal colors
 */
public class Terrain {

    // the width and height as specified in the terrain file.
    private int WIDTH = 1024;
    private int HEIGHT = 1024;
    private int[][] map;
    Color c = new Color();
    Random rd = new Random();

    /** TERRAIN
     * Constructor for the terrain class
     */
    public Terrain() {
        this.map = new int[WIDTH][HEIGHT];
    }

    /** BG NOISE
     * Intro Screen, aptly named bgNoise due to the look. This was inspired by Notch's Left4kDead mini game.
     * When the user begins the simulation, the map is solidified at that moment, using the bgNoise as a means
     * to generate terrain.
     */
    public void bgNoise() {
        for (int i=0; i < WIDTH; i++) {
            for (int j=0; j < HEIGHT; j++) {
                int rand = rd.nextInt(2);
                if (rand == 0) {
                    map[i][j] = c.BLACK;
                } else {
                    map[i][j] = c.GREEN;
                }
            }
        }
    }

    /** MAIN BOARD
     *
     * After the simulation is started, mainBoard ensures that the environment evolves and changes over time.
     * This gives a sense of chaos in an ever-changing environment.
     */
    public void mainBoard() {
        int rand = rd.nextInt(WIDTH)+8;
        for (int i=0; i <  WIDTH; i+= rand) {
            for (int j=0; j < HEIGHT; j += rand) {
                if (map[i][j] == c.DARK_GREEN){
                    map[i][j] = c.GREEN;
                }
                else if (map[i][j] == c.GREEN){
                    map[i][j] = c.DARK_GREEN;
                }
                else if (map[i][j] == c.BLUE) {
                    map[i][j] = c.BLACK;
                }

            }
        }
    }

    /** GENERATE LAKES
     *
     * After the simulation is started, rain is simulated every X seconds, and fills in black (or empty) tiles on
     * the map. Again this gives a sense of chaos in an ever-changing environment.
     */
    public void generateLakes() {
        int rand = rd.nextInt(WIDTH) + 3;
        for (int i=0; i < WIDTH; i+= rand) {
            for (int j=0; j < HEIGHT; j += rand) {
                if (map[i][j] == c.BLACK) {
                        map[i][j] = c.BLUE;
                } else if (map[i][j] == c.BLUE) {
                    map[i][j] = c.BLACK;
                }
            }
        }
    }

    // Growth Generator, used for planting, unused for now, see Disease.java for more info
    public void generateGrowth() {
        for (int i=0; i < WIDTH; i++) {
            for (int j=0; j < HEIGHT; j++) {
                if (map[i][j] == c.ORANGE) {
                    map[i][j] = c.DARK_GREEN;
                }
            }
        }
    }

    // Used to reset the map to a default, fully-green map. Unused for now
    public void reset() {
        for(int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++)
                map[i][j] = c.GREEN;
        }
    }

    /** GET LINEAR MAP
     *  Translate 2d map into a 1d array
     */

    public int[] getLinearMap() {
        int[] linearMap = new int[WIDTH * HEIGHT];
        int y = 0;
        for(int i=0; i < WIDTH; i++) {
            for (int j=0; j < HEIGHT; j++) {
                linearMap[y++] = this.map[i][j];
            }
        }
        return linearMap;
    }

    /** GET MAP
     * Fetch current map
     * @return
     */
    public int[][] getMap() {
        return map;
    }

    /** SET MAP
     * Set Current Map
     * @param map
     */
    public void setMap(int[][] map) {
        this.map = map;
    }

    /** TERRAIN TO PIXELS
     * Translate the current map from a 2d array into an array of pixels for the buffer to render
     * @param pixels
     * @return
     */
    public int[] terrainToPixels(int[]pixels) {
        int[] linearMap = getLinearMap();
        /* Map Design */
        for (int y = 0; y < 240; y++) {
            for (int x = 0; x < 240; x++) {
                pixels[x + y * 240] = linearMap[(x + y * 1024) & (1024 * 1024 - 1)];
            }
        }

        return pixels;
    }
    /** GET WIDTH
     * Get the terrain's width.
     * @return the terrain's width.
     */
    public int getWidth() { return this.WIDTH; }

    /** GET HEIGHT
     * Get the terrain's height.
     * @return the terrain's height.
     */
    public int getHeight() { return this.HEIGHT; }



        /* --Testing out various static lines

        // HORIZONTAL
        // Top Bar
        for (int i = 920000; i < 962900; i++) {
            map[i] = C_GREY;
        }

        //Above Above Middle Line
        for (int i = 962900; i < 963970; i++) {
            map[i] = C_PURPLE;
        }
        //Above Middle Line
        for (int i = 1012900; i < 1013970; i++) {
            map[i] = C_PURPLE;
        }

        // Middle Line
        for (int i = 12900; i < 13970; i++) {
            map[i] = C_PURPLE;
        }

        // Below Middle Line
        for (int i = 62900; i < 63970; i++) {
            map[i] = C_PURPLE;
        }

        // Below Below Middle Line
        for (int i = 112900; i < 113970; i++) {
            map[i] = C_PURPLE;
        }

        // Bottom Bar
        for (int i = 113970; i < 125000; i++) {
            map[i] = C_GREY;
        }
        */
}
