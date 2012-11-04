import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;


public class programme extends JApplet implements KeyListener {
	//Paramètres html
	String LOGIN,MDP;
	//Composants
	public MIDI_out output=new MIDI_out();
	
	private JLabel label=new JLabel("Port d'écoute fermé.");
	private Button play=new Button(this,Button.PLAY);
	public Keyboard keyboard=new Keyboard(this.getRootPane(),this.label,this.play,this.output);
	
	private Button repeat=new Button(this,Button.REPEAT_ON);
	private JPanel pan=new JPanel(); //Panneau principal
	private Toolbar toolbar=new Toolbar(); //Barre d'outils
	private JPanel bottom=new JPanel(); //Bas de fenêtre
	private Header_PianoRoll header_pr=new Header_PianoRoll(40+34);
	private PianoRoll roll=new PianoRoll(this,this.output,this.keyboard); //Piano roll
	private Scroll scrollRoll=new Scroll(this.roll);
	private Grid grid=new Grid(this,this.toolbar,this.roll,this.output,this.keyboard); //Grille des pistes
	private Scroll scrollGrid=new Scroll(this.grid);
	private RaccKeys raccKeys=new RaccKeys(this.output,this.keyboard); //Touches clavier
	private Scroll scrollRaccKeys=new Scroll(this.raccKeys);
	private JSplitPane subsplit=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,this.scrollRaccKeys,this.scrollGrid);
	private JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,this.subsplit,this.scrollRoll);
	private Slider slide_bpm=new Slider(); //Slider de tempo
	//Menu
	private JMenuBar menuBar=new JMenuBar();
	private JMenu menu0=new JMenu("Fichier");
	private JMenuItem item0_1=new JMenuItem("Ouvrir...");
	private JMenuItem item0_2=new JMenuItem("Sauvegarder");
	private JMenu menu1=new JMenu("Pistes");
	private JMenuItem item1_1=new JMenuItem("Ajouter");
	private JMenuItem item1_2=new JMenuItem("Supprimer");
	private JMenuItem item1_3=new JMenuItem("Tout supprimer");
	private JMenu menu2=new JMenu("Affichage");
	private JMenuItem item2_1=new JMenuItem("Plus petit");
	private JMenuItem item2_2=new JMenuItem("Plus grand");
	private JMenuItem item2_3=new JMenuItem("Réinitialiser");
	//private JMenu menu3=new JMenu("Préférences");
	//private JMenuItem item3_1=new JMenuItem("Ouvrir port");
	//Constructeur
	public programme() {
		this.setSize(800,600);
		//MENU FICHIER
		//Ouvrir
		this.item0_1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				//output.LoadContent(LOGIN,MDP);
			}
		});
		this.item0_1.setIcon(UIManager.getIcon("FileChooser.newFolderIcon"));
		this.menu0.add(this.item0_1);
		//Sauvegarder
		this.item0_2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				//output.SaveContent(null);
			}
		});
		this.item0_2.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
		this.menu0.add(this.item0_2);
		this.item0_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_DOWN_MASK));
		//MENU PISTES
		//Ajouter une piste
		this.item1_1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				grid.AddTrack();
			}
		});
		this.menu1.add(this.item1_1);
		this.item1_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,KeyEvent.CTRL_DOWN_MASK));
		//Supprimer une piste
		this.item1_2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				grid.RemoveTrack(false);
			}
		});
		this.menu1.add(this.item1_2);
		this.item1_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,KeyEvent.CTRL_DOWN_MASK));
		this.menu1.addSeparator();
		//Supprimer toutes les pistes
		this.item1_3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				int option=JOptionPane.showConfirmDialog(grid,"Etes-vous sûr(e) de vouloir supprimer toutes les pistes ?","Question",JOptionPane.OK_OPTION,JOptionPane.QUESTION_MESSAGE);
				if ((option != JOptionPane.NO_OPTION) && (option != JOptionPane.CANCEL_OPTION) && (option != JOptionPane.CLOSED_OPTION)) {
					while (grid.RemoveTrack(true)) {}
				}
			}
		});
		//this.menu1.add(this.item1_3);
		//MENU AFFICHAGE
		//Plus petit
		this.item2_1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				grid.grid_track_h-=5;
				item2_2.setEnabled(true);
				item2_3.setEnabled(true);
				if (grid.grid_track_h <= grid.grid_track_min_h) {
					item2_1.setEnabled(false);
					item2_3.setEnabled(false);
				}
				int nbr_tracks=(grid.nbr_tracks < grid.nbr_max_tracks)?(grid.nbr_tracks+1):grid.nbr_tracks;
				//Resize si nécessaire
				if ((nbr_tracks*grid.grid_track_h) < grid.getHeight()) {
					grid.setPreferredSize(new Dimension(0,nbr_tracks*grid.grid_track_h));
					grid.revalidate();
					grid.scrollRectToVisible(new Rectangle(0,nbr_tracks*grid.grid_track_h,10,10));
				}
				grid.repaint();
			}
		});
		this.menu2.add(this.item2_1);
		this.item2_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,KeyEvent.CTRL_DOWN_MASK));
		this.item2_1.setEnabled(false);
		//Plus grand
		this.item2_2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				grid.grid_track_h+=5;
				item2_1.setEnabled(true);
				item2_3.setEnabled(true);
				if (grid.grid_track_h >= grid.grid_track_max_h) item2_2.setEnabled(false);
				int nbr_tracks=(grid.nbr_tracks < grid.nbr_max_tracks)?(grid.nbr_tracks+1):grid.nbr_tracks;
				//Resize si nécessaire
				if ((nbr_tracks*grid.grid_track_h) > grid.getHeight()) {
					grid.setPreferredSize(new Dimension(0,nbr_tracks*grid.grid_track_h));
					grid.revalidate();
					grid.scrollRectToVisible(new Rectangle(0,nbr_tracks*grid.grid_track_h,10,10));
				}
				grid.repaint();
			}
		});
		this.menu2.add(this.item2_2);
		this.item2_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP,KeyEvent.CTRL_DOWN_MASK));
		this.menu2.addSeparator();
		//Réinitialiser l'affichage
		this.item2_3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				grid.grid_track_h=grid.grid_track_min_h;
				item2_1.setEnabled(false);
				item2_2.setEnabled(true);
				item2_3.setEnabled(false);
				grid.repaint();
			}
		});
		this.menu2.add(this.item2_3);
		this.item2_3.setEnabled(false);
		//MENU PREFERENCES
		//Ajout des menus
		this.menuBar.add(this.menu0);
		this.menuBar.add(this.menu1);
		this.menuBar.add(this.menu2);
		//this.menuBar.add(this.menu3);
		this.setJMenuBar(this.menuBar);
		//Composants
		this.play.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg) {
				if (output.getContentIsPlaying()) {
					output.Stop();
					play.type=Button.PLAY;
				}
				else {
					output.Play(play);
					play.type=Button.STOP;
				}
				//getRootPane().requestFocusInWindow();
			}
		});
		this.repeat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg) {
				if (output.Repeat) {
					output.Repeat=false;
					repeat.type=Button.REPEAT_OFF;
				}
				else {
					output.Repeat=true;
					repeat.type=Button.REPEAT_ON;
				}
				//getRootPane().requestFocusInWindow();
			}
		});
		//Informations
		this.label.setHorizontalAlignment(JLabel.LEFT);
		this.label.setFont(new Font("Arial",Font.PLAIN,12));
		this.label.setPreferredSize(new Dimension(this.getWidth()-10,12));
		//Tempo
		this.slide_bpm.setOpaque(false);
		this.slide_bpm.setPreferredSize(new Dimension(100,20));
		this.slide_bpm.setMaximum(220);
		this.slide_bpm.setMinimum(1);
		this.slide_bpm.setValue(120);
		this.slide_bpm.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg) {
				output.tempo=slide_bpm.getValue();
				toolbar.tempo=new String(slide_bpm.getValue() + " bpm");
				toolbar.repaint();
			}
		});
		JPanel temp=new JPanel();
		temp.setPreferredSize(new Dimension(105,50));
		temp.setOpaque(false);
		//Barre d'outils
		this.play.setPreferredSize(new Dimension(50,50));
		this.repeat.setPreferredSize(new Dimension(50,50));
		JLabel space=new JLabel();
		space.setPreferredSize(new Dimension(570,10));
		this.toolbar.add(space);
		this.toolbar.add(this.play);
		this.toolbar.add(this.repeat);
		this.toolbar.add(this.slide_bpm);
		this.toolbar.setPreferredSize(new Dimension(this.getWidth(),60));
		this.toolbar.setBackground(new Color(150,150,148));
		//Touches clavier
		this.raccKeys.addMouseListener(this.raccKeys);
		this.raccKeys.addMouseMotionListener(this.raccKeys);
		this.raccKeys.setPreferredSize(new Dimension(0,15+(26+1)*15+1));//this.grid.grid_track_h*2));
		//Grid
		this.grid.addMouseListener(this.grid);
		this.grid.addMouseMotionListener(this.grid);
		this.grid.setPreferredSize(new Dimension(0,this.grid.grid_track_h*2));
		//Piano roll
		this.header_pr.setPreferredSize(new Dimension(100,20));
		this.scrollRoll.setColumnHeaderView(this.header_pr);
		JLabel l_title2=new JLabel("/64");
		l_title2.setFont(new Font("Arial",Font.PLAIN,10));
		this.scrollRoll.setCorner(JScrollPane.UPPER_RIGHT_CORNER,l_title2);
		this.roll.setAutoscrolls(true);
		this.roll.addMouseListener(this.roll);
		this.roll.addMouseMotionListener(this.roll);
		this.roll.setPreferredSize(new Dimension(0,960+3));
		this.roll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.roll.scrollRectToVisible(new Rectangle(10,960/2+50,10,10));
		//Split
		this.subsplit.setDividerLocation(190);
		this.subsplit.setOneTouchExpandable(true);
		this.split.setPreferredSize(new Dimension(this.getWidth(),this.getHeight()-60-55-7));
		this.split.setDividerLocation(270);
		this.split.setOneTouchExpandable(true);
		//Bas de fenêtre
		this.bottom.setPreferredSize(new Dimension(this.getWidth(),20));
		this.bottom.setBackground(new Color(220,220,220));
		this.bottom.add(this.label);
		//Ajout des composants
		pan.add(this.toolbar);
		pan.add(this.split);
		pan.add(this.bottom);
		this.pan.setBackground(new Color(220,220,220));
		this.setContentPane(this.pan);
		//Initialisation du port MIDI out
		this.output.Open();
		for(int a=0;a < 16;a++) this.output.Volume(a,80);
		this.output.setPianoRoll(this.roll);
		this.output.setToolbar(this.toolbar);
		//Loook & Feel
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.put("nimbusBase",new Color(60,60,150));
			//UIManager.put("nimbusBlueGrey",Color.RED);
			//UIManager.put("control",Color.RED);
			SwingUtilities.updateComponentTreeUI(this);
		}
		catch (Exception e) {}
		//Evénements clavier
		this.play.setFocusable(false);
		this.repeat.setFocusable(false);
		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke((char)KeyEvent.VK_SPACE),"space");
		this.getRootPane().getActionMap().put("space",this.keyboard);
		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke((char)KeyEvent.VK_DELETE),"delete");
		this.getRootPane().getActionMap().put("delete",this.keyboard);
		//Bound
		this.keyboard.addPropertyChangeListener(this.raccKeys);
		//Visibilité de la fenêtre
		this.setVisible(true);
	}
	//Point d'entrée de l'application
	public void init() {
		//Récupération des paramètres html
		this.LOGIN=getParameter("login");
		if (this.LOGIN == null) this.LOGIN="Vincent";
		this.MDP=getParameter("pass");
		if (this.MDP == null) this.MDP="v1345w";
	}
	/*public void start() {
	}*/
	public void stop() {
		if (this.output.getContentIsPlaying()) this.output.Stop();
		this.output.Close();
	}
	//Ecouteurs
	public void keyPressed(KeyEvent arg) {
		int key=arg.getKeyCode();
		switch(key) {
			//Play / Pause
			case KeyEvent.VK_SPACE:{
				/*if (this.output.getContentIsPlaying()) {
					this.output.Stop();
					play.type=Button.PLAY;
				}
				else {
					this.output.Play(play);
					play.type=Button.STOP;
				}
				play.repaint();*/
				break;
			}
			//Touches clavier
			/*default:{
				if ((key < KeyEvent.VK_A) && (key > KeyEvent.VK_Z)) return;
				if (this.keyboard.isActive(key)) {
					long startTime=System.nanoTime();
					try {
						Class.forName("MIDI_out").getMethod("SendShortMsg",new Class[]{int.class,int.class,int.class,int.class}).invoke(this.output,this.keyboard.getKey(key));
					}
					catch(Exception e) { this.label.setText("Erreur logicielle."); }
					   this.label.setText(this.keyboard.getMsgKey(key) + " " + (System.nanoTime()-startTime));
				}
			}*/
		}
	}
	public void keyTyped(KeyEvent e) {
	}
	public void keyReleased(KeyEvent e) {
	}
}
