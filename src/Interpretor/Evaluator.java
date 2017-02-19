package Interpretor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;

public class Evaluator {
	
	private String numeFisierScriereEE = null;
	
	private PrintWriter pwee = null;
	
	private HashMap<String,Integer> hmTabelValori = null;
	
	/**Constructorul initializeaza evaluatorul prin deschiderea fisierului in care se va scrie
	 * rezultatul evaluarii expresiilor si prin initializarea unui HashMap ce va tine evidenta 
	 * valoriilor variabilelor.
	 * @param numeFisierCitire
	 * Numele fisierului ce contine instructiunile de interpretat si va influenta numele fisierului de output.
	 */
	public Evaluator(String numeFisierCitire){
		//Se formeaza numele fisierului in care se va scrie rezultatul evaluarii expresiilor.
		int pozitiePunctExtensie = numeFisierCitire.indexOf('.');
		if(pozitiePunctExtensie!=-1){
			numeFisierScriereEE = numeFisierCitire.substring(0, pozitiePunctExtensie) + "_ee" + numeFisierCitire.substring(pozitiePunctExtensie);
		}else{
			numeFisierScriereEE = numeFisierCitire+"_ee";
		}
		//Initializare HashMap pentru mentinerea unui tabel cu valoriile asociate variabilelor.
		hmTabelValori = new HashMap<String,Integer>();
		
		//Se incearca deschiderea fisierului de output pentru evaluarea expresiilor.
		try{
			pwee = new PrintWriter(new File(numeFisierScriereEE));
		}catch(IOException e){
			System.out.println("Nu s-a putut deschide fisierul de scriere ee.");
			e.printStackTrace();
		}
	}
	
	/**Metoda pentru a termina evaluarea expresiilor in sensul inchiderii
	 * fisierului de output si salvarii continutului acestuia.
	 */
	public void close(){
		hmTabelValori=null;
		//Se inchide fisierul de output.
		pwee.close();
	}
	
	/**Metoda ce permite Interpretorului sa forteze evaluatorul sa scrie
	 * un mesaj de eroare atunci cand o instructiune nu poate fi executata
	 * (cand nu este corecta din punct de vedere semantic).
	 */
	public void nonCalculable(){
		//Se scrie eroare in caz ca o expresie este necalculabila.
		pwee.println("error");
	}
	
	/**Metoda atribuie variabilei rezultatul evaluarii expresiei in forma poloneza
	 * si scrie variabila si valoarea acesteia in fisierul de output.
	 * @param variabila
	 * Variabila careia i se face atribuirea evaluarii expresiei in forma poloneza.
	 * @param formaPoloneza
	 * Expresia in forma poloneza care se doreste evaluata pentru a se atribui rezultatul variabilei.
	 */
	public void evaluate(String variabila, String formaPoloneza){
		/*Evaluarea unei expresii din forma poloneza, scrierea rezultatului
		 * in fisierul de output si adaugarea sau reinnoirea valorii careia
		 * i s-a aplicat atribuirea expresiei.
		 */
		Integer rezultat = calculate(formaPoloneza);
		pwee.println(variabila + "=" + rezultat);
		hmTabelValori.put(variabila, rezultat);
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
	
	/**Metoda primeste o expresie in forma poloneza si intoarce rezultatul evaluarii acesteia.
	 * @param formaPoloneza
	 * Expresia in forma poloneza care se doreste evaluata.
	 * @return
	 * Un intreg ce reprezinta rezultatul evaluarii expresiei in forma poloneza.
	 */
	public Integer calculate(String formaPoloneza){
		/*Se initializeaza rezultatul si stiva in care se 
		 * va efectua calculul expresiei primita in forma poloneza.
		 */
		int rezultat = 0;
		Stack<Integer> numere = new Stack<Integer>();
		
		/*Se doreste descompunerea expresiei in token dupa spatii
		 * (similar in modul in care a fost construita).
		 */
		StringTokenizer st = new StringTokenizer(formaPoloneza);
		String variabila="";
		
		//Se incepe evaluarea expresiei.
		while(st.hasMoreTokens()){
			variabila=st.nextToken();
			if(esteNumar(variabila)){
				/*Daca se intalneste un numar intreg atunci acesta este
				 * pus in stiva.
				 */
				numere.push(Integer.parseInt(variabila));
			}else{
				if(variabila.equals("+")){
					/*La adunare (operator binar) se scot doua numere din varful stivei
					 * si se pune in stiva suma lor. 
					 */
					int a=numere.pop();
					int b=numere.pop();
					numere.push(b+a);
				}else if(variabila.equals("$")){
					/*La adunare (operator unar) se scoate numarul din varful stivei
					 * si se introduce acelasi numar in stiva.
					 */
					int a=numere.pop();
					numere.push(+a);
				}else if(variabila.equals("@")){
					/*La scadere (operator unar) se scoate numarul din varful stivei
					 * si se introduce opusul numarului respectiv in stiva.
					 */
					int a=numere.pop();
					numere.push(-a);
				}else if(variabila.equals("-")){
					/*La scadere (operator binar) se scot doua numere din varful stivei
					 * si se introduce diferenta dintre cele doua in stiva.
					 */
					int a=numere.pop();
					int b=numere.pop();
					numere.push(b-a);
				}else if(variabila.equals("*")){
					/*La inmultire se scot doua numere din varful stivei si se introduce
					 * in stiva produsul lor.
					 */
					int a=numere.pop();
					int b=numere.pop();
					numere.push(b*a);
				}else if(variabila.equals("#")){
					/*Cand se intalneste functia asociata operatorului ternar(#)
					 * se scot 4 numere din stiva, se evalueaza expresia si se 
					 * introduce rezultatul in stiva.
					 */
					int d = numere.pop();
					int c = numere.pop();
					int b = numere.pop();
					int a = numere.pop();
					if(a>b){
						numere.push(c);
					}else{
						numere.push(d);
					}
				}else{
					/*Daca s-a intalnit o variabila atunci se pune in stiva
					 * valoarea asociata acesteia(aceasta se obtine din 
					 * hashmap-ul tabel de valori).
					 */
					numere.push(hmTabelValori.get(variabila));
				}
			}
		}
		
		/*La final va ramane doar un intreg in stiva care va reprezenta
		 * rezultatul expresiei primite ca parametru.
		 */
		rezultat=numere.pop();
		return new Integer(rezultat);
	}
}
