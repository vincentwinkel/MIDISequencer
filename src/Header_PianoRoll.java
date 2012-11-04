import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;
import javax.swing.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
 
public class Header_PianoRoll extends JPanel {
	//Constantes
	final private Color shadow=new Color(250,250,250);
	//Attributs
	public int roll_piano_size=0;
	//Constructeur
	public Header_PianoRoll(int piano_size) {
		this.roll_piano_size=piano_size;
	}
	//Rafraichissement de la fenêtre
	public void paintComponent(Graphics g) {
		int roll_w=this.getWidth();
		int roll_h=this.getHeight();
		//Image de fond
		Graphics2D g2d=(Graphics2D)g;
        GradientPaint gp=new GradientPaint(0,0,new Color(216,216,216),0,this.getHeight(),new Color(180,180,178),true);
        g2d.setPaint(gp);
        g2d.fillRect(0,0,this.getWidth(),this.getHeight());
        g.setColor(new Color(100,100,100));
        g.drawLine(0,roll_h-1,roll_w,roll_h-1);
		//Texte
		roll_w-=this.roll_piano_size;
        g.setColor(this.shadow);
		for(int a=0;a < 8;a++) g.drawString("" + (a+1),this.roll_piano_size+a*roll_w/8,roll_h-5);
        g.setColor(new Color(50,50,50));
		for(int a=0;a < 8;a++) g.drawString("" + (a+1),this.roll_piano_size+a*roll_w/8,roll_h-6);
    }
}
