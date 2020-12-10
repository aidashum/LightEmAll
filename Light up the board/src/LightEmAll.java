
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Comparator;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

import javax.swing.JOptionPane;

class LightEmAll extends World {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  // ArrayList<Edge> mst;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;

  public LightEmAll(int height, int width) {
    
    
      this.height = height;
      this.width = width;
      this.powerRow = 0;
      this.powerCol = 0;
      board = new ArrayList<ArrayList<GamePiece>>();
      nodes = new ArrayList<GamePiece>();
    
  }

  public void initializeGame() {
    // setup the board according to requirement
    this.initializeBoard();

    // scramble the board (by randomly rotating each node)
    scrambleBoard();

    // calculate the radius of power station
    this.radius = calculateRadius(this.findGP(powerCol, powerCol));
    // calculate the depth between power station and each node
    this.nodeDepth();

    // figure out if each node on the board is powered
    powerBoard();
  }

  // setup the board
  public void initializeBoard() {
    // create a empty board with no wires
    makeBoard();

    // wire the board using using subdivision algorithm
    // subDivideBoard(board);

    // wire the board using Kruskal Minimum Spanning Tree algorithm
    this.wireBoard();
  }

  // create a board of tiles that have no wires
  public void makeBoard() {
    board = new ArrayList<ArrayList<GamePiece>>();
    nodes = new ArrayList<GamePiece>();
    
    for (int i = 0; i < height; i++) {
      ArrayList<GamePiece> columnPieces = new ArrayList<GamePiece>();
      for (int j = 0; j < width; j++) {
        boolean powerStation = false;
        if (i == powerCol && j == powerRow) {
          powerStation = true;
        }
        // initialize game piece with no wires.
        // wires will be setup later
        GamePiece gamePiece = new GamePiece(j, i);
        gamePiece.powerStation = powerStation;
        nodes.add(gamePiece);
        columnPieces.add(gamePiece);
      }
      board.add(columnPieces);
    }
  }
  
  public void wireBoard() {
	  this.wireBoardUseKruskalMST(board);
  }

  public void wireBoardUseKruskalMST(ArrayList<ArrayList<GamePiece>> board) {
    ArrayList<Edge> edgesInTree = mstKrus(board);
    for (Edge edge : edgesInTree) {
      GamePiece fromNode = edge.fromNode;
      GamePiece toNode = edge.toNode;
      if (fromNode.col == toNode.col) {
        if (fromNode.row < toNode.row) {
          fromNode.bottom = true;
          toNode.top = true;
        }
        else {
          fromNode.top = true;
          toNode.bottom = true;
        }
      }
      else {
        if (fromNode.col < toNode.col) {
          fromNode.right = true;
          toNode.left = true;
        }
        else {
          fromNode.left = true;
          toNode.right = true;
        }
      }
    }
  }

  public ArrayList<Edge> mstKrus(ArrayList<ArrayList<GamePiece>> board) {
    HashMap<String, String> representatives;
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    ArrayList<Edge> workList;

    workList = makeEdges(board);
    representatives = makeRepresentatives(board);
    while (!workList.isEmpty()) {
      Edge currentEdge = workList.remove(0);
      String fromNodeUI = currentEdge.fromNode.getUniqueName();
      String toNodeUI = currentEdge.toNode.getUniqueName();
      String fromRepresentative = find(representatives, fromNodeUI);
      String toRepresentative = find(representatives, toNodeUI);
      if (!fromRepresentative.equals(toRepresentative)) {
        edgesInTree.add(currentEdge);
        union(representatives, fromRepresentative, toRepresentative);
      }
    }
    return edgesInTree;
  }

