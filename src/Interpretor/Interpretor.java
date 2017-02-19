package Interpretor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

public class Interpretor {
	
	private String numeFisierCitire = null;
	private BufferedReader br = null;
	
	private AnalizatorSemantic as = null;
	private ArboreDeParsare ap = null;
	private Evaluator eval = null;
	
	/**Constructor specifica interpretorului fisierul in care se afla secventa de instructiuni de analizat.
	 * @param numeFisierCitire
	 * Numele fisierului ce contine instructiunile de interpretat si va influenta numele fisierelor de output.
	 */
	public Interpretor(String numeFisierCitire){
		this.numeFisierCitire=numeFisierCitire;
	}
	
	/**Metoda realizeaza analiza efectiva din cele 3 puncte de vedere
	 * asupra continutului fisierului.
	 */
	public void interpret(){
		//Se incearca deschiderea fisierului in care se afla comenzile de interprtat.
		try{
			br = new BufferedReader(new FileReader(numeFisierCitire));
		}catch(IOException e){
			System.out.println("Nu s-a putut deschide fisierul de citire.");
			e.printStackTrace();
		}
		
		//Instantierea instrumentelor de prelucrare a instructiunilor.
		as = new AnalizatorSemantic(numeFisierCitire);
		ap = new ArboreDeParsare(numeFisierCitire);
		eval = new Evaluator(numeFisierCitire);
		
		String linie = null;
		int indiceLinie = 1;
		boolean ok = false;
		try{
			//Se incepe interpretarea comenzilor din fisier linie cu linie.
			while( (linie = br.readLine() )!=null ){
				
				/*Se realizeaza analiza semantica a liniei curente
				 * si se determina corectitudinea ei pentru a se putea
				 * stii daca se poate evalua expresia de pe aceasta linie.
				 */
				ok = as.analyzeRow(linie, indiceLinie++);
				
				//Se determina variabila careia i se face atribuirea
				int pozitieEgal = linie.indexOf('=');
				String variabila = linie.substring(0, pozitieEgal);
				
				/*Se inlocuieste structura operatorului ternar cu functia #
				 * pentru a usura transformarea expresiei in forma poloneza,
				 * si deasemenea usurarea evaluarii din aceasta forma.
				 */
				String linieTernar = parseTernar(linie);
				String expresie = linieTernar.substring(pozitieEgal+1);
				String poloneza=toPolish(expresie);
				
				ap.toTree(variabila, poloneza);
				
				if(ok){
					/*Daca expresia de pe linia curenta se poate evalua
					 * atunci rezultatul evaluarii se va scrie in fisierul ee.
					 */
					eval.evaluate(variabila, poloneza);
				}else{
					/*Daca expresia nu se poate evalua atunci in fisierul
					 * ee se va scrie mesajul "error".
					 */
					eval.nonCalculable();
				}
			}
			//Se inchide fisierul din care s-a citit.
			br.close();
		}catch(IOException e){
			System.out.println("Probleme la citire sau inchiderea fisierului de citire.");
			e.printStackTrace();
		}
		
		//Inchiderea instrumentelor de prelucrare pentru salvarea rezultatelor din fisierele de output.
		as.close();
		ap.close();
		eval.close();
	}
	
