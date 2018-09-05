/* TODO:
 - Add code for the menu option "Allow Custom Images"
   (when checked, players use the custom images defined in their class
    when not checked, player 1 is always black and player 2 is always white
*/
package othello;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;

public class OthelloController {

	private Board board;
	private OthelloGUI gui;
	private GridView view;
	private OthelloLogic othelloLogic;
	private HighlightPanel highlightPanel;
	private Player humanPlayer1; // human player object
	private Player humanPlayer2; // human player object
	private Player player1;		// player 1 within the game
	private Player player2;		// player 1 within the game
	private Player whoseTurn;
	private boolean gameOver;
	private Timer gameTimer;
	private static ArrayList<Player> allAIPlayerList;			// List of all AI players
	private ArrayList<Player> availableAIPlayerList;	// Available AI players
	private ArrayList<Player> selectedAIPlayerList;		// Players selected for tournament
	private ArrayList<GameResult> gameResultList;
	private int player1Index = 0;
	private int player2Index = 1;
	private OthelloController controller = this;
	private int gameCounter = 1;
	private boolean pauseAfterGame = true;
	private boolean pauseOnWarning = false;
	private boolean continuousMode = false;
	private static boolean tournamentPlay = false; // tournament play or match play
	private int timerSpeed = 0;
	private static BufferedImage[] imageIDMap;	// Maps playerID to player image
	private static BufferedImage blackImage;
	private static BufferedImage whiteImage;
		
