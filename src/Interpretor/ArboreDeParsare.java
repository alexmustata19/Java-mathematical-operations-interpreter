package Interpretor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Stack;
import java.util.StringTokenizer;

public class ArboreDeParsare {

	/**Clasa interna pentru modelarea conceptului de
	 * nod intr-un arbore binar ce reprezinta o expresie.
	 */
	private class Nod{
		public Nod st,dr;
		public boolean isOperator;
		public String variabila;
		public char operator;
		public char tip;
	}

	private String numeFisierScrierePT = null;
	
	private PrintWriter pwpt = null;
	
	/**Constructorul initializeaza arborele de parsare prin deschiderea
	 * fisierului in care se va scrie rezultatul parsarii.
	 * @param numeFisierCitire
	 * Numele fisierului ce contine instructiunile de interpretat si va influenta numele fisierului de output.
	 */
	public ArboreDeParsare(String numeFisierCitire){
		//Se formeaza numele fisierului in care se va scrie pasarea expresiilor.
		int pozitiePunctExtensie = numeFisierCitire.indexOf('.');
		if(pozitiePunctExtensie !=-1){
			numeFisierScrierePT = numeFisierCitire.substring(0, pozitiePunctExtensie) + "_pt" + numeFisierCitire.substring(pozitiePunctExtensie);
		}else{
			numeFisierScrierePT = numeFisierCitire+"_pt";
		}
		
		//Se incearca deschiderea fisierului de output pentru arborele de parsare.
		try{
			pwpt = new PrintWriter(new File(numeFisierScrierePT));
		}catch(IOException e){
			System.out.println("Nu s-a putut deschide fisierul de scriere pt.");
			e.printStackTrace();
		}
	}
	
	/**Metoda pentru a termina parsarea in sensul inchiderii
	 * fisierului de output si salvarii continutului acestuia.
	 */
	public void close(){
		//Se inchide fisierul de output.
		pwpt.close();
	}
	
	/**Metoda stabileste daca un String primit ca parametru este numar intreg.
	 * @param variabila
	 * String-ul ce se doreste sa se verifice daca este numar intreg.
	 * @return
	 * Raspunsul daca String-ul este numar intreg sau nu.
	 */
	private boolean esteNumar(String variabila){
		/*Se testeaza daca un string primit ca parametru este
		 * numar intreg sau nu prin intermediul prinderii unei
		 * exceptii.
		 */
		try{
			int i = Integer.parseInt(variabila);
		}catch(NumberFormatException e){
			return false;
		}
		return true;
	}
	
	/**Metoda realizeaza arborele de parsare al expresiei in forma 
	 * poloneza ce i se atribuie variabilei si afiseaza rezultatul
	 * in fisierul de output.
	 * @param variabila
	 * Variabila careia i se atribuie forma poloneza.
	 * @param formaPoloneza
	 * Expresia in forma poloneza careia i se doreste construit 
	 * arborele de parsare.
	 */
	public void toTree(String variabila, String formaPoloneza){
		//Se creaza arborele de parsare pentru expresia curenta.
		Nod arbore=new Nod();
		arbore.isOperator=true;
		arbore.operator='=';
		
		arbore.st=new Nod();
		arbore.st.isOperator=false;
		arbore.st.variabila=variabila;
		
		arbore.dr=calculateArbore(formaPoloneza);
		
		//Se scrie parsarea expresiei in fisierul pt.
		recursiveDisplay(arbore);
	}
	
