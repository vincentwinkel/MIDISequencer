import java.awt.*;

import javax.swing.JButton;
 
public class Button extends JButton {
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
	public int type=-1;
	//Constructeur
	public Button(programme p,int type) {
		this.type=type;
		//Evénements clavier
		//this.addKeyListener(p);
	}
	//Rafraichissement de la fenêtre
	public void paintComponent(Graphics g) {
		Graphics2D g2d=(Graphics2D)g;
		int w_=this.getWidth()/4;
		int h_=this.getHeight()/4;
        //Bouton PLAY
        if (this.type == Button.PLAY) {
        	//Fond
        	GradientPaint gp=new GradientPaint(0,0,new Color(196,216,196),0,this.getHeight(),new Color(150,150,168),true);
    	    g2d.setPaint(gp);
    	    g2d.fillRect(0,0,this.getWidth(),this.getHeight());
    	    //Symbole
        	int x_[]={w_,w_,this.getWidth()-w_};
			int y_[]={h_,this.getHeight()-h_,this.getHeight()/2};
        	g.setColor(this.backPlay);
			g.fillPolygon(x_,y_,x_.length);
        	g.setColor(this.borderPlay);
			g.drawPolygon(x_,y_,x_.length);
        }
        //Bouton STOP
        else if (this.type == Button.STOP) {
        	//Fond
        	GradientPaint gp=new GradientPaint(0,0,new Color(216,196,196),0,this.getHeight(),new Color(150,150,168),true);
    	    g2d.setPaint(gp);
    	    g2d.fillRect(0,0,this.getWidth(),this.getHeight());
    	    //Symbole
        	g.setColor(this.backStop);
			g.fillRect(w_,h_,this.getWidth()-w_*2,this.getHeight()-w_*2);
        	g.setColor(this.borderStop);
			g.drawRect(w_,h_,this.getWidth()-w_*2,this.getHeight()-w_*2);
        }
        //Bouton REPEAT OFF
        else if (this.type == Button.REPEAT_OFF) {
        	//Fond
        	GradientPaint gp=new GradientPaint(0,0,new Color(216,196,216),0,this.getHeight(),new Color(150,150,168),true);
    	    g2d.setPaint(gp);
    	    g2d.fillRect(0,0,this.getWidth(),this.getHeight());
    	    //Symbole
        	g.setColor(this.backRepeatOFF);
        	g2d.setStroke(new BasicStroke(4));
			g.drawArc(w_+2,h_+2,this.getWidth()-w_*2-3,this.getHeight()-h_*2-3,-58,300);
        	g.setColor(this.borderRepeatOFF);
        	g2d.setStroke(new BasicStroke(1));
			g.drawArc(w_,h_,this.getWidth()-w_*2,this.getHeight()-h_*2,-60,300);
			g.drawArc(w_+4,h_+4,this.getWidth()-w_*2-8,this.getHeight()-h_*2-8,-60,300);
			int x_[]={this.getWidth()-w_-10,this.getWidth()-w_+1,this.getWidth()-w_-10};
			int y_[]={this.getHeight()-h_-12,this.getHeight()-h_-1,this.getHeight()-h_};
        	g.setColor(this.backRepeatOFF);
			g.fillPolygon(x_,y_,x_.length);
        	g.setColor(this.borderRepeatOFF);
			g.drawPolygon(x_,y_,x_.length);
        }
        //Bouton REPEAT ON
        else if (this.type == Button.REPEAT_ON) {
        	//Fond
        	GradientPaint gp=new GradientPaint(0,0,new Color(120,120,138),0,this.getHeight(),new Color(206,186,206),true);
    	    g2d.setPaint(gp);
    	    g2d.fillRect(0,0,this.getWidth(),this.getHeight());
    	    //Symbole
    	    g.setColor(this.backRepeatON);
        	g2d.setStroke(new BasicStroke(4));
			g.drawArc(w_+2,h_+2,this.getWidth()-w_*2-3,this.getHeight()-h_*2-3,-58,300);
        	g.setColor(this.borderRepeatON);
        	g2d.setStroke(new BasicStroke(1));
			g.drawArc(w_,h_,this.getWidth()-w_*2,this.getHeight()-h_*2,-60,300);
			g.drawArc(w_+4,h_+4,this.getWidth()-w_*2-8,this.getHeight()-h_*2-8,-60,300);
			int x_[]={this.getWidth()-w_-10,this.getWidth()-w_+1,this.getWidth()-w_-10};
			int y_[]={this.getHeight()-h_-12,this.getHeight()-h_-1,this.getHeight()-h_};
        	g.setColor(this.backRepeatON);
			g.fillPolygon(x_,y_,x_.length);
        	g.setColor(this.borderRepeatON);
			g.drawPolygon(x_,y_,x_.length);
        }
        //Contour
        g.setColor(new Color(122,138,153));
        g.drawRect(0,0,this.getWidth()-1,this.getHeight()-1);
    }
}
