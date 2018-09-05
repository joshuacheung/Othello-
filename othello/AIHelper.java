package othello;

import java.util.ArrayList;

/**
 * <b>StudentRelevant</b>
 * <p/>
 * This is a class containing static helper methods that {@link AIPlayer}s can (and should) use to help ensure they're
 * following the rules/mechanics of the Othello board game, in particular what valid moves are available for
 * a given board state ({@link #getValidMoves(int[][], int, int)}) and the board state the results from making a specific
 * move ({@link #tryMove(int[][], Location, int, int)}).
 * 
 * <p/>
 * This class acts as an interface to the "real" game logic methods in {@link OthelloLogic} by converting the
 * student representation of a board ({@code int[][]}) into temporary instances of {@link Board} classes.
 */
public class AIHelper {

	/**
	 * <b>StudentRelevant</b>
	 * <p/>
	 * Computes the valid moves from a given board state
	 * 
	 * @param boardArray a 2D int array representing the current board state (with 0s for unoccupied tiles, and occupied tiles containing their owner's playerID)  
	 * @param playerID the ID of the current player (the one who is about to make a move).  Usually obtained from {@link AIPlayer#getID()}
	 * @param opponentID the ID of the opponent player. Usually obtained from {@link AIPlayer#getOpponentID()}
	 * @return an {@link ArrayList} of {@link Location} objects representing the valid moves (empty list if none available)
	 */
	public static ArrayList<Location> getValidMoves(int[][] boardArray, int playerID, int opponentID)
	{
		Board tempBoard = boardFromArray(boardArray);
		OthelloLogic logic = new OthelloLogic(tempBoard);
		
		return logic.getValidMoves(playerID, opponentID);
	}
	
	/**
 	 * <b>StudentRelevant</b>
	 * <p/>
	 * Performs the provided move (and resulting captures/flips) and returns a new <i>copy</i> of the resulting board.
	 * <p/>
	 * <b>Precondition:</b> the move {@link Location} should be a valid move (e.g. a move returned from a call
	 *  to {@link #getValidMoves(int[][], int, int)} with the same board state)
	 *
	 * @param boardArray a 2D int array representing the current board state. This array does <i>not</i> get updated. 
	 * @param loc the {@link Location} to place a tile, i.e. the move
	 * @param playerID the ID of the current player (the one who is placing the tile).  Usually obtained from {@link AIPlayer#getID()}
	 * @param opponentID the ID of the opponent player. Usually obtained from {@link AIPlayer#getOpponentID()}
	 * @return a new <i>copy</i> of the board, with the newly placed tile and all of opponent's captured pieces flipped 
	 */
	public static int[][] tryMove(int[][] boardArray, Location loc, int playerID, int opponentID)
	{
		Board tempBoard = boardFromArray(boardArray);
		OthelloLogic logic = new OthelloLogic(tempBoard);
		
		if (logic.isValidMove(loc, playerID, opponentID))
		{
			logic.doMove(loc, playerID, opponentID);
		}
		else
		{
			String warningString = String.format("*** WARNING *** AI called tryMove with an invalid move (%d, %d).  board not updated", loc.getRow(), loc.getCol()); 
			System.out.println(warningString);
		}
		
		return copyOfBoard(tempBoard);
	}
	
	/**
	 * Converts/copies a Board into a simpler 2D array of integers representing player IDs on the board (0s for empty squares)
	 * 
	 * @param board the current board state
	 * @return the {@code int[][]} equivalent of the provided {@link Board}
	 */
	public static int[][] copyOfBoard(Board board) {
		int[][] boardCopy = new int[board.numRows()][board.numCols()];
		for (int row = 0; row < board.numRows(); row++) {
			for (int col = 0; col < board.numCols(); col++) {
				if (board.read(row,  col) == null) {
					boardCopy[row][col] = 0;
				}
				else {
					boardCopy[row][col] = ((GridObject)(board.read(row, col))).getID();
				}
			}
		}
		return boardCopy;
	}

	/**
	 * Converts/copies a 2D array of integers into a new @{link Board} class
	 * 
	 * @param board the current board state
	 * @return the {@code int[][]} equivalent of the provided {@link Board}
	 */
	public static Board boardFromArray(int[][] boardArray)
	{
		Board board = new Board(boardArray.length, boardArray[0].length);
		
		for (int r = 0; r < board.numRows(); r++)
			for (int c = 0; c < board.numCols(); c++)
				board.write(r, c, new GridObject(r, c, boardArray[r][c]));
		
		return board;
	}
	
}