	/**Metoda construieste arborele de parsare al expresiei primite ca parametru in forma poloneza. 
	 * @param formaPoloneza
	 * Expresia in forma poloneza careia i se doreste realizat un arbore de parsare.
	 * @return
	 * Arborele de parsare al expresiei.
	 */
	private Nod calculateArbore(String formaPoloneza){
		/*Se initializeaza arborele rezultat cu null
		 * si stiva in care se va produce "calculul" arborelui.
		 */
		Nod rezultat = null;
		Stack<Nod> numere = new Stack<Nod>();
		
		/*Se doreste descompunerea expresiei in token dupa spatii
		 * (similar in modul in care a fost construita).
		 */
		StringTokenizer st = new StringTokenizer(formaPoloneza);
		String variabila="";
		
		//Se incepe evaluarea expresiei.
		while(st.hasMoreTokens()){
			variabila=st.nextToken();
			if(esteNumar(variabila)){
				/*Daca se intalneste un numar atunci se creaza un nod
				 * pentru acel numar si nodul este introdus in stiva.
				 */
				Nod aux = new Nod();
				aux.isOperator=false;
				aux.variabila=variabila;
				numere.push(aux);
			}else{
				if(variabila.equals("+")){
					/*La adunare (operator binar) se scot doua noduri (care pot reprezenta 
					 * arbori) din stiva, se creaza un nod + ce va avea ca fii cele doua noduri
					 * si acest nod este pus in stiva. 
					 */
					Nod a=numere.pop();
					Nod b=numere.pop();
					Nod aux=new Nod();
					aux.isOperator=true;
					aux.operator='+';
					aux.st=b;
					aux.dr=a;
					numere.push(aux);
				}else if(variabila.equals("$")){
					/*La adunare (operator unar) se scoate un nod (ce poate reprezenta un arbore)
					 * din stiva, se creaza un nod $ ce va avea ca fiu drept nodul scos si se va
					 * introduce in stiva nodul creat.
					 */
					Nod a=numere.pop();
					Nod aux=new Nod();
					aux.dr=a;
					aux.isOperator=true;
					aux.operator='$';
					numere.push(aux);
				}else if(variabila.equals("@")){
					/*La scadere (operator unar) se va scoate un nod (ce poate reprezenta un arbore)
					 * din stiva, se creaza un nod @ ce va avea ca fiu drept nodul scos si se va
					 * pune in stiva nodul creat. 
					 */
					Nod a=numere.pop();
					Nod aux=new Nod();
					aux.dr=a;
					aux.isOperator=true;
					aux.operator='@';
					numere.push(aux);
				}else if(variabila.equals("-")){
					/*La scadere (operator binar) se vor scoate doua noduri (ce pot reprezenta arbori)
					 * din stiva, se creaza un nod - ce va avea ca fii nodurile extrase si se va
					 * introduce in stiva nodul creat.
					 */
					Nod a=numere.pop();
					Nod b=numere.pop();
					Nod aux=new Nod();
					aux.isOperator=true;
					aux.operator='-';
					aux.st=b;
					aux.dr=a;
					numere.push(aux);
				}else if(variabila.equals("*")){
					/*La inmultire se vor scoate doau noduri (ce pot reprezenta arbori) din stiva,
					 * se creeaza un nod * ce va avea ca fii cele doua noduri extrase si se va 
					 * introduce in stiva nodul creat.
					 */
					Nod a=numere.pop();
					Nod b=numere.pop();
					Nod aux=new Nod();
					aux.isOperator=true;
					aux.operator='*';
					aux.st=b;
					aux.dr=a;
					numere.push(aux);
				}else if(variabila.equals("#")){
					/*La intalnirea functiei asociate operatorului ternar(#) se scot 4 noduri
					 * (ce pot reprezenta arbori) din stiva si va fi creata structura operatorului
					 * ternar prezentata in enunt si nodul : va fi introdus in stiva.
					 */
					Nod d=numere.pop();
					Nod c=numere.pop();
					Nod b=numere.pop();
					Nod a=numere.pop();
					Nod aux=new Nod();
					aux.isOperator=true;
					aux.operator=':';
					aux.dr=d;
					
					aux.st=new Nod();
					aux.st.isOperator=true;
					aux.st.operator='?';
					aux.st.dr=c;
					
					aux.st.st=new Nod();
					aux.st.st.isOperator=true;
					aux.st.st.operator='>';
					aux.st.st.dr=b;
					aux.st.st.st=a;
					
					numere.push(aux);
				}else{
					/*Daca se intalneste o variabila atunci acesteia
					 * i se creaza un nod ce este introdus in stiva.
					 */
					Nod aux = new Nod();
					aux.isOperator=false;
					aux.variabila=variabila;
					numere.push(aux);
				}
			}
		}
		
		/*La final va ramane in stiva doar un nod ce va
		 * reprezenta arborele expresiei primita in forma poloneza.
		 */
		rezultat=numere.pop();
		return rezultat;
	}
	/**Metoda stabileste inaltimea arborelui si identifica tipul nodurilor componente.
	 * @param parcurgere
	 * Subarborele curent in care s-a ajuns cu parcurgerea.
	 * @param parinte
	 * Operatorul din nodul parinte.
	 * @return
	 * Inaltimea subarborelui curent.
	 */
	private int initializareTipCalculInaltime(Nod parcurgere, char parinte){
		if(parcurgere.isOperator==false){
			/*Daca nodul este frunza se face indentificare tipului (T/F/N)
			 * si se returneaza inaltimea 1.
			 */
			if(parinte=='+' || parinte=='-' || parinte=='@' || parinte=='$' || parinte=='='){
				parcurgere.tip='T';
			}
			if(parinte=='*'){
				parcurgere.tip='F';
			}
			if(parinte=='?' || parinte==':' || parinte=='>'){
				parcurgere.tip='N';
			}
			return 1;
		}else{
			/*Daca nodul nu este frunza deci este operator, atunci se precizeaza tipul E pentru nod
			 * si se calculeaza inaltimea arborelui curent in functie de tipul de operator (unar
			 * sau binar).
			 */
			parcurgere.tip='E';
			if(parcurgere.operator=='$' || parcurgere.operator=='@'){
				return initializareTipCalculInaltime(parcurgere.dr, parcurgere.operator)+1;
			}else{
				int a=initializareTipCalculInaltime(parcurgere.st, parcurgere.operator);
				int b=initializareTipCalculInaltime(parcurgere.dr, parcurgere.operator);
				if(a>b){
					return a+1;
				}else{
					return b+1;
				}
			}
		}
		
	}
	
