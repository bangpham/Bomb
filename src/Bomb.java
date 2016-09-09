import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
/**
 * Mini JavaFX game of Minesweeper.
 * @author Bang Pham
 * @version 1.0
 */
public class Bomb extends Application {
    private static final int TILE_SIZE = 20;
    private static final int rootWIDTH = 600;
    private static final int rootHEIGHT = 400;
    private static int WIDTH = 600;
    private static int HEIGHT = 400;
    private static int NUM_XTILES = WIDTH / TILE_SIZE;
    private static int NUM_YTILES = HEIGHT / TILE_SIZE;
    private Tile[][] grid = new Tile[NUM_XTILES][NUM_YTILES];
    private Scene scene;
    private static Label gameOver = new Label(" GAME OVER");
    private static final Label gameOver2
    	= new Label("Press SPACE to get to back main menu");
    private static int totalBomb = 0;
    private static int score = 0;
    private static final Label instruction
		= new Label("Score : 0    Bombs : "
			+ totalBomb
			+ "    ENTER : restart game "
			+ "    SPACE : main menu    Q : quit");
    private static VBox message = new VBox(10, gameOver, gameOver2);

    /**
     * Initialize game board
     * Initialize tiles, ramdomise bombs.
     * @return game board pane
     */
    private Parent gameInit() {
    	totalBomb = 0;
        score = 0;

    	Random random = new Random();
    	
    	StackPane root = new StackPane();
        Pane gameBoard = new Pane();
        root.setPrefSize(rootWIDTH, rootHEIGHT);
        root.setMaxSize(rootWIDTH, rootWIDTH);
        gameBoard.setPrefSize(WIDTH, HEIGHT);
        gameBoard.setMaxSize(WIDTH, HEIGHT);
        gameBoard.setStyle("-fx-background-color: #DBD3D4");
        
        //initializing tiles, randomizing bombs
        for (int y = 0; y < NUM_YTILES; y++) {
            for (int x = 0; x < NUM_XTILES; x++) {
                Tile tile = new Tile(x, y, random.nextDouble() < 0.2);
                grid[x][y] = tile;
                gameBoard.getChildren().add(tile);
            }
        }
        for (int y = 0; y < NUM_YTILES; y++) {
            for (int x = 0; x < NUM_XTILES; x++) {
                Tile tile = grid[x][y];
                if (tile.isBomb) {
                	totalBomb++;
                    continue;
                }
                long bombs
                	= getNeighbors(tile).stream().filter(t -> t.isBomb).count();
                if (bombs > 0)
                    tile.text.setText("" + bombs);
            }
        }        
        
        gameOver2.setId("label-space");
        StackPane statusPane = new StackPane(instruction);
        
        instruction.setId("label-instruction");
        message.setAlignment(Pos.CENTER);
        message.setVisible(false);
        
        statusPane.setStyle("-fx-background-color: #121617");
        root.getChildren().addAll(gameBoard, message);
        statusPane.setMinWidth(rootWIDTH);
        statusPane.setMaxHeight(20);
        statusPane.setMinHeight(20);
        
        BorderPane border = new BorderPane();
        border.setBottom(statusPane);
        border.setCenter(root);
        
        return border;
    }

    /**
     * Get all 8 neighbors of a tile
     * @param tile
     * @return
     */
    private List<Tile> getNeighbors(Tile tile) {
        List<Tile> neighbors = new ArrayList<>();

        int[] neighborCoordinates = new int[] {
              -1, -1, -1, 0, -1, 1,
              0, -1, 0, 1, 1, -1, 1, 0, 1, 1
        };

        //getting position of neighbor tiles and add to list
        for (int i = 0; i < neighborCoordinates.length; i = i + 2) {
            int dx = neighborCoordinates[i];
            int dy = neighborCoordinates[i + 1];
            int neighborX = tile.x + dx;
            int neighborY = tile.y + dy;
            //check bound
            if (neighborX >= 0 && neighborX < NUM_XTILES
                    && neighborY >= 0 && neighborY < NUM_YTILES) {
                neighbors.add(grid[neighborX][neighborY]);
            }
        }
        return neighbors;
    }

    private class Tile extends StackPane {
        private int x;
        private int y;
        private boolean isBomb;
        private boolean isOpen;
        private Rectangle border = new Rectangle(TILE_SIZE - 1, TILE_SIZE - 1);
        private Text text = new Text();
        
        /**
         * Constructor of Tile.
         * @param x coordinate of tile
         * @param y coordinate of tile
         * tile will open when on mouse click.
         * @param isBomb whether tile contains bomb
         */
        public Tile(int x, int y, boolean isBomb) {
            this.x = x;
            this.y = y;
            this.isBomb = isBomb;
            border.setStroke(Color.RED);
            text.setId("tile-text");
            text.setText(isBomb ? "☢" : "☺");
            text.setVisible(false);

            getChildren().addAll(border, text);

            setTranslateX(x * TILE_SIZE);
            setTranslateY(y * TILE_SIZE);

            setOnMouseClicked(e -> open());
        }

