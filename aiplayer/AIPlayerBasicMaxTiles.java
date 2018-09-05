/**
 * 
 */
package aiplayer;

import java.util.ArrayList;

import othello.AIHelper;
import othello.AIPlayer;
import othello.Location;

/**
 * <b>StudentRelevant</b>
 * <p/>
 * This is a basic AI player that always picks the move that captures the most pieces.  This is only slightly smarter than a random AI
 * (it still often loses to the random AI), so you should strive to design an AI smarter than this one.
 */
public class AIPlayerBasicMaxTiles extends AIPlayer {

	private static String name = "BASIC AI - Max Captures";
	private static String iconFile = "black.png";
	
	public AIPlayerBasicMaxTiles(int id) {
		super(iconFile, name, id);
	}

	@Override
	public Location chooseMove(int[][] idArray) {
		// determine which moves are available/valid
		ArrayList<Location> validMoves = AIHelper.getValidMoves(idArray, this.getID(), this.getOpponentID());
		
		if (validMoves.size() > 0)
		{
			Location bestMoveSoFar = null;
			int mostTilesSoFar = -1;
			
			// iterate over each potential move
			for( int moveOption = 0; moveOption < validMoves.size(); moveOption++)
			{
				// try out the move to see what would happen
				int[][] resultBoard = AIHelper.tryMove(idArray, validMoves.get(moveOption), this.getID(), this.getOpponentID());

				// count the total number of our tiles after the move
				int resultTileCount = countPlayerPieces(resultBoard, this.getID());

				// is this move "better"?
				if (resultTileCount > mostTilesSoFar)
					bestMoveSoFar = validMoves.get(moveOption);
			}

			return bestMoveSoFar;
		}
		else
		{
			// no moves available
			return null;	
		}
	}
	
	/**
	 * Helper method that counts the number of tiles belonging to a specified player
	 * @param idArray the board
	 * @param playerID the player to count
	 * @return number of pieces belonging to the player
	 */
	private int countPlayerPieces(int[][] idArray, int playerID)
	{
		int count = 0;
		
		for(int r = 0; r < idArray.length; r++)
			for(int c = 0; c < idArray[0].length; c++)
				if (idArray[r][c] == playerID)
					count++;
		
		return count;
	}
	
}