	/**Metoda condenseaza operatorii unari care se afla inaintea frunzelor
	 * pentru a nu reduce acele frunze in mod eronat la termeni. 
	 * @param parcurgere
	 * Subarborele curent in care s-a ajuns cu procesarea.
	 * @param parinte
	 * Operatorul din nodul parinte.
	 */
	private void condensare(Nod parcurgere, char parinte){
		/*Se condenseza operatorii unari pentru a se respecta semnificatia notatiilor T,F si N 
		 * si faptul ca gramatica nu permite aplicarea operatorilor unari pe expresii.
		 */
		if(parcurgere.isOperator==false) return;
		if(parcurgere.isOperator && parcurgere.operator!='$' && parcurgere.operator!='@' ){
			condensare(parcurgere.st, parcurgere.operator);
			condensare(parcurgere.dr, parcurgere.operator);
			return;
		}
		if(parcurgere.isOperator && (parcurgere.operator=='$' || parcurgere.operator=='@') && parcurgere.dr.isOperator){
			parcurgere.isOperator=true;
			parcurgere.tip='E';
			parcurgere.operator = parcurgere.dr.operator;
			parcurgere.st = parcurgere.dr.st;
			parcurgere.dr = parcurgere.dr.dr;
			condensare(parcurgere,parinte);
			return;
		}
		if(parinte=='*'){
			parcurgere.tip='F';
		}
		if(parinte=='+' || parinte=='-' || parinte=='='){
			parcurgere.tip='T';
		}
		if(parinte=='?' || parinte==':' || parinte=='>'){
			parcurgere.tip='N';
		}
		parcurgere.isOperator=false;
		parcurgere.variabila="";
		if(parcurgere.operator=='@'){
			parcurgere.variabila+="-";
		}
		parcurgere.variabila+=parcurgere.dr.variabila;
		parcurgere.dr=null;
	}
	
	/**Metoda afiseaza arborele de parsare in fisierul de output
	 * de la radacina pana la nivelul inaltimii lui.
	 * @param parcurgere
	 * Arborele care se doreste afisat in fisier.
	 */
	private void recursiveDisplay(Nod parcurgere){
		/*Se realizeaza afisarea dorita in enunt a expresiei
		 * la fiecare pas cu un nivel mai mult in arbore.
		 */
		condensare(parcurgere,'#');
		int h=initializareTipCalculInaltime(parcurgere,'#');
		for(int i=0; i<h; i++){
			afisareNrNivele(parcurgere, i,false);
			pwpt.println();
		}
	}
	
