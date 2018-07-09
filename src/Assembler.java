import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Assembler : 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다. 프로그램의 수행 작업은 다음과 같다. <br>
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. <br>
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. <br>
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) <br>
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) <br><br><br>
 * 
 * 작성중의 유의사항 : <br>
 * 1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.<br>
 * 2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 * 3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 * 4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br><br><br>
 * 
 * + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간 */
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간 */
	ArrayList<TokenTable> TokenList;
	/**
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. <br>
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;

	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름.
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/**
	 * 어셈블러의 메인 루틴
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");

		assembler.pass1();
		assembler.printSymbolTable("symtab_20160311");

		assembler.pass2();
		assembler.printObjectCode("output_20160311");
	}

	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.<br>
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		try {
			File file = new File(fileName);
			FileWriter filewriter = new FileWriter(file);
			//각 섹션 별 object program code를 출력.*/
			for(int i = 0 ; i < TokenList.size(); i++){
				filewriter.write(codeList.get(i));
			}
			filewriter.flush();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.<br>
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		try {
			File file = new File(fileName);
			FileWriter filewriter = new FileWriter(file);
			for (int i = 0; i < symtabList.size(); i++) {
				for (int j = 0; j < symtabList.get(i).symbolList.size(); j++) {
					filewriter.write(symtabList.get(i).symbolList.get(j) + "\t\t");
					String location = String.format("%X", symtabList.get(i).locationList.get(j));
					filewriter.write(location);
					filewriter.write("\r\n");
				}
				filewriter.write("\r\n");
			}
			filewriter.flush();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * pass1 과정을 수행한다.<br>
	 * 1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성<br>
	 * 2) 프로그램 소스 각 라인에 location 값 지정
	 * 3) label을 symbolTable에 정리<br><br><br>
	 * 
	 * 주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		String label;
		String operator;
		for (int i = 0, k = -1; i < lineList.size(); i++) {
			label = (lineList.get(i)).split("\t")[0];
			if (!label.equals(".")) {
				operator = (lineList.get(i)).split("\t")[1];
				/*operator가 "START"이거나 "CSECT"일 때 새로운 섹션이 시작된다고 간주하여 
				 * symTabList와 TokenList에 각각 SymbolTable과 TokenTable을 추가해준다.*/ 
				if (operator.equals("START") || operator.equals("CSECT")) {
					k++;
					symtabList.add(new SymbolTable());
					TokenList.add(new TokenTable(symtabList.get(k), instTable));
				}
				TokenList.get(k).putToken(lineList.get(i));
			}
		}
		/* Token별 byteSize값 설정*/
		for (int i = 0; i < TokenList.size(); i++){
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				TokenList.get(i).setByteSize(j);
			}
		}
		/* Token별 location값 지정 */
		for (int i = 0; i < TokenList.size(); i++){
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++){
				TokenList.get(i).assignMemory(j);
			}
		}
		/* Section별 symbolTable 생성*/
		for (int i = 0; i < TokenList.size(); i++) {
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				TokenList.get(i).makeSymbolTable(j);
				/* operator가 EQU일 떄 symbol의 location값 바꿔주기*/
				if (TokenList.get(i).getToken(j).operator.equals("EQU")) {
					if (!TokenList.get(i).getToken(j).operand[0].equals("*")) {
						String[] str = TokenList.get(i).getToken(j).operand[0].split("-");
						int a = symtabList.get(i).search(str[0]);
						int b = symtabList.get(i).search(str[1]);
						symtabList.get(i).modifySymbol(TokenList.get(i).getToken(j).label, a-b);
					}
				}
			}
		}
	}

	/**
	 * pass2 과정을 수행한다.<br>
	 * 1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		/*각 Token별 objectCode를 생성*/
		for (int i = 0; i < TokenList.size(); i++) {
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				TokenList.get(i).makeObjectCode(j);
			}
		}
		/* codeList 만들기 */
		for (int i = 0; i < TokenList.size(); i++) {
			String code = "";
			String text = "";
			int size = 0;
			int progLength = 0;
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				/*Header record*/
				if (TokenList.get(i).getToken(j).operator.equals("START") || TokenList.get(i).getToken(j).operator.equals("CSECT")) {
					for (int k = TokenList.get(i).tokenList.size() - 1; k >= 0; k--) {
						if (!TokenList.get(i).tokenList.get(k).operator.equals("EQU")) {
							if (!TokenList.get(i).tokenList.get(k).operator.equals("RESB"))
								progLength = TokenList.get(i).tokenList.get(k).location + TokenList.get(i).tokenList.get(k).byteSize;
							else
								progLength = TokenList.get(i).tokenList.get(k).location + Integer.parseInt(TokenList.get(i).getToken(k).operand[0]);
							break;
						}
					}
					code = "H" + TokenList.get(i).getToken(j).label +"\t" + String.format("%06X", TokenList.get(i).getToken(j).location);
					code += String.format("%06X", progLength) + "\r\n";
				/*Define record*/
				} else if (TokenList.get(i).getToken(j).operator.equals("EXTDEF")) {
					code += "D";
					String[] def = TokenList.get(i).getToken(j).operand[0].split(",");
					for (int k = 0; k < def.length; k++)
						code += def[k] + String.format("%06X", symtabList.get(i).search(def[k]));
					code += "\r\n";
				/*Refer record*/
				} else if (TokenList.get(i).getToken(j).operator.equals("EXTREF")) {
					code += "R";
					String[] def = TokenList.get(i).getToken(j).operand[0].split(",");
					for (int k = 0; k < def.length; k++)
						code += def[k];
					code += "\r\n";
				/*Text record*/
				} else {
					if (TokenList.get(i).getToken(j).objectCode.length() != 0) {
						if (text.length() == 0)
							code += "T" + String.format("%06X", TokenList.get(i).getToken(j).location);
						text += TokenList.get(i).getToken(j).objectCode;
						size += TokenList.get(i).getToken(j).byteSize;
						if (j == TokenList.get(i).tokenList.size() - 1)
							code += String.format("%02X", size) + text + "\r\n";
						else if ((size + TokenList.get(i).getToken(j + 1).byteSize >= 30)
								|| ((TokenList.get(i).getToken(j + 1).objectCode.length() == 0) && (!TokenList.get(i).getToken(j + 1).operator.equals("END")))) {
							code += String.format("%02X", size) + text + "\r\n";
							text = "";
							size = 0;
						}
					}

					if (j == TokenList.get(i).tokenList.size() - 1) {
						/* Modification record*/
						for (int k = 0; k < TokenList.get(i).tokenList.size(); k++) {
							if (TokenList.get(i).tokenList.get(k).operator.charAt(0) == '+')
								code += "M" + String.format("%06X", TokenList.get(i).tokenList.get(k).location + 1) + "05+" + TokenList.get(i).tokenList.get(k).operand[0] + "\r\n";
							else if (TokenList.get(i).tokenList.get(k).operator.equals("WORD")) {
								String[] str = TokenList.get(i).tokenList.get(k).operand[0].split("-");
								for (int m = 0; m < str.length; m++) {
									if (m == 0)
										code += "M" + String.format("%06X", TokenList.get(i).tokenList.get(k).location) + "06+" + str[m] + "\r\n";
									else if (m == 1)
										code += "M" + String.format("%06X", TokenList.get(i).tokenList.get(k).location) + "06-" + str[m] + "\r\n";
								}
							}
						}
						/*End record*/
						code += "E";
						if (i == 0)
							code += String.format("%06X", TokenList.get(0).getToken(0).location);
						code += "\r\n\r\n";
					}
				}
			}
			/*각 섹션 별 object program code를 codeList에 추가해줌.*/
			codeList.add(code);
		}
	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.<br>
	 * 
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		try {
			File file = new File(inputFile);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			/*inputFile을 한 줄씩 읽어들여 lineList에 추가해준다.*/
			while ((line = bufReader.readLine()) != null)
				lineList.add(line);
			bufReader.close(); 
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
