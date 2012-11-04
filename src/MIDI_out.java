import javax.sound.midi.*;
import javax.swing.JOptionPane;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.*;

public class MIDI_out implements Runnable {
	//Attributs
	private Receiver receiver=null; //Receveur MIDI IN
	private boolean contentIsPlaying=false; //true si le contenu est joué, false sinon
	private Thread threadPlaying=null; //Thread de lectrue du contenu
	private int cptPlay=0; //Compteur de lecture
	private int Delta=0; //Delta absolu de lecture
	public boolean Repeat=true; //Ture si mode repeat, false sinon
	private int lastNotePerChannel[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; //Dernière note jouée par channel (utile lors d'un stop)
	public int volPerChannel[]={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1}; //Volume par channel
	public ArrayList<Content> Content[]=new ArrayList[16]; //Contenu : 16 channels
	private Button buttonStart=null; //Bouton à mettre à jour lorsque la lecture est terminée
	public int mutePerChannel[]={1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}; //Mute par channel (0 = mute, 1 = normal, 2 = solo)
	private int channelSolo=-1; //ID du channel Solo
	public int tempo=120; //Tempo du contenu
	public int nbrTps=64; //Nombre de temps par mesure
	private PianoRoll roll=null;
	private Toolbar toolbar=null;
	public boolean editing=false; //true si mode édition (affectation racourcis clavier), false sinon
	
	private int DEBUG=0;
	//Constructeur
	public MIDI_out() {
		for(int a=0;a < 16;a++) {
			this.Content[a]=new ArrayList<Content>();
		}
		int oct=0;//8*Integer.parseInt(this.octave.getText());
		int duree=4;
		int chan=0;
		int t=2;
		this.InsertMsg(chan,t*0,ShortMessage.NOTE_ON,oct+45,100,t*duree);
		this.InsertMsg(chan,t*duree,ShortMessage.NOTE_OFF,oct+45,0,t*duree);
		this.InsertMsg(chan,t*duree,ShortMessage.NOTE_ON,oct+45,100,t*duree);
		this.InsertMsg(chan,t*2*duree,ShortMessage.NOTE_OFF,oct+45,0,t*duree);
		this.InsertMsg(chan,t*2*duree,ShortMessage.NOTE_ON,oct+45,100,t*duree);
		this.InsertMsg(chan,t*3*duree,ShortMessage.NOTE_OFF,oct+45,0,t*duree);
		this.InsertMsg(chan,t*3*duree,ShortMessage.NOTE_ON,oct+60,100,t*duree);
		this.InsertMsg(chan,t*4*duree,ShortMessage.NOTE_OFF,oct+60,0,t*duree);
		this.InsertMsg(chan,t*4*duree,ShortMessage.NOTE_ON,oct+50,100,t*duree);
		this.InsertMsg(chan,t*5*duree,ShortMessage.NOTE_OFF,oct+50,0,t*duree);
		this.InsertMsg(chan,t*5*duree,ShortMessage.NOTE_ON,oct+80,100,t*duree);
		this.InsertMsg(chan,t*6*duree,ShortMessage.NOTE_OFF,oct+80,0,t*duree);
		this.InsertMsg(chan,t*6*duree,ShortMessage.NOTE_ON,oct+70,100,t*duree);
		this.InsertMsg(chan,t*7*duree,ShortMessage.NOTE_OFF,oct+70,0,t*duree);
		this.InsertMsg(chan,t*7*duree,ShortMessage.NOTE_ON,oct+40,100,t*duree);
		this.InsertMsg(chan,t*8*duree,ShortMessage.NOTE_OFF,oct+40,0,t*duree);
		
		this.InsertMsg(9,t*0,ShortMessage.NOTE_ON,36,100);
		this.InsertMsg(9,t*0,ShortMessage.NOTE_ON,42,100);
		this.InsertMsg(9,t*4,ShortMessage.NOTE_ON,42,100);
		this.InsertMsg(9,t*8,ShortMessage.NOTE_ON,40,100);
		this.InsertMsg(9,t*8,ShortMessage.NOTE_ON,42,100);
		this.InsertMsg(9,t*12,ShortMessage.NOTE_ON,36,100);
		this.InsertMsg(9,t*12,ShortMessage.NOTE_ON,42,100);
		this.InsertMsg(9,t*16,ShortMessage.NOTE_ON,42,100);
		this.InsertMsg(9,t*20,ShortMessage.NOTE_ON,36,100);
		this.InsertMsg(9,t*20,ShortMessage.NOTE_ON,42,100);
		this.InsertMsg(9,t*24,ShortMessage.NOTE_ON,40,100);
		this.InsertMsg(9,t*24,ShortMessage.NOTE_ON,42,100);
		this.InsertMsg(9,t*28,ShortMessage.NOTE_ON,42,100);
		int inst=56;
		this.InsertMsg(9,t*6,ShortMessage.NOTE_ON,inst,100);
		this.InsertMsg(9,t*12,ShortMessage.NOTE_ON,inst,100);
		this.InsertMsg(9,t*2,ShortMessage.NOTE_ON,inst,100);
		this.InsertMsg(9,t*20,ShortMessage.NOTE_ON,inst,100);
		this.InsertMsg(9,t*24,ShortMessage.NOTE_ON,inst,100);
		this.InsertMsg(9,t*26,ShortMessage.NOTE_ON,inst,100);
		this.InsertMsg(9,t*30,ShortMessage.NOTE_ON,inst,100);
	}
	public void setPianoRoll(PianoRoll r) {
		this.roll=r;
	}
	public void setToolbar(Toolbar t) {
		this.toolbar=t;
	}
	//Ouverture du port MIDI IN
	public boolean Open() {
		boolean res=true; //Valeur de retour
		try {
			this.receiver=MidiSystem.getReceiver();
		}
		catch(MidiUnavailableException e) { res=false; }
		return res;
	}
	//Fermeture du port
	public void Close() {
		this.receiver.close();
	}
	//Accesseur de : contentIsPlaying
	public boolean getContentIsPlaying() {
		return this.contentIsPlaying;
	}
	//Jouer le contenu
	public boolean Play(Button buttonStart) {
		this.buttonStart=buttonStart;
		boolean res=true; //Valeur de retour
		if (!this.contentIsPlaying) {
			this.contentIsPlaying=true;
			//Remise à zéro des notes jouées
			for(int a=0;a < 16;a++) this.lastNotePerChannel[a]=0;
			//Démarrage du thread de lecture
			this.threadPlaying=new Thread(this);
			this.threadPlaying.setPriority(Thread.MAX_PRIORITY);
			this.threadPlaying.start();
			//Roll
			this.roll.threadPlaying=new Thread(this.roll);
			this.roll.threadPlaying.start();
			//Compteur
			this.toolbar.threadPlaying=new Thread(this.toolbar);
			this.toolbar.threadPlaying.start();
			this.toolbar.threadEqualizer=new Thread(this.toolbar);
			this.toolbar.threadEqualizer.start();
		}
		else res=false;
		return res;
	}
	//Stopper le contenu
	public boolean Stop() {
		boolean res=true; //Valeur de retour
		if (this.contentIsPlaying) {
			this.contentIsPlaying=false;
			try {
				if (Thread.currentThread() != this.threadPlaying) this.threadPlaying.stop();
				this.roll.threadPlaying.stop();
				this.roll.lect_bar=0;
				this.roll.repaint();
				this.toolbar.threadPlaying.stop();
				this.toolbar.threadEqualizer.stop();
				this.toolbar.RazCpt();
				this.toolbar.repaint();
			}
			catch(java.lang.NullPointerException e) { res=false; }
			//Arrêt des notes en cours
			for(int a=0;a < 16;a++) this.SendShortMsg(a,new Content(0,ShortMessage.NOTE_OFF,this.lastNotePerChannel[a],0));
		}
		else res=false;
		return res;
	}
	//Jouer une note
	public boolean Note(int channel,int note,int duree) {
		boolean res=true; //Valeur de retour
		int	nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int	nKey=Math.min(127,Math.max(0,note)); //MIDI key number
		int	nVelocity=100; //Vélocité
		ShortMessage onMessage=new ShortMessage();
		ShortMessage offMessage=new ShortMessage();
		try {
			onMessage.setMessage(ShortMessage.NOTE_ON,nChannel,nKey,nVelocity);
			offMessage.setMessage(ShortMessage.NOTE_OFF,nChannel,nKey,0);
		}
		catch(InvalidMidiDataException e) { res=false; }
		this.receiver.send(onMessage,-1);
		try { Thread.sleep(duree); }
		catch(InterruptedException e) { res=false; }
		this.receiver.send(offMessage,-1);
		return res;
	}
	public boolean NoteOn(int channel,int note) {
		boolean res=true; //Valeur de retour
		int	nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int	nKey=Math.min(127,Math.max(0,note)); //MIDI key number
		int	nVelocity=100; //Vélocité
		ShortMessage onMessage=new ShortMessage();
		try {
			onMessage.setMessage(ShortMessage.NOTE_ON,nChannel,nKey,nVelocity);
		}
		catch(InvalidMidiDataException e) { res=false; }
		this.receiver.send(onMessage,-1);
		return res;
	}
	public boolean NoteOff(int channel,int note) {
		boolean res=true; //Valeur de retour
		int	nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int	nKey=Math.min(127,Math.max(0,note)); //MIDI key number
		ShortMessage onMessage=new ShortMessage();
		ShortMessage offMessage=new ShortMessage();
		try {
			offMessage.setMessage(ShortMessage.NOTE_OFF,nChannel,nKey,0);
		}
		catch(InvalidMidiDataException e) { res=false; }
		this.receiver.send(onMessage,-1);
		return res;
	}
	//Changement d'instrument
	public boolean Instrument(int channel,int value) {
		boolean res=true; //Valeur de retour
		int	nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int nValue=Math.min(127,Math.max(0,value)); //Valeur
		ShortMessage Message=new ShortMessage();
		try {
			Message.setMessage(ShortMessage.PROGRAM_CHANGE,nChannel,nValue,0);
		}
		catch(InvalidMidiDataException e) { res=false; }
		this.receiver.send(Message,-1);
		return res;
	}
	//Réglage du volume
	public boolean Volume(int channel,int value) {
		boolean res=true; //Valeur de retour
		int	nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int nValue=Math.min(127,Math.max(0,value)); //Valeur
		this.volPerChannel[channel]=nValue;
		ShortMessage Message=new ShortMessage();
		try {
			Message.setMessage(ShortMessage.CONTROL_CHANGE,nChannel,7,nValue);
		}
		catch(InvalidMidiDataException e) { res=false; }
		this.receiver.send(Message,-1);
		return res;
	}
	//Réglage du Mute / Solo
	public void ToggleMuteSolo(int channel,int value) {
		int	nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int nValue=Math.min(2,Math.max(0,value)); //Valeur
		int oldMode=this.mutePerChannel[nChannel];
		//Si mode ajouté
		if (oldMode != nValue) {
			if (nValue == 2) {
				for(int a=0;a < 16;a++) {
					if (this.mutePerChannel[a] == 2) this.mutePerChannel[a]=1;
					if (this.contentIsPlaying) this.SendShortMsg(a,new Content(0,ShortMessage.NOTE_OFF,this.lastNotePerChannel[a],0));
				}
				this.channelSolo=nChannel;
			}
			this.mutePerChannel[nChannel]=nValue;
		}
		//Si mode supprimé
		else {
			this.mutePerChannel[nChannel]=1;
			if (nValue == 2) this.channelSolo=-1;
		}
	}
	//Envoi d'un message au port MIDI IN
	public boolean SendShortMsg(int c,int ev,int p1,int p2) {
		boolean res=true; //Valeur de retour
		ShortMessage Message=new ShortMessage();
		try {
			Message.setMessage(ev,c,p1,p2);
		}
		catch(InvalidMidiDataException e) { res=false; }
		this.receiver.send(Message,-1);
		return res;
	}
	private boolean SendShortMsg(int channel,Content msg) {
		boolean res=true; //Valeur de retour
		ShortMessage Message=new ShortMessage();
		try {
			Message.setMessage(msg.event,channel,msg.param1,msg.param2);
		}
		catch(InvalidMidiDataException e) { res=false; }
		this.receiver.send(Message,-1);
		return res;
	}
	//Ajout d'un message MIDI au contenu (delta absolu)
	public void InsertMsg(int channel,int id,int e,int p1,int p2) {
		this.InsertMsg(channel,id,e,p1,p2,-1);
	}
	public void InsertMsg(int channel,int id,int e,int p1,int p2,int l) {
		int nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int old=0;
		int pos=0; //Position du message dans le contenu
		boolean pos_exists=false; //true si la position existe (mettre delta = 0), false sinon
		int size=this.Content[nChannel].size(); //Taille du contenu
		if (id > 0) {
			//Recherche de la position du message à insérer (delta absolu)
			for(pos=0;pos < size;pos++) {
				old+=this.Content[nChannel].get(pos).delta;
				if (id <= old) {
					if (id < old) old-=this.Content[nChannel].get(pos).delta;
					else {
						pos_exists=true;
						pos++;
					}
					break;
				}
			}
		}
		//Insertion du message
		this.Content[nChannel].add(pos,new Content((pos_exists)?0:(id-old),e,p1,p2,l));
		//Modifications de message suivant
		if (pos < (size)) this.Content[nChannel].get(pos+1).delta-=(pos_exists)?0:(id-old);
	}
	//Suppression d'un message MIDI du contenu (delta absolu) + suppression du NoteOFF associé si NoteON
	public boolean RemoveMsg(int channel,int id,int e,int p1,int p2) {
		int nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int old=0;
		int pos=0; //Position du message dans le contenu
		int size=this.Content[nChannel].size(); //Taille du contenu
		//Recherche de la position du message à supprimer (delta absolu)
		for(pos=0;pos < size;pos++) {
			old+=this.Content[nChannel].get(pos).delta;
			if ((id == old) || (id == -1)) {
				Content msg=this.Content[nChannel].get(pos);
				if ((msg.event == e) && (msg.param1 == p1) && (msg.param2 == p2)) {
					//Modifications de message suivant
					if (pos < (size-1)) this.Content[nChannel].get(pos+1).delta+=msg.delta;
					//Suppression du message
					this.Content[nChannel].remove(pos);
					//Suppression du NoteOFF associé si NoteON
					if (e == ShortMessage.NOTE_ON) {
						this.RemoveMsg(channel,-1,ShortMessage.NOTE_OFF,p1,0);
					}
					return true;
				}
			}
		}
		return false;
	}
	//Retourne la position d'un message s'il existe déjà dans le contenu (sinon -1)
	public int ExistsMsg(int channel,int id,int e,int p1,int p2) {
		int	nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int old=0;
		int pos=0; //Position du message dans le contenu
		int size=this.Content[nChannel].size(); //Taille du contenu
		//Recherche de la position du message (delta absolu)
		for(pos=0;pos < size;pos++) {
			old+=this.Content[nChannel].get(pos).delta;
			if (id == old) {
				Content msg=this.Content[nChannel].get(pos);
				if ((msg.event == e) && (msg.param1 == p1) && (msg.param2 == p2)) {
					return pos;
				}
			}
		}
		return -1;
	}
	//Retourne la position du message suivant de même type s'il existe déjà dans le contenu (sinon -1)
	public int NextMsg(int channel,int id,int e,int p1) {
		int	nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int old=0;
		int pos=0; //Position du message dans le contenu
		int size=this.Content[nChannel].size(); //Taille du contenu
		//Recherche de la position du message (delta absolu)
		for(pos=0;pos < size;pos++) {
			old+=this.Content[nChannel].get(pos).delta;
			if (id == old) { //Message trouvé
				Content msg=this.Content[nChannel].get(pos);
				if ((msg.event == e) && (msg.param1 == p1)) {System.out.println(id+" "+pos);
					for(pos=pos;pos < size;pos++) {
						msg=this.Content[nChannel].get(pos);
						if ((msg.event == e) && (msg.param1 == p1)) return pos;
					}
				}
			}
		}
		return -1;
	}
	//Retourne la taille d'un message sinon -1
	public int LengthMsg(int channel,int id,int e,int p1,int p2) {
		int	nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int old=0;
		int pos=0; //Position du message dans le contenu
		int size=this.Content[nChannel].size(); //Taille du contenu
		//Recherche de la position du message (delta absolu)
		for(pos=0;pos < size;pos++) {
			old+=this.Content[nChannel].get(pos).delta;
			if (id == old) {
				Content msg=this.Content[nChannel].get(pos);
				if ((msg.event == e) && (msg.param1 == p1) && (msg.param2 == p2)) {
					return msg.length;
				}
			}
		}
		return -1;
	}
	//Met à jour la taille d'un message
	public boolean setLengthMsg(int channel,int id,int e,int p1,int p2,int l) {
		int	nChannel=Math.min(16,Math.max(0,channel)); //Channel
		int old=0;
		int pos=0; //Position du message dans le contenu
		int size=this.Content[nChannel].size(); //Taille du contenu
		//Recherche de la position du message (delta absolu)
		for(pos=0;pos < size;pos++) {
			old+=this.Content[nChannel].get(pos).delta;
			if (id == old) {
				Content msg=this.Content[nChannel].get(pos);
				if ((msg.event == e) && (msg.param1 == p1) /*&& (msg.param2 == p2)*/) {
					msg.length=l;
					return true;
				}
			}
		}
		return false;
	}
	//Supprimer le contenu d'un channel
	public void ClearChannel(int channel) {
		this.Content[channel].clear();
	}
	//Supprimer tout le contenu
	public void ClearContent() {
		for(int a=0;a < 16;a++) this.Content[a].clear();
	}
	//Récupération de la taille du contenu
	public int SizeOfContent() {
		int res=0; //Valeur de retour;
		for(int a=0;a < 16;a++) if (this.Content[a].size() > res) res=this.Content[a].size();
		return res;
	}
	//Ouverture d'un fichier
	public boolean LoadContent(String login,String mdp) {
		boolean res=true; //Valeur de retour
		String files_=null;
		if (login == "_") {
			JOptionPane.showMessageDialog(this.toolbar.getParent(),"Vous devez vous inscrire sur winky pour sauvegarder vos créations.","Erreur",JOptionPane.ERROR_MESSAGE);
			res=false;
		}
		else {
			//Récupération des morceaux du membre
			try {
				HttpClient c=new HttpClient("http://winky.fr/java_getfiles.php");
				c.connect("POST");
				c.post("id=" + login + "&pass=" + mdp);
				files_=c.displayResponse(false);
				c.disconnect();
			}
			catch(Exception e) { res=false; }
			//Choix du fichier
			if (files_ == null) JOptionPane.showMessageDialog(this.toolbar.getParent(),"Vous n'avez aucun fichier.","Erreur",JOptionPane.ERROR_MESSAGE);
	        String files[]=files_.split("!");
			String file=(String)JOptionPane.showInputDialog((Component)this.toolbar.getParent(),"Choisissez un fichier.","Ouvrir...",JOptionPane.INFORMATION_MESSAGE,null,files,0);
			//Ouverture du fichier
			try {
				HttpClient c=new HttpClient("http://winky.fr/java_load.php");
				c.connect("POST");
				c.post("id=" + login + "&pass=" + mdp + "&file=" + file);
				c.displayResponse(true);
				c.disconnect();
			}
			catch(Exception e) { res=false; }
		}
		return res;
	}
	//Sauvegarde dans un fichier
	public boolean SaveContent(String file) {
		boolean res=true; //Valeur de retour
		try {
			String content=new String();
			//Sauvegarde des pistes
			for(int a=0;a < this.Content.length;a++) {
				//Pour chaque piste, sauvegarde des messages MIDI
				for(int b=0;b < this.Content[a].size();b++) {
					Content item=this.Content[a].get(b);
					String msg=new String((char)item.delta + "" + (char)item.event + "" + (char)item.param1 + "" + (char)item.param2 + "*");
					content=new String(content + msg);
				}
				content.concat("$");
			}//System.out.println(">>>" + content);
			//this.LoadContent(content);
			/*********************************************************/
			try {
		      HttpClient c = new HttpClient("http://winky.fr/java_save.php");
		      for(int a=0;a < 10;a++) {
		    	  c.connect("POST");
			      c.post("size=2&0=ABCD*EFGH&1=IJKL");
			      c.displayResponse(true);
			      c.disconnect();
		      }
		      return true;
		    }
		    catch (Exception e) {
		      e.printStackTrace();
		    }
			/*********************************************************/
		}
		catch (Exception ex) { res=false; }
        return res;
	}
	//Thread de lecture
	@Override public void run() {
		this.cptPlay=65535;
		int cptPerChannel[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		int deltaPerChannel[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		this.Delta=0;
		int maskPerChannel[]={1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192,16384,32768};
		boolean deltaNull=false; //true si le delta du message suivant le message actuel = 0, false sinon
		int bpm=this.nbrTps; //Nombre de temps à jouer avant fin de piste (pour Repeat)
		Content msg=null;
		//Lecture du contenu
		while (true) {
			//Si Fin du morceau
			if ((this.cptPlay <= 0) && (bpm <= 0)) {
				//Si Repeat
				if (this.Repeat) {
					this.cptPlay=65535;
					int cptPerChannel_[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
					cptPerChannel=cptPerChannel_;
					int deltaPerChannel_[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
					deltaPerChannel=deltaPerChannel_;
					this.Delta=0;
					bpm=this.nbrTps;
				}
				else {
					this.roll.lect_bar=this.nbrTps;
					Rectangle rect=this.roll.getVisibleRect();
					//this.roll.repaint(rect.x+this.roll.roll_piano_size+(this.roll.lect_bar-1)*this.roll.size,rect.y,rect.x+this.roll.roll_piano_size+this.roll.lect_bar*this.roll.size,rect.height);
					this.roll.repaint();
					break;
				}
			}
			//Boucle sur les 16 channels
			for(int a=0;a < 16;a++) {
				//Si un message suit dans la pile
				if (cptPerChannel[a] < this.Content[a].size()) {
						//Boucle pour les delta = 0
						do {
							deltaNull=false;
							//Récupération du message à envoyer
							msg=this.Content[a].get(cptPerChannel[a]);
							//Si delta = delta récupéré, envoi
							if ((deltaPerChannel[a]+msg.delta) == this.Delta) {
								//Sauvegarde de la note si NoteOn
								if (msg.event == ShortMessage.NOTE_ON) this.lastNotePerChannel[a]=msg.param1;
								//Envoi du message
								if (((this.mutePerChannel[a] == 1) && (this.channelSolo == -1)) || (this.channelSolo == a))
									this.SendShortMsg(a,msg);
								//Remise à zéro des paramètres d'attente
								deltaPerChannel[a]+=msg.delta;
								cptPerChannel[a]++;
								//Recherche si delta message suivant = 0
								if (cptPerChannel[a] < this.Content[a].size()) {
									msg=(Content)this.Content[a].get(cptPerChannel[a]);
									if (msg.delta == 0) deltaNull=true;
								}
							}
						} while (deltaNull);
				}
				else this.cptPlay&=~maskPerChannel[a];
			}
			this.Delta++;
			this.roll.lect_bar=this.nbrTps-bpm;
			bpm--;
			if (bpm >= 0) {
				try { Thread.sleep((long)(((60.0/this.tempo)*1000)/(this.nbrTps/4))); }
				catch(InterruptedException e) {}
			}
		}
		this.buttonStart.type=Button.PLAY;
		this.Stop();
	}
}