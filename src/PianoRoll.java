import javax.sound.midi.ShortMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import javax.imageio.ImageIO;
 
public class PianoRoll extends JPanel implements MouseListener, MouseMotionListener, Runnable {
	//Constantes
	final private int roll_border=0;
	final private int roll_x=0;
	final private int roll_y=0;
	private int roll_w;
	private int roll_h;
	final private Color borderDark=new Color(50,50,50);
	final private Color borderWhite=new Color(250,250,250);
	final private Color notePlaying=new Color(204,61,61);
	final private Color notePlayingFade=new Color(224,81,81,100);
	final private Color backRoll=new Color(245,235,245,180);
	final private Color backRollDisabled=new Color(215,205,215,180);
	final private Color verticalLines=new Color(190,190,190);
	final private Color verticalLinesDisabled=new Color(170,170,170);
	final private Color verticalLinesDark=new Color(140,140,140);
	final private Color verticalLinesDarkDisabled=new Color(120,120,120);
	final private Color horizontalLines=new Color(200,200,200);
	final private Color horizontalLinesDisabled=new Color(180,180,180);
	final private int Do0=24;
	final private String Names[]={"Do","Do#","Ré","Ré#","Mi","Fa","Fa#","Sol","Sol#","La","La","Si"};
	//Attributs
	private Robot robot; //Pour récupérer les pixels
	private int size_NoteON=1;
	private int state_modif=0; //0 = ajout de NoteON, 1 = modification de NoteOFF
	private int note_for_off=-1; //NoteON du NoteOFF draggué (pour modification de NoteOFF)
	private int next_note_for_off=-1; //NoteON suivante du NoteOFF draggué (pour modification de NoteOFF)
	private Point old_case_in_drag=new Point(-1,-1); //Ancienne case dragguée (pour modification de NoteOFF)
	private int last_button_pressed=-1;
	public int size=0; //Taille d'une case
	public int roll_piano_size=40+30;
	private int roll_touch_h=10;
	private int nbr_octaves=8;
	private Point note_playing=new Point(-1,-1); //Note jouée
	private int note_playing_value=-1; //Note jouée
	private String note_cur_name=new String(); //Note
	private Point note_cur=new Point(-1,-1); //Note survolée par la souris
	private boolean note_dragged=false;
	private int channel_attribuate=-1; //Channel du contenu attibué au piano roll (pour être édité)
	public int lect_bar=0; //Barre de lecture
	public Thread threadPlaying=null; //Thread de lectrue du contenu

