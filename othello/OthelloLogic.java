package othello;
import java.util.ArrayList;

public class OthelloLogic {

	private Board board;
	
	// row/column units for iterating through directions: N, NE, E, SE, S, SW, W, NW
	private static final int[] DIR_UNITS_R = {-1, -1,  0,  1,  1,  1,  0, -1};
	private static final int[] DIR_UNITS_C = { 0,  1,  1,  1,  0, -1, -1, -1};
	
	public OthelloLogic(Board b) {
		board = b;
	}
	
	public boolean isEmpty(int row, int col) {
		return board.read(row, col) == null || ((GridObject)board.read(row, col)).getID() == 0;
	}
	
	public boolean isInBounds(int row, int col) {
		return row >= 0 && row < board.numRows() && col >= 0 && col < board.numCols();
	}
	
	public GridObject getGridObjectAt(int row, int col) {
		return (GridObject)board.read(row, col);
	}

	// Returns the number of pieces on the board for the given player
	public int getNumPieces(int playerID) {
		int count = 0;
		for(int r = 0; r < board.numRows(); r++) {
			for (int c = 0; c < board.numCols(); c++) {
				if (((GridObject)board.read(r, c)).getID() == playerID)
					count++;
			}
		}
		return count;
	}
	
	public boolean boardIsFull()
	{
		for (int r = 0; r < board.numRows(); r++)
			for (int c = 0; c < board.numCols(); c++)
				if (getGridObjectAt(r, c).getID() == 0)
					return false; // found an empty spot, so not full
		
		return true; // all spots were full
	}
	 
	public ArrayList<Location> getValidMoves(int playerID, int opponentID)
	{
		ArrayList<Location> validMoves = new ArrayList<Location>();
		for (int r = 0; r < board.numRows(); r++)
			for (int c = 0; c < board.numCols(); c++)
				if (isValidMove(new Location(r, c), playerID, opponentID))
					validMoves.add(new Location(r, c));
		
		return validMoves;
	}
	
	public boolean isValidMove(Location loc, int playerID, int opponentID)
	{
		// out of bounds, can't place tile here
		if (!isInBounds(loc.getRow(), loc.getCol()))
			return false;
		
		// spot occupied, can't place tile here
		if (!isEmpty(loc.getRow(), loc.getCol()))
			return false;
		
		// check for capture along each direction
		for(int dirIndex = 0; dirIndex < DIR_UNITS_R.length; dirIndex++)
		{
			// found a capture, so move is valid (don't need to check other directions)
			if (willCaptureAlongDirection(loc, DIR_UNITS_R[dirIndex], DIR_UNITS_C[dirIndex], playerID, opponentID))
				return true;
		}
		
		// no capture in any direction, not a valid move
		return false;
	}
	
	// precondition: isValidMove was called and returned true for the same params
	// postcondition: board is updated with the newly placed tile and corresponding captured pieces are flipped
	// returns the list of tile Locations that were flipped	
	public ArrayList<Location> doMove(Location loc, int playerID, int opponentID)
	{
		ArrayList<Location> capturedTiles = new ArrayList<Location>();
		
		board.write(loc.getRow(), loc.getCol(), new GridObject(loc.getRow(), loc.getCol(), playerID));
		
		// check/do captures along each direction
		for(int dirIndex = 0; dirIndex < DIR_UNITS_R.length; dirIndex++)
		{
			if(willCaptureAlongDirection(loc, DIR_UNITS_R[dirIndex], DIR_UNITS_C[dirIndex], playerID, opponentID))
				capturedTiles.addAll(doCaptureAlongDirection(loc, DIR_UNITS_R[dirIndex], DIR_UNITS_C[dirIndex], playerID, opponentID));
		}
		
		return capturedTiles;
	}
	
	//
	// Private helper methods
	//
	
	// returns true if placing a tile at this location captures/flips some tiles in the specified direction (deltaR, deltaC)
	private boolean willCaptureAlongDirection(Location loc, int deltaR, int deltaC, int playerID, int opponentID)
	{
		// jump to the first space in the given direction
		int currentR = loc.getRow() + deltaR;
		int currentC = loc.getCol() + deltaC;
		
		// check the first space in the given direction
		if (isInBounds(currentR, currentC) && getGridObjectAt(currentR, currentC).getID() == opponentID)
		{
			// opponent tile in this direction, continue on
		}
		else // no opponent tile in this direction, so there is no capture in this direction
		{
			return false;
		}
		
		// keep following opponent tiles in this direction
		do
		{
			currentR += deltaR;
			currentC += deltaC;
		}
		// till we find something else (out of bounds, empty space, or our own tile)
		while(isInBounds(currentR, currentC) && getGridObjectAt(currentR, currentC).getID() == opponentID);
		
		// if ended with our own tile, there is a capture in this direction
		if (isInBounds(currentR, currentC) && getGridObjectAt(currentR, currentC).getID() == playerID)
			return true;
		
		// else, ended with out of bounds or empty space, so no capture in this direction
		return false;
	}
	
	// precondition: willCaptureAlongDirection was called and returned true for the same params
	// postcondition: board is updated so that the captured tiles along that direction (deltaR, deltaC) get captured/flipped
	// returns: the list of tile Locations that were flipped 
	private ArrayList<Location> doCaptureAlongDirection(Location loc, int deltaR, int deltaC, int playerID, int opponentID)
	{
		ArrayList<Location> capturedTiles = new ArrayList<Location>();
		
		// step onto first opponent tile (assumed via precondition)
		int currentR = loc.getRow() + deltaR;
		int currentC = loc.getCol() + deltaC;

		// keep following opponent tiles in this direction
		// note: no need to check for outOfBounds due to precondition
		while(getGridObjectAt(currentR, currentC).getID() == opponentID)
		{
			// capture/flip opponent tile
			board.write(currentR, currentC, new GridObject(currentR, currentC, playerID));
			capturedTiles.add(new Location(currentR, currentC));
			
			// keep moving
			currentR += deltaR;
			currentC += deltaC;
		}
		// reached the end tile of the capture (yours)
		
		return capturedTiles;
	}

}
