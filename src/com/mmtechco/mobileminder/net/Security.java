package com.mmtechco.mobileminder.net;

/**
 * The encryption logic and can be called by other classes when encryption or
 * decryption is needed.
 */
public class Security {
	final char valBACK[] = {(char)117,(char)123,(char)139,(char)164,(char)140,(char)191,(char)205,(char)134,(char)20,(char)224,(char)214,(char)120,(char)168,(char)225,(char)217,(char)157,(char)49,(char)72,(char)249,(char)131,(char)210,(char)23,(char)146,(char)239,(char)42,(char)76,(char)231,(char)74,(char)11,(char)43,(char)33,(char)245,(char)103,(char)208,(char)25,(char)113,(char)246,(char)218,(char)99,(char)219,(char)18,(char)221,(char)4,(char)6,(char)181,(char)151,(char)237,(char)67,(char)178,(char)202,(char)150,(char)212,(char)173,(char)235,(char)16,(char)216,(char)244,(char)78,(char)169,(char)41,(char)21,(char)58,(char)184,(char)64,(char)97,(char)7,(char)95,(char)177,(char)138,(char)209,(char)112,(char)200,(char)160,(char)96,(char)89,(char)88,(char)101,(char)71,(char)75,(char)30,(char)183,(char)179,(char)29,(char)201,(char)189,(char)44,(char)107,(char)132,(char)69,(char)222,(char)162,(char)161,(char)175,(char)176,(char)73,(char)167,(char)136,(char)126,(char)15,(char)128,(char)251,(char)159,(char)13,(char)116,(char)147,(char)165,(char)129,(char)110,(char)46,(char)34,(char)133,(char)242,(char)211,(char)3,(char)127,(char)172,(char)80,(char)105,(char)53,(char)137,(char)24,(char)50,(char)238,(char)79,(char)77,(char)234,(char)31,(char)171,(char)252,(char)135,(char)17,(char)108,(char)182,(char)38,(char)104,(char)170,(char)51,(char)250,(char)68,(char)196,(char)100,(char)190,(char)143,(char)70,(char)185,(char)52,(char)109,(char)57,(char)36,(char)111,(char)197,(char)86,(char)142,(char)240,(char)227,(char)156,(char)60,(char)254,(char)8,(char)121,(char)144,(char)90,(char)12,(char)81,(char)84,(char)163,(char)141,(char)114,(char)54,(char)174,(char)35,(char)82,(char)230,(char)87,(char)187,(char)215,(char)66,(char)236,(char)19,(char)85,(char)223,(char)102,(char)56,(char)207,(char)188,(char)83,(char)125,(char)155,(char)180,(char)228,(char)198,(char)5,(char)206,(char)98,(char)10,(char)232,(char)92,(char)55,(char)247,(char)9,(char)192,(char)241,(char)153,(char)145,(char)106,(char)233,(char)154,(char)199,(char)47,(char)124,(char)195,(char)93,(char)204,(char)94,(char)130,(char)27,(char)148,(char)115,(char)152,(char)1,(char)45,(char)14,(char)193,(char)253,(char)22,(char)158,(char)243,(char)166,(char)26,(char)61,(char)62,(char)63,(char)2,(char)118,(char)194,(char)149,(char)39,(char)203,(char)37,(char)248,(char)226,(char)255,(char)220,(char)91,(char)65,(char)119,(char)40,(char)229,(char)32,(char)213,(char)186,(char)122,(char)48,(char)28,(char)59};
	final char valFORW[] = {(char)220,(char)233,(char)114,(char)43,(char)192,(char)44,(char)66,(char)159,(char)200,(char)195,(char)29,(char)163,(char)103,(char)222,(char)99,(char)55,(char)131,(char)41,(char)179,(char)9,(char)61,(char)225,(char)22,(char)121,(char)35,(char)229,(char)216,(char)254,(char)83,(char)80,(char)127,(char)249,(char)31,(char)110,(char)171,(char)149,(char)239,(char)134,(char)237,(char)247,(char)60,(char)25,(char)30,(char)86,(char)221,(char)109,(char)209,(char)253,(char)17,(char)122,(char)137,(char)146,(char)119,(char)169,(char)198,(char)183,(char)148,(char)62,(char)255,(char)157,(char)230,(char)231,(char)232,(char)64,(char)245,(char)177,(char)48,(char)139,(char)89,(char)144,(char)78,(char)18,(char)95,(char)28,(char)79,(char)26,(char)125,(char)58,(char)124,(char)117,(char)164,(char)172,(char)186,(char)165,(char)180,(char)152,(char)174,(char)76,(char)75,(char)162,(char)244,(char)197,(char)212,(char)214,(char)67,(char)74,(char)65,(char)194,(char)39,(char)141,(char)77,(char)182,(char)33,(char)135,(char)118,(char)205,(char)87,(char)132,(char)147,(char)108,(char)150,(char)71,(char)36,(char)168,(char)218,(char)104,(char)1,(char)234,(char)246,(char)12,(char)160,(char)252,(char)2,(char)210,(char)187,(char)98,(char)115,(char)100,(char)107,(char)215,(char)20,(char)88,(char)111,(char)8,(char)130,(char)97,(char)120,(char)69,(char)3,(char)5,(char)167,(char)153,(char)143,(char)161,(char)204,(char)23,(char)105,(char)217,(char)236,(char)51,(char)46,(char)219,(char)203,(char)207,(char)188,(char)156,(char)16,(char)226,(char)102,(char)73,(char)92,(char)91,(char)166,(char)4,(char)106,(char)228,(char)96,(char)13,(char)59,(char)136,(char)128,(char)116,(char)53,(char)170,(char)93,(char)94,(char)68,(char)49,(char)82,(char)189,(char)45,(char)133,(char)81,(char)63,(char)145,(char)251,(char)175,(char)185,(char)85,(char)142,(char)6,(char)201,(char)223,(char)235,(char)211,(char)140,(char)151,(char)191,(char)208,(char)72,(char)84,(char)50,(char)238,(char)213,(char)7,(char)193,(char)184,(char)34,(char)70,(char)21,(char)113,(char)52,(char)250,(char)11,(char)176,(char)56,(char)15,(char)38,(char)40,(char)243,(char)42,(char)90,(char)181,(char)10,(char)14,(char)241,(char)155,(char)190,(char)248,(char)173,(char)27,(char)196,(char)206,(char)126,(char)54,(char)178,(char)47,(char)123,(char)24,(char)154,(char)202,(char)112,(char)227,(char)57,(char)32,(char)37,(char)199,(char)240,(char)19,(char)138,(char)101,(char)129,(char)224,(char)158,(char)242};
	
