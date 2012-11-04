import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;

import javax.swing.JSlider;
 
public class Slider extends JSlider {
	//Constantes
	static final public int PLAY=0;
	static final public int STOP=1;
	static final public int REPEAT_OFF=2;
	static final public int REPEAT_ON=3;
	final private Color backPlay=new Color(61,194,61);
	final private Color borderPlay=new Color(1,164,1);
	final private Color backStop=new Color(194,61,61);
	final private Color borderStop=new Color(164,1,1);
	final private Color backRepeatOFF=new Color(194,61,194);
	final private Color borderRepeatOFF=new Color(164,1,164);
	final private Color backRepeatON=new Color(204,71,204);
	final private Color borderRepeatON=new Color(164,1,164);
	//Attributs
	//Constructeur
	public Slider() {
	}
	//Rafraichissement de la fenêtre
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
    }
}