	public OthelloController(OthelloGUI g, GridView v, Board b, HighlightPanel mp) {
		gui = g;
		board = b;
		view = v;
		highlightPanel = mp;
		MyMouseListener myMouseListener = new MyMouseListener();
		view.addBoardListeners(myMouseListener, myMouseListener);
		MyGUIListener myGUIListener = new MyGUIListener();
		gui.addGUIListeners(myGUIListener);
		othelloLogic = new OthelloLogic(board);
		gameOver = true;
		allAIPlayerList = new ArrayList<Player>();
		availableAIPlayerList = new ArrayList<Player>();
		selectedAIPlayerList = new ArrayList<Player>();
		gameResultList = new ArrayList<GameResult>();
		int playerID = 1;

		// Default images for player1 and player 2
		try {
			blackImage = ImageIO.read(new File("images" + System.getProperty("file.separator") + "black.png"));
			whiteImage = ImageIO.read(new File("images" + System.getProperty("file.separator") + "white.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Default human players
		humanPlayer1 = new Player("black.png", "Bob", playerID++);
		humanPlayer2 = new Player("white.png", "Joe", playerID++);

		// This section adds all AIPlayers in the "aiplayer" folder to the tournament.
		FileSystemView fsv = FileSystemView.getFileSystemView();
		File [] file = fsv.getFiles(new File("aiplayer"),true);
		String className = "";
		for (int i = 0; i < file.length; i++) {
			if (file[i].getName().endsWith("class") && file[i].getName().contains("AIPlayer") && !file[i].getName().equals("AIPlayer.class")) {
				Class<? extends AIPlayer> theClass = null;
				AIPlayer obj = null;
				className = file[i].getName();
				int spot = className.lastIndexOf(".");
				className = className.substring(0,spot);						
				try {
					theClass = Class.forName("aiplayer." + className).asSubclass(AIPlayer.class);
					Constructor<?> constructor = theClass.getConstructor(int.class);
					obj = (AIPlayer) constructor.newInstance(playerID++);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (obj != null) {
					availableAIPlayerList.add(obj);
					allAIPlayerList.add(obj);
				}
			}
		}
		
		if (availableAIPlayerList.size() == 0) {
			System.out.println("Warning: No AI players found...");
		}

		// Fill map of player ID to player image
		// Size + 3 because slot zero isn't used and (two human default players + AIPlayers)
		imageIDMap = new BufferedImage[allAIPlayerList.size() + 3];
		imageIDMap[1] = humanPlayer1.getImage();
		imageIDMap[2] = humanPlayer2.getImage();
		for (Player p : allAIPlayerList) {
			imageIDMap[p.getID()] = p.getImage();
		}

		String gameResult = String.format(" %-6s %-20.20s        %-20.20s", "Game", " Player 1", " Player 2");
		gui.addGameInfo(gameResult);
		String header = "";
		for (int i = 0; i < 65; i++) {
			switch(i) {
				case 7:
				case 35: header += "\u252C"; break;
				default: header += "\u2500";
			}
		}
		gui.addGameInfo(header);
		gameTimer = new Timer(timerSpeed, new MyTimerListener());

		player1 = humanPlayer1;
		player2 = humanPlayer2;
		player1.setOpponentID(player2.getID());
		player1.setIsFirstPlayer(true);
		player2.setOpponentID(player1.getID());
		player2.setIsFirstPlayer(false);

		initializeNewGame();
		initializeEmptyBoard();
	}

	// Precondition: player1 and player2 have been set along with calls to setOpponentID
	public void initializeNewGame() {
		board.reset();
		initializeBoardPieces();
		player1.setMyCaptures(0);
		player1.setOpponentCaptures(0);
		player2.setMyCaptures(0);
		player2.setOpponentCaptures(0);
		gui.setPlayer1Name(player1.getName());
		gui.setPlayer2Name(player2.getName());
		gui.setPlayer1Icon(getPlayerImage(player1.getID()));
		gui.setPlayer2Icon(getPlayerImage(player2.getID()));
		gui.setVSMessage(player1.getName(), player2.getName(), getPlayerImage(player1.getID()), getPlayerImage(player2.getID()));
		whoseTurn = player1;
		gameOver = false;
		highlightPanel.setDropLoc(null);
		highlightPanel.setCaptureLocs(null);
		highlightPanel.repaint();
		gui.setPlayer1NumPieces("" + othelloLogic.getNumPieces(player1.getID()));
		gui.setPlayer2NumPieces("" + othelloLogic.getNumPieces(player2.getID()));
		gui.repaint();
	}

	public void initializeBoardPieces() {

		initializeNormalStartingBoard();
		
		// other starting boards for testing/debugging purposes
		//initializeFullRandomBoardExceptMiddleEmpty(0.5);
		//initializeCustomStartingBoard();
	}
	
	private void initializeEmptyBoard()	{
		for (int r = 0; r < board.numRows(); r++)
			for (int c = 0; c < board.numCols(); c++)
				board.write(r, c, new GridObject(r, c, 0));
	}
	
	private void initializeNormalStartingBoard() {
		initializeEmptyBoard();
		board.write(3, 4, new GridObject(3, 4, player1.getID()));
		board.write(4, 3, new GridObject(4, 3, player1.getID()));
		board.write(3, 3, new GridObject(3, 3, player2.getID()));
		board.write(4, 4, new GridObject(4, 4, player2.getID()));
	}
	
	private void initializeFullRandomBoardExceptMiddleEmpty(double probabilityForPlayer1Pieces) {
		for (int r = 0; r < board.numRows(); r++) {
			for (int c = 0; c < board.numCols(); c++) {
				if (Math.random() < probabilityForPlayer1Pieces)
					board.write(r, c, new GridObject(r, c, player1.getID()));
				else
					board.write(r, c, new GridObject(r, c, player2.getID()));					
			}
		}
		board.write(3, 4, new GridObject(3, 4, 0));
		board.write(4, 3, new GridObject(4, 3, 0));
		board.write(3, 3, new GridObject(3, 3, 0));
		board.write(4, 4, new GridObject(4, 4, 0));
		
	}
	
	private void initializeCustomStartingBoard() {
		// modify this hardcoded array to whatever starting board you want
		int[][] customArray = {
				{1, 0, 0, 0, 0, 0, 0, 2},
				{0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 2, 1, 0, 0, 0},
				{0, 0, 0, 1, 2, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0},
				{1, 0, 0, 0, 0, 0, 0, 2}
		};
		// transform 1 -> player1 ID, and 2 -> player2 ID
		for (int r = 0; r < customArray.length; r++)
			for (int c = 0; c < customArray[0].length; c++)
			{
				if (customArray[r][c] == 1)
					customArray[r][c] = player1.getID();
				else if (customArray[r][c] == 2)
					customArray[r][c] = player2.getID();
			}
		// write into the official board
		for (int r = 0; r < board.numRows(); r++)
			for (int c = 0; c < board.numCols(); c++) 
				board.write(r, c, new GridObject(r, c, customArray[r][c]));
	}
	
	public void makeMove(Location loc) {

		if (othelloLogic.isValidMove(loc, whoseTurn.getID(), whoseTurn.getOpponentID())) {
			
			highlightPanel.setDropLoc((int)view.getParent().getLocation().getX() + (int)view.getLocation().getX(), (int)view.getParent().getLocation().getY() + (int)view.getLocation().getY(), new Location(loc.getRow(), loc.getCol()));
			highlightPanel.repaint();
			ArrayList<Location> flippedTiles = othelloLogic.doMove(loc, whoseTurn.getID(), whoseTurn.getOpponentID());

			// Highlight flipped tiles
			highlightPanel.setCaptureLocs(flippedTiles);
			highlightPanel.repaint();
			
			// Updated how many pieces each player has
			int player1Score = othelloLogic.getNumPieces(player1.getID());
			int player2Score = othelloLogic.getNumPieces(player2.getID());
			gui.setPlayer1NumPieces("" + player1Score);
			gui.setPlayer2NumPieces("" + player2Score);
			
			// Check for end-game conditions
			// Check for a full board
			if (othelloLogic.boardIsFull()) {
				displayWinnerAndRecordStats(player1Score, player2Score);
			}
			// check if neither player has a valid move
			else if(othelloLogic.getValidMoves(whoseTurn.getID(), whoseTurn.getOpponentID()).size() == 0 && othelloLogic.getValidMoves(whoseTurn.getOpponentID(), whoseTurn.getID()).size() == 0) {
				if (pauseOnWarning)
					JOptionPane.showMessageDialog(null, "Neither player has valid moves available.  Accouncing winner...");
				displayWinnerAndRecordStats(player1Score, player2Score);
			}
			// If only the next player has no valid moves. If so then it's still our turn
			else if(othelloLogic.getValidMoves(whoseTurn.getOpponentID(), whoseTurn.getID()).size() == 0) {
				whoseTurn = (whoseTurn == player1 ? player2 : player1); // Will be reversed again below				
				if (pauseOnWarning)
					JOptionPane.showMessageDialog(null, whoseTurn.getName() + " has no valid moves available.  Skipping turn.");
			}
		// Player made an invalid move
		} else {
			// If human player, try again
			if (!(whoseTurn instanceof AIPlayer)) {
				if (pauseOnWarning)
					JOptionPane.showMessageDialog(null, "Invalid move by " + whoseTurn.getName() + "!  Try again...");
				else
					System.out.println("Invalid move by " + whoseTurn.getName() + "!");
				whoseTurn = (whoseTurn == player1 ? player2 : player1); // Will be reversed again below
			}
			// If AIPlayer, choose a random tile
			else {
				if (pauseOnWarning) {
					JOptionPane.showMessageDialog(null, "Invalid move by " + whoseTurn.getName() + "!  Choosing random move...");
				} else {
					System.out.println("Invalid move by " + whoseTurn.getName() + "!  Choosing random move...");
				}
				ArrayList<Location> validMoves = AIHelper.getValidMoves(AIHelper.copyOfBoard(board), whoseTurn.getID(), whoseTurn.getOpponentID());
				makeMove(validMoves.get((int)(Math.random()*validMoves.size())));
				return;
			}
			
		}

		if (gameOver && tournamentPlay) {
			gameCounter++;
			player2Index++;
			if (player2Index < selectedAIPlayerList.size() && selectedAIPlayerList.get(player2Index).equals(player1))
				player2Index++;
			if (player2Index >= selectedAIPlayerList.size()) {
				player2Index = 0;
				player1Index++;
				if (player1Index  >= selectedAIPlayerList.size()) {
					System.out.println("Tournament Over.");
					
					JOptionPane.showMessageDialog(null, "End of tournament");
					gui.getPlayButton().setText("Play");
					gameTimer.stop();
					player1Index = 0;
					player2Index = 1;
				}
			}
			
			if (player1Index < selectedAIPlayerList.size() && player2Index < selectedAIPlayerList.size()) {
				// Set players
				player1 = selectedAIPlayerList.get(player1Index);
				player2 = selectedAIPlayerList.get(player2Index);				
				player1.setOpponentID(player2.getID());
				player1.setIsFirstPlayer(true);
				player2.setOpponentID(player1.getID());
				player2.setIsFirstPlayer(false);
			}
			
			initializeNewGame();

		}
		else if (gameOver && !tournamentPlay) {
			System.out.println("End of game " + gameCounter++);
			player1Index = 0;
			player2Index = 1;
			highlightPanel.setDropLoc(null);
			highlightPanel.setCaptureLocs(null);
			highlightPanel.repaint();

			gameOver = false;
			highlightPanel.setDropLoc(null);
			highlightPanel.setCaptureLocs(null);
			highlightPanel.repaint();
			board.reset();
			gui.repaint();
			
			initializeNewGame();
		}
		else {
			whoseTurn = (whoseTurn == player1 ? player2 : player1);
			highlightPanel.setDropLoc(null);
			highlightPanel.repaint();
		}
	}

	// Determine who win, announce winners, update game statistics
	public void displayWinnerAndRecordStats(int player1Score, int player2Score) {

		String displayMessage;
		String gameResult;
		
		if (player1Score > player2Score) {
			displayMessage = player1.getName() + " Wins!";
			updateGameResults(player1, 1);
			updateGameResults(player2, 0);
			gameResult = String.format(" %-5d \u2502 %-18.18s (W+%2d) \u2502 %-20.20s     ", gameCounter, player1.getName(), othelloLogic.getNumPieces(player1.getID()) - othelloLogic.getNumPieces(player2.getID()), player2.getName());
		}
		else if (player1Score < player2Score) {
			displayMessage = player2.getName() + " Wins!";
			updateGameResults(player2, 1);
			updateGameResults(player1, 0);
			gameResult = String.format(" %-5d \u2502 %-18.18s        \u2502 %-20.20s (W+%2d)", gameCounter, player1.getName(), player2.getName(), othelloLogic.getNumPieces(player2.getID()) - othelloLogic.getNumPieces(player1.getID()));
		}
		else {
			displayMessage = "Tie Game!";
			updateGameResults(player1, 0.5);
			updateGameResults(player2, 0.5);
			gameResult = String.format(" %-5d \u2502 %-18.18s (Tie)  \u2502 %-20.20s (Tie)", gameCounter, player1.getName(), player2.getName());
		}
		gui.addGameInfo(gameResult);
		Collections.sort(gameResultList, Collections.reverseOrder());

		String tournamentResultString = String.format(" %-5s %-26s %-7s\n", "Rank", " Player", "Points");

		for (int i = 0; i < 60; i++) {
			switch(i) {
				case 6: case 32: tournamentResultString += "\u252C"; break;
				default: tournamentResultString += "\u2500";
			}
		}
		tournamentResultString += "\n";

		for (int i = 0; i < gameResultList.size(); i++) {
			GameResult g = gameResultList.get(i);
			tournamentResultString += String.format(" %-5d\u2502 %-23.20s \u2502 %-6.1f\n", i+1, g.getPlayer().getName(), g.getPoints());
		}
		
		
		if (pauseAfterGame) {
			JOptionPane.showMessageDialog(null, displayMessage);
			// if in Player vs Player mode, stop the game and reset the button to "Play"
			if (!tournamentPlay) {
				if (gameTimer.isRunning())  {
					gameTimer.stop();
					gui.getPlayButton().setText("Play");					
				}
				
			}
		}
		
		gui.setTournamentInfo(tournamentResultString);
		gameOver = true;
	}
	
	// Points:  Win is 1, Tie is 0.5
	public void updateGameResults(Player p, double points) {
		boolean recordFound = false;

		for (GameResult g : gameResultList) {
			if (g.getPlayer().getID() == p.getID()) {
				g.addPoints(points);
				recordFound = true;
			}
		}
		if (!recordFound) {
			gameResultList.add(new GameResult(p, points));
		}
	}

	public static BufferedImage getPlayerImage(int playerID) {
		if (tournamentPlay) {
			return imageIDMap[playerID];
		}
		else {
			if (playerID == 1) // human 1, always 1st player so black
				return blackImage;
			else if (playerID == 2) // human 2, always 2nd player so black
				return whiteImage;
			else {
				// AI players. playerID 3 is at index 0 in ai player list (id 4 at index 1, etc)
				return allAIPlayerList.get(playerID - 3).getIsFirstPlayer() ? blackImage : whiteImage;					
			}
		}			
	}
	
	class MyGUIListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Play")) {
				if (continuousMode && !gameTimer.isRunning()) {
					((JButton)(e.getSource())).setText("Stop");
					gameTimer.start();
				}
				else if (continuousMode && gameTimer.isRunning()) {
					((JButton)(e.getSource())).setText("Step");
					gui.getContinuousModeCheckBox().setSelected(false);
					continuousMode = false;
					gameTimer.stop();
				}
				else if (!continuousMode)
				if (whoseTurn instanceof AIPlayer) {
						Location loc = whoseTurn.chooseMove(AIHelper.copyOfBoard(board));
						controller.makeMove(loc);
				}
			}
			else if (e.getActionCommand().equals("Continuous Mode")) {
				continuousMode = ((JCheckBox)(e.getSource())).isSelected();
				if (!continuousMode) {
					gui.getPlayButton().setText("Step");
					if (gameTimer.isRunning()) {
						gameTimer.stop();						
					}
				}
				else if (continuousMode) {
					gui.getPlayButton().setText("Play");
				}
			}
			else if (e.getActionCommand().equals("Pause After Game")) {
				pauseAfterGame = ((JCheckBox)(e.getSource())).isSelected();
			}
			else if (e.getActionCommand().equals("Pause on Warnings")) {
				pauseOnWarning = ((JCheckBox)(e.getSource())).isSelected();
			}
			else if (e.getActionCommand().equals("Change Speed")) {
				String result = (String)(((JComboBox<?>)(e.getSource())).getSelectedItem());
				if (result.equals("No Delay")) timerSpeed = 0;
				else if (result.equals("Fast")) timerSpeed = 10;
				else if (result.equals("Medium")) timerSpeed = 800;
				else if (result.equals("Slow")) timerSpeed = 2000;
				gameTimer.setDelay(timerSpeed);
			}
			// Menu commands
			else if (e.getActionCommand().equals("Player vs Player Game")) {
				// TODO: move all this code elsewhere
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
				JComboBox<String> p1Box = new JComboBox<String>();
				p1Box.setBorder(new TitledBorder("Player 1"));
				p1Box.addItem("Human");
				for (Player p : allAIPlayerList)
					p1Box.addItem(p.getName());
				JComboBox<String> p2Box = new JComboBox<String>();
				p2Box.setBorder(new TitledBorder("Player 2"));
				p2Box.addItem("Human");
				for (Player p : allAIPlayerList)
					p2Box.addItem(p.getName());
				panel.add(p1Box);
				panel.add(p2Box);
				gui.getGlassPane().setVisible(false);
				JOptionPane.showInternalMessageDialog(gui.getContentPane(), panel, "Player vs Player Game", JOptionPane.PLAIN_MESSAGE, null);
				gui.getGlassPane().setVisible(true);
				
				if (p1Box.getSelectedItem().equals("Human")) {
					gui.getGlassPane().setVisible(false);
					String name = JOptionPane.showInternalInputDialog(gui.getContentPane(), "Enter Name", "Player 1", JOptionPane.PLAIN_MESSAGE);
					gui.getGlassPane().setVisible(true);
					if (name == null || name.equals(""))
						name = "Human Player 1";
					humanPlayer1.setName(name);
					player1 = humanPlayer1;
				}
				else {
					for (Player p : availableAIPlayerList) {
						if (p.getName().equals(p1Box.getSelectedItem())) {
							player1 = p;
							break;
						}
					}
				}

				if (p2Box.getSelectedItem().equals("Human")) {
					gui.getGlassPane().setVisible(false);
					String name = JOptionPane.showInternalInputDialog(gui.getContentPane(), "Enter Name", "Player 2", JOptionPane.PLAIN_MESSAGE);
					gui.getGlassPane().setVisible(true);
					if (name == null || name.equals(""))
						name = "Human Player 2";
					humanPlayer2.setName(name);
					player2 = humanPlayer2;
				}
				else {
					for (Player p : availableAIPlayerList) {
						if (p.getName().equals(p2Box.getSelectedItem())) {
							player2 = p;
							break;
						}
					}
				}

				if (!p1Box.getSelectedItem().equals("Human") && p1Box.getSelectedItem().equals(p2Box.getSelectedItem())) {
					JOptionPane.showMessageDialog(null, "Sorry, you can't play a player against itself yet.  This feature is coming soon!");
				}
				else {				
					player1.setOpponentID(player2.getID());
					player1.setIsFirstPlayer(true);
					player2.setOpponentID(player1.getID());
					player2.setIsFirstPlayer(false);
					initializeNewGame();
				}				
			
			}
			else if (e.getActionCommand().equals("AI Tournament")) {
				// TODO: Move this code elsewhere
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
				DefaultListModel<String> availableListModel = new DefaultListModel<String>();
				for (Player p : availableAIPlayerList)
					availableListModel.addElement(p.getName());
				JList<String> p1List = new JList<String>(availableListModel);
				sortListModel(availableListModel);
				
				DefaultListModel<String> tournamentListModel = new DefaultListModel<String>();
				for (Player p : selectedAIPlayerList)
					tournamentListModel.addElement(p.getName());
				JList<String> p2List = new JList<String>(tournamentListModel);

				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new GridBagLayout());
				buttonPanel.setPreferredSize(new Dimension(140, 100));
				JPanel subPanel = new JPanel();
				subPanel.setPreferredSize(new Dimension(100, 70));
				JButton addButton = new JButton("Add -->");
				addButton.addActionListener(new ActionListener() {
					// Move selected items from availableList to tournamentList
					public void actionPerformed(ActionEvent evt) {
				    	int[] indexes = p1List.getSelectedIndices();
				        for (int i = indexes.length - 1; i >= 0; i--) {
				        	tournamentListModel.addElement(availableListModel.getElementAt(indexes[i]));
				        	availableListModel.remove(indexes[i]);
				        }
				        sortListModel(tournamentListModel);

				        // Update global player lists
				        availableAIPlayerList.clear();
				        selectedAIPlayerList.clear();
				        for (Player p : allAIPlayerList) {
				        	for (int i = 0; i < availableListModel.size(); i++) {
				        		if (availableListModel.getElementAt(i).equals(p.getName())) {
				        			availableAIPlayerList.add(p);
				        			break;
				        		}
				        	}
				        }
				        for (Player p : allAIPlayerList) {
				        	for (int i = 0; i < tournamentListModel.size(); i++) {
				        		if (tournamentListModel.getElementAt(i).equals(p.getName())) {
				        			selectedAIPlayerList.add(p);
				        			break;
				        		}
				        	}
				        }
				    }
				});
				JButton removeButton = new JButton("<-- Remove");
				removeButton.addActionListener(new ActionListener() {
					// Move selected items from tournamentList to availableList
				    public void actionPerformed(ActionEvent evt) {
				        int[] indexes = p2List.getSelectedIndices();
				        for (int i = indexes.length - 1; i >= 0; i--) {
				        	availableListModel.addElement(tournamentListModel.getElementAt(indexes[i]));
				        	tournamentListModel.remove(indexes[i]);
				        }
				        sortListModel(availableListModel);

				        // Update global player lists
				        availableAIPlayerList.clear();
				        selectedAIPlayerList.clear();
				        for (Player p : allAIPlayerList) {
				        	for (int i = 0; i < availableListModel.size(); i++) {
				        		if (availableListModel.getElementAt(i).equals(p.getName())) {
				        			availableAIPlayerList.add(p);
				        			break;
				        		}
				        	}
				        }
				        for (Player p : allAIPlayerList) {
				        	for (int i = 0; i < tournamentListModel.size(); i++) {
				        		if (tournamentListModel.getElementAt(i).equals(p.getName())) {
				        			selectedAIPlayerList.add(p);
				        			break;
				        		}
				        	}
				        }				        
				    }
				});

				subPanel.add(addButton);
				subPanel.add(removeButton);
				buttonPanel.add(subPanel);

				JScrollPane p1ScrollPane = new JScrollPane(p1List);
				p1ScrollPane.setPreferredSize(new Dimension(250, 200));
				p1ScrollPane.setBorder(new TitledBorder("Available Players"));
				JScrollPane p2ScrollPane = new JScrollPane(p2List);
				p2ScrollPane.setPreferredSize(new Dimension(250, 200));
				p2ScrollPane.setBorder(new TitledBorder("Players In Tournament"));
				
				panel.add(p1ScrollPane);
				panel.add(buttonPanel);
				panel.add(p2ScrollPane);
				gui.getGlassPane().setVisible(false);
				JOptionPane.showInternalMessageDialog(gui.getContentPane(), panel, "AI Tournament", JOptionPane.PLAIN_MESSAGE, null);
				gui.getGlassPane().setVisible(true);

				if (selectedAIPlayerList.size() < 2) {
					gui.getGlassPane().setVisible(false);
					JOptionPane.showInternalMessageDialog(gui.getContentPane(), "Not enough players added to tournament.  Try again.");
					gui.getGlassPane().setVisible(true);
				}
				else {				
					player1 = selectedAIPlayerList.get(0);
					player2 = selectedAIPlayerList.get(1);				
					player1.setOpponentID(player2.getID());
					player1.setIsFirstPlayer(true);
					player2.setOpponentID(player1.getID());
					player2.setIsFirstPlayer(false);
					tournamentPlay = true;
					initializeNewGame();
				}
			}
			else if (e.getActionCommand().equals("Exit")) {
				System.exit(0);
			}
			else if (e.getActionCommand().equals("Reset Stats")) {
				System.out.println("Reset stats....");
				gameResultList.clear();
				gui.clearGameInfo();
				gui.setTournamentInfo("");
				String gameResult = String.format(" %-6s %-20.20s        %-20.20s", "Game", " Player 1", " Player 2");
				gui.addGameInfo(gameResult);
				String header = "";
				for (int i = 0; i < 65; i++) {
					switch(i) {
						case 7:
						case 35: header += "\u252C"; break;
						default: header += "\u2500";
					}
				}
				gui.addGameInfo(header);
				gui.repaint();
			}
		}
		
		private void sortListModel(DefaultListModel<String> model) {
	        ArrayList<String> tempList = new ArrayList<String>();
	        for (int i = 0; i < model.getSize(); i++)
	        	tempList.add(model.getElementAt(i));
	        Collections.sort(tempList);
	        model.removeAllElements();
	        for (int i = 0; i < tempList.size(); i++)
	        	model.addElement(tempList.get(i));

		}
	}
	