        /**
         * Opens a tile.
         * display tile's text
         * if tile is a bomb, reset game.
         */
        public void open() {
            if (isOpen)
                return;
            
            if (isBomb) {
            	text.setVisible(true);
                border.setFill(null);
                gameOver.setText("GAME OVER");
                message.setVisible(true);
            	return;
            }
            if (!isBomb && NUM_XTILES * NUM_YTILES
            		- totalBomb - score == 1) {
            	gameOver.setText(" VICTORY");
            	message.setVisible(true);
            }
            isOpen = true;
            score++;
            instruction.setText("Score : " + score
            		+ "    Bombs :  " + totalBomb
        			+ "    ENTER : restart game"
        			+ "    SPACE : main menu    Q : quit");
            text.setVisible(true);
            border.setFill(null);
            //open neighbors of clear field
            if (text.getText().equals("☺")) {
                getNeighbors(this).forEach(p -> p.open());
            }
            
        }
    }
    /**
     * Helper method initializing menu pane.
     * @return menu pane
     */
    private Parent menuInit() {
    	StackPane pane = new StackPane();
    	
    	pane.setPrefSize(rootWIDTH, rootHEIGHT + 20);
    	pane.setStyle("-fx-background-color: #121617");
    	ImageView image =
    			new ImageView(new Image("image.png", 400, 400, false, true));
    	image.setId("image");
    	image.setTranslateX(110);
    	image.setTranslateY(120);
    	Button buttonStart = new Button("START");
    	Button buttonQuit = new Button("QUIT");
    	Button buttonAbout = new Button("ABOUT");
    	VBox vbox = new VBox(15, buttonStart, buttonQuit, buttonAbout);
    	vbox.setTranslateX(30);
    	vbox.setTranslateY(180);
    	
    	Button buttonEasy = new Button("EASY");
    	Button buttonMedium = new Button("MEDIUM");
    	Button buttonHard = new Button("HARD");
    	VBox vDifficulty = new VBox(15, buttonEasy, buttonMedium, buttonHard);
    	vDifficulty.setAlignment(Pos.CENTER);
    	vDifficulty.setId("difficulty");
    	vDifficulty.setMinSize(300, 300);  
    	vDifficulty.setMaxSize(300, 300);
    	vDifficulty.setVisible(false);
    	
    	pane.getChildren().addAll(image, vbox, vDifficulty);
    	buttonStart.setOnAction(event -> difficultyMenu(pane));
    	buttonEasy.setOnAction(event -> startGame(pane, 80, 80));
    	buttonMedium.setOnAction(event -> startGame(pane, 240, 240));
    	buttonHard.setOnAction(event -> startGame(pane, 600, 400));
    	buttonQuit.setOnAction(event -> Platform.exit());
    	buttonAbout.setOnAction(event -> {
    		        try {
    		            Desktop.getDesktop()
    		            .browse(new URI("https://github.com/bangpham"));
    		        } catch (IOException e) {
    		            e.printStackTrace();
    		        } catch (URISyntaxException e) {
    		            e.printStackTrace();
    		        }
    		    }
    		);
		return pane;
    }
    
    /**
     * Helper method pops up difficulty menu pane.
     * blur out background components.
     * @param pane
     */
    private void difficultyMenu(StackPane pane) {
    	pane.getChildren().get(2).setVisible(true);
    	pane.getChildren().get(1).setEffect(new GaussianBlur());
    	pane.getChildren().get(0).setEffect(new GaussianBlur());
    }

    /**
     * Start the game.
     * @param pane
     * @param W game difficulty level's width
     * @param H game difficulty level's height.
     */
    private void startGame(StackPane pane, int W, int H) {
    	WIDTH = W;
        HEIGHT = H;
        NUM_XTILES = WIDTH / TILE_SIZE;
        NUM_YTILES = HEIGHT / TILE_SIZE;
    	pane.getChildren().get(0).setVisible(true);
    	for (int i = 1; i <= 2; i++) {
    		pane.getChildren().get(i).setVisible(false);
    	}
    	scene.setRoot(gameInit());
	}

	@Override
    public void start(Stage stage) throws Exception {
        scene = new Scene(menuInit());
        scene.getStylesheets().add("style.css");
        scene.setOnKeyPressed((event)-> {
    		if(event.getCode() == KeyCode.SPACE) {
    			scene.setRoot(menuInit());
        	}
    		if(event.getCode() == KeyCode.ENTER) {
    			scene.setRoot(gameInit());
        	}
    		if(event.getCode() == KeyCode.Q) {
    		    Platform.exit();
        	} 
    	});
        stage.setTitle("Bomb");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}