  public ArrayList<Edge> makeEdges(ArrayList<ArrayList<GamePiece>> aBoard) {
    ArrayList<Edge> edgeList = new ArrayList<Edge>();
    Random rand = new Random(1);
    for (int i = 0; i < aBoard.size(); i++) {
      for (int j = 0; j < aBoard.get(i).size(); j++) {
        if (i != aBoard.size() - 1) {
          // horizontal edge
          Edge edge = new Edge();
          edge.fromNode = findGP(i, j);
          edge.toNode = findGP(i + 1, j);
          edge.weight = rand.nextInt(256);
          edgeList.add(edge);
        }
        if (j != aBoard.get(i).size() - 1) {
          // horizontal edge
          Edge edge = new Edge();
          edge.fromNode = findGP(i, j);
          edge.toNode = findGP(i, j + 1);
          edge.weight = rand.nextInt(256);
          edgeList.add(edge);
        }
      }
    }
    // sort edge list from low to high with weight using heap sort
    return HeapSort.heapSort(edgeList);
  }

  public HashMap<String, String> makeRepresentatives(
      ArrayList<ArrayList<GamePiece>> board) {
    HashMap<String, String> representatives = new HashMap<String, String>();
    for (int i = 0; i < board.size(); i++) {
      for (int j = 0; j < board.get(i).size(); j++) {
        GamePiece node = board.get(i).get(j);
        representatives.put(node.getUniqueName(), node.getUniqueName());
      }
    }
    return representatives;
  }

  public static String find(HashMap<String, String> representatives, String key) {
    String value = representatives.get(key);
    while (!key.equals(value)) {
      key = value;
      value = representatives.get(key);
    }
    return value;
  }

  public static void union(HashMap<String, String> representatives, String rep1, String rep2) {
    representatives.put(rep1, rep2);
  }

  // scramble the board by randomly rotating each tiles
  public void scrambleBoard() {
    Random rand = new Random(1);
    for (GamePiece gamePiece : nodes) {
      int rotates = rand.nextInt(4);
      for (int i = 0; i < rotates; i++) {
        gamePiece.rotate();
      }
    }
  }

  public int calculateRadius(GamePiece node) {
    int radius = 0;
    GamePiece lastFound = breadthFirstSearch(node);
    GamePiece newLastFound = breadthFirstSearch(lastFound);
    radius = (newLastFound.depth / 2) + 1;
    return radius;
  }

  public void nodeDepth() {
	  GamePiece PS = null;
	for(GamePiece gp : nodes) {
		if(gp.powerStation) {
			PS = gp;
		}
		
	}
    
    breadthFirstSearch(PS);
  }

  public GamePiece breadthFirstSearch(GamePiece startNode) {
    Queue<GamePiece> queue = new LinkedList<GamePiece>();
    ArrayList<GamePiece> explored = new ArrayList<GamePiece>();
    int currentDepth;

    // reset the depth of all nodes
    for (GamePiece gamePiece : nodes) {
      gamePiece.depth = 0;
    }

    queue.add(startNode);
    // explored.add(startNode);

    while (!queue.isEmpty()) {
      GamePiece current = queue.remove();
      explored.add(current);
      currentDepth = current.depth + 1;
      // add neighboring tiles to queue if needed
      if (current.top && current.row > 0) {
        GamePiece topTile = findGP(current.row - 1, current.col);
        if (!explored.contains(topTile)) {
          topTile.depth = currentDepth;
          queue.add(topTile);
        }
      }
      if (current.right && current.col < board.size() - 1) {
        GamePiece rightTile = findGP(current.row, current.col + 1);
        if (!explored.contains(rightTile)) {
          rightTile.depth = currentDepth;
          queue.add(rightTile);
        }
      }
      if (current.bottom && current.row < board.get(0).size() - 1) {
        GamePiece bottomTile = findGP(current.row + 1, current.col);
        if (!explored.contains(bottomTile)) {
          bottomTile.depth = currentDepth;
          queue.add(bottomTile);
        }
      }
      if (current.left && current.col > 0) {
        GamePiece leftTile = findGP(current.row, current.col - 1);
        if (!explored.contains(leftTile)) {
          leftTile.depth = currentDepth;
          queue.add(leftTile);
        }
      }
    }
    if (explored.size() > 0) {
      return explored.get(explored.size() - 1);
    }
    else {
      return null;
    }
  }