	class MyTimerListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			// If current player is an AI player, get the move from the player and make the move
			if (whoseTurn instanceof AIPlayer) {
				Location loc = whoseTurn.chooseMove(AIHelper.copyOfBoard(board));
				controller.makeMove(loc);
			}
		}
	}

	
	
	class MyMouseListener extends MouseAdapter {

		// Highlight piece placement
		@Override
		public void mousePressed(MouseEvent e) {
			if (!(whoseTurn instanceof AIPlayer)) {
				int row = view.yCoordToRow(e.getY());
				int col = view.xCoordToCol(e.getX());
				if (othelloLogic.isInBounds(row, col)) {
					if (othelloLogic.isValidMove(new Location(row, col), whoseTurn.getID(), whoseTurn == player1 ? player2.getID() : player1.getID())) {
						// Explanation of the line below:  After adding the menu, the highlighted pieces were out of place because
						// they were based on the overall GUI having (0, 0) as the upper left corner.  That's what the getParent()
						// offset below is for.  Because the highlightPanel is a GlassPane, we need to calculate the overall offset.
						highlightPanel.setDropLoc((int)view.getParent().getLocation().getX() + (int)view.getLocation().getX(), (int)view.getParent().getLocation().getY() + (int)view.getLocation().getY(), new Location(row, col));
					} else {
						highlightPanel.setDropLoc(null);
					}
					highlightPanel.setHighlightImage(whoseTurn.getImage());
					highlightPanel.repaint();
				}
			}
		}

		// Highlight piece placement
		@Override
		public void mouseMoved(MouseEvent e) {
			mousePressed(e);
		}

		// Highlight piece placement
		@Override
		public void mouseDragged(MouseEvent e) {
			mousePressed(e);
		}
		
		// If current player is a human, get move location from mouse coordinates
		@Override
		public void mouseReleased(MouseEvent e) {
			// If current player is a human player, get move location from mouseReleased location
			if (!(whoseTurn instanceof AIPlayer)) {
				int row = view.yCoordToRow(e.getY());
				int col = view.xCoordToCol(e.getX());
				if (othelloLogic.isValidMove(new Location(row, col), whoseTurn.getID(), whoseTurn == player1 ? player2.getID() : player1.getID())) {
					controller.makeMove(new Location(row, col));
				}
			}
		}
	}
}