	/**
	 * Encrypts or decrypts specified string.
	 * 
	 * @param inputText
	 *            - string to be encrypted or decrypted.
	 * @param encrypt
	 *            - flag to indicate whether string should be encrypted or
	 *            decrypted.
	 * @return the encrypted or decrypted string.
	 */
	public String cryptFull(String inputText, boolean encrypt) {

		char[] charArray = inputText.toCharArray();

		int counter = 0;
		for (int countOutter = 0; countOutter < charArray.length; countOutter++) {
			if (encrypt && 255 < charArray[countOutter]) {
				charArray[countOutter] = ' ';
			}

			if (counter >= 4) {
				counter = 1;
			} else {
				counter++;
			}

			for (int countInner = 0; countInner < counter; countInner++) {
				charArray[countOutter] = crypt(charArray[countOutter], encrypt);
			}
		}

		return new String(charArray);
	}

	/**
	 * Specifies which character set to be used when encrypting and decrypting.
	 * It subtracts 1 from the char value
	 * 
	 * @param index
	 *            character position in the character set
	 * @param encrypt
	 *            specifies whether the character array should be encrypted or
	 *            decrpted.
	 * @return the character in the encrypt or decrypt character sets.
	 */
	private char crypt(char index, boolean encrypt) {
		return crypt(index - 1, encrypt);
	}

	/**
	 * Specifies which character set to be used when encrypting and decrypting.
	 * 
	 * @param index
	 *            character position in the character set
	 * @param encrypt
	 *            specifies whether the character array should be encrypted or
	 *            decrpted.
	 * @return the character in the encrypt or decrypt character sets.
	 */
	private char crypt(int index, boolean encrypt) {
		if (encrypt) {
			return valFORW[index];
		} else {
			return valBACK[index];
		}
	}
}
