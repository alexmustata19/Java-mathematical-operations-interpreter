package Interpretor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

public class AnalizatorSemantic {
	
	private String numeFisierScriereSA = null;
	
	private PrintWriter pwsa = null;
	
	private HashSet<String> hsEvidentaVariabile = null;
	
	/**Constructorul initializeaza analizatorul semantic prin deschiderea fisierului in care se va scrie
	 * rezultatul analizei semantice si prin initializarea unui HashSet ce va tine evidenta 
	 * variabilelor declarate.
	 * @param numeFisierCitire
	 * Numele fisierului ce contine instructiunile de interpretat si va influenta numele fisierului de output.
	 */
	public AnalizatorSemantic(String numeFisierCitire){		
		//Se formeaza numele fisierului in care se va scrie analiza.
		int pozitiePunctExtensie = numeFisierCitire.indexOf('.');
		if(pozitiePunctExtensie!=-1){
			numeFisierScriereSA = numeFisierCitire.substring(0, pozitiePunctExtensie) + "_sa" + numeFisierCitire.substring(pozitiePunctExtensie);
		}else{
			numeFisierScriereSA = numeFisierCitire+"_sa";
		}
		
		//Initializare HashSet pentru mentinerea evidentei variabilelor declarate. 
		hsEvidentaVariabile = new HashSet<String>();
				
		//Se incearca deschiderea fisierului de output pentru analiza semantica.
		try{
			pwsa = new PrintWriter(new File(numeFisierScriereSA));
		}catch(IOException e){
			System.out.println("Nu s-a putut deschide fisierul de scriere sa.");
			e.printStackTrace();
		}
	}
	
	/**Metoda pentru a termina analiza semantica in sensul inchiderii
	 * fisierului de output si salvarii continutului acestuia.
	 */
	public void close(){
		hsEvidentaVariabile=null;
		//Se inchide fisierul de output.
		pwsa.close();
	}
	
	/**Metoda ce analizeaza din punct de vedere semantic o instructiune ce se afla pe o linie.
	 * @param linie
	 * Linia care se doreste a fi analizata din punct de vedere semantic.
	 * @param indiceLinie
	 * Numarul liniei la care s-a ajuns cu analizarea semantica.
	 * @return
	 * Metoda intoarce true sau false in functie de corectitudinea semantica a instructiunii. 
	 */
	public boolean analyzeRow(String linie, int indiceLinie){
		int pozitieCursor = 1;
		int pozitieEgal = linie.indexOf('=');
		//Se obtine variabila careia trebuie sa i se faca declararea (memorarea in hashset).
		String deDeclarat = linie.substring(0, pozitieEgal);
		
		//Se obtine expresia din care trebuie analizata existenta variabilelor.
		String verificareAtribuire = linie.substring(pozitieEgal);
		
		//Daca primul caracter din membrul stang este o cifra automat membrul stang nu este o variabila.
		if(Character.isDigit(deDeclarat.charAt(0))){
			pwsa.println("membrul stang nu este o variabila la linia "+ indiceLinie +" coloana 1");
			return false;
		}
		
		//Se incepe analiza existentei variabilelor din expresie.
		String variabila = "";
		while(pozitieCursor < verificareAtribuire.length()){
			char c = verificareAtribuire.charAt(pozitieCursor);
			
			//Se trece peste caracterele ce nu pot alcatui o variabila.
			if(variabila.equals("") && 
				(Character.isDigit(c) || c=='+' || c=='-' || c=='*' || c=='(' || c==')' || c=='?' || c=='>' || c==':')){
				pozitieCursor++;
				continue;
			}
			
			/*Daca s-a gasit o litera inseamana ca am ajuns la inceputul
			 * numelui unei variabile.
			 */
			int pozitieInceputVariabila = 0;
			if(variabila.equals("") && Character.isLetter(c)){
				//Se alcatuieste numele variabilei si se retine coloana la care a aparut.
				variabila+=c;
				pozitieInceputVariabila = pozitieCursor;
				pozitieCursor++;
				while(pozitieCursor < verificareAtribuire.length()){
					c = verificareAtribuire.charAt(pozitieCursor);
					if(Character.isDigit(c) || Character.isLetter(c)){
						variabila+=c;
						pozitieCursor++;
					}else{
						pozitieCursor++;
						break;
					}
				}
			}
			
			if(hsEvidentaVariabile.contains(variabila)){
				/*Daca variabila a fost declarata intr-o instructiune precedenta
				 * atunci se continua cu analiza expresiei.
				 */
				variabila="";
				continue;
			}else{
				/*Daca variabila nu exista in hashset inseamna ca acesta nu a fost
				 * declarata intr-o instructiune anterioara si se semnaleaza eroarea.
				 */
				pwsa.println(variabila + " nedeclarata la linia " + indiceLinie + " coloana " + (pozitieInceputVariabila+pozitieEgal+1));
				return false;
			}
		}
		
		/*Daca nu a fost semnalata nicio eroare insemana ca expresia
		 * este corecta din punct de vedere semantic si atunci se si 
		 * declara variabila careia i se face atribuirea.
		 */
		pwsa.println("Ok!");
		hsEvidentaVariabile.add(deDeclarat);
		return true;
	}
}