	/**Metoda scrie in fisierul de output parsarea expresiei pana la un nivel
	 * maxim de adancime in arbore egal cu nr.
	 * @param parcurgere
	 * Subarborele curent in care s-a ajuns cu procesarea afisarii.
	 * @param nr
	 * Cate nivele se mai poate inainta in adancime in arbore.
	 * @param paranteze
	 * Daca pe nivelul curent expresia de afisat trebuie sau nu incadrata de paranteze.
	 */
	private void afisareNrNivele(Nod parcurgere, int nr, boolean paranteze){
		/*Se afiseaza nr nivele din arbore prin recursivitate si se tin cont
		 * daca expresia ce trebuie afisata trebuie incadrata sau nu de paranteze
		 * (acest lucru pentru a nu afisa paranteze inutile si pentru a fi cat mai
		 * apropiata de o scriere realizata de un om).
		 */
		if(nr<0) return;
		if(parcurgere.isOperator==false || nr==0){
			pwpt.print(parcurgere.tip);
			return;
		}
		if(parcurgere.isOperator && parcurgere.operator=='='){
			afisareNrNivele(parcurgere.st,nr-1,false);
			pwpt.print('=');
			afisareNrNivele(parcurgere.dr,nr-1,false);
			return;
		}
		if(paranteze){
			pwpt.print("(");
		}
		if(parcurgere.isOperator && (parcurgere.operator=='+' || parcurgere.operator=='-')){
			if((parcurgere.st.isOperator && parcurgere.st.operator!=':' && parcurgere.st.operator!='@' && parcurgere.st.operator!='$') || !parcurgere.st.isOperator){
				afisareNrNivele(parcurgere.st,nr-1,false);
			}else if(parcurgere.st.isOperator && (parcurgere.st.operator==':' || parcurgere.st.operator=='@' || parcurgere.st.operator=='$')){
				afisareNrNivele(parcurgere.st,nr-1,true);
			}
			pwpt.print(parcurgere.operator);
			if((parcurgere.dr.isOperator && parcurgere.dr.operator!=':' && parcurgere.dr.operator!='@' && parcurgere.dr.operator!='$') || !parcurgere.dr.isOperator){
				afisareNrNivele(parcurgere.dr,nr-1,false);
			}else if(parcurgere.dr.isOperator && (parcurgere.dr.operator==':' || parcurgere.dr.operator=='@' || parcurgere.dr.operator=='$')){
				afisareNrNivele(parcurgere.dr,nr-1,true);
			}
		}
		
		if(parcurgere.isOperator && parcurgere.operator=='*'){
			if(!parcurgere.st.isOperator || (parcurgere.st.isOperator && parcurgere.st.operator=='*')){
				afisareNrNivele(parcurgere.st,nr-1,false);
			}else{
				afisareNrNivele(parcurgere.st,nr-1,true);
			}
			pwpt.print(parcurgere.operator);
			if(!parcurgere.dr.isOperator || (parcurgere.dr.isOperator && parcurgere.dr.operator=='*')){
				afisareNrNivele(parcurgere.dr,nr-1,false);
			}else{
				afisareNrNivele(parcurgere.dr,nr-1,true);
			}
		}
		
		if(parcurgere.isOperator && parcurgere.operator==':'){
			afisareNrNivele(parcurgere.st,nr-1,false); //Parametrii A, B si C de la ternar
 			pwpt.print(parcurgere.operator);//:
			if(!parcurgere.dr.isOperator){//Parametrul D de la ternar
				afisareNrNivele(parcurgere.dr,nr-1,false);
			}else{
				afisareNrNivele(parcurgere.dr,nr-1,true);
			}
		}
		
		if(parcurgere.isOperator && parcurgere.operator=='?'){
			afisareNrNivele(parcurgere.st,nr-1,true); //Parametrii A si B de la ternar
			pwpt.print(parcurgere.operator);//?
			if(!parcurgere.dr.isOperator){//Parametrul C de la ternar
				afisareNrNivele(parcurgere.dr,nr-1,false);
			}else{
				afisareNrNivele(parcurgere.dr,nr-1,true);
			}
		}
		
		if(parcurgere.isOperator && parcurgere.operator=='>'){
			if(!parcurgere.st.isOperator || (parcurgere.st.isOperator && parcurgere.st.operator!=':')){//Parametrul  A de la ternar
				afisareNrNivele(parcurgere.st,nr-1,false);
			}else{
				afisareNrNivele(parcurgere.dr,nr-1,true);
			}
			pwpt.print(parcurgere.operator);//?
			if(!parcurgere.dr.isOperator || (parcurgere.dr.isOperator && parcurgere.dr.operator!=':')){//Parametrul B de la ternar
				afisareNrNivele(parcurgere.dr,nr-1,false);
			}else{
				afisareNrNivele(parcurgere.dr,nr-1,true);
			}
		}
		
		if(parcurgere.isOperator && parcurgere.operator=='$'){
			pwpt.print('+');
			afisareNrNivele(parcurgere.dr,nr-1,true);
		}
		if(parcurgere.isOperator && parcurgere.operator=='@'){
			pwpt.print('-');
			afisareNrNivele(parcurgere.dr,nr-1,true);
		}
		if(paranteze){
			pwpt.print(")");
		}
	}
}
