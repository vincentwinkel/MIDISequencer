//import java.awt.AWTEvent;
import javax.swing.*;
import java.awt.image.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.imageio.ImageIO;
 
public class Grid extends JPanel implements MouseListener, MouseMotionListener {
	//Constantes
	final public int nbr_min_tracks=1;
	final public int nbr_max_tracks=16;
	final private int grid_border=0;
	final private int grid_y=0;
	private int grid_w;
	private int grid_h;
	final public int grid_track_min_h=55;
	final public int grid_track_max_h=110;
	final public int grid_header_min_size=180;
	final public int grid_tracks_min_x=20;
	final public int grid_tracks_max_x=400;
	/*final private Color trackHeader=new Color(230,230,230);
	final private Color trackHeaderDark=new Color(200,200,200);
	final private Color trackHeaderSel=new Color(250,250,250);
	final private Color trackHeaderSelDark=new Color(210,210,210);*/
	final private Color trackHeader=new Color(240,240,240);
	final private Color trackHeaderDark=new Color(210,210,210);
	final private Color trackHeaderSel=new Color(240,240,250);
	final private Color trackHeaderSelDark=new Color(200,200,230);
	
	final private Color trackPair=new Color(215,215,225);
	final private Color trackImpair=new Color(200,200,210);
	final private Color trackSel=new Color(240,240,240);
	final private Color borderDark=new Color(100,100,100);
	final private Color borderWhite=new Color(255,255,255);
	final private Color curVol=new Color(180,180,180);
	final private Color modeOn=new Color(204,61,61);//Color(222,142,31);//Color(129,114,214);
	final private Color modeOff=new Color(50,50,50);
	final private Color shadow=new Color(250,250,250);
	final private Color trackAdd=new Color(200,200,200,80);
	//Attributs
	private int grid_x=0;
	public int nbr_tracks=this.nbr_min_tracks;
	public int selected_track=-1; //Piste sélectionnée (-1 = rien, 1 = Batterie, 2 = channel 0, etc)
	public int selected_channel=-1; //Channel sélectionné
	public int grid_track_h=this.grid_track_min_h;
	private int grid_header_size=this.grid_header_min_size;
	private boolean resizeHeader=false;
	private boolean resizeTracks=false;
	private boolean setVolume=false;
	private Class_to_Grid infosPerTrack[]=new Class_to_Grid[16]; //Channel + nom par piste (-1 = rien, 0-15)
	private MIDI_instruments inst=new MIDI_instruments();
	public MIDI_out output=null;
	public PianoRoll roll=null;
	public Toolbar toolbar=null;
	public Keyboard keyboard=null;
	JPopupMenu popup=new JPopupMenu();
	//Constructeur
	public Grid(programme p,Toolbar toolbar,PianoRoll roll,MIDI_out out,Keyboard kb) {
		this.toolbar=toolbar;
		this.roll=roll;
		this.output=out;
		this.keyboard=kb;
		this.infosPerTrack[0]=new Class_to_Grid(9,0,"Kit de batterie MIDI");
		for(int a=1;a < 16;a++) this.infosPerTrack[a]=new Class_to_Grid(-1,0,"Aucun");
		//Evénements clavier
		//this.addKeyListener(p);
		
		//Menu contextuel
		JMenuItem item=new JMenuItem("Supprimer");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		//Finalisation
		this.popup.add(item);
		final Grid g=this;
		java.awt.event.MouseAdapter action=new MouseAdapter() {
			public void mouseReleased(MouseEvent arg) {
				if (arg.getButton() == MouseEvent.BUTTON3) {
					//Si souris sur les boucles
					
					//if ()
					popup.show(g,arg.getX(),arg.getY());
				}
			}
		};
		this.addMouseListener(action);
	}
	//Rafraichissement de la fenêtre
	public void paintComponent(Graphics g) {
		this.grid_w=this.getWidth()-this.grid_border-this.grid_x-2;
		this.grid_h=this.getHeight()-this.grid_border-this.grid_y;
		//Image de fond
		Graphics2D g2d=(Graphics2D)g;
        GradientPaint gp=new GradientPaint(0,0,new Color(230,230,245),this.getWidth()/2,0,new Color(240,240,240),true);
        g2d.setPaint(gp);
        g2d.fillRect(0,0,this.getWidth(),this.getHeight());
        //Panneau gauche
		//Image de fond du grid
        g2d.setPaint(gp);
        g2d.fillRect(this.grid_x,0,this.getWidth(),this.getHeight());
		//Grid des pistes
		for (int b=0;b < 2;b++) {
			g.setColor((b == 0)?this.borderDark:this.borderWhite);
        	g.drawRect(this.grid_x+b,this.grid_y+b,this.grid_w,this.grid_h);
        	//Boucle sur les pistes
        	int max=(this.nbr_tracks < this.nbr_max_tracks)?(this.nbr_tracks+1):this.nbr_tracks;
        	for(int a=1;a <= max;a++) {
        		int height=this.grid_y+a*this.grid_track_h+b;
        		//Pistes réelles
        		if (a <= this.nbr_tracks) {
	        		//Fond
	        		if (b == 0) {
	        			if (a == this.selected_track) {
	        				//Fond header
	        				gp=new GradientPaint(0,0,new Color(255,255,255),0,this.grid_track_h/2,this.trackHeaderSelDark,true);
	        		        g2d.setPaint(gp);
	        				g.fillRect(this.grid_x+1+b,this.grid_y+(a-1)*this.grid_track_h+1,this.grid_header_size-1,this.grid_track_h-1);
	            			//Fond piste
	            			g2d.setPaint(gp);
	                		g.fillRect(this.grid_x+this.grid_header_size+b,this.grid_y+(a-1)*this.grid_track_h+1,this.grid_w-this.grid_header_size,this.grid_track_h-1);
	            		}
	        			else {
	        				//Fond header
	            			gp=new GradientPaint(0,0,this.trackHeader,0,this.grid_track_h/2,this.trackHeaderDark,true);
	        		        g2d.setPaint(gp);
	            			g.fillRect(this.grid_x+1+b,this.grid_y+(a-1)*this.grid_track_h+1,this.grid_header_size-1,this.grid_track_h-1);
	            			//Fond piste
	            			GradientPaint gp2=new GradientPaint(0,0,this.trackHeaderSel,this.getWidth(),0,this.trackHeaderSelDark,true);
	        				g2d.setPaint(gp2);
	                		g.fillRect(this.grid_x+this.grid_header_size+b,this.grid_y+(a-1)*this.grid_track_h+1,this.grid_w-this.grid_header_size,this.grid_track_h-1);
	        			}
	        		}
	        		//Header
	        		g.setColor((b == 0)?this.borderDark:this.borderWhite);
	        		int width=this.grid_x+this.grid_header_size+b;
	        		g.drawLine(width,this.grid_y+this.grid_track_h*(a-1)+1,width,this.grid_y+this.grid_track_h*(a-1)+1+this.grid_track_h-2);
	        		int y=this.grid_y+(a-1)*this.grid_track_h+15;
	        		if (b == 0) {
	        			//Texte
	        			g.setFont(g.getFont().deriveFont(Font.BOLD));
        				g.setColor(this.shadow);
	        			g.drawString(this.infosPerTrack[a-1].name,this.grid_x+10,y+1);
        				g.setColor(this.modeOff);
	        			g.drawString(this.infosPerTrack[a-1].name,this.grid_x+10,y);
	        			//Mute / Solo
	        			if (this.output.mutePerChannel[(a == 1)?9:(a-2)] == 2) {
	        				g.setFont(g.getFont().deriveFont(Font.BOLD));
	        				g.setColor(this.shadow);
	            			g.drawString("[Solo]",this.grid_x+10,y+16);
	        				g.setColor(this.modeOn);
	            			g.drawString("[Solo]",this.grid_x+10,y+15);
	        			}
	        			else {
	        				g.setFont(g.getFont().deriveFont(Font.PLAIN));
	        				g.setColor(this.shadow);
	            			g.drawString(" Solo ",this.grid_x+10,y+16);
	        				g.setColor(this.modeOff);
	            			g.drawString(" Solo ",this.grid_x+10,y+15);
	        			}
	        			if (this.output.mutePerChannel[(a == 1)?9:(a-2)] == 0) {
	        				g.setFont(g.getFont().deriveFont(Font.BOLD));
	        				g.setColor(this.shadow);
	            			g.drawString("[Mute]",this.grid_x+50,y+16);
	        				g.setColor(this.modeOn);
	            			g.drawString("[Mute]",this.grid_x+50,y+15);
	        			}
	        			else {
	        				g.setFont(g.getFont().deriveFont(Font.PLAIN));
	        				g.setColor(this.shadow);
	            			g.drawString(" Mute ",this.grid_x+50,y+16);
	        				g.setColor(this.modeOff);
	            			g.drawString(" Mute ",this.grid_x+50,y+15);
	        			}
	        			//Volume
	        			g.setColor(new Color((int)(100+this.output.volPerChannel[(a == 1)?9:(a-2)]*1.2),200,100+this.output.volPerChannel[(a == 1)?9:(a-2)]/2));
	        			g.fillRect(this.grid_x+10,y+this.grid_track_h/2-5,this.grid_header_size-20,10);
	        			g.setColor(new Color((int)(0+this.output.volPerChannel[(a == 1)?9:(a-2)]*1.2),100,0+this.output.volPerChannel[(a == 1)?9:(a-2)]/2));
	        			g.drawLine(this.grid_x+10,y+this.grid_track_h/2-4,this.grid_x+10+this.grid_header_size-20,y+this.grid_track_h/2-4);
	        			g.drawLine(this.grid_x+11,y+this.grid_track_h/2-4,this.grid_x+11,y+this.grid_track_h/2-5+10);
	        			g.setColor(this.borderDark);
	        			g.drawRect(this.grid_x+10,y+this.grid_track_h/2-5,this.grid_header_size-20,10);
	        			int xCur=(this.output.volPerChannel[(a == 1)?9:(a-2)]*(this.grid_header_size-20)/127)+this.grid_x+10;
	        			g.fillOval(xCur-4,y+this.grid_track_h/2-7,8,14);
	        			GradientPaint gp2=new GradientPaint(0,0,new Color(150,150,148),0,this.grid_track_h/2,new Color(230,230,230),true);
	        			g2d.setPaint(gp2);
	        			g.fillOval(xCur-3,y+this.grid_track_h/2-6,6,12);
	        			g.setColor(this.borderDark);
	        		}
	        		//Contenu des pistes
	        		if (this.output.Content[this.getChannelWithTrack(a)].size() > 0) {
	        			y-=15;
	        			int line=0;
	        			g.drawRect(this.grid_x+this.grid_header_size+5+b,y+5+line*(this.grid_track_h/2)+b,25,this.grid_track_h/2-10);
	        			int x_[]={
	        					this.grid_x+this.grid_header_size+13+b,
	        					this.grid_x+this.grid_header_size+13+b,
	        					this.grid_x+this.grid_header_size+23+b
	        				},
	        				y_[]={
	        					y+8+line*(this.grid_track_h/2)+b,
	        					y+18+line*(this.grid_track_h/2)+b,
	        					y+13+line*(this.grid_track_h/2)+b
	        				};
	        			g.fillPolygon(x_,y_,x_.length);
	        			line++;
	        		}
	        		//Bordure
	        		g.drawLine(this.grid_x+b,height,this.grid_w+b+this.grid_x-1,height);
        		}
        		//Piste à ajouter
        		else {
        			if (b == 0) {
        				//Fond
        				g.setColor(this.trackAdd);
                		g.fillRect(this.grid_x+b,this.grid_y+(a-1)*this.grid_track_h+1,this.grid_w,this.grid_track_h-2);
                		//Texte
                		g.setColor(this.curVol);
                		g.setFont(g.getFont().deriveFont(Font.PLAIN));
                		String text=new String("Cliquez ici pour ajouter une piste");
                		g.drawString(text,this.grid_x+(this.grid_w-g.getFontMetrics().stringWidth(text))/2,this.grid_y+(a-1)*this.grid_track_h+1+this.grid_track_h/2+4);
        			}
        			//Bordure
        			g.setColor((b == 0)?this.trackAdd:this.borderWhite);
        			g.drawLine(this.grid_x+b,height,this.grid_w+b+this.grid_x-1,height);
        		}
        	}
		}
    }
	public void saveImage(Component component, File destination){
		BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);

