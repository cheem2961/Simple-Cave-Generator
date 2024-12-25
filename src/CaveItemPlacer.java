import java.util.Random;

public class CaveItemPlacer {

    private int row;
    private int col;

    private Random rand = new Random();


    //cave identification grid
    private float[][] field;
    private float[][] caveMap;
    private int caveCount;

    private float stoneThreshold;
    private float dirtThreshold;
    private float floorThreshold;

    private static final int playerID = 2;
    private static final int boulderID = 3;
    private static final int diamondID = 4;
    private static final int frogID = 5;
    private static final int butterflyID = 6;
    private static final int amoebaID = 7;
    private static final int magicWallID = 8;

    public CaveItemPlacer(float[][] aField, int aRow, int aCol, float aStoneThreshold, float aDirtThreshold, float aFloorThreshold){
        field = aField;
        row = aRow;
        col = aCol;
        caveMap = new float[aCol][aRow];
        stoneThreshold = aStoneThreshold;
        dirtThreshold = aDirtThreshold;
        floorThreshold = aFloorThreshold;

    }

    //run all the steps that create a random map and return a 2d array
    public float[][] mapProcess(int playerCount, int boulderCount, int diamondCount, int frogCount,
                                 int butterflyCount,int amoebaCount, int magicWallCount){
        identifyCaves();
        field = sprinkleItems(playerCount,boulderCount,diamondCount,frogCount,butterflyCount,amoebaCount,magicWallCount);

        //System.out.println(field);
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (field[i][j] > 1 ) {
                    System.out.print(field[i][j] + ",");
                }

            }
            //System.out.println();
        }


        return field;
    }

    //find out how many caves there are in the field
    private void identifyCaves() {
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {

                //if it's not stone (therefore a cave) AND the map where its...
                // ...gonna be stored hasn't seen this cell before
                //then add the cells group into the caveMap
                if (field[i][j] >= stoneThreshold && caveMap[i][j] == 0) {
                    //updates amount of caves
                    caveCount++;
                    //flood fill
                    addToGroup(i, j, caveCount);
                    //adding ID of caves
                    //caveIdList.add(caveCount);
                    //keeping track of the origin cell that started the flood fill
                    //caveOrigin.add(new int[]{i, j});
                }
            }
        }
    }

    //the recusrive function which performs a flood fill of all the cells in its group...
    //... and adds it to the caveMap
    private void addToGroup(int x, int y, int caveID) {

        //if the cell is being checked is stone OR its already seen this cell before
        if (field[x][y] < stoneThreshold || caveMap[x][y] != 0) return;

        caveMap[x][y] = caveID;
        addToGroup(x + 1, y, caveID);
        addToGroup(x - 1, y, caveID);
        addToGroup(x, y + 1, caveID);
        addToGroup(x, y - 1, caveID);
    }

    private float[][] sprinkleItems(int playerCount, int boulderCount, int diamondCount, int frogCount,
                              int butterflyCount,int amoebaCount, int magicWallCount){

        placeItem(playerID, playerCount);
        placeItem(boulderID,boulderCount);
        placeItem(diamondID,diamondCount);
        placeItem(frogID,frogCount);
        placeItem(butterflyID,butterflyCount);
        placeItem(amoebaID,amoebaCount);
        placeItem(magicWallID,magicWallCount);

        return field;
    }


    private void placeItem(int objectID, int objectAmount){

        for(int i = 0; i < objectAmount; i++){

            for(int attempts = 0; attempts < 100; attempts++){
                int x = rand.nextInt(col);
                int y = rand.nextInt(row);
                //System.out.println(objectID + " Placed");
                if(field[x][y] >= dirtThreshold && field[x][y] < floorThreshold && caveMap[x][y] > 0  && canPlaceItem(x,y)){
                    field[x][y] = objectID;
                    //System.out.println(objectID + " Placed");
                    //i forgot if we were allowed break or not maybe its bad practice?
                    break;
                }
            }
        }
    }


    //checking if an object placed blocks a 1 x n narrow path..
    //..i think i havent tested it yet
    private boolean canPlaceItem(int x, int y){
        if(x < 0 || x >= row || y < 0 || y >= col) return false;
        if(caveMap[x][y] < dirtThreshold) return false;


        //basically checking if a boulder would block a tunnel making the game impossible
        //this is part of some of the ruleset
        int numOpNeighbours = 0;
        if (x > 0 && field[x - 1][y] >= dirtThreshold && field[x - 1][y] < floorThreshold){
            numOpNeighbours++;
        }
        if(x < row - 1 && field[x + 1][y] >= dirtThreshold && field[x + 1][y] < floorThreshold){
            numOpNeighbours++;
        }
        if(y > 0 && field[x][y-1] >= dirtThreshold && field[x][y-1] < floorThreshold){
            numOpNeighbours++;
        }
        if(y < col - 1 && field[x][y+1] >= dirtThreshold && field[x][y+1] < floorThreshold){
            numOpNeighbours++;
        }
        //System.out.println(numOpNeighbours > 0);

        return numOpNeighbours > 0;

    }
}