package othello;

public class GridObject {

	private int row;
	private int col;
	private int id;

	public GridObject(int r, int c, int id) {
		row = r;
		col = c;
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public boolean equals(Object other) {
		return ((GridObject)other).hashCode() == this.hashCode();
	}

	public int hashCode() {
		return 10*row + col;
	}	
}
