package Interpretor;

public class MainClass {
	public static void main(String[] args){
		if(args.length==0){
			System.out.println("Parametrul obligatoriu este numele fisierului ce contine secventa de comenzi de interpretat.");
			return;
		}
		Interpretor inter = new Interpretor(args[0]);
		inter.interpret();
	}
}
