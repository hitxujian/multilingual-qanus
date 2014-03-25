/**
 * 
 */
package ar.uba.dc.galli.qa.ml.utils.comparatos;

/**
 * compares strings by strict equality
 * @author julian
 *
 */
public class EqualNoPunctComparator extends BaseComparator {

	/**
	 * @param one of the string to compare
	 */
	public EqualNoPunctComparator(String input) {
		super(input);
		
	}
	
	public EqualNoPunctComparator() {
		super();

	}
	 /**
	   * Funcion que elimina acentos y caracteres especiales de
	   * una cadena de texto.
	   * @param input
	   * @return cadena de texto limpia de acentos y caracteres especiales.
	   */
	  public static String replace(String input) {
	    // Cadena de caracteres original a sustituir.
	    String original = "áéíóúñÁÉÍÓÚÑ_";
	    // Cadena de caracteres ASCII que reemplazarán los originales.
	    String ascii = "aeiounAEIOUN ";
	    String output = input;
	    for (int i=0; i<original.length(); i++) {
	        // Reemplazamos los caracteres especiales.
	        output = output.replace(original.charAt(i), ascii.charAt(i));
	    }//for i
	    return output;
	  }
	  
	  public static String remove(String input) {
		    // Cadena de caracteres original a sustituir.
		    String original = ".,;:?¡¿!\"&";
		    
		    String output = input.replace('¿', '?');
		    
		    for (int i=0; i<original.length(); i++) {
		        // Reemplazamos los caracteres especiales.
		        output = output.replaceAll("[\\p{Punct}&&[^()]]", "");		        		
		    }//for i
		    return output;
		  }
	 
	 public static String removeThis(String input, String thisOnes) 
	 {
	   // Cadena de caracteres original a sustituir.
		    
		    
		    return  input.replaceAll(thisOnes, "");		        		
		  }
	  
	public boolean compare(String other)
	{
		//the order determines that _ is ' ' and not ''
		return ignore_case? remove(replace(one)).compareToIgnoreCase(remove(replace(other))) == 0: 
								remove(replace(one)).compareTo(remove(replace(other))) == 0;
	}

	@Override
	public void subclass_constructor() {
		name = "eq-nopunk";
		method_confidence = 1.0;
		result_confidence = 1.0;
		symetric = true;
		exact = true; 
	}

}
