import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
 
public class Toolbar extends JPanel implements Runnable {
	//Constantes
	private String DIR=new String("http://winky.fr/test_java/");
	private Color shadow=new Color(240,240,240);
	//Attributs
	private Image equalizer[]={
		null,null,null,null,null,null,null,null,null,null,
		null,null,null,null,null,null,null,null,null,null,
		null
	};
	private int effet_size=417;
	private Point cpt_coord=new Point(this.effet_size+30,5);
	private String cpt=new String("00:00");
	private int real_cpt=0;
	public Thread threadPlaying=null;
	public Thread threadEqualizer=null;
	public String tempo=new String("120 bpm");
	private int id_gif=-1;
	private int cpt_w=0;
	//Constructeur
	public Toolbar() {
		/*try {
			for(int a=0;a < 21;a++)
				this.equalizer[a]=ImageIO.read((new URL(DIR + "img/equalizer_" + (a+1) + ".png")).openStream());
		}
		catch(IOException e) {
			JOptionPane.showMessageDialog(this,"Erreur de téléchargement des ressources.","Erreur",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}*/
	}
	//Rafraichissement de la fenêtre
	public void paintComponent(Graphics g){
		//Image de fond
		Graphics2D g2d=(Graphics2D)g;
        GradientPaint gp=new GradientPaint(0,0,new Color(196,196,196),0,this.getHeight(),new Color(150,150,148),true);
        GradientPaint gp2=new GradientPaint(0,0,new Color(150,150,148),0,this.getHeight(),new Color(196,196,196),true);
        g2d.setPaint(gp);
        g2d.fillRect(0,0,this.getWidth(),this.getHeight());
        //Effets de fond
        g2d.setPaint(gp2);
		g.fillRect(0,0,this.effet_size,this.getHeight()-4);
		g.fillRect(this.effet_size,0,50,this.getHeight()/2);
        g.fillArc(this.effet_size-25,2,50,this.getHeight()-6,0,-90);
        g2d.fillRect(this.effet_size+50,0,this.getWidth()-this.effet_size,2);
        g2d.setPaint(gp);
        g.fillArc(this.effet_size+25,2,50,this.getHeight()-6,180,-90);
        g.setColor(new Color(200,200,200));
        for(int a=0;a < 2;a++) {
        	g.drawLine(0,this.getHeight()-5-a,this.effet_size,this.getHeight()-5-a);
	        g.drawArc(this.effet_size-25,2,50,this.getHeight()-7-a,0,-90);
	        g.drawArc(this.effet_size+25,2,50,this.getHeight()-6-a,180,-90);
        }
        //Compteur
		g.setFont(new Font("Arial",Font.PLAIN,40));
		this.cpt_w=g.getFontMetrics().stringWidth("00:00")+12*2;
		g.setColor(new Color(200,200,200));
		g.fillRoundRect(this.cpt_coord.x+2,this.cpt_coord.y,this.cpt_w,50+2,50,50);
		g.setColor(new Color(100,100,100));
		g.fillRoundRect(this.cpt_coord.x,this.cpt_coord.y,this.cpt_w,50,50,50);
		int cpt_x=this.cpt_w-12-g.getFontMetrics().stringWidth(this.cpt);
		g.setColor(new Color(255,150,150));
		g.drawString(this.cpt,this.cpt_coord.x+cpt_x-1,this.cpt_coord.y+40-1);
		g.setColor(new Color(204,61,61));
		g.drawString(this.cpt,this.cpt_coord.x+cpt_x,this.cpt_coord.y+40);
		//Tempo
		g.setFont(new Font("Arial",Font.BOLD,12));
		g.setColor(this.shadow);
		int tempo_x=700+(105-g.getFontMetrics().stringWidth(this.tempo))/2;
		g.drawString(this.tempo,tempo_x,50+1);
		g.setColor(new Color(50,50,50));
		g.drawString(this.tempo,tempo_x,50);
		//Equalizer
		//if (this.id_gif > -1) g.drawImage(this.equalizer[this.id_gif],0,0,this.effet_size,this.getHeight(),null);
    }
	//Remise à zéro du compteur
	public void RazCpt() {
		this.id_gif=-1;
		this.real_cpt=0;
		this.cpt=new String("00:00");
	}
	//Incrémentation du compteur
	public void IncCpt() {
		this.real_cpt++;
		int min=this.real_cpt/60;
		int sec=this.real_cpt%60;
		this.cpt=new String(((min < 10)?"0":"") + min + ":" + ((sec < 10)?"0":"") + sec);
	}
	//Thread de lecture
	@Override public void run() {
		while (true) {
			//Equalizer
			if (Thread.currentThread() == this.threadEqualizer) {
				//Compteur du gif
				this.id_gif++;
				if (this.id_gif >= 21) this.id_gif=0;
				try { Thread.sleep(100); }
				catch(InterruptedException e) {}
				this.repaint(0,0,this.effet_size,this.getHeight());
			}
			//Lecture
			else if (Thread.currentThread() == this.threadPlaying) {
				try { Thread.sleep(1000); }
				catch(InterruptedException e) {}
				this.IncCpt();
				this.repaint(this.cpt_coord.x,this.cpt_coord.y,this.cpt_w,50);
			}
		}
	}
}
