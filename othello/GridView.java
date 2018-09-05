package othello;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

public class GridView extends JPanel implements Observer {

	private static final long serialVersionUID = -4358507809242582233L;
	private int cellSize;
	private int imageMargin;
	private int numRows;
	private int numCols;
	private int gridLineThickness;
	private boolean showGridLines;
	private BufferedImage bgImage;
	private ArrayList<GridObject> gridObjects = new ArrayList<GridObject>();
	
	/**
	 * Creates a new GridView with the given number of rows, columns. Grid lines
	 * are set to visible with a thickness of 1. The Dimension of this GridView
	 * is calculated and set to (cols*cellSize) x (rows*cellSize) plus the space
	 * the grid lines take up.
	 * 
	 * @param rows
	 *            The number of rows in this GridView
	 * @param cols
	 *            The number of columns in this GridView
	 * @param cellSize
	 *            The size in pixels of each cell
	 * @param imageMargin
	 *            the GridObject image inside each cell is shrunk so that there is a margin of the specified number of pixels
	 * @param observeObj
	 * 			  An Observable object that updates this view
	 */
	public GridView(int rows, int cols, int cellSize, int imageMargin, Observable observeObj) {
		this.cellSize = cellSize;
		this.imageMargin = imageMargin;
		this.numRows = rows;
		this.numCols = cols;
		this.setGridLineThickness(1);
		this.showGridLines = false;
		setPreferredSize(new Dimension(cellSize * cols + gridLineThickness
				* (cols + 1), cellSize * rows + gridLineThickness * (rows + 1)));
		setMinimumSize(new Dimension(cellSize * cols + gridLineThickness
				* (cols + 1), cellSize * rows + gridLineThickness * (rows + 1)));
		if (observeObj != null) {
			observeObj.addObserver(this);
		}
	}

	/**
	 * Creates a new GridView with the given number of rows, columns and grid
	 * lines turned on with the given thickness. The Dimension of this GridView
	 * is calculated and set to (cols*cellSize) x (rows*cellSize) plus the space
	 * the grid lines take up.
	 * 
	 * @param rows
	 *            The number of rows in this GridView
	 * @param cols
	 *            The number of columns in this GridView
	 * @param cellSize
	 *            The size in pixels of each cell
	 * @param imageMargin
	 *            the GridObject image inside each cell is shrunk so that there is a margin of the specified number of pixels
	 * @param gridLineThickness
	 * 			  The thickness in pixels of the grid lines
	 * @param observeObj
	 * 			  An Observable object that updates this view
	 */
	public GridView(int rows, int cols, int cellSize, int imageMargin, int gridLineThickness, Observable observeObj) {
		this.cellSize = cellSize;
		this.imageMargin = imageMargin;
		this.numRows = rows;
		this.numCols = cols;
		this.setGridLineThickness(gridLineThickness);
		this.showGridLines = true;
		setPreferredSize(new Dimension(cellSize * cols + gridLineThickness
				* (cols + 1), cellSize * rows + gridLineThickness * (rows + 1)));
		if (observeObj != null) {
			observeObj.addObserver(this);
		}
	}

	/**
	 * Sets the background of this GridView to the specified image
	 * @param img
	 */
	public void setBackground(BufferedImage img) {
		bgImage = img;
	}
	
	/**
	 * Returns the width of each cell in pixels
	 * 
	 * @return Width of each cell in pixels
	 */
	public int getCellSize() {
		return cellSize;
	}

	/**
	 * Sets the width of each cell in pixels and updates the display for this
	 * GridView.
	 * 
	 * @param cellSize
	 *            Width of each cell in pixels.
	 */
	public void setCellSize(int cellSize) {
		this.cellSize = cellSize;
		this.repaint();
	}

	/**
	 * Returns the size (in pixels) of the margin around GridObject images
	 * 
	 * @return Width of each cell in pixels
	 */
	public int getImageMargin() {
		return imageMargin;
	}

	/**
	 * Sets the size (in pixels) of the margin around GridObject images
	 * 
	 * @param cellSize
	 *            Width of each cell in pixels.
	 */
	public void setImageMargin(int imageMargin) {
		this.imageMargin = imageMargin;
		this.repaint();
	}
	
	/**
	 * Returns the number of rows in this GridView
	 * 
	 * @return Number of rows in this GridView
	 */
	public int getNumRows() {
		return numRows;
	}

	/**
	 * Sets the number of rows for this GridView and updates the display.
	 * 
	 * @param numRows
	 *            Number of rows in this GridView
	 */
	public void setNumRows(int numRows) {
		this.numRows = numRows;
		this.repaint();
	}