	public MIDI_out output=null;
	public Keyboard keyboard=null;
	//Constructeur
	public PianoRoll(programme p,MIDI_out out,Keyboard kb) {
		this.output=out;
		this.keyboard=kb;
		try { this.robot=new Robot(); }
		catch(Exception e) {}
		//this.output.setPianoRoll(this);
		//Evénements clavier
		//this.addKeyListener(p);
	}
	//Rafraichissement de la fenêtre
	public void paintComponent(Graphics g) {
		this.roll_w=this.getWidth()-this.roll_border-this.roll_x-2;
		this.roll_h=this.getHeight()-this.roll_border-this.roll_y;
		this.size=(this.roll_w-this.roll_piano_size)/this.output.nbrTps;
		//Image de fond
		g.setColor(new Color(150,150,148));
		g.fillRect(0,0,this.getWidth(),this.getHeight());
		g.setColor((!this.output.getContentIsPlaying())?this.backRoll:this.backRollDisabled);
		g.fillRect(0,0,this.getWidth(),this.getHeight());
		//g.drawImage(imgBack,0,0,this.getWidth(),768,this);
		//Piano roll
        //Boucle sur la grille de notes
		for (int b=0;b < 2;b++) {
			for(int octave=0;octave < this.nbr_octaves;octave++) {
				for(int a=1; a <= 12;a++) {
		        	int y2=octave*12*this.roll_touch_h+this.roll_y+a*this.roll_touch_h+2;
	        		if (b == 0) {
	        			g.setColor((!this.output.getContentIsPlaying())?this.horizontalLines:this.horizontalLinesDisabled);
		        		g.drawLine(this.roll_x+b,y2,this.roll_w+this.roll_x-1+b,y2);
	        		}
	        	}
				g.setColor((b == 1)?this.borderDark:this.borderWhite);
	        	g.drawRect(this.roll_x+b,this.roll_y+b,this.roll_w,this.roll_h-3);
			}
			//Recherche si contenu attribué
			if (this.channel_attribuate > -1) {
				int max=this.output.Content[this.channel_attribuate].size();
				int old=0;
				Content msg=null;
				for(int cpt=0;cpt < max;cpt++) {
					msg=this.output.Content[this.channel_attribuate].get(cpt);
					//NoteON
					if (msg.event == ShortMessage.NOTE_ON) {
						g.setColor(this.notePlaying);
						int x=this.roll_x+this.roll_piano_size+this.size*(old+msg.delta)+b;
						int y=this.roll_h-this.roll_y-((msg.param1+1-this.Do0)*this.roll_touch_h)+b;
						g.fillRect(x,y,this.size,this.roll_touch_h);
						if (msg.length > 0) {
							g.setColor(this.notePlayingFade);
							g.fillRect(x+this.size,y,this.size*(msg.length-1),this.roll_touch_h);
						}
					}
					//NoteOFF
					if (msg.event == ShortMessage.NOTE_OFF) {
						g.setColor(Color.GREEN);
						int x=this.roll_x+this.roll_piano_size+this.size*(old+msg.delta)+b;
						int y=this.roll_h-this.roll_y-((msg.param1+1-this.Do0)*this.roll_touch_h)+b;
						g.fillRect(x,y,this.size,this.roll_touch_h);
						/*if (msg.length > 0) {
							g.setColor(this.notePlayingFade);
							g.fillRect(x-this.size*(msg.length-1),y,this.size*(msg.length-1),this.roll_touch_h);
						}*/
					}
					old+=msg.delta;
				}
			}
		}
		//Boucle sur le clavier du piano
		int offset=0;
		int offset_noires=0;
		for(int octave=0;octave < this.nbr_octaves;octave++) {
			offset=0;
			offset_noires=0;
			for(int a=1; a <= 12;a++) {
	        	int y2=octave*12*this.roll_touch_h+this.roll_y+a*this.roll_touch_h+3;
        		//Piano
            	g.setColor((!this.output.getContentIsPlaying())?this.borderWhite:(new Color(220,220,220)));
            	int line=this.roll_touch_h+(((a == 1) || (a == 4) || (a == 5) || (a == 7))?(this.roll_touch_h/2):this.roll_touch_h);
            	boolean noire=((a == 2) || (a == 4) || (a == 6) || (a == 8) || (a == 11))?true:false;
            	offset_noires=(a == 8)?this.roll_touch_h:0;
            	if (a <= 7) g.fillRect(this.roll_x+2,y2+offset-this.roll_touch_h,this.roll_piano_size-2,line-2);
            	if (noire) {
            		g.setColor(this.borderDark);
        			g.fillRect(this.roll_x+2,(int)(y2+offset_noires-2*this.roll_touch_h+this.roll_touch_h),(this.roll_piano_size/2)-2,this.roll_touch_h+1-2);
            	}
            	offset+=line-this.roll_touch_h;
        	}
		}
		g.setColor((!this.output.getContentIsPlaying())?this.verticalLines:this.verticalLinesDisabled);
		//Barres verticales
		for(int a=0;a < (this.output.nbrTps+1);a++) {
			if ((a%(this.output.nbrTps/8)) == 0) {
				g.setColor((!this.output.getContentIsPlaying())?this.verticalLinesDark:this.verticalLinesDarkDisabled);
				g.drawLine(this.roll_x+this.roll_piano_size+a*this.size,this.roll_y+2,this.roll_x+this.roll_piano_size+a*this.size,this.getHeight()-3);
				g.setColor((!this.output.getContentIsPlaying())?this.verticalLines:this.verticalLinesDisabled);
			}
			else g.drawLine(this.roll_x+this.roll_piano_size+a*this.size,this.roll_y+2,this.roll_x+this.roll_piano_size+a*this.size,this.getHeight()-3);
		}
		//Note jouée
		if (this.note_cur.y > -1) {
			//Etiquette de la note
			g.setFont(new Font("Arial",Font.PLAIN,10));
			int text_w=g.getFontMetrics().stringWidth(this.note_cur_name);
			g.setColor(this.borderWhite);
			g.fillRect(this.roll_x+2+5,this.note_cur.y-4,text_w,this.roll_touch_h+1-2+5);
			g.setColor(this.borderDark);
			g.drawRect(this.roll_x+2+5,this.note_cur.y-4,text_w,this.roll_touch_h+1-2+5);
			g.setColor(this.borderDark);
			g.drawString(this.note_cur_name,this.roll_x+2+2,this.note_cur.y+this.roll_touch_h-4);
			if ((this.note_playing.y > -1) && (this.channel_attribuate > -1)) {
				//Rectangle rouge
				g.setColor(this.notePlaying);
				g.fillRect(this.note_playing.x,this.note_playing.y,this.size,this.roll_touch_h);
			}
		}
		//Barre de lecture
		if (this.lect_bar > 0) {
			g.setColor(this.notePlaying);
			g.drawLine(this.roll_piano_size+this.lect_bar*this.size,0,this.roll_piano_size+this.lect_bar*this.size,this.roll_h-this.roll_y);
		}
    }
	//Chargement d'une piste dans l'éditeur
	public void LoadTrack(int channel) {
		this.channel_attribuate=channel;
		this.repaint();
	}
	//Ecouteurs
	public void mouseMoved(MouseEvent arg) {
		//Ecouteur activé si pas de lecture
		if (!this.output.getContentIsPlaying()) {
			//Calcul de la note à jouer
			int max=this.nbr_octaves*12;
			for(int a=0;a < max;a++) {
				if (arg.getY() > (this.roll_h-this.roll_y-this.roll_touch_h*(a+1))) {
					this.note_cur=new Point(this.Do0+a,this.roll_h-this.roll_y-((a+1)*this.roll_touch_h));
					this.note_cur_name=new String(" "+ this.Names[a%12] + " (" + (a/12) + ") ");
					//Si souris sur note
					Point note_playing=this.note_cur;
					note_playing.x=this.roll_piano_size+((arg.getX()-this.roll_x-this.roll_piano_size)/this.size)*this.size;
					int note_playing_value=this.Do0+a;
					//Si souris au bord droit (dur NoteOFF)
					if (this.output.ExistsMsg(this.channel_attribuate,(note_playing.x-this.roll_piano_size)/this.size+1,ShortMessage.NOTE_OFF,note_playing_value,0) >= 0) {
						if (arg.getX() >= (note_playing.x+this.size+1-3))
							this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
					}
					//Si souris au bord gauche (sur NoteON)
					else if (this.output.ExistsMsg(this.channel_attribuate,(note_playing.x-this.roll_piano_size)/this.size,ShortMessage.NOTE_ON,note_playing_value,100) >= 0) {
						this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
					else if (!this.getCursor().equals(Cursor.DEFAULT_CURSOR)) this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					
					
					this.repaint(this.roll_x,this.roll_y,this.roll_x+this.roll_piano_size,this.roll_h);
					return;
				}
			}
		}
	}
	public void mouseClicked(MouseEvent arg) {
		//Ecouteur activé si pas de lecture
		if (!this.output.getContentIsPlaying()) {
			//Bouton droit
			if (arg.getButton() == MouseEvent.BUTTON3) {
				//Calcul de la note à supprimer
				int max=this.nbr_octaves*12;
				for(int a=0;a < max;a++) {
					if (arg.getY() > (this.roll_h-this.roll_y-this.roll_touch_h*(a+1))) {
						Point note_cur=new Point(0,this.roll_h-this.roll_y-((a+1)*this.roll_touch_h));
						Point note_playing=note_cur;
						note_playing.x=this.roll_piano_size+((arg.getX()-this.roll_x-this.roll_piano_size)/this.size)*this.size;
						int note_playing_value=this.Do0+a;
						if (this.output.ExistsMsg(this.channel_attribuate,(note_playing.x-this.roll_piano_size)/this.size,ShortMessage.NOTE_ON,note_playing_value,100) >= 0) {
							this.output.RemoveMsg(this.channel_attribuate,(note_playing.x-this.roll_piano_size)/this.size,ShortMessage.NOTE_ON,note_playing_value,100);
						}
						Rectangle rect=this.getVisibleRect();
						this.repaint(rect.x,rect.y,rect.width,rect.height);
						return;
					}
				}
			}
		}
	}
	public void mousePressed(MouseEvent arg) {
		this.last_button_pressed=arg.getButton();
		//Ecouteur activé si pas de lecture
		if (!this.output.getContentIsPlaying()) {
			//Bouton gauche
			if (arg.getButton() == MouseEvent.BUTTON1) {
				//Calcul de la note à jouer
				int max=this.nbr_octaves*12;
				for(int a=0;a < max;a++) {
					if (arg.getY() > (this.roll_h-this.roll_y-this.roll_touch_h*(a+1))) {
						Point note_playing=this.note_cur;
						note_playing.x=this.roll_piano_size+((arg.getX()-this.roll_x-this.roll_piano_size)/this.size)*this.size;
						int note_playing_value=this.Do0+a;
						int x=(note_playing.x-this.roll_piano_size)/this.size+1;
						//Si souris au bord droit (sur NoteOFF)
						if (this.output.ExistsMsg(this.channel_attribuate,x,ShortMessage.NOTE_OFF,note_playing_value,0) >= 0) {
							if (arg.getX() >= (this.note_playing.x+this.size+1-3)) {
								this.state_modif=1;
								this.old_case_in_drag=new Point(x,note_playing_value);
								this.note_for_off=x-this.output.LengthMsg(this.channel_attribuate,x,ShortMessage.NOTE_OFF,note_playing_value,0);
								this.next_note_for_off=this.output.NextMsg(this.channel_attribuate,this.note_for_off,ShortMessage.NOTE_ON,note_playing_value);
								System.out.println("> fleche 1 : " + this.note_for_off + " " + this.next_note_for_off);
								return;
							}
						}
						//Si souris au bord gauche (sur NoteON)
						else {System.out.println("> fleche 2");
							this.note_cur=new Point(0,this.roll_h-this.roll_y-((a+1)*this.roll_touch_h));
							this.note_playing=this.note_cur;
							this.note_playing.x=this.roll_piano_size+((arg.getX()-this.roll_x-this.roll_piano_size)/this.size)*this.size;
							this.note_cur_name=new String(" "+ this.Names[a%12] + " (" + (a/12) + ") ");
							this.output.Note(this.channel_attribuate,this.Do0+a,100);
							this.note_playing_value=this.Do0+a;
							if (this.output.ExistsMsg(this.channel_attribuate,(this.note_playing.x-this.roll_piano_size)/this.size,ShortMessage.NOTE_ON,this.note_playing_value,100) >= 0) {
								this.size_NoteON=this.output.LengthMsg(this.channel_attribuate,(this.note_playing.x-this.roll_piano_size)/this.size,ShortMessage.NOTE_ON,this.note_playing_value,100);
								//this.output.RemoveMsg(this.channel_attribuate,(this.note_playing.x-this.roll_piano_size)/this.size,ShortMessage.NOTE_ON,this.note_playing_value,100);
							}
							//this.repaint(this.roll_x,this.roll_y,this.roll_x+this.roll_piano_size,this.roll_h);
							//this.repaint();
							Rectangle rect=this.getVisibleRect();
							this.repaint(rect.x,rect.y,rect.width,rect.height);
							return;
						}
					}
				}
			}
		}
	}
	public void mouseReleased(MouseEvent arg) {
		this.last_button_pressed=-1;
		//Ecouteur activé si pas de lecture
		if (!this.output.getContentIsPlaying()) {
			//Bouton gauche
			if (arg.getButton() == MouseEvent.BUTTON1) {
				if (this.state_modif == 1) { this.state_modif=0; this.old_case_in_drag=new Point(-1,-1); this.note_for_off=-1; this.next_note_for_off=-1; System.out.println("> fleche 0"); return; }
				if (this.note_dragged) {
					//Calcul de la note à jouer
					int max=this.nbr_octaves*12;
					for(int a=0;a < max;a++) {
						if (arg.getY() > (this.roll_h-this.roll_y-this.roll_touch_h*(a+0))) {
							this.output.Note(this.channel_attribuate,this.note_playing_value,200);
							this.note_dragged=false;
							break;
						}
					}
				}
				//Si piste sélectionnée
				if (this.output.editing) {
					this.keyboard.AddKey((int)Keyboard.char_a+this.keyboard.letter_hl,(this.channel_attribuate < 0)?0:this.channel_attribuate,ShortMessage.NOTE_ON,this.note_playing_value,100);
				}
				else {
					if (this.channel_attribuate > -1) {
						//Si souris sur les cases (ajout de la note dans le contenu)
						if ((arg.getX() > (this.roll_x+this.roll_piano_size)) && (arg.getX() < (this.roll_w+this.roll_x)) && (arg.getY() > this.roll_y) && (arg.getY() < (this.roll_h+this.roll_y))) {
							if (this.output.ExistsMsg(this.channel_attribuate,(this.note_playing.x-this.roll_piano_size)/this.size,ShortMessage.NOTE_ON,this.note_playing_value,100) < 0) {
								this.output.InsertMsg(this.channel_attribuate,(this.note_playing.x-this.roll_piano_size)/this.size,ShortMessage.NOTE_ON,this.note_playing_value,100,this.size_NoteON);
								if (this.channel_attribuate != 9)
									this.output.InsertMsg(this.channel_attribuate,(this.note_playing.x-this.roll_piano_size)/this.size+this.size_NoteON,ShortMessage.NOTE_OFF,this.note_playing_value,0);
							}
							else this.output.RemoveMsg(this.channel_attribuate,(this.note_playing.x-this.roll_piano_size)/this.size,ShortMessage.NOTE_ON,this.note_playing_value,100);
						}
					}
				}
				this.note_playing=new Point(-1,-1);
				this.note_playing_value=-1;
				this.size_NoteON=1;
				//this.repaint();
				Rectangle rect=this.getVisibleRect();
				this.repaint(rect.x,rect.y,rect.width,rect.height);
				return;
			}
		}
	}
	public void mouseEntered(MouseEvent arg) {
	}
	public void mouseExited(MouseEvent arg) {
		//Ecouteur activé si pas de lecture
		if (!this.output.getContentIsPlaying()) {
			this.note_cur=new Point(-1,-1);
			this.repaint(this.roll_x,this.roll_y,this.roll_x+this.roll_piano_size,this.roll_h);
		}
	}
	public void mouseDragged(MouseEvent arg) {
		//Ecouteur activé si pas de lecture
		if (!this.output.getContentIsPlaying()) {
			//Bouton gauche
			if (this.last_button_pressed == MouseEvent.BUTTON1) {
				Rectangle r = new Rectangle(arg.getX(), arg.getY(), 1, 1);
				scrollRectToVisible(r);
				//Calcul de la note à jouer
				int max=this.nbr_octaves*12;
				for(int a=0;a < max;a++) {
					if (arg.getY() > (this.roll_h-this.roll_y-this.roll_touch_h*(a+1))) {
						Color c=this.robot.getPixelColor(arg.getX(),arg.getY());
						System.out.println(c.getRGB());
						if (c.getRGB() != -3618616) {
							//Prolongation d'une note
							if (this.state_modif == 1) {
								Point note_playing=new Point(0,this.roll_h-this.roll_y-((a+1)*this.roll_touch_h));
								note_playing.x=this.roll_piano_size+((arg.getX()-this.roll_x-this.roll_piano_size)/this.size)*this.size;
								int x=(note_playing.x-this.roll_piano_size)/this.size+1;
								//this.output.RemoveMsg(this.channel_attribuate,this.old_case_in_drag.x,ShortMessage.NOTE_OFF,this.old_case_in_drag.y,0);
								int old_x=this.old_case_in_drag.x;
								this.old_case_in_drag.x=(x > this.note_for_off)?x:this.note_for_off;
								//Mise à jour
								if ((this.output.ExistsMsg(this.channel_attribuate,x,ShortMessage.NOTE_OFF,this.old_case_in_drag.y,0) < 0) && (this.old_case_in_drag.x > this.note_for_off)) {
									this.output.RemoveMsg(this.channel_attribuate,old_x,ShortMessage.NOTE_OFF,this.old_case_in_drag.y,0);
									this.output.InsertMsg(this.channel_attribuate,x,ShortMessage.NOTE_OFF,this.old_case_in_drag.y,0,this.old_case_in_drag.x-this.note_for_off);
									this.output.setLengthMsg(this.channel_attribuate,this.note_for_off,ShortMessage.NOTE_ON,this.old_case_in_drag.y,0,this.old_case_in_drag.x-this.note_for_off);
									this.output.setLengthMsg(this.channel_attribuate,this.old_case_in_drag.x,ShortMessage.NOTE_OFF,this.old_case_in_drag.y,0,this.old_case_in_drag.x-this.note_for_off);
								}
							}
							//Drag d'une note
							else {System.out.println("> fleche 3");
								this.output.RemoveMsg(this.channel_attribuate,(this.note_playing.x-this.roll_piano_size)/this.size,ShortMessage.NOTE_ON,this.note_playing_value,100);
								this.note_cur=new Point(0,this.roll_h-this.roll_y-((a+1)*this.roll_touch_h));
								this.note_playing=this.note_cur;
								this.note_playing.x=this.roll_piano_size+((arg.getX()-this.roll_x-this.roll_piano_size)/size)*size;
								this.note_cur_name=new String(" "+ this.Names[a%12] + " (" + (a/12) + ") ");
								if (this.note_playing_value != (this.Do0+a)) this.output.Note(this.channel_attribuate,this.Do0+a,100);
								this.note_playing_value=this.Do0+a;
								this.note_dragged=true;
							}
							//this.repaint(this.roll_x,this.roll_y,this.roll_x+this.roll_piano_size,this.roll_h);
							//this.repaint();
							Rectangle rect=this.getVisibleRect();
							this.repaint(rect.x,rect.y,rect.width,rect.height);
							return;
						}
					}
				}
			}
		}
	}
	//Thread de lecture
	@Override public void run() {
		while (true) {
			Rectangle rect=this.getVisibleRect();
			this.repaint(rect.x/*+this.roll_piano_size*/,rect.y,rect.width/*-this.roll_piano_size*/,rect.height);
			try { Thread.sleep(100); }
			catch(InterruptedException e) {}
		}
	}
}