  public void onMouseClicked(Posn pos) {
    int x = (pos.x - GamePiece.HEIGHT / 2 ) / GamePiece.HEIGHT;
    int y = (pos.y - GamePiece.WIDTH / 2) / GamePiece.WIDTH;
    
    if (x == this.width) {
    	x --;
    }
    if (y == this.height) {
    	y --;
    }
    ArrayList<GamePiece> columnPieces = board.get(x);
    GamePiece gamePiece = columnPieces.get(y);
    gamePiece.rotate();
    this.resetPowerCoord();
    // calculate the radius of power station
    this.radius = calculateRadius(this.findGP(powerCol, powerRow));
    // calculate the depth between power station and each node
    nodeDepth();
    // figure out if each node on the board is powered
    powerBoard();
  }

  public void onKeyEvent(String key) {
	  GamePiece power = this.findGP(powerCol, powerRow);
	    if (key.equals("left") && powerCol > 0) {
	      GamePiece leftOfPow = this.board.get(powerCol - 1).get(powerRow);
	      if (power.left && leftOfPow.right) {
	       // power.powerStation = false;
	        //leftOfPow.powerStation = true;
	        this.changePowerCoord(leftOfPow);
	      }
	    }
	    if (key.equals("right") && powerCol < this.width - 1) {
	      GamePiece rightOfPow = this.board.get(powerCol + 1).get(powerRow);
	      if (power.right && rightOfPow.left) {
	       // power.powerStation = false;
	        //rightOfPow.powerStation = true;
	        this.changePowerCoord(rightOfPow);
	      }
	    }
	    if (key.equals("up") && powerRow > 0) {
	      GamePiece topOfPow = this.board.get(powerCol).get(powerRow - 1);
	      if (power.top && topOfPow.bottom) {
	       // power.powerStation = false;
	        //topOfPow.powerStation = true;
	        this.changePowerCoord(topOfPow);
	      }
	    }
	    if (key.equals("down") && powerRow < this.height - 1) {
	      GamePiece botOfPow = this.board.get(powerCol).get(powerRow + 1);
	      if (power.bottom && botOfPow.top) {
	       // power.powerStation = false;
	        //botOfPow.powerStation = true;
	        this.changePowerCoord(botOfPow);
	      }
	    }
	  this.resetPowerCoord();
      this.radius = calculateRadius(this.findGP(powerCol, powerRow));
      // calculate the depth between power station and each node
      nodeDepth();
      // figure out if each node on the board is powered
      powerBoard();
    }
  


  public void resetPowerCoord() {
	  for(GamePiece gp: nodes) {
		  if(gp.col == powerCol && gp.row == powerRow) {
			  powerCol = gp.col;
			  powerRow = gp.row;
		  }
	  }
  }
  
  
  public void changePowerCoord(GamePiece gp) {
	    this.board.get(this.powerCol).get(this.powerRow).powerStation = false;
	    this.powerCol = gp.col;
	    this.powerRow = gp.row;
	    gp.powerStation = true;
	  

	  }
  
  public void onTick() {
	  this.resetPowerCoord();
  }
  

  public GamePiece findGP(int row, int col) {
    GamePiece gamePieceTarget = null;
    for (GamePiece gamePiece : nodes) {
      if (gamePiece.row == row && gamePiece.col == col) {
        gamePieceTarget = gamePiece;
        break;
      }
    }
    return gamePieceTarget;
  }
  
  

  // function to decide how the board is powered
  public void powerBoard() {
	  GamePiece PS = null; 
  
    // switch off all gamePieces
    for (GamePiece gamePiece : nodes) {
      gamePiece.hasConnection = false;
    }
    
    for(GamePiece gp : nodes) {
		  if(gp.powerStation) {
			  PS = gp;
		  }
	  }
    // find powerStation
    
    
    PS.hasConnection = true;
    // power neighbors
    powerNeighbors(PS);
    if (isGameWon()) {
      gameWonStartNewGame();
    }
  }