	/**
	 * Returns the number of columns in this GridView
	 * 
	 * @return Number of columns in this GridView
	 */
	public int getNumCols() {
		return numCols;
	}

	/**
	 * Sets the number of columns for this GridView and updates the display.
	 * 
	 * @param numRows
	 *            Number of columns in this GridView
	 */
	public void setNumCols(int numCols) {
		this.numCols = numCols;
		this.repaint();
	}

	/**
	 * Returns the thickness of the grid lines in pixels.
	 * 
	 * @return Thickness of the grid lines in pixels.
	 */
	public int getGridLineThickness() {
		return gridLineThickness;
	}

	/**
	 * Sets the thickness of the grid lines in pixels and updates the display.
	 * 
	 * @param gridLineThickness
	 */
	public void setGridLineThickness(int gridLineThickness) {
		this.gridLineThickness = gridLineThickness;
		this.repaint();
	}

	/**
	 * Makes grid lines visible and updates the display.
	 * 
	 * @param newState
	 */
	public void showGridLines(boolean newState) {
		showGridLines = newState;
		this.repaint();
	}

	/**
	 * Clears the objects in this grid
	 */
	public void clear() {
		gridObjects.clear();
	}
	
	/**
	 * Returns the number of objects in this grid.
	 * @return
	 */
	public int getNumObjects() {
		return gridObjects.size();
	}
	
	/**
	 * Draws a GridObject, which knows its own location, and has an id that maps to a BufferedImage (OthelloController has the id -> image mapping) 
	 * @param g Graphics context for the current GridView
	 */
	private void drawCellObjects(Graphics g) {
		for(GridObject go : gridObjects) {
			if (go != null && go.getID() != 0) {
				g.drawImage(OthelloController.getPlayerImage(go.getID()), colToXCoord(go.getCol()) + imageMargin, rowToYCoord(go.getRow()) + imageMargin, cellSize - 2*imageMargin, cellSize - 2*imageMargin, null);
			}
		}
	}

	/**
	 * Converts a column value into an x-coordinate for the upper left corner of this cell
	 * @param col
	 * @return
	 */
	public int colToXCoord(int col) {
		return gridLineThickness + col * (cellSize + gridLineThickness);
	}

	/**
	 * Converts a row value into a y-coordinate for the upper left corner of this cell
	 * @param row
	 * @return
	 */
	public int rowToYCoord(int row) {
		return gridLineThickness + row * (cellSize + gridLineThickness);
	}

	/**
	 * Converts an x-coordinate into a column.
	 * @param x
	 * @return
	 */
	public int xCoordToCol(int x) {
		return x / (cellSize + gridLineThickness);
	}

	/**
	 * Converts a y-coordinate into a row.
	 * @param row
	 * @return
	 */
	public int yCoordToRow(int y) {
		return y / (cellSize + gridLineThickness);
	}

	/**
	 * Fills the GridView one by one in row major order
	 */
	public void fillNextSpot(GridObject go) {
		for (int r = 0; r < numRows; r++) {
			for (int c = 0; c < numCols; c++) {
				GridObject temp = new GridObject(r, c, go.getID());
				if (!gridObjects.contains(temp)) {
					gridObjects.add(temp);
					repaint();
					return;
				}
			}
		}
	}
	
	public void update(Observable o, Object arg) {

		// Clear all items
		if (arg == null) {
			gridObjects.clear();
		}
		// Clear the item at this coordinate
		else if (((GridObject)arg).getID() == 0) {
			gridObjects.remove(((GridObject)arg));
		}
		// Add an item
		else {
			gridObjects.add((GridObject) arg);
		}
		repaint();
	}

	public void addBoardListeners(MouseListener listener, MouseMotionListener listener2) {
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener2);
	}
	
	
	/**
	 * Handler method for repainting this component.
	 * 
	 * @param g
	 *            Graphics context for this component.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (bgImage != null) {
			g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null, null);
		}
		
		if (showGridLines) {		
			g.setColor(Color.black);
			// Draw horizontal gridlines
			for (int r = 0; r <= numRows; r++) {
				g.fillRect(0, r * (cellSize + gridLineThickness), getWidth(),
						gridLineThickness);
			}
	
			// Draw vertical gridlines
			for (int c = 0; c <= numCols; c++) {
				g.fillRect(c * (cellSize + gridLineThickness), 0,
						gridLineThickness, getHeight());
			}
		}

		// Draw cell objects
		drawCellObjects(g);
	}	
}
