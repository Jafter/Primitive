package Functional;

import java.util.Random;

/** Disease.java serves as an AI who's sole purpose is to gather resources and store it within its parent node.
 *  Each disease has a maximum carrying capacity, nodes have unlimited capacity. Gathering movement is randomly
 *  generated, deposits to the parent nodes take the fastest path back. A disease may only return to its parent node,
 *  and cannot use other nodes as a means for depositing resources.
 @author Jafter
 */

public class Disease {
    private int WIDTH = 1024;
    private int HEIGHT = 1024;
    private int positionX;
    private int positionY;
    private int oldPositionX;
    private int oldPositionY;
    private int[][] map;
    private int[] oldTerrain = new int[4];
    private int oldTerrainCounter = 0;
    Color c = new Color();
    Random rd = new Random();

    //Stats
    private int hunger;
    private int fatigue;
    private int boredom;
    private int carryingCapacity;
    private int nodeStorage;
    private boolean hasNode = false;
    private boolean foundFood = false;
    private boolean isForaging = false;
    private boolean foundPlant = false;
    private boolean pickedUpSeeds = false;
    private boolean isFarming = false;
    private Boolean shouldPlant = false;
    private int[] nodeCoordsX = new int[4];
    private int[] nodeCoordsY = new int[4];

    // Position of closest food source
    int foodSourceX;
    int foodSourceY;

    // Position of the disease's origin
    int nodeLocationX;
    int nodeLocationY;

    private int MAXSTAT = 100;
    private int MINSTAT = 0;

    public Disease(){}

    /** DISEASE
     * Main Constructor for Disease
     * @param terrain
     */
    public Disease(int[][] terrain) {
        this.map = terrain;
        this.hunger = MAXSTAT;
        this.fatigue = MAXSTAT;
        this.boredom = MAXSTAT;
        this.carryingCapacity = MINSTAT;
        this.nodeStorage = MINSTAT;
    }

    /** -- INFECT --
     * Infect occurs on each loop cycle in the "run()" function in Primitive.java
     *
     * After the user clicks the starting screen, a seed is injected for each disease. They serve as "nodes" to store gathered resources.
     * These "nodes" serve as a way-point for the disease, giving them the ability to drop off resources when they reach maximum capacity.
     * A number representing the total amount of gathered resources is shown above each node
     *
     * Disease AI is simple; It searches around itself for the closest food source (a green pixel), moves towards it, and begins
     * collecting resources by moving around the targeted food source randomly. When its carrying capacity is filled, it returns
     * to its parent node for a deposit, then cycles back to gathering
     *
     * Note: You may activate "Trigger Agriculture" to simulate re-planting.
     *
     * Terrain is a two dimensional array representing each pixel in the field of view. Information stored within each subscript
     * contains a decimal representation of a certain color -- See Color.java for more information.
     *
     * @param terrain
     */
    public void infect(int[][] terrain) {

        //TODO: These two lines belong @ the bottom
        this.map = terrain; // Refresh Map
        replaceTiles();  // Place terrain that was walked onto back in

        //Used to slow down walk speed
        int handicap = rd.nextInt(32);

        // Carrying capacity is full, return to base
        if(!shouldPlant && carryingCapacity >= MAXSTAT && handicap == 16){
            towardsLocation(nodeCoordsX[0], nodeCoordsY[0]);
            // Deposit Goods, trigger look for new food
            if(positionX == nodeCoordsX[0] && positionY == nodeCoordsY[0]) {
                nodeStorage += carryingCapacity;
                carryingCapacity = MINSTAT;
                foundFood = false;
                isForaging = false;

                /* Trigger Agriculture
                if(nodeStorage > 200 ) {
                    shouldPlant = true;
                    foundPlant = false;
                    isFarming = false;
                    nodeStorage -= 200;
                    carryingCapacity += 400;
                }
                */
            }
        }
        /* Plant some crops || NOT CURRENTLY USED, uncomment "Trigger Agriculture" to activate
        // TODO: This feature is very primitive, needs work */
        else if(shouldPlant && handicap == 16) {
            plantFood(); // See function for more info
        }
        // Find a food source
        else if(handicap == 16) {
            searchAndForage(); // Look for food, pick it up
        }

        // Border Sanitization, prevents disease from going off-map
        sanitizeBorders();

        // Save the tile being walked onto
        saveTiles();

        // Move into the tile.
        moveIntoTile();
    }