	/**Metoda transforma o expresie primita ca parametru in forma poloneza.
	 * @param expresie
	 * Expresia ce se doreste convertita in forma poloneza.
	 * @return
	 * Un String ce reprezinta expresia primita ca parametru in forma poloneza.
	 */
	private String toPolish(String expresie){
		String rezultat="";		
		Stack<Character> op = new Stack<Character>();
		String variabila = "";
		char c='@';
		/*Se presupune ca nu s-a intalnit inca un numar sau o
		 * variabila in procesarea expresiei (la nivelul curent de paranteze).
		 */
		boolean checkNumberBefore = false;
		for(int i=0; i<expresie.length(); i++){
			c=expresie.charAt(i);
			if(c=='*' || c=='+' || c=='-' || c=='#' || c=='(' || c==')'){
				if(!op.isEmpty()){
					/*Daca stiva nu este vida si s-a citit un operator sau paranteze se trateaza
					 * cazul specific caracterului citit.
					 */
					if(c=='*'){
						while(!op.isEmpty() && (op.peek()=='*'|| op.peek()=='#')){
							rezultat+=op.pop()+" ";
						}	
						op.push(c);
					}
					if(c=='+' || c=='-'){
						while(!op.isEmpty() && (op.peek()=='*' || op.peek()=='-' || op.peek()=='+'|| op.peek()=='#' || op.peek()=='$' || op.peek()=='@') ){
							rezultat+=op.pop()+" "; 
						}
						/*Daca s-a citit + sau - dar am intalnit numar sau variabila
						 * inainte de semn inseamna ca acest operator este binar,
						 * altfel se interpreteaza ca unar.
						 */
						if(c=='+'){
							if(checkNumberBefore){
								op.push(c);
							}else{
								op.push('$');
								checkNumberBefore=true;
							}
						}
						if(c=='-'){
							if(checkNumberBefore){
								op.push(c);
							}else{
								op.push('@');
								checkNumberBefore=true;
							}
						}
					}
					if(c=='#'){
						while( !op.isEmpty() && (op.peek()=='*' || op.peek()=='-' || op.peek()=='+') ){
							rezultat+=op.pop()+" ";
						}
						op.push(c);
					}
					if(c==')'){
						while( !op.isEmpty() && op.peek()!='(' ){
							rezultat+=op.pop()+" ";
						}
						op.pop();
					}
					if(c=='('){ 
						op.push(c);
						checkNumberBefore=false;
					}
				}else{
					if(checkNumberBefore==false && (c=='(' || c=='-' || c=='+')){
						checkNumberBefore=false;
						/*Daca stiva este goala si s-a intalnit unul din operatorii de mai sus
						 * si nici nu s-a citit numar sau variabila atunci se interpreteaza
						 * operatorii unari.
						 */
						if(c=='('){
							op.push(c);
						}
						if(c=='+'){
							op.push('$');
						}
						if(c=='-'){
							op.push('@');
						}
					}else{
						//Altfel se introduce in stiva operatorul binar.
						op.push(c);
					}
				}
			}else{
				/*Daca nu s-a intalnit operator sau functia # atunci avem fie variabila,
				 * numar sau virgula(virgula separa argumentele functiei #).
				 */
				while(c==','){
					i++;
					c=expresie.charAt(i);
				}
				
				boolean ok=false;
				//Se alcatuieste numele variabilei sau numarul intalnit.
				while(Character.isLetter(c) || Character.isDigit(c)){
					variabila+=c;
					i++;
					if(i==expresie.length()){
						ok=true;
						break;
					}
					c=expresie.charAt(i);
				}
				i--;
				
				/*Se concateneaza numarul sau variabila obtinuta la coada de
				 * rezultat obtinuta pana acum.
				 */
				rezultat+=variabila+" ";
				checkNumberBefore=true;
				variabila="";
				if(ok) break;
			}
		}
		
		/*Daca au mai ramas operatori in stiva atunci acestia sunt transferati
		 * in coada rezultat ce va reprezenta expresia in forma poloneza.
		 */
		while(!op.isEmpty()){
			rezultat+=op.pop()+" ";
		}
		
		return rezultat;
	}

