import java.util.ArrayList;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ��̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/* bit ������ �������� ���� ���� */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/** Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	InstTable instTab;

	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;

	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� instTable�� ��ũ��Ų��.
	 * 
	 * @param symTab : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab) {
		tokenList = new ArrayList<Token>();
		this.symTab = symTab;
		this.instTab = instTab;
	}

	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * 
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		boolean isOverlap = false;
		tokenList.add(new Token(line));
		/* ���ͷ� ó�� */
		if (getToken(tokenList.size() - 1).operator.equals("LTORG")
				|| getToken(tokenList.size() - 1).operator.equals("END") ||  getToken(tokenList.size() - 1).operator.equals("CSECT")) {
			for (int i = 0; i < tokenList.size(); i++) {
				if (getToken(i).operand[0] != null && getToken(i).operand[0].charAt(0) == '=') {
					for (int j = 0; j < tokenList.size(); j++) {
						if (getToken(i).operand[0].equals(getToken(j).operator)) {
							isOverlap = true;
							break;
						}
					}
					/*���ͷ��� tokenTable�� ����Ǿ� ���� ���� ��쿡 tokenTable�� �߰����ش�. 
					 * (�ߺ��Ǵ� ���ͷ� �߰� �������� isOverlap ���� ���)*/ 
					if (!isOverlap) {
						line = "*\t" + getToken(i).operand[0] + "\t\t";
						tokenList.add(new Token(line));
					}
				}
			}
		}
	}

	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * 
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * ���ڷ� ���޵� literal��  �ּҸ� �˷��ش�.
	 * 
	 * @param literal : �˻��� ���ϴ� Token�� operand
	 * @return ���ͷ��� �ּҸ� ��ȯ���ش�. �ش� literal�� ���� ��� -1 ����
	 */
	public int searchLiteral(String literal) {
		int address = -1;
		for (int i = 0; i < tokenList.size(); i++) {
			if (tokenList.get(i).operator.equals(literal)) {
				address = tokenList.get(i).location;
				break;
			}
		}
		return address;
	}
	
	/**
	 * tokenList���� index�� �ش��ϴ� Token�� byteSize�� �����Ѵ�.
	 * 
	 * @param index
	 */
	public void setByteSize(int index) {
		if (getToken(index).operator.equals("BYTE")) {
			if (getToken(index).operand[0].charAt(0) == 'C') {
				getToken(index).byteSize = (getToken(index).operand[0].length() - 4) * 2;
			} else if (getToken(index).operand[0].charAt(0) == 'X') {
				getToken(index).byteSize = (getToken(index).operand[0].length() - 3) / 2;
			}
		} else if (getToken(index).operator.equals("WORD")) {
			getToken(index).byteSize = 3;
			/*���ͷ��� �� */
		} else if (getToken(index).operator.charAt(0) == '=') {
			if (getToken(index).operator.charAt(1) == 'C') {
				getToken(index).byteSize = getToken(index).operator.length() - 4;
			} else if (getToken(index).operator.charAt(1) == 'X') {
				getToken(index).byteSize = (getToken(index).operator.length() - 4) / 2;
			}
		} else {
			/*1,2,3���� ��ɾ� �� �� */
			if (instTab.instMap.containsKey(getToken(index).operator)) {
				int format = instTab.searchInst(getToken(index).operator).format.charAt(0) - '0';
				getToken(index).byteSize = format;
			/*4���� ��ɾ� �� �� */
			} else if (getToken(index).operator.charAt(0) == '+') {
				getToken(index).byteSize = 4;
			}
		}
	}
	
	/**
	 * tokenList���� index�� �ش��ϴ� Token�� location ���� �����Ѵ�.
	 * 
	 * (index-1)��°�� ��ɾ� �Ǵ� ���þ ���� index��°�� location���� ��������.
	 * 
	 * @param index
	 */
	public void assignMemory(int index) {
		/* location ó�� */
		if (!getToken(index).operator.equals("EXTDEF") || !getToken(index).operator.equals("EXTREF")) {
			/*�� ������ ���� �κп��� location counter���� 0���� �ʱ�ȭ�����ִ� ����*/
			if (index == 0)
				getToken(index).location = 0;
			else {
				if (getToken(index - 1).operator.equals("RESW")) {
					getToken(index).location = getToken(index - 1).location + (Integer.parseInt(getToken(index - 1).operand[0]) * 3);
				} else if (getToken(index - 1).operator.equals("RESB")) {
					getToken(index).location = getToken(index - 1).location + Integer.parseInt(getToken(index - 1).operand[0]);
				} else if (getToken(index - 1).operator.equals("LTORG") || getToken(index - 1).operator.equals("END")) {
					getToken(index).location = getToken(index - 1).location;
				} else{
					getToken(index).location = getToken(index - 1).location + getToken(index-1).byteSize;
				}
			}
			System.out.println(getToken(index).location);
		}
	}

	/**
	 * tokenList���� index�� �ش��ϴ� Token�� symbol�� �ش��ϸ� symbolTable�� �־��ش�.
	 * 
	 * @param index
	 */
	public void makeSymbolTable(int index){
		/*Token�� label�� null�̰ų� *(���ͷ��� ���)�� �����Ѵ�.*/
		if ((tokenList.get(index).label.length() >= 1) && (!tokenList.get(index).label.equals("*"))){
			symTab.putSymbol(tokenList.get(index).label, tokenList.get(index).location);
		}
	}

	/**
	 * Pass2 �������� ����Ѵ�.
	 *  
	 * instruction table, symbol table ���� �����Ͽ� objectCode�� �����ϰ�, �̸� �����Ѵ�.
	 * 
	 * @param index
	 */
	public void makeObjectCode(int index) {
		int obcode = 0; // ��ɾ��� opcode�� nixbpe, disp �� �Ǵ� �������� ���� ��Ʈ�����ϱ����� �ʿ��� �����̸� 0���� �ʱ�ȭ �����ش�.*/
		/* operator�� ��ɾ� �϶�(2, 3, 4����) */
		if ((instTab.instMap.containsKey(tokenList.get(index).operator))|| tokenList.get(index).operator.charAt(0) == '+') {
			/* opcode ���� */
			String str_inst; // ��ɾ��� operation�� �����ϴ� ����
			String str_format; // ��ɾ��� format�� �����ϴ� ����
			String str_opcode; // ��ɾ��� opcode�� �����ϴ� ����
			if (tokenList.get(index).operator.charAt(0) == '+') {
				str_inst = tokenList.get(index).operator.substring(1, tokenList.get(index).operator.length());
				str_format = "4";
			} else {
				str_inst = tokenList.get(index).operator;
				str_format = instTab.instMap.get(str_inst).format.substring(0, 1);
			}
			str_opcode = instTab.searchInst(str_inst).opcode;
			/*��ɾ��� opcode�� 1)0~9�϶��� '0'��, 2)a~f�϶��� 55�� ���� shift������ ���� ���� obcode ������ �����־���.*/ 
			for (int k = 0; k < 2; k++) {
				if (str_opcode.charAt(k) >= '0' && str_opcode.charAt(k) <= '9'){
					obcode |= ((str_opcode.charAt(k) - '0') << ((tokenList.get(index).byteSize * 2) - (k + 1)) * 4);
				}else{
					obcode|= ((str_opcode.charAt(k) - 55) << ((tokenList.get(index).byteSize * 2) - (k + 1)) * 4);
				}
			}

			/* 3,4���� �� �� n,i,x,b,p,e �� ���� */
			if (str_format.equals("3") || str_format.equals("4")) {
				// disp�� b,p flag �� �������ֱ� ���� �ʿ��� ���� 
				int ta = 0; // target �� 
				int pc = 0; // ���� Token�� location ��
				//char���� nixbpe�� ��Ʈ �̵������� �ϱ� ����  nixbpe�� int������ �Ű��ֱ� ���� ����
				int x = 0; 
				
				/* n, i ���� */
				if (tokenList.get(index).operator.equals("RSUB")) {
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(iFlag, 1);
				}else if (tokenList.get(index).operand[0].charAt(0) == '#') {
					tokenList.get(index).setFlag(nFlag, 0);
					tokenList.get(index).setFlag(iFlag, 1);
				}else if (tokenList.get(index).operand[0].charAt(0) == '@') {
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(iFlag, 0);
				}else {
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(iFlag, 1);
				}
				/* x ���� */
				if (tokenList.get(index).operand[2] != null) {
					if (tokenList.get(index).operand[2].equals("X")){
						tokenList.get(index).setFlag(xFlag, 1);
					}else
						tokenList.get(index).setFlag(xFlag, 0);
				}else{
					tokenList.get(index).setFlag(xFlag, 0);
				}
				/* b, p ���� */
				if ((tokenList.get(index).operator.charAt(0) == '+') || tokenList.get(index).operator.equals("RSUB")) {
					tokenList.get(index).setFlag(bFlag, 0);
					tokenList.get(index).setFlag(pFlag, 0);
				}else {
					if (tokenList.get(index).operand[0].charAt(0) != '#') {
						String str_operand;
						if (tokenList.get(index).operand[0].charAt(0) == '@'){
							str_operand = tokenList.get(index).operand[0].substring(1, tokenList.get(index).operand[0].length());
						}else{
							str_operand = tokenList.get(index).operand[0];
						}
						if (tokenList.get(index).operand[0].charAt(0) != '='){
							ta = symTab.search(str_operand);
						}else{ // operand[0]�� ���ͷ��� ���
							ta = searchLiteral(str_operand);
						}
						pc = tokenList.get(index + 1).location;

						if ((ta - pc >= -2048) && (ta - pc <= 2047)) {
							tokenList.get(index).setFlag(pFlag, 1);
							tokenList.get(index).setFlag(bFlag, 0);
						}else {
							tokenList.get(index).setFlag(pFlag, 0);
							tokenList.get(index).setFlag(bFlag, 1);
						}
					}else {
						tokenList.get(index).setFlag(pFlag, 0);
						tokenList.get(index).setFlag(bFlag, 0);
					}
				}

				/* e ���� */
				if (tokenList.get(index).operator.charAt(0) == '+'){
					tokenList.get(index).setFlag(eFlag, 1);
				}else{
					tokenList.get(index).setFlag(eFlag, 0);
				}
				
				x = tokenList.get(index).nixbpe;
				/*obcode�� opcode�� nixbpe���� ��. */
				obcode |= x << ((tokenList.get(index).byteSize * 2 - 3) * 4);
				
				/* disp ���� */
				if (str_format.equals("4")) {
					obcode |= 0;
				} else {
					if (tokenList.get(index).getFlag(pFlag) == 2) { // pFlag��  1�� ������ ���
						if (ta >= pc){
							obcode |= ta - pc;
						}else{
							obcode |= ((ta - pc) & 0x00000FFF);
						}
					}else { // ex) operand[0]�� #0 �Ǵ� #3�� ���
						if (tokenList.get(index).operand[0] != null) {
							int z;
							z = Integer.parseInt(tokenList.get(index).operand[0].substring(1, tokenList.get(index).operand[0].length()));
							obcode |= z;
						}
					}
				}
				tokenList.get(index).objectCode = String.format("%06X", obcode);
			}
			/* 2�����϶� �������� �� ���� */
			else {
				int x = 0;
				for (int k = 0; k < 2; k++) {
					if (tokenList.get(index).operand[k] != null) {
						if (tokenList.get(index).operand[k].equals("A")){
							x |= 0;
						}else if(tokenList.get(index).operand[k].equals("X")){
							x |= 1;
						}else if (tokenList.get(index).operand[k].equals("L")){
							x |= 2;
						}else if (tokenList.get(index).operand[k].equals("B")){
							x |= 3;
						}else if (tokenList.get(index).operand[k].equals("S")){
							x |= 4;
						}else if (tokenList.get(index).operand[k].equals("T")){
							x |= 5;
						}else if (tokenList.get(index).operand[k].equals("F")){
							x |= 6;
						}else if (tokenList.get(index).operand[k].equals("PC")){
							x |= 8;
						}else if (tokenList.get(index).operand[k].equals("SW")){
							x |= 9;
						}if (k == 0){
							x = x << 4;
						}
					}
				}
				obcode |= x;
				tokenList.get(index).objectCode = String.format("%04X", obcode);
			}
		}
		/* ��ɾ� �ƴ� �� */
		else {
			if(tokenList.get(index).operator.equals("BYTE")){
				tokenList.get(index).objectCode = tokenList.get(index).operand[0].substring(2, tokenList.get(index).operand[0].length()-1);
			}else if(tokenList.get(index).operator.equals("WORD")){
				String[] str = tokenList.get(index).operand[0].split("-");
				int a = symTab.search(str[0]);
				int b = symTab.search(str[1]);
				if((a  == -1) && (b == -1)){
					tokenList.get(index).objectCode = String.format("%06X", obcode);
				}else if( (a != -1)&& (b != -1)){
					tokenList.get(index).objectCode = String.format("%06X", a-b);
				}
			//���ͷ��� ���
			}else if(tokenList.get(index).operator.charAt(0) == '='){
				if(tokenList.get(index).operator.charAt(1) == 'X'){
					tokenList.get(index).objectCode = tokenList.get(index).operator.substring(3, tokenList.get(index).operator.length()-1);
				}else if(tokenList.get(index).operator.charAt(1) == 'C'){
					for(int i = 3 ; i < tokenList.get(index).operator.length()-1 ; i++){
						tokenList.get(index).objectCode+=String.format("%02X",(int)tokenList.get(index).operator.charAt(i));
					}
				}
			}		
		}
	}

	/**
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * 
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ �� �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. �ǹ� �ؼ��� ������ pass2����
 * object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token {
	// �ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code ���� �ܰ迡�� ���Ǵ� ������
	String objectCode;
	int byteSize;

	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�.
	 * 
	 * @param line : ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		// ���� initialize
		location = 0;
		label = "";
		operator = "";
		operand = new String[3];
		comment = "";
		nixbpe = 0;
		byteSize = 0;
		objectCode = "";
		parsing(line);
	}

	/**
	 * line�� ��ū���� ��� �����ϴ� �Լ�.
	 * 
	 * @param line : ��������� ����� ���α׷� �ڵ�.
	 */
	public void parsing(String line) {
		String str;
		label = line.split("\t")[0];
		operator = line.split("\t")[1];
		/*operand�� �������� �ʴ� operator ����*/
		if (!operator.equals("LTORG") && !operator.equals("CSECT") && !operator.equals("RSUB") && !label.equals("*")) {
			str = line.split("\t")[2];
			if (operator.equals("EXTDEF") || operator.equals("EXTREF") || (str.split(",").length == 1)){
				operand[0] = str;
			}else {
				operand[0] = str.split(",")[0];
				/*��ɾ X�������͸� ����ϸ�  "X"�� operand[2]�� ����*/
				if (str.split(",")[1].equals("X")){
					operand[2] = str.split(",")[1];
				}else{
					operand[1] = str.split(",")[1];
				}
			}
		}
		/*comment�� �����ϴ� ���*/
		if (line.split("\t").length == 4){
			comment = line.split("\t")[3];
		}
	}

	/**
	 * n,i,x,b,p,e flag�� �����Ѵ�. <br><br>
	 * 
	 * ��� �� : setFlag(nFlag, 1); <br>
	 * �Ǵ� setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
	 */
	public void setFlag(int flag, int value) {
		if (value == 1){
			this.nixbpe |= flag;
		}
	}

	/**
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� <br><br>
	 * 
	 * ��� �� : getFlag(nFlag) <br>
	 * �Ǵ� getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
