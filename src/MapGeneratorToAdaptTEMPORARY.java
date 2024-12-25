import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapGeneratorToAdaptTEMPORARY extends Application {

    //size of the window
    private static final int WINDOW_WIDTH = 850;
    private static final int WINDOW_HEIGHT = 900;

    // The size of the canvas
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 800;

    private Canvas canvas;

    // Loaded images
    private Image dirtImage;
    private Image stoneImage;
    private Image floorImage;



    //----------------------IMPORTANT INFO--------------------
    //if you want to mess around with the program try changing rez to different values...
    //low values creates bigger maps and caves but longer to run
    //if you want more stuff to mess around with go to the createField() method
    private int rez = 16;   //resolution/size of the cells
    //--------------------------------------------------------

    //main grid

    private int rows = CANVAS_WIDTH / rez;
    private int columns = CANVAS_HEIGHT / rez;
    private float[][] field = new float[columns][rows];

    //cave identification grid
    private float[][] caveMap = new float[columns][rows];
    private int caveCount = 0;
    private List<int[]> caveOrigin = new ArrayList<>();
    private List<Integer> caveIdList = new ArrayList<>();

    //random numbers
    Random rand = new Random();

    //block thresholds
    float stoneThreshold = 0.5f;
    float dirtThreshold = 0.6f;
    float floorThreshold = 1f;

    //blockValue
    float stoneValue = 0.25f;
    float dirtValue = 0.55f;
    float floorValue = 0.8f;



    public void start(Stage primaryStage) {
        loadImages();
        createField();

        Scene scene = new Scene(buildScene(), WINDOW_WIDTH, WINDOW_HEIGHT);

        //generate map
        field = blur(field);
        identifyCaves();
        connectCaves();


        //space to add more random objects
        CaveItemPlacer newCave = new CaveItemPlacer(field,rows,columns,stoneThreshold,dirtThreshold,floorThreshold);
        field = newCave.mapProcess(1,20,1,1,1,1,1);

        System.out.println(createMap());

        drawGame();
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    //converting the 2d array into the level file format and returning it as such
    public String createMap(){
        float[][] grid = field;
        /*
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println("\n");
        }*/

        String stringGrid = "Grid\n";

        stringGrid += String.valueOf(rows) + " " + String.valueOf(columns) +"\n";

        System.out.println(stringGrid);
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if(field[i][j] < stoneThreshold){
                    stringGrid += "S";
                } else if (field[i][j] >= stoneThreshold && field[i][j] < dirtThreshold){
                    stringGrid += "D";
                } else if(field[i][j] >= dirtThreshold && field[i][j] < floorThreshold) {
                    stringGrid += "P";
                }
                stringGrid += ",";
            }
            stringGrid += "\n";
        }
        return stringGrid;
    }


    public void drawGame() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Clear canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Set the background to gray.
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());


        //drawing blocks
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                if (field[i][j] < stoneThreshold) {
                    gc.drawImage(stoneImage, j * rez, i * rez ,rez,rez);
                }
                if (field[i][j] >= stoneThreshold && field[i][j] < dirtThreshold) {
                    gc.drawImage(dirtImage, j * rez, i * rez ,rez,rez);
                }
                if (field[i][j] >= dirtThreshold && field[i][j] < floorThreshold) {
                    gc.drawImage(floorImage, j * rez, i * rez ,rez,rez);
                }
            }
        }
    }

    private void loadImages() {
        // Load images. Note we use png images with a transparent background.
        //kinda going for a factory theme as i lovee factorio
        stoneImage = new Image("neonMetal.png");
        dirtImage = new Image("fuckCrate.png");
        floorImage = new Image("fuckFloor.png");
    }

    private void createField() {
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {

                //---------------------IMPORTANT INFO-------------------
                //the ratio between stone and walkable objects drawn is decided here
                //0 is stone
                //1 is walkable objects
                //Math.random() < 1 ? 0 : 1 = fully stone map
                //Math.random() < "X chance of creating stone" : walkableObject : stone;
                //try changing the X value and seeing how it affects the map
                field[i][j] = Math.random() < 0.45 ? 0 : 1;
                //--------------------------------------------------------------------
            }
        }
    }

    //smoothing jagged map edges and creating caves
    private float[][] blur(float[][] grid){
        float left;
        float up;

        //blur iterations
        int it =10;

        //for every cell in the grid, average its gird value from 4 around it
        //this is the blurring process
        for (int h = 0; h < it; h++) {
            float[][] newGrid = new float[columns][rows];
            for (int i = 1; i < columns; i++) {
                for (int j = 1; j < rows; j++) {

                    //gathering blocks around the cell being checked, taking borders into account
                    if (i - 1 < 1) {
                        left = grid[columns - 1][j];
                    }else{
                        left = grid[i - 1][j];
                    }
                    float right = grid[(i + 1) % columns][j];
                    if (j - 1 < 1) {
                        up = grid[i][rows - 1];
                    }else{
                        up = grid[i][j - 1];
                    }
                    float down = grid[i][(j + 1) % rows];
                    float middle = grid[i][j];

                    //creating a new value for the current cell
                    float average = (left + right + up + down + middle) / 5;
                    newGrid[i][j] = average;
                }
            }

            //finally updating the gird with the values in each cell
            grid = newGrid;
        }

        //adding a stone border around the map
        for (int i = 0; i < columns; i++) {
            grid[i][0] = 0;
            grid[i][rows - 1] = 0;
        }
        for (int j = 0; j < rows; j++) {
            grid[0][j] = 0;
            grid[0][columns - 1] = 0;
        }
        return grid;
    }

    //find out how many caves there are in the field
    private void identifyCaves(){
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {

                //if it's not stone (therefore a cave) AND the map where its...
                // ...gonna be stored hasn't seen this cell before
                //then add the cells group into the caveMap
                if (field[i][j] >= stoneThreshold && caveMap[i][j] == 0) {
                    //updates amount of caves
                    caveCount++;
                    //flood fill
                    addToGroup(i, j, caveCount);
                    //adding ID of caves
                    caveIdList.add(caveCount);
                    //keeping track of the origin cell that started the flood fill
                    caveOrigin.add(new int[]{i,j});

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


    //connecting the caves by each of the caves origin cells
    private void connectCaves(){
        for (int i = 0; i < caveOrigin.size() - 1; i++) {
            int[] start = caveOrigin.get(i);
            int[] end = caveOrigin.get(i + 1);
            carveTunnel(start[0],start[1],end[0],end[1]);
        }
    }

    //tunnel carving from origin cells provided
    private void carveTunnel(int startX, int startY, int endX, int endY) {

        //carving a horizontal tunnel
        while (startX != endX){
            field[startX][startY] = dirtValue;
            startX += (endX > startX) ? 1 : -1;
        }
        //carving a vertucal tunnel
        while (startY != endY) {
            field[startX][startY] = dirtValue;
            startY += (endY > startY) ? 1 : -1;
        }
    }


    private Pane buildScene() {
        BorderPane root = new BorderPane();
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        root.setCenter(canvas);

        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}