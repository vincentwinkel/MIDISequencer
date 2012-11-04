
public class Content {
	//Attributs
	public int delta=0;
	public int event=0;
	public int param1=0;
	public int param2=0;
	public int length=0;
	//Constructeur avec paramètres
	public Content(int d,int e,int p1,int p2) {
		this(d,e,p1,p2,-1);
	}
	public Content(int d,int e,int p1,int p2,int l) {
		this.delta=d;
		this.event=e;
		this.param1=p1;
		this.param2=p2;
		this.length=l;
	}
}
