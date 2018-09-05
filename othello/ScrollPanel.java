package othello;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;


public class ScrollPanel extends JPanel {

	private static final long serialVersionUID = 2440740773785470569L;
	private BufferedImage bgImage;
	private String topLine;
	private String bottomLine;
	private Image p1Image;
	private Image p2Image;
	
	public ScrollPanel() {
		
		topLine = "";
		
		try {
			bgImage = ImageIO.read(new File("images" + System.getProperty("file.separator") + "scroll.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void setVSMessage(String p1Name, String p2Name, Image p1Image, Image p2Image) {
		this.topLine = p1Name;
		this.bottomLine = p2Name;
		this.p1Image = p1Image;
		this.p2Image = p2Image;
	}
	

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
		g.setFont(new Font("Gabriola", Font.BOLD, 36));

		FontMetrics fm = g.getFontMetrics();
	    int x = (this.getWidth() - fm.stringWidth(topLine)) / 2;
	    int y = (fm.getAscent() + (this.getHeight() - (fm.getAscent() + fm.getDescent())) / 2);
	    g.drawString(topLine, x, y - 40);
	    g.drawImage(p1Image, x - 32 - 10, y - 64, 32, 32, null, null);
	    
	    x = (this.getWidth() - fm.stringWidth(bottomLine)) / 2;
	    g.drawString(bottomLine, x, y + 10);
	    g.drawImage(p2Image, x - 32 - 10, y - 16, 32, 32, null, null);

		g.setFont(new Font("Gabriola", Font.BOLD, 30));
	    x = (this.getWidth() - fm.stringWidth("vs")) / 2;
	    y = (fm.getAscent() + (this.getHeight() - (fm.getAscent() + fm.getDescent())) / 2);
	    
	    g.drawString("vs", x, y - 17);
	    
	}
	

}