  public boolean isGameWon() {
    boolean isAllPowered = true;
    for (GamePiece node : nodes) {
      if (!node.hasConnection) {
        isAllPowered = false;
        break;
      }
    }
    return isAllPowered;
  }

  public void gameWonStartNewGame() {
    JOptionPane.showMessageDialog(null, "You Won! Click OK to Start New Game.", "You Won!",
        JOptionPane.INFORMATION_MESSAGE);
    initializeBoard();
  }

  public void powerNeighbors(GamePiece gamePiece) {
    // power left neighboring gamePiece if possible
    if (gamePiece.col > 0 && gamePiece.left) {
      powerGamePiece(gamePiece.row, gamePiece.col - 1, "left");
    }
    // power right neighboring gamePiece if possible
    if (gamePiece.col < board.size() - 1 && gamePiece.right) {
      powerGamePiece(gamePiece.row, gamePiece.col + 1, "right");
    }
    // power top neighboring gamePiece if possible
    if (gamePiece.row > 0 && gamePiece.top) {
      powerGamePiece(gamePiece.row - 1, gamePiece.col, "top");
    }
    // power bottom neighboring gamePiece if possible
    if (gamePiece.row < board.get(0).size() - 1 && gamePiece.bottom) {
      powerGamePiece(gamePiece.row + 1, gamePiece.col, "bottom");
    }
  }

  public void powerGamePiece(int row, int col, String powerFrom) {
    // get the gamePiece by position
    GamePiece gamePiece = findGP(row, col);
    // if not powered yet then proceed to see if it can be powered
    if (!gamePiece.hasConnection) {
      if (("left".equals(powerFrom) && gamePiece.right && gamePiece.depth <= radius)
          || ("right".equals(powerFrom) && gamePiece.left && gamePiece.depth <= radius)
          || ("top".equals(powerFrom) && gamePiece.bottom && gamePiece.depth <= radius)
          || ("bottom".equals(powerFrom) && gamePiece.top && gamePiece.depth <= radius)) {
        gamePiece.hasConnection = true;
      }
      // if gamePiece is powered now, then try to power its neighbors
      if (gamePiece.hasConnection) {
        powerNeighbors(gamePiece);
      }
    }
  }

  public WorldScene makeScene() {
	  WorldScene s = new WorldScene(this.width * GamePiece.SIZE, 
			  this.height * GamePiece.SIZE + (this.height/ 2) * GamePiece.SIZE);
	  for (int i = 0; i < this.board.size(); i++) {
	        for (int j = 0; j < this.board.get(i).size(); j++) {
	          GamePiece gp = board.get(i).get(j);
	          
	          gp.drawGamePieceAt(s, radius);
	        }
	  }
    return s;
  }

  
}

class GamePiece {
  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  // whether this node is powered
  boolean hasConnection;
  // the depth between this node to powerStation
  int depth;
  

  public final static int WIDTH = 40;
  public final static int HEIGHT = 40;
  public final static int SIZE = 50;
  
//Constants 
	static final int GP_SIZE = 50;
	// Images
	public static WorldImage POWERSTATION = new StarImage(20, 7, OutlineMode.SOLID , Color.CYAN);
	public static WorldImage BASE_TILE = new FrameImage(new RectangleImage(GP_SIZE,GP_SIZE, "solid", 
	    Color.GRAY));

  public GamePiece(int row, int col) {
    this.col = col;
    this.row = row;
    
    }
  public GamePiece(int row, int col, boolean left, boolean right, boolean top, boolean bottom,
      boolean powerStation) {
    this(row, col);
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = powerStation;
    
    
    
  }

