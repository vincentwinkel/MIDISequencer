import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
 
public class RaccKeys extends JPanel implements MouseListener, MouseMotionListener, PropertyChangeListener {
	//Constantes
	static final public int char_a=97;//KeyEvent.VK_A;
	final private Color color=new Color(50,50,50);
	final private Color modeOn=new Color(204,61,61);
	final private Color shadow=new Color(250,250,250);
	//Attributs
	public MIDI_out output=null;
	public Keyboard keyboard=null;
	//Constructeur
	public RaccKeys(MIDI_out out,Keyboard kb) {
		this.output=out;
		this.keyboard=kb;
	}
	//Rafraichissement de la fenêtre
	public void paintComponent(Graphics g) {
		//Image de fond
		Graphics2D g2d=(Graphics2D)g;
        GradientPaint gp;
        if (this.keyboard.letter_hl > -1) gp=new GradientPaint(0,0,new Color(220,220,255),this.getWidth()/2,0,new Color(230,230,245),true);
        else gp=new GradientPaint(0,0,new Color(230,230,245),this.getWidth()/2,0,new Color(240,240,240),true);
        g2d.setPaint(gp);
        g2d.fillRect(0,0,this.getWidth(),this.getHeight());
		//Touches clavier
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		g.setColor(this.shadow);
		g.drawString("Touches clavier :",10,15+1);
		g.setColor(this.color);
		g.drawString("Touches clavier :",10,15);
		g.setFont(g.getFont().deriveFont(Font.PLAIN));
		g.setColor(this.shadow);
		for(int val=0;val < 26;val++) {
			char key=(char)(val+65);
			g.drawString("" + key,10,15+(val+1)*15+1);
			g.drawString(": " + this.keyboard.getMsgKey(val+RaccKeys.char_a),25,15+(val+1)*15+1);
		}
		g.setColor(this.color);
		for(int val=0;val < 26;val++) {
			char key=(char)(val+65);
			if (val == this.keyboard.letter_hl) {
				g.setColor(this.modeOn);
				g.setFont(g.getFont().deriveFont(Font.BOLD));
			}
			g.drawString("" + key,10,15+(val+1)*15);
			g.drawString(": " + this.keyboard.getMsgKey(val+RaccKeys.char_a),25,15+(val+1)*15);
			if (val == this.keyboard.letter_hl) {
				g.setColor(this.color);
				g.setFont(g.getFont().deriveFont(Font.PLAIN));
			}
		}
    }
	//Bound
	public void propertyChange(PropertyChangeEvent evt) {
		Rectangle rect=this.getVisibleRect();
		this.repaint(rect.x,rect.y,rect.width,rect.height);
	}
	//Ecouteurs
	public void mouseMoved(MouseEvent arg) {
		//Si souris sur les lettres
		int letter_hl=(arg.getY()-15)/15;
		if ((letter_hl >= 0) && (letter_hl < 26) && (arg.getY() > 15)) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			return;
		}
		if (!this.getCursor().equals(Cursor.DEFAULT_CURSOR)) this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	public void mouseClicked(MouseEvent arg) {
		//Bouton gauche
		if (arg.getButton() == MouseEvent.BUTTON1) {
			//Si souris sur les lettres
			int letter_hl=(arg.getY()-15)/15;
			if ((letter_hl >= 0) && (letter_hl < 26) && (arg.getY() > 15)) {
				this.keyboard.letter_hl=(letter_hl == this.keyboard.letter_hl)?-1:letter_hl;
				this.output.editing=(this.keyboard.letter_hl == -1)?false:true;
				Rectangle rect=this.getVisibleRect();
				this.repaint(rect.x,rect.y,rect.width,rect.height);
				return;
			}
		}
	}
	public void mousePressed(MouseEvent arg) {
	}
	public void mouseReleased(MouseEvent arg) {
	}
	public void mouseEntered(MouseEvent arg) {
	}
	public void mouseExited(MouseEvent arg) {
	}
	public void mouseDragged(MouseEvent arg) {
	}
}