		Graphics2D g2d = image.createGraphics();
		
		if(!component.isOpaque()){
			Color bg = component.getBackground();
			bg = (bg.getAlpha() < 255) ? new Color(bg.getRed(), bg.getGreen(), bg.getBlue()) : bg;
			
			Color color = g2d.getColor();
			g2d.setColor(bg);
			g2d.fillRect(0, 0, component.getWidth(), component.getHeight());
			g2d.setColor(color);
		}
		
		component.paint(g2d);
		g2d.dispose();
		
		try {
			ImageIO.write(image, "jpeg", destination);
		} catch (IOException e) {      
			e.printStackTrace();
		} 
	}
	//Récupération du channel selon la piste
	public int getChannelWithTrack() {
		return this.getChannelWithTrack(this.selected_track);
	}
	public int getChannelWithTrack(int track) {
		if (track < 0) return -1;
		return this.infosPerTrack[track-1].channel;
	}
	//Affichage de la liste d'instruments
	public String ShowInstrumentsList(String title,int id) {
		return (String)JOptionPane.showInputDialog(this,"Choisissez un instrument MIDI.",title,JOptionPane.INFORMATION_MESSAGE,null,this.inst.list,this.inst.list[id]);
	}
	//Ajout d'une piste
	public void AddTrack() {
		
		//File file=new File("moche_qui_pue.jpg");
		//this.saveImage(this,file);
		
		
		if (this.nbr_tracks >= this.nbr_max_tracks) JOptionPane.showMessageDialog(this,"Vous avez atteint le nombre maximum de pistes.","Erreur",JOptionPane.ERROR_MESSAGE);
		else {
			String name=this.ShowInstrumentsList("Ajout d'une piste",0);
			if (name != null) {
				//Recherche du premier channel disponible à attribuer à la nouvelle piste
				int chan=-1;
				boolean stop=true;
				for(int a=0;((a < 16) && (stop));a++) {
					chan=a;
					stop=false;
					for(int b=0;((b < 16) && (!stop));b++) {
						if (a == this.infosPerTrack[b].channel) {
							stop=true;
							break;
						}
					}
					if (!stop) break;
				}
				this.infosPerTrack[this.nbr_tracks].name=name;
				this.selected_channel=chan;
				this.infosPerTrack[this.nbr_tracks].channel=chan;
				//Envoi du message MIDI
				for(int a=0;a < this.inst.list.length;a++) {
					if (this.inst.list[a] == name) {
						this.output.Instrument(chan,a);
						this.infosPerTrack[this.nbr_tracks].instrument=a;
						break;
					}
				}
				this.nbr_tracks++;
				this.selected_track=this.nbr_tracks;
				//Resize si nécessaire
				int nbr_tracks=(this.nbr_tracks < this.nbr_max_tracks)?(this.nbr_tracks+1):this.nbr_tracks;
				if ((nbr_tracks*this.grid_track_h) > this.getHeight()) {
					this.setPreferredSize(new Dimension(0,nbr_tracks*this.grid_track_h));
					this.revalidate();
					this.scrollRectToVisible(new Rectangle(0,nbr_tracks*this.grid_track_h,10,10));
				}
				this.repaint();
			}
		}
	}
	//Suppression d'une piste
	public boolean RemoveTrack(boolean all) {
		boolean res=true; //Valeur de retour
		if (this.nbr_tracks <= this.nbr_min_tracks) JOptionPane.showMessageDialog(this,"Vous avez atteint le nombre minimum de pistes.","Erreur",JOptionPane.ERROR_MESSAGE);
		else {
			//Supprimer une piste
			if (!all) {
				if (this.selected_track <= this.nbr_min_tracks) {
					if (this.selected_track < 1) JOptionPane.showMessageDialog(this,"Veuillez sélectionner la piste à supprimer.","Erreur",JOptionPane.ERROR_MESSAGE);
					else JOptionPane.showMessageDialog(this,"Cette piste ne peut pas être supprimée.","Erreur",JOptionPane.ERROR_MESSAGE);
				}
				else {
					int option=JOptionPane.showConfirmDialog(this,"Etes-vous sûr(e) de vouloir supprimer cette piste ?","Question",JOptionPane.OK_OPTION,JOptionPane.QUESTION_MESSAGE);
					if ((option != JOptionPane.NO_OPTION) && (option != JOptionPane.CANCEL_OPTION) && (option != JOptionPane.CLOSED_OPTION)) {
						this.output.ClearChannel(this.selected_channel);
						//Suppression de la piste
						for(int a=this.selected_track;a < this.nbr_tracks;a++) {
							this.infosPerTrack[a]=this.infosPerTrack[a+1];
						}
						this.infosPerTrack[this.nbr_tracks]=new Class_to_Grid(-1,0,"");
						this.selected_track--;
						this.selected_channel=this.getChannelWithTrack();
						this.nbr_tracks--;
						//Resize si nécessaire
						int nbr_tracks=(this.nbr_tracks < this.nbr_max_tracks)?(this.nbr_tracks+1):this.nbr_tracks;
						if ((nbr_tracks*this.grid_track_h) < this.getHeight()) {
							this.setPreferredSize(new Dimension(0,nbr_tracks*this.grid_track_h));
							this.revalidate();
						}
						this.repaint();
					}
				}
			}
			//Tout supprimer
			else {
				for(int a=this.selected_channel;a < this.nbr_tracks;a++) {
					this.infosPerTrack[a]=this.infosPerTrack[a+1];
				}
				this.infosPerTrack[this.nbr_tracks]=new Class_to_Grid(-1,0,"");
				this.output.ClearChannel(this.selected_channel);
				this.selected_track=this.nbr_tracks;
				this.selected_channel=this.getChannelWithTrack();
				this.nbr_tracks--;
				//Resize si nécessaire
				int nbr_tracks=(this.nbr_tracks < this.nbr_max_tracks)?(this.nbr_tracks+1):this.nbr_tracks;
				if ((nbr_tracks*this.grid_track_h) < this.getHeight()) {
					this.setPreferredSize(new Dimension(0,nbr_tracks*this.grid_track_h));
					this.revalidate();
				}
				this.repaint();
			}
		}
		if (this.nbr_tracks <= this.nbr_min_tracks) res=false;
		return res;
	}
	//Ecouteurs
	public void mouseMoved(MouseEvent arg) {
		//Si souris sur resize vertical des pistes
		if ((arg.getX() > (this.grid_x-3)) && (arg.getX() < (this.grid_x+3)) && (this.grid_x > 0)) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			return;
		}
		//Si souris dans le grid
		if ((arg.getX() > this.grid_x) && (arg.getX() < (this.grid_w+this.grid_x)) && (arg.getY() > this.grid_y) && (arg.getY() < (this.grid_h+this.grid_y))) {
			int end_tracks_y=this.grid_y+this.grid_track_h*this.nbr_tracks;
			//Si souris sur une piste
			if (arg.getY() < end_tracks_y) {
				//Si souris sur resize vertical du header
				if ((arg.getX() > (this.grid_x+this.grid_header_size-3)) && (arg.getX() < (this.grid_x+this.grid_header_size+3))) {
					this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
					return;
				}
				//Boucle sur les pistes
				for(int a=1;a <= this.nbr_tracks;a++) {
					//Si souris sur un composant d'une piste
					if (arg.getY() < this.grid_y+a*this.grid_track_h) {
						int x=this.grid_x+10;
						int x2=this.grid_header_size-10;
						int y=this.grid_y+(a-1)*this.grid_track_h+15+this.grid_track_h/2-5;
						int y2=y+10;
						int y3=this.grid_y+(a-1)*this.grid_track_h+19;
						int y4=y3-16;
						//Si souris sur volume
						if ((arg.getX() > x) && (arg.getX() < x2) && (arg.getY() > y) && (arg.getY() < y2)) {
							this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							return;
						}
						//Si souris sur mode Solo
						if ((arg.getX() > x) && (arg.getX() < (x+31)) && (arg.getY() > y3) && (arg.getY() < (y3+14))) {
							this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							return;
						}
						//Si souris sur mode Mute
						if ((arg.getX() > (x+40)) && (arg.getX() < (x+40+31)) && (arg.getY() > y3) && (arg.getY() < (y3+14))) {
							this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							return;
						}
						//Si souris sur instrument
						if ((a > 1) && (arg.getX() > x) && (arg.getX() < x2) && (arg.getY() > y4) && (arg.getY() < (y4+14))) {
							this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							return;
						}
					}
				}
			}
			//Si souris sur la piste "Ajouter"
			else if ((this.nbr_tracks < this.nbr_max_tracks) && (arg.getY() > end_tracks_y) && (arg.getY() < (end_tracks_y+this.grid_track_h))) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}
		}
		if (!this.getCursor().equals(Cursor.DEFAULT_CURSOR)) this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	public void mouseClicked(MouseEvent arg) {
	}
	public void mousePressed(MouseEvent arg) {
		//Bouton gauche
		if (arg.getButton() == MouseEvent.BUTTON1) {
			this.selected_track=-1;
			this.selected_channel=-1;
			int end_tracks_y=this.grid_y+this.grid_track_h*this.nbr_tracks;
			//Si souris sur resize vertical des pistes
			if ((arg.getX() > (this.grid_x-3)) && (arg.getX() < (this.grid_x+3)) && (this.grid_x > 0)) {
				this.resizeTracks=true;
			}
			//Si souris dans le grid
			if ((arg.getX() > this.grid_x) && (arg.getX() < (this.grid_w+this.grid_x)) && (arg.getY() > this.grid_y) && (arg.getY() < (this.grid_h+this.grid_y))) {
				//Si souris sur une piste
				if (arg.getY() < end_tracks_y) {
					for(int a=1;a <= this.nbr_tracks;a++) {
						if (arg.getY() < this.grid_y+a*this.grid_track_h) {
							this.selected_track=a;
							this.selected_channel=this.getChannelWithTrack();
							break;
						}
					}
					int x=this.grid_x+10;
					int x2=this.grid_header_size-10;
					int y=this.grid_y+(this.selected_track-1)*this.grid_track_h+15+this.grid_track_h/2-5;
					int y2=y+10;
					int y3=this.grid_y+(this.selected_track-1)*this.grid_track_h+19;
					int y4=y3-16;
					//Si souris sur volume
					if ((arg.getX() > x) && (arg.getX() < x2) && (arg.getY() > y) && (arg.getY() < y2)) {
						this.output.Volume(this.selected_channel,(127*(arg.getX()-x))/(x2-x));
						this.setVolume=true;
						//this.repaint(this.grid_x,this.grid_y,this.grid_w,this.grid_y+this.nbr_tracks*this.grid_track_h);
					}
					//Si souris sur mode Solo
					else if ((arg.getX() > x) && (arg.getX() < (x+31)) && (arg.getY() > y3) && (arg.getY() < (y3+14))) {
						this.output.ToggleMuteSolo(this.selected_channel,2);
					}
					//Si souris sur mode Mute
					else if ((arg.getX() > (x+40)) && (arg.getX() < (x+40+31)) && (arg.getY() > y3) && (arg.getY() < (y3+14))) {
						this.output.ToggleMuteSolo(this.selected_channel,0);
					}
					//Si souris sur instrument
					else if ((this.selected_track > 1) && (arg.getX() > x) && (arg.getX() < x2) && (arg.getY() > y4) && (arg.getY() < (y4+14))) {
						String name=this.ShowInstrumentsList("Changement d'instrument",this.infosPerTrack[this.selected_track-1].instrument);
						if (name != null) {
							for(int a=0;a < this.inst.list.length;a++) {
								if (this.inst.list[a] == name) {
									this.output.Instrument(this.selected_channel,a);
									this.infosPerTrack[this.selected_track-1].instrument=a;
									break;
								}
							}
							this.infosPerTrack[this.selected_track-1].name=name;
						}
					}
					//Si souris sur resize vertical du header
					else if ((arg.getX() > (this.grid_x+this.grid_header_size-3)) && (arg.getX() < (this.grid_x+this.grid_header_size+3))) {
						this.resizeHeader=true;
					}
				}
				//Si souris sur la piste "Ajouter"
				else if ((this.nbr_tracks < this.nbr_max_tracks) && (arg.getY() > end_tracks_y) && (arg.getY() < (end_tracks_y+this.grid_track_h))) {
					this.AddTrack();
					this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
			this.roll.LoadTrack(this.selected_channel);
			this.repaint(this.grid_x,this.grid_y,this.grid_w,this.grid_y+this.nbr_tracks*this.grid_track_h);
		}
	}
	public void mouseReleased(MouseEvent arg) {
		//Bouton gauche
		if (arg.getButton() == MouseEvent.BUTTON1) {
			this.resizeHeader=false;
			this.resizeTracks=false;
			this.setVolume=false;
		}
	}
	public void mouseEntered(MouseEvent arg) {
	}
	public void mouseExited(MouseEvent arg) {
	}
	public void mouseDragged(MouseEvent arg) {
		//Bouton gauche
		//if (arg.getButton() == MouseEvent.BUTTON1) {
			//Si resize vertical des pistes
			if (this.resizeTracks) {
				this.grid_x=arg.getX();
				if (arg.getX() < (this.grid_tracks_min_x)) this.grid_x=this.grid_tracks_min_x;
				else if (arg.getX() > this.grid_tracks_max_x) this.grid_x=this.grid_tracks_max_x;
				this.repaint();//this.grid_x,this.grid_y,this.grid_w,this.grid_y+this.nbr_tracks*this.grid_track_h);
				return;
			}
			//Si piste sélectionnée
			if (this.selected_track != -1) {
				//Si resize vertical du header
				if (this.resizeHeader) {
					this.grid_header_size=arg.getX()-this.grid_x;
					if (arg.getX() < (this.grid_x+this.grid_header_min_size)) this.grid_header_size=this.grid_header_min_size;
					else if (arg.getX() > (this.grid_x+this.grid_w-20)) this.grid_header_size=this.grid_w-20;
					this.repaint();//this.grid_x,this.grid_y,this.grid_w,this.grid_y+this.nbr_tracks*this.grid_track_h);
					return;
				}
				int x=this.grid_x+10;
				int x2=this.grid_header_size-10;
				int y=this.grid_y+(this.selected_track-1)*this.grid_track_h+15+this.grid_track_h/2-5;
				int y2=y+10;
				//Si souris sur volume
				if (this.setVolume) {
					//this.setCursor((Cursor)Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(32,32,BufferedImage.TRANSLUCENT),new Point(0,0),"curseurInvisble"));
					if (this.output.Volume(this.selected_channel,(127*(arg.getX()-x))/(x2-x)))
						this.repaint(x-4,y-4,x2-x+8,y2-y+8);
					return;
				}
			}
		//}
	}
	class Class_to_Grid {
		//Attributs
		public int channel=0;
		public int instrument=0;
		public String name=new String();
		//Constructeur avec paramètres
		public Class_to_Grid(int c,int i,String n) {
			this.channel=c;
			this.instrument=i;
			this.name=n;
		}
	}
	/*protected void processEvent(AWTEvent e) {
		super.processEvent(e);
		System.out.println("grid: "+(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner())+" >> "+e.toString());
	}*/
}
