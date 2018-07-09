import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Assembler : �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�. ���α׷��� ���� �۾��� ������ ����. <br>
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. <br>
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. <br>
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) <br>
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) <br><br><br>
 * 
 * �ۼ����� ���ǻ��� : <br>
 * 1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�.<br>
 * 2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 * 3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 * 4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br><br><br>
 * 
 * + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ���� */
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ���� */
	ArrayList<TokenTable> TokenList;
	/**
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����. <br>
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;

	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�.
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/**
	 * ������� ���� ��ƾ
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
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.<br>
	 * 
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printObjectCode(String fileName) {
		try {
			File file = new File(fileName);
			FileWriter filewriter = new FileWriter(file);
			//�� ���� �� object program code�� ���.*/
			for(int i = 0 ; i < TokenList.size(); i++){
				filewriter.write(codeList.get(i));
			}
			filewriter.flush();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.<br>
	 * 
	 * @param fileName : ����Ǵ� ���� �̸�
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
	 * pass1 ������ �����Ѵ�.<br>
	 * 1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����<br>
	 * 2) ���α׷� �ҽ� �� ���ο� location �� ����
	 * 3) label�� symbolTable�� ����<br><br><br>
	 * 
	 * ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		String label;
		String operator;
		for (int i = 0, k = -1; i < lineList.size(); i++) {
			label = (lineList.get(i)).split("\t")[0];
			if (!label.equals(".")) {
				operator = (lineList.get(i)).split("\t")[1];
				/*operator�� "START"�̰ų� "CSECT"�� �� ���ο� ������ ���۵ȴٰ� �����Ͽ� 
				 * symTabList�� TokenList�� ���� SymbolTable�� TokenTable�� �߰����ش�.*/ 
				if (operator.equals("START") || operator.equals("CSECT")) {
					k++;
					symtabList.add(new SymbolTable());
					TokenList.add(new TokenTable(symtabList.get(k), instTable));
				}
				TokenList.get(k).putToken(lineList.get(i));
			}
		}
		/* Token�� byteSize�� ����*/
		for (int i = 0; i < TokenList.size(); i++){
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				TokenList.get(i).setByteSize(j);
			}
		}
		/* Token�� location�� ���� */
		for (int i = 0; i < TokenList.size(); i++){
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++){
				TokenList.get(i).assignMemory(j);
			}
		}
		/* Section�� symbolTable ����*/
		for (int i = 0; i < TokenList.size(); i++) {
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				TokenList.get(i).makeSymbolTable(j);
				/* operator�� EQU�� �� symbol�� location�� �ٲ��ֱ�*/
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
	 * pass2 ������ �����Ѵ�.<br>
	 * 1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		/*�� Token�� objectCode�� ����*/
		for (int i = 0; i < TokenList.size(); i++) {
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				TokenList.get(i).makeObjectCode(j);
			}
		}
		/* codeList ����� */
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
			/*�� ���� �� object program code�� codeList�� �߰�����.*/
			codeList.add(code);
		}
	}

	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.<br>
	 * 
	 * @param inputFile : input ���� �̸�.
	 */
	private void loadInputFile(String inputFile) {
		try {
			File file = new File(inputFile);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			/*inputFile�� �� �پ� �о�鿩 lineList�� �߰����ش�.*/
			while ((line = bufReader.readLine()) != null)
				lineList.add(line);
			bufReader.close(); 
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
