import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�. <br>
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/**
	 * inst.data ������ �ҷ��� �����ϴ� ����. ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;

	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * 
	 * @param instFile : instruction�� ���� ���� ����� ���� �̸�
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}

	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 * 
	 * @param fileName : ������ �ϴ� ���� �̸�
	 */
	public void openFile(String fileName) {
		try {
			File file = new File(fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			/*instFile�� ���پ� �о���δ�.*/ 
			while ((line = bufReader.readLine()) != null) {
				Instruction inst = new Instruction(line);
				/*instMap�� ��ɾ�, ��ɾ��� ������ ���� Ű(key)�� value(��)���� �־��ش�.*/ 
				instMap.put(inst.instruction, inst);
			}
			bufReader.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	/**
	 * ��ɾ��� �̸��� ���� �ʿ��� ��ɾ��� ������ ã���ش�. 
	 * 
	 * @param Int : ã���� �ϴ� ��ɾ��� �̸�
	 */
	public Instruction searchInst(String Inst) {
		return instMap.get(Inst);
	}
}

/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����. 
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {
	/** instruction�� ���� ��ɾ����� ����*/
	String instruction;
	/** instruction�� �� ����Ʈ ��ɾ����� ���� */
	String format;
	/** instruction�� opcode ���� */
	String opcode;
	/** instruction�� ��� operand�� �������� ���� */
	int numberOfOperand;

	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * 
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}

	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * 
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		instruction = line.split("\t")[0];
		format = line.split("\t")[1];
		opcode = line.split("\t")[2];
		numberOfOperand = Integer.parseInt(line.split("\t")[3]);
	}
}