	/**Metoda converteste operatorii ternari in functii # astfel:
	 * ((A gt B)?C:D) in #((A),(B),(C),(D)) , pentru a usura transformarea expresiei
	 * in forma poloneza. (A gt B inseamna greater than; probleme la generarea JavaDoc pentru semnul mai mare)
	 * @param linie
	 * Expresia ai carei operatori ternari se doresc transformati in functii #.
	 * @return
	 * Noul tip de expresie in care operatorii ternari sunt inlocuiti cu functii #.
	 */
	private String parseTernar(String linie){
		/*Se numara cate semne ternare exista in expresie
		 * pentru a se stii daca exista macar unu si daca
		 * are rost prelucrarea. 
		 */
		int semne = 0;
		for(int i=0; i<linie.length(); i++){
			if(linie.charAt(i)=='?') semne++;
		}
		if (semne==0) return linie;
		
		/*Se alcatuiec cei patru parametrii ai functiei ternar astfel:
		 *  ((A>B)?C:D) -> #((A),(B),(C),(D))
		 */
		String paramA = "";
		String paramB = "";
		String paramC = "";
		String paramD = "";
	
		int pozitieIntrebare = linie.indexOf('?');
		
		//Formarea parametrului B
		int indexB = pozitieIntrebare-2;
		char b = '_';
		int inceputB = 0;
		int parantezeB = 0;
		while( indexB>-1){
			b=linie.charAt(indexB);
			if( parantezeB!=0 || b!='>' ){
				paramB+=b;
				inceputB = indexB;
				indexB--;
				if(b==')') parantezeB++;
				if(b=='(') parantezeB--;
			}else{
				break;
			}
		}
		paramB = linie.substring(inceputB, pozitieIntrebare-1);
		
		//Formarea parametrului A
		int indexA = inceputB-2;
		char a='_';
		int inceputA=0;
		int parantezeA = 0;
		while( indexA>-1){
			a=linie.charAt(indexA);
			if( parantezeA!=0 || a!='(' ){
				paramA+=a;
				inceputA = indexA;
				indexA--;
				if(a==')') parantezeA++;
				if(a=='(') parantezeA--;
			}else{
				break;
			}
		}
		paramA = linie.substring(inceputA, inceputB-1);
		char inainteDeA=linie.charAt(inceputA-2);
		
		//Formarea parametrului C
		int nrparantezeC = 0;
		char c = '_';
		int indexC = pozitieIntrebare+1;
		while( indexC<linie.length() ){
			c=linie.charAt(indexC);
			if( (nrparantezeC!=0 || c!=':') ){
				paramC+=c;
				indexC++;
				if(c=='(') nrparantezeC++;
				if(c==')') nrparantezeC--;
			}else{
				break;
			}
		}
		
		//Formarea parametrului D
		int sfarsitD=0;
		if(inainteDeA=='('){
			//Inseamna ca avem paranteza si dupa d
			int nrparantezeD = 0;
			char d = '_';
			int indexD = indexC+1;
			while( indexD<linie.length() ){
				d=linie.charAt(indexD);
				if( (nrparantezeD!=0 || d!=')') ){
					paramD+=d;
					sfarsitD=indexD;
					indexD++;
					if(d=='(') nrparantezeD++;
					if(d==')') nrparantezeD--;
				}else{
					break;
				}
			}
		}else{
			//Inseamna ca nu avem paranteza dupa d
			int indexD = indexC+1;
			int nrparantezeD = 0;
			char d = linie.charAt(indexD);
			if(d=='(') nrparantezeD++;
			while(indexD<linie.length() && (nrparantezeD!=0 ||(Character.isDigit(d)||Character.isLetter(d)) || (indexD==indexC+1 && (d=='+')||(d=='-')))){
				paramD+=d;
				sfarsitD=indexD;
				indexD++;
				if(indexD<linie.length()) d=linie.charAt(indexD);
				if(d=='(') nrparantezeD++;
				if(d==')') nrparantezeD--;
			}
		}
		
		//Inlocuire cu #((A),(B),(C),(D))
		String linieNoua=linie.substring(0, inceputA-1) + "#(("+paramA+"),("+paramB+"),("+paramC+"),("+paramD+"))" +linie.substring(sfarsitD+1);
		//Se continua inlocuirea recursiva a tuturor operatorilor ternari.
		return parseTernar(linieNoua);
	}
}
