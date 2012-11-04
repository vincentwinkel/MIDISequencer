import java.awt.*;

import javax.swing.JScrollPane;

public class Scroll extends JScrollPane {
	//Constantes
	final private Color color=new Color(50,50,50);
	final private Color shadow=new Color(250,250,250);
	//Attributs
	//Constructeur
	public Scroll(Component c) {
		super(c);
	}
	//Rafraichissement de la fenêtre
	public void repaint(Graphics g) {
		//Image de fond
		Graphics2D g2d=(Graphics2D)g;
        GradientPaint gp=new GradientPaint(0,0,new Color(230,230,245),this.getWidth()/2,0,new Color(240,240,240),true);
        g2d.setPaint(gp);
        g2d.fillRect(0,0,this.getWidth(),this.getHeight());
    }
}
