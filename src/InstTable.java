import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다. <br>
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/**
	 * inst.data 파일을 불러와 저장하는 공간. 명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;

	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * 
	 * @param instFile : instruction에 대한 명세가 저장된 파일 이름
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}

	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 * 
	 * @param fileName : 열고자 하는 파일 이름
	 */
	public void openFile(String fileName) {
		try {
			File file = new File(fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			/*instFile을 한줄씩 읽어들인다.*/ 
			while ((line = bufReader.readLine()) != null) {
				Instruction inst = new Instruction(line);
				/*instMap에 명령어, 명령어의 정보를 각각 키(key)와 value(값)으로 넣어준다.*/ 
				instMap.put(inst.instruction, inst);
			}
			bufReader.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	/**
	 * 명령어의 이름을 보고 필요한 명령어의 정보를 찾아준다. 
	 * 
	 * @param Int : 찾고자 하는 명령어의 이름
	 */
	public Instruction searchInst(String Inst) {
		return instMap.get(Inst);
	}
}

/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다. 
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
 */
class Instruction {
	/** instruction이 무슨 명령어인지 저장*/
	String instruction;
	/** instruction이 몇 바이트 명령어인지 저장 */
	String format;
	/** instruction의 opcode 저장 */
	String opcode;
	/** instruction이 몇개의 operand를 가지는지 저장 */
	int numberOfOperand;

	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * 
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}

	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * 
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line) {
		instruction = line.split("\t")[0];
		format = line.split("\t")[1];
		opcode = line.split("\t")[2];
		numberOfOperand = Integer.parseInt(line.split("\t")[3]);
	}
}