  public Color findWireColor(int radius) {
	  Color wire = Color.LIGHT_GRAY;
	  if(this.hasConnection) {
		  int veryLit = radius / 3;
		  int lit = veryLit * 2;
		  if(this.depth <= veryLit) {
			  wire = Color.YELLOW;
		  }
		  if(this.depth <= lit) {
			  wire = Color.ORANGE;
		  }
		  else {
			 // Color darkerOrangeYellow = new Color(215,168,13);
			  wire = Color.red;
		  }
	  } 
	  return wire;
	  
  }
  public void drawGamePieceAt(WorldScene scene, int radius) {
	  
      Color wireCol = this.findWireColor(radius);
	  WorldImage finalTile = GamePiece.BASE_TILE;
	  
	  
	  scene.placeImageXY(finalTile, (col * SIZE) + SIZE / 2, (row * SIZE) + SIZE / 2);

	  if (left) {
	    WorldImage connector = new RotateImage(new RectangleImage(SIZE / 25, SIZE / 2,"solid",
	    		wireCol), 90.00);
	    scene.placeImageXY(connector, (col * SIZE) + SIZE / 2 - SIZE / 4, 
	        (row ) * SIZE + SIZE / 2);

	  }
	  if (right) {
	    WorldImage connector = new RotateImage(new RectangleImage( SIZE / 25, SIZE / 2,"solid", 
	    		wireCol), 90.00);
	    scene.placeImageXY(connector, (col * SIZE) + SIZE / 2 + SIZE / 4, 
	        (row) * SIZE + SIZE / 2);

	  }
	  if (top) {
	    WorldImage connector = new RectangleImage(SIZE / 25, SIZE / 2,"solid", wireCol);
	    scene.placeImageXY(connector, ((col) * SIZE) + SIZE / 2 ,
	        (row * SIZE) + SIZE / 2 - SIZE / 4);

	  }
	  if (bottom) {
	    WorldImage connector = new RectangleImage( SIZE / 25, SIZE / 2,"solid", wireCol);
	    scene.placeImageXY(connector, ((col) * SIZE) + SIZE / 2 , 
	        (row * SIZE) + SIZE / 2 + SIZE / 4);

	  }
	  if (powerStation) {
	    WorldImage pow = GamePiece.POWERSTATION;
	    scene.placeImageXY(pow,(col * SIZE) + SIZE / 2, (row * SIZE) + SIZE / 2);
	  }
	}

  public void rotate() {
    boolean saveLeft = left;
    left = bottom;
    bottom = right;
    right = top;
    top = saveLeft;
  }

  // a unique name for this node. for example "(3,1)" for node of row 3 col 1.
  public String getUniqueName() {
    return this.col +","+ this.row;
  }
}

class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;

  public Edge() {

  }

  public Edge(GamePiece fromNode, GamePiece toNode, int weight) {
    super();
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.weight = weight;
  }
}

class HeapSort {

  public static ArrayList<Edge> heapSort(ArrayList<Edge> unsortedList) {
    int count = unsortedList.size();
    heapify(unsortedList, count);
    int end = count - 1;
    while (end > 0) {
      swap(unsortedList, end, 0);
      end = end - 1;
      siftDown(unsortedList, 0, end);
    }
    return unsortedList;
  }

  public static void heapify(ArrayList<Edge> unsortedList, int count) {
    int start = count / 2 - 1;
    while (start >= 0) {
      siftDown(unsortedList, start, count - 1);
      start -= 1;
    }
  }

  public static void siftDown(ArrayList<Edge> unsortedList, int start, int end) {
    int root = start;
    while (root * 2 + 1 <= end) {
      int child = root * 2 + 1;
      int swap = root;
      if (unsortedList.get(swap).weight < unsortedList.get(child).weight) {
        swap = child;
      }
      if (child + 1 <= end && unsortedList.get(swap).weight < unsortedList.get(child + 1).weight) {
        swap = child + 1;
      }
      if (swap != root) {
        swap(unsortedList, root, swap);
        root = swap;
      }
      else {
        return;
      }
    }
  }

