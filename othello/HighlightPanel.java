package othello;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JComponent;

public class HighlightPanel extends JComponent {

	private static final long serialVersionUID = -5473786350256457600L;
	private Color highlightColor = new Color(255, 255, 0, 130);
	private BufferedImage highlightImage;
	private Location dropLoc;
	private ArrayList<Location> captureLocs = null;
	private int cellSize;
	private int xOffset;
	private int yOffset;
	
	public HighlightPanel(int cellSize) {
		this.cellSize = cellSize;
	}

	public HighlightPanel(int cellSize, BufferedImage image) {
		this.cellSize = cellSize;
		this.highlightImage = image;
	}
	
	public BufferedImage getHighlightImage() {
		return highlightImage;
	}
	
	public void setHighlightImage(BufferedImage highlightImage) {
		this.highlightImage = highlightImage;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Draw ghost of Piece about to be dropped, if any.
		if (dropLoc != null) {
			if (highlightImage == null) {
				g.setColor(highlightColor);
				g.fillOval(xOffset + dropLoc.getCol() * (cellSize + 1)+1, yOffset + dropLoc.getRow() * (cellSize + 1)+1, cellSize, cellSize);
			}
			else
				g.drawImage(highlightImage, xOffset + dropLoc.getCol() * (cellSize + 1)+1, yOffset + dropLoc.getRow() * (cellSize + 1)+1, cellSize, cellSize, null);
		}

		// Highlight captured pieces, if any
		if (captureLocs != null && captureLocs.size() > 0) {
			for (Location loc : captureLocs) {
				g.setColor(highlightColor);
				g.fillRect(xOffset + loc.getCol() * (cellSize+1)+1, yOffset + loc.getRow() * (cellSize+1)+1, cellSize, cellSize);
			}
		}
	}

	 public void setCaptureLocs(ArrayList<Location> locs) {
		 captureLocs = locs;
	 }
	
	public Location getDropLoc() {
		return dropLoc;
	}

	public void setDropLoc(Location dropLoc) {
		this.dropLoc = dropLoc;
	}
	
	public void setDropLoc(int xOffset, int yOffset, Location dropLoc) {
		this.dropLoc = dropLoc;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

}