    /** GET NODE DETAILS
     * Fetch node coords and current number of recources collected in a given node
     * @return
     */
    public int[] getNodeDetails() {
        int[] node = new int[3];
        node[0] = nodeCoordsX[0];
        node[1] = nodeCoordsY[0];
        node[2] = nodeStorage;
        return node;
    }

    /** MOVE INTO TILE
     *  Allows the disease to walk around the terrain. The disease is 2 pixels tall and 2 pixels wide,
     *  which is why a for loop is needed to move the entire disease.
     */
    public void moveIntoTile(){
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                map[positionX + i][positionY + j] = c.PURPLE;
            }
        }
    }

    /** SAVE TILES
     *  Save the tiles that the disease walked onto, this is used to place that tile back in
     *  when the disease walks out of its current tile. The disease is 2 pixels tall and 2 pixels wide,
     *  which is why a for loop is needed to replace the disease's pathing pixels.
     */
    public void saveTiles() {
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                oldTerrain[oldTerrainCounter] = map[positionX + i][positionY + j];
                oldTerrainCounter++;
            }
        }
        oldTerrainCounter = 0;
    }

    /** SANITIZE BORDERS
     *  Prevents the disease or its nodes from walking / residing off-map.
     */
    public void sanitizeBorders() {
        if (positionX > 240) positionX = 240;
        if (positionX < 0) positionX = 0;
        if (positionY > 240) positionY = 240;
        if (positionY < 0) positionY = 0;
    }

    /** REPLACE TILES
     * Places tiles that have been saved after having been walked onto, pixels are
     * placed back onto the terrain according to what the disease has done to that terrain. Green becomes Dark Green,
     * the rest is untouched.
     */
    public void replaceTiles() {
        // Place terrain that was walked onto back in
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                // Set node/depot to store materials if there is none
                if(!hasNode) {
                    nodeCoordsX[oldTerrainCounter] = positionX+i;
                    nodeCoordsY[oldTerrainCounter] = positionY+j;
                    map[positionX + i][positionY + j] = c.WHITE;
                    if(oldTerrainCounter == 3){
                        hasNode = true;
                    }
                }
                // Set old terrain, pick-up/plant food if needed
                else {
                    if(!shouldPlant && oldTerrain[oldTerrainCounter] == c.GREEN && carryingCapacity < MAXSTAT) {
                        oldTerrain[oldTerrainCounter] = c.GREY;
                        carryingCapacity++;
                    }
                    else if(shouldPlant && oldTerrain[oldTerrainCounter] != c.WHITE){
                        oldTerrain[oldTerrainCounter] = c.ORANGE;
                        carryingCapacity--;
                        if(carryingCapacity == 0){
                            shouldPlant = false;
                        }
                    }

                    map[positionX + i][positionY + j] = oldTerrain[oldTerrainCounter];
                }
                oldTerrainCounter++;
            }
        }
        oldTerrainCounter = 0;
    }

    /** PLANT FOOD
     * Allows for agriculture; The disease will re-plant a food source after it has gathered X amount of resources.
     */
    public void plantFood() {
        if(!foundPlant){
            int randX = rd.nextInt(50 + 1 + 50) - 50;
            int randY = rd.nextInt(50 + 1 + 50) - 50;
            int mapPosX = positionX + randX;
            int mapPosY = positionY + randY;

            if (mapPosX > 240) mapPosX = 240;
            if (mapPosX < 0) mapPosX = 0;
            if (mapPosY > 240) mapPosY = 240;
            if (mapPosY < 0) mapPosY = 0;


            if (map[mapPosX][mapPosY] == c.GREY) {
                nodeLocationX = mapPosX;
                nodeLocationY = mapPosY;
                foundPlant = true;
            }
        }
        else {
                isFarming = doWork(nodeLocationX, nodeLocationY, isFarming);
        }
    }

    /** SEARCH AND FORAGE
     *  Allows the disease to look around itself and find the closest food source, once that source is found, the
     *  disease will walk to that location and begin gathering in that area. Gathering is done randomly, but the
     *  chosen harvesting spot isn't.
     */
    public void searchAndForage(){
        // Find the location of a random food source within view
        if(!foundFood) {
            int randX = rd.nextInt(50 + 1 + 50) - 50;
            int randY = rd.nextInt(50 + 1 + 50) - 50;
            int mapPosX = positionX + randX;
            int mapPosY = positionY + randY;

            if (mapPosX > 240) mapPosX = 240;
            if (mapPosX < 0) mapPosX = 0;
            if (mapPosY > 240) mapPosY = 240;
            if (mapPosY < 0) mapPosY = 0;

            if (map[mapPosX][mapPosY] == c.GREEN) {
                foodSourceX = mapPosX;
                foodSourceY = mapPosY;
                foundFood = true;
            }
        }
        // Go to the location of the food source and forage
        else {
            isForaging = doWork(foodSourceX, foodSourceY, isForaging);
        }
    }

    /** RANDOM MOVEMENT
     * Allows the disease to move around in a random fashion.
     */
    public void randomMovement() {
        int randX = rd.nextInt(2);
        int randY = rd.nextInt(2);
        if (randX == 0) {
            positionX++;
        } else {
            positionX--;
        }
        if (randY == 0) {
            positionY++;
        } else {
            positionY--;
        }
    }

    /** DO WORK
     * Main function designating the disease's work prerogative. It is used to trigger gathering and storing events.
     * @param locationX
     * @param locationY
     * @param condition
     * @return
     */
    public boolean doWork(int locationX, int locationY, boolean condition) {
        // Got to food source, forage around the location at random
        if(condition){
            randomMovement();
        }
        // Go towards food source
        else {
            towardsLocation(locationX, locationY);

            // Got to location, start work
            if (positionX == locationX && positionY == locationY) {
                condition = true;
            }
        }
        return condition;
    }

    /** TOWARDS LOCATION
     * Used by the disease to reach its gathering location in the fastest possible manner.
     * @param locationX
     * @param locationY
     */
    public void towardsLocation(int locationX, int locationY){
        // Towards Farm Source X
        if (positionX > locationX)
            positionX--;
        if (positionX < locationX)
            positionX++;

        // Towards Farm Source Y
        if (positionY > locationY)
            positionY--;
        if (positionY < locationY)
            positionY++;
    }

    /** INJECT SEED
     *  Creates a node for the disease to deposit its resources.
     * @return
     */
    public int[][] injectSeed() {
        positionX = rd.nextInt(240);
        positionY = rd.nextInt(240);

        if(positionX > 239) positionX = 239;
        if(positionX < 0) positionX = 0;
        if(positionY > 239) positionY = 239;
        if(positionY < 0) positionY = 0;

        for(int i = 0; i <= 1; i++){
            for(int j = 0; j <= 1; j++){
                map[positionX + i][positionY + j] = c.PURPLE;
            }
        }

        return map;
    }

    public int getFoodSourceX() {
        return foodSourceX;
    }

    public int getFoodSourceY(){
        return foodSourceY;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    /** GET CARRYING CAPACITY
     * Fetch current carrying capacity.
     * @return
     */
    public int getCarryingCapacity() {
        return carryingCapacity;
    }
    /* RANDOM MOVEMENT
                int randX = rd.nextInt(2);
                int randY = rd.nextInt(2);
                if (randX == 0) {
                    positionX++;
                } else {
                    positionX--;
                }
                if (randY == 0) {
                    positionY++;
                } else {
                    positionY--;
                }
     */

}