  public static void swap(ArrayList<Edge> unsortedList, int swapOne, int swapTwo) {
    Edge holder = unsortedList.get(swapOne);
    unsortedList.set(swapOne, unsortedList.get(swapTwo));
    unsortedList.set(swapTwo, holder);
  }
}
//Tester class for the Game
class ExamplesLightEmAll {
  LightEmAll lightEmAll;
  LightEmAll lightEmAll1;
  LightEmAll lightEmAll2;
  LightEmAll lightEmAll3;
  LightEmAll lightEmAll4;
  GamePiece gamePiece1;
  GamePiece gamePiece2;
  GamePiece gamePiece3;
  Color wireCol;


  //this method also tests the initialize game method
  void initData() {
 	  wireCol = Color.GRAY;
    lightEmAll = new LightEmAll(2, 2);
    lightEmAll.initializeGame();
    lightEmAll1 = new LightEmAll(2, 2);
    lightEmAll1.initializeGame();
    lightEmAll2 = new LightEmAll(4, 4);
    lightEmAll2.initializeGame();
    gamePiece1 = new GamePiece(0, 0);
    gamePiece2 = new GamePiece(0, 0);
    gamePiece3 = new GamePiece(2, 0);
  }


//tests the initializeBoard method
void testInitializeBoard(Tester t) {
  initData();
  // the constructors above call initializeBoard
  t.checkExpect(lightEmAll1.board.size(), 2);
  t.checkExpect(lightEmAll2.board.size(), 4);

}



//tests the heapSort method in the heapSort class
void testHeapSort(Tester t) {
  GamePiece gamePiece1 = new GamePiece(0, 0);
  GamePiece gamePiece2 = new GamePiece(0, 1);
  GamePiece gamePiece3 = new GamePiece(0, 2);
  GamePiece gamePiece4 = new GamePiece(0, 3);
  GamePiece gamePiece5 = new GamePiece(1, 0);
  GamePiece gamePiece6 = new GamePiece(2, 0);
  GamePiece gamePiece7 = new GamePiece(3, 0);
  ArrayList<Edge> edgeList = new ArrayList<Edge>();
  Edge edge1 = new Edge(gamePiece1, gamePiece2, 3);
  edgeList.add(edge1);
  Edge edge2 = new Edge(gamePiece2, gamePiece3, 2);
  edgeList.add(edge2);
  Edge edge3 = new Edge(gamePiece3, gamePiece4, 1);
  edgeList.add(edge3);
  Edge edge4 = new Edge(gamePiece1, gamePiece5, 3);
  edgeList.add(edge4);
  Edge edge5 = new Edge(gamePiece5, gamePiece6, 3);
  edgeList.add(edge5);
  Edge edge6 = new Edge(gamePiece6, gamePiece7, 5);
  edgeList.add(edge6);
  ArrayList<Edge> sortedList = HeapSort.heapSort(edgeList);
  t.checkExpect(sortedList.get(0).weight, 1);
}

//tests the HashMap's find method 
void testFind(Tester t) {
  HashMap<String, String> reps = new HashMap<String, String>();
  reps.put("A", "B");
  reps.put("B", "B");
  reps.put("C", "D");
  reps.put("D", "D");
  reps.put("E", "E");
  t.checkExpect(LightEmAll.find(reps, "A").equals("B"), true);
  t.checkExpect(LightEmAll.find(reps, "B").equals("B"), true);
  t.checkExpect(LightEmAll.find(reps, "C").equals("D"), true);
  t.checkExpect(LightEmAll.find(reps, "D").equals("D"), true);
  t.checkExpect(LightEmAll.find(reps, "E").equals("E"), true);
}

//tests the union method in the LightEmAll class
void testUnion(Tester t) {
  HashMap<String, String> reps = new HashMap<String, String>();
  reps.put("A", "A");
  reps.put("B", "B");
  reps.put("C", "C");
  reps.put("D", "D");
  reps.put("E", "E");
  LightEmAll.union(reps, "C", "D");
  t.checkExpect(LightEmAll.find(reps, "C").equals("D"), true);
}

//tests the onMouseClicked method in the LightEmAll class
void testOnMouseClicked(Tester t) {
  LightEmAll testBoard = new LightEmAll(2, 2);
  testBoard.initializeGame();
  GamePiece node = testBoard.findGP(0, 1);
  testBoard.onMouseClicked(new Posn(1, 1));
  t.checkExpect(node.left, true);
  t.checkExpect(node.right, false);
  t.checkExpect(node.top, true);
  t.checkExpect(node.bottom, false);
}


//tests the method that deals with a key being pressed
void testOnKeyEvent(Tester t) {
  LightEmAll testBoard = new LightEmAll(6, 6);
  testBoard.initializeGame();
  testBoard.onKeyReleased("down");
  t.checkExpect(testBoard.powerRow, 0);
  t.checkExpect(testBoard.powerCol, 0);
}

void testGetPowerStationGamePiece(Tester t) {
  LightEmAll lightEmAll = new LightEmAll(5, 5);
  lightEmAll.initializeGame();
  GamePiece powerStation = new GamePiece(lightEmAll.powerRow, lightEmAll.powerCol);
  t.checkExpect(powerStation.row, 0);
  t.checkExpect(powerStation.col, 0);
}

//tests the findGP method
void testFindGP(Tester t) {
  LightEmAll lightEmAll = new LightEmAll(5, 5);
  lightEmAll.initializeGame();
  GamePiece node = lightEmAll.findGP(4, 2);
  t.checkExpect(node.row, 4);
  t.checkExpect(node.col, 2);
}

//tests the PowerGamePiece method
void testPowerGamePiece(Tester t) {
  LightEmAll testBoard = new LightEmAll(3, 3);
  testBoard.initializeGame();
  testBoard.powerGamePiece(1, 1, "top");
  t.checkExpect(testBoard.findGP(0, 1).powerStation, false);
  testBoard.powerGamePiece(1, 0, "down");
  t.checkExpect(testBoard.findGP(1, 0).powerStation, false);
}

//tests the calculateRadius method 
void testCalculateRadius(Tester t) {
  LightEmAll testBoard = new LightEmAll(6, 6);
  testBoard.initializeGame();
  GamePiece node = testBoard.findGP(0, 1);
  int radius = testBoard.calculateRadius(node);
  t.checkExpect(radius, 1);
}

//tests tests the nodeDepth method
void testCalculateDepthForEachNode(Tester t) {
  LightEmAll testBoard = new LightEmAll(6, 6);
  testBoard.initializeGame();
  testBoard.nodeDepth();
  GamePiece node = testBoard.findGP(5, 3);
  t.checkExpect(node.depth, 0);
}

//test the makeBoard method
void testMakeBoard(Tester t) {
  initData();
  WorldScene s = new WorldScene(120, 120);
  WorldScene r = new WorldScene(400, 400);
  lightEmAll.makeBoard();
}

//tests the drawGamePiece method
void testDrawGamePiece(Tester t) {
  initData();
  WorldScene s = new WorldScene(50, 50);
  gamePiece2.drawGamePieceAt(s, 3);
}

//tests the rotate method
void testRotate(Tester t) {
  initData();
  t.checkExpect(gamePiece1.left, false);
  t.checkExpect(gamePiece1.right, false);
  t.checkExpect(gamePiece1.top, false);
  t.checkExpect(gamePiece1.bottom, false);
  gamePiece1.rotate();
  t.checkExpect(gamePiece1.left, false);
  t.checkExpect(gamePiece1.right, false);
  t.checkExpect(gamePiece1.top, false);
  t.checkExpect(gamePiece1.bottom, false);
}



//tests the whole game 
  void testWorld(Tester t) {
    LightEmAll g = new LightEmAll(5, 5);
    g.initializeBoard();
    g.initializeGame();
    g.bigBang(g.width * GamePiece.SIZE, g.height * GamePiece.SIZE, 0.1);
   }
} 