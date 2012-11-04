import java.awt.event.*;
import java.beans.*;
import javax.sound.midi.ShortMessage;
import javax.swing.*;

public class Keyboard extends AbstractAction {
	//Constantes
	static final public int char_a=97;//KeyEvent.VK_A;
	//Attributs
	private JRootPane root=null;
	private JLabel label=null;
	private MIDI_out output=null;
	private Button play=null;
	public int letter_hl=-1;
	
	private PropertyChangeSupport pcs=new PropertyChangeSupport(this);
	
	public Class_to_Keys Keys[]={
		null,null,null,null,null,null,null,null,null,null,
		null,null,null,null,null,null,null,null,null,null,
		null,null,null,null,null,null
	}; //Touches clavier
	//Constructeur
	public Keyboard(JRootPane root,JLabel label,Button play,MIDI_out out) {
		this.root=root;
		this.label=label;
		this.play=play;
		this.output=out;
		//Remplissage du tableau vide
		int max=this.Keys.length;
		for(int a=0;a < max;a++) this.Keys[a]=new Class_to_Keys(false,false,-1,-1,-1,-1);

		this.AddKey((int)'s',9,ShortMessage.NOTE_ON,36,100); //Kick
		this.AddKey((int)'d',9,ShortMessage.NOTE_ON,40,100); //Snare
		this.AddKey((int)'m',9,ShortMessage.NOTE_ON,42,100); //Hi-Hat fermé
		this.AddKey((int)'l',9,ShortMessage.NOTE_ON,46,100); //Hi-Hat ouvert
		this.AddKey((int)'z',9,ShortMessage.NOTE_ON,49,100); //Crash 1
	}
	//Ajout d'une touche
	public void AddKey(int key,int channel,int event,int p1,int p2) {
		key-=Keyboard.char_a; //Compatibilité tableau
		int	nKey=Math.min(this.Keys.length,Math.max(0,key));
		this.Keys[key]=new Class_to_Keys(true,false,channel,event,p1,p2);
		String keyString1=new String((char)(key+Keyboard.char_a)+"_active");
		String keyString2=new String((char)(key+Keyboard.char_a)+"_release");
		key+=KeyEvent.VK_A; //Compatibilité KeyEvent
		//Attribution de la touche
		this.root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke((char)key,0,false),keyString1);
		this.root.getActionMap().put(keyString1,this);
		//Attribution du release
		this.root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke((char)key,0,true),keyString2);
		this.root.getActionMap().put(keyString2,new KeyRelease(this));
		//Rafraichissement de la fenêtre des racourcis
		this.pcs.firePropertyChange(null,null,null);
	}
	//Suppression d'une touche
	public void RemoveKey(int key) {
		key-=Keyboard.char_a;
		int	nKey=Math.min(this.Keys.length,Math.max(0,key));
		this.Keys[key]=new Class_to_Keys(false,false,-1,-1,-1,-1);
		this.root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke((char)(key+Keyboard.char_a)));
		//Rafraichissement de la fenêtre des racourcis
		this.pcs.firePropertyChange(null,null,null);
	}
	//Accesseur de l'attribut isActive
	public boolean isActive(int key) {
		key-=Keyboard.char_a;
		int	nKey=Math.min(this.Keys.length,Math.max(0,key));
		return this.Keys[key].isActive;
	}
	//Accesseur de l'attribut isPlayed
	public boolean isPlayed(int key) {
		key-=Keyboard.char_a;
		int	nKey=Math.min(this.Keys.length,Math.max(0,key));
		return this.Keys[key].isPlayed;
	}
	//Récupérer le message d'une touche
	public Object[] getKey(int key) {
		key-=Keyboard.char_a;
		int	nKey=Math.min(this.Keys.length,Math.max(0,key));
		return new Object[]{this.Keys[key].channel,this.Keys[key].event,this.Keys[key].param1,this.Keys[key].param2};
		//return new Object[]{9,ShortMessage.NOTE_ON,36,100};
	}
	//Récupérer le message d'une touche
	public String getMsgKey(int key) {
		key-=Keyboard.char_a;
		int nKey=Math.min(this.Keys.length,Math.max(0,key));
		if (!this.Keys[key].isActive) return "";
		//Evénement
		String event=new String("...");
		switch(this.Keys[key].event) {
			case ShortMessage.NOTE_ON: new String(event="Note ON"); break;
			case ShortMessage.NOTE_OFF: new String(event="Note OFF"); break;
			case ShortMessage.PROGRAM_CHANGE: new String(event="Programme Change"); break;
			case ShortMessage.CONTROL_CHANGE: new String(event="Control Change"); break;
			case ShortMessage.PITCH_BEND: new String(event="Pitch Bend"); break;
			case ShortMessage.START: new String(event="Start"); break;
			case ShortMessage.STOP: new String(event="Stop"); break;
		}
		return new String("[" + event + "] #" + this.Keys[key].channel + ": " + this.Keys[key].param1 + " " + this.Keys[key].param2);
	}
	//Bound
	public void addPropertyChangeListener(PropertyChangeListener l) {
		this.pcs.addPropertyChangeListener(l);
	}
	public void removePropertyChangeListener(PropertyChangeListener l) {
		this.pcs.removePropertyChangeListener(l);
	}
	//Ecouteur
	public void actionPerformed(ActionEvent arg) {
		int key=(int)arg.getActionCommand().charAt(0);
		switch(key) {
			//Play / Pause
			case KeyEvent.VK_SPACE:{
				if (this.output.getContentIsPlaying()) {
					this.output.Stop();
					this.play.type=Button.PLAY;
				}
				else {
					this.output.Play(play);
					this.play.type=Button.STOP;
				}
				this.play.repaint();
				break;
			}
			//Suppression racourci
			case KeyEvent.VK_DELETE:{
				if (this.letter_hl > -1) this.RemoveKey(Keyboard.char_a+this.letter_hl);
				break;
			}
			//Touches clavier
			default:{
				if ((this.isActive(key)) && (!this.isPlayed(key))) {
					this.Keys[key-Keyboard.char_a].isPlayed=true;
					long startTime=System.nanoTime();
					try {
						Class.forName("MIDI_out").getMethod("SendShortMsg",new Class[]{int.class,int.class,int.class,int.class}).invoke(this.output,this.getKey(key));
					}
					catch(Exception e) { this.label.setText("Erreur logicielle."); }
					this.label.setText(this.getMsgKey(key) + " " + (System.nanoTime()-startTime));
				}
			}
		}
	}
	//Classe des touches
	class Class_to_Keys {
		public boolean isActive=false; //true si la touche est activée, false sinon
		public boolean isPlayed=false; //true si la touche est enfoncée, false sinon
		private int channel=-1; //Channel
		private int event=-1; //Evénement MIDI
		private int param1=-1; //Paramètre 1
		private int param2=-1; //Paramètre 2
		public Class_to_Keys(boolean active,boolean played,int channel,int event,int p1,int p2) {
			this.isActive=active;
			this.isPlayed=played;
			this.channel=channel;
			this.event=event;
			this.param1=p1;
			this.param2=p2;
		}
	}
	//Ecouteur pour les relachements de touches
	class KeyRelease extends AbstractAction {
		//Attributs
		private Keyboard parent=null;
		//Constructeur
		public KeyRelease(Keyboard p) {
			this.parent=p;
		}
		//Ecouteur
		public void actionPerformed(ActionEvent arg) {
			int key=(int)arg.getActionCommand().charAt(0);
			//Touches clavier
			if (this.parent.isActive(key)) {
				this.parent.Keys[key-Keyboard.char_a].isPlayed=false;
				long startTime=System.nanoTime();
				//Si NoteON, envoi de NoteOFF
				Object tab[]=this.parent.getKey(key);
				if ((new Integer(tab[1].toString())) == ShortMessage.NOTE_ON) tab[1]=new Integer(ShortMessage.NOTE_OFF);
				try {
					Class.forName("MIDI_out").getMethod("SendShortMsg",new Class[]{int.class,int.class,int.class,int.class}).invoke(this.parent.output,tab);
				}
				catch(Exception e) { this.parent.label.setText("Erreur logicielle."); }
				this.parent.label.setText(this.parent.getMsgKey(key) + " " + (System.nanoTime()-startTime));
			}
		}
	}
}
