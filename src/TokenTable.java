import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/** Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	InstTable instTab;

	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;

	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * 
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab) {
		tokenList = new ArrayList<Token>();
		this.symTab = symTab;
		this.instTab = instTab;
	}

	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * 
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		boolean isOverlap = false;
		tokenList.add(new Token(line));
		/* 리터럴 처리 */
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
					/*리터럴이 tokenTable에 저장되어 있지 않은 경우에 tokenTable에 추가해준다. 
					 * (중복되는 리터럴 추가 방지위해 isOverlap 변수 사용)*/ 
					if (!isOverlap) {
						line = "*\t" + getToken(i).operand[0] + "\t\t";
						tokenList.add(new Token(line));
					}
				}
			}
		}
	}

	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * 
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * 인자로 전달된 literal의  주소를 알려준다.
	 * 
	 * @param literal : 검색을 원하는 Token의 operand
	 * @return 리터럴의 주소를 반환해준다. 해당 literal이 없을 경우 -1 리턴
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
	 * tokenList에서 index에 해당하는 Token의 byteSize를 지정한다.
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
			/*리터럴일 때 */
		} else if (getToken(index).operator.charAt(0) == '=') {
			if (getToken(index).operator.charAt(1) == 'C') {
				getToken(index).byteSize = getToken(index).operator.length() - 4;
			} else if (getToken(index).operator.charAt(1) == 'X') {
				getToken(index).byteSize = (getToken(index).operator.length() - 4) / 2;
			}
		} else {
			/*1,2,3형식 명령어 일 때 */
			if (instTab.instMap.containsKey(getToken(index).operator)) {
				int format = instTab.searchInst(getToken(index).operator).format.charAt(0) - '0';
				getToken(index).byteSize = format;
			/*4형식 명령어 일 때 */
			} else if (getToken(index).operator.charAt(0) == '+') {
				getToken(index).byteSize = 4;
			}
		}
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token의 location 값을 지정한다.
	 * 
	 * (index-1)번째의 명령어 또는 지시어에 따라 index번째의 location값이 정해진다.
	 * 
	 * @param index
	 */
	public void assignMemory(int index) {
		/* location 처리 */
		if (!getToken(index).operator.equals("EXTDEF") || !getToken(index).operator.equals("EXTREF")) {
			/*각 섹션의 시작 부분에서 location counter값을 0으로 초기화시켜주는 역할*/
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
	 * tokenList에서 index에 해당하는 Token이 symbol에 해당하면 symbolTable에 넣어준다.
	 * 
	 * @param index
	 */
	public void makeSymbolTable(int index){
		/*Token의 label이 null이거나 *(리터럴일 경우)를 제외한다.*/
		if ((tokenList.get(index).label.length() >= 1) && (!tokenList.get(index).label.equals("*"))){
			symTab.putSymbol(tokenList.get(index).label, tokenList.get(index).location);
		}
	}

	/**
	 * Pass2 과정에서 사용한다.
	 *  
	 * instruction table, symbol table 등을 참조하여 objectCode를 생성하고, 이를 저장한다.
	 * 
	 * @param index
	 */
	public void makeObjectCode(int index) {
		int obcode = 0; // 명령어의 opcode와 nixbpe, disp 값 또는 레지스터 값을 비트연산하기위해 필요한 변수이며 0으로 초기화 시켜준다.*/
		/* operator가 명령어 일때(2, 3, 4형식) */
		if ((instTab.instMap.containsKey(tokenList.get(index).operator))|| tokenList.get(index).operator.charAt(0) == '+') {
			/* opcode 설정 */
			String str_inst; // 명령어의 operation을 저장하는 변수
			String str_format; // 명령어의 format을 저장하는 변수
			String str_opcode; // 명령어의 opcode를 저장하는 변수
			if (tokenList.get(index).operator.charAt(0) == '+') {
				str_inst = tokenList.get(index).operator.substring(1, tokenList.get(index).operator.length());
				str_format = "4";
			} else {
				str_inst = tokenList.get(index).operator;
				str_format = instTab.instMap.get(str_inst).format.substring(0, 1);
			}
			str_opcode = instTab.searchInst(str_inst).opcode;
			/*명령어의 opcode를 1)0~9일때는 '0'을, 2)a~f일때는 55를 빼서 shift연산을 해준 다음 obcode 변수에 합쳐주었다.*/ 
			for (int k = 0; k < 2; k++) {
				if (str_opcode.charAt(k) >= '0' && str_opcode.charAt(k) <= '9'){
					obcode |= ((str_opcode.charAt(k) - '0') << ((tokenList.get(index).byteSize * 2) - (k + 1)) * 4);
				}else{
					obcode|= ((str_opcode.charAt(k) - 55) << ((tokenList.get(index).byteSize * 2) - (k + 1)) * 4);
				}
			}

			/* 3,4형식 일 때 n,i,x,b,p,e 값 설정 */
			if (str_format.equals("3") || str_format.equals("4")) {
				// disp와 b,p flag 값 설정해주기 위해 필요한 변수 
				int ta = 0; // target 값 
				int pc = 0; // 다음 Token의 location 값
				//char형인 nixbpe를 비트 이동연산을 하기 위해  nixbpe를 int형으로 옮겨주기 위한 변수
				int x = 0; 
				
				/* n, i 설정 */
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
				/* x 설정 */
				if (tokenList.get(index).operand[2] != null) {
					if (tokenList.get(index).operand[2].equals("X")){
						tokenList.get(index).setFlag(xFlag, 1);
					}else
						tokenList.get(index).setFlag(xFlag, 0);
				}else{
					tokenList.get(index).setFlag(xFlag, 0);
				}
				/* b, p 설정 */
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
						}else{ // operand[0]이 리터럴일 경우
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

				/* e 설정 */
				if (tokenList.get(index).operator.charAt(0) == '+'){
					tokenList.get(index).setFlag(eFlag, 1);
				}else{
					tokenList.get(index).setFlag(eFlag, 0);
				}
				
				x = tokenList.get(index).nixbpe;
				/*obcode에 opcode와 nixbpe값이 들어감. */
				obcode |= x << ((tokenList.get(index).byteSize * 2 - 3) * 4);
				
				/* disp 설정 */
				if (str_format.equals("4")) {
					obcode |= 0;
				} else {
					if (tokenList.get(index).getFlag(pFlag) == 2) { // pFlag가  1로 설정된 경우
						if (ta >= pc){
							obcode |= ta - pc;
						}else{
							obcode |= ((ta - pc) & 0x00000FFF);
						}
					}else { // ex) operand[0]이 #0 또는 #3인 경우
						if (tokenList.get(index).operand[0] != null) {
							int z;
							z = Integer.parseInt(tokenList.get(index).operand[0].substring(1, tokenList.get(index).operand[0].length()));
							obcode |= z;
						}
					}
				}
				tokenList.get(index).objectCode = String.format("%06X", obcode);
			}
			/* 2형식일때 레지스터 값 설정 */
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
		/* 명령어 아닐 떄 */
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
			//리터럴인 경우
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
	 * index번호에 해당하는 object code를 리턴한다.
	 * 
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후 의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 의미 해석이 끝나면 pass2에서
 * object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token {
	// 의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들
	String objectCode;
	int byteSize;

	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
	 * 
	 * @param line : 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		// 변수 initialize
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
	 * line을 토큰별로 끊어서 저장하는 함수.
	 * 
	 * @param line : 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		String str;
		label = line.split("\t")[0];
		operator = line.split("\t")[1];
		/*operand가 존재하지 않는 operator 제외*/
		if (!operator.equals("LTORG") && !operator.equals("CSECT") && !operator.equals("RSUB") && !label.equals("*")) {
			str = line.split("\t")[2];
			if (operator.equals("EXTDEF") || operator.equals("EXTREF") || (str.split(",").length == 1)){
				operand[0] = str;
			}else {
				operand[0] = str.split(",")[0];
				/*명령어가 X레지스터를 사용하면  "X"를 operand[2]에 저장*/
				if (str.split(",")[1].equals("X")){
					operand[2] = str.split(",")[1];
				}else{
					operand[1] = str.split(",")[1];
				}
			}
		}
		/*comment가 존재하는 경우*/
		if (line.split("\t").length == 4){
			comment = line.split("\t")[3];
		}
	}

	/**
	 * n,i,x,b,p,e flag를 설정한다. <br><br>
	 * 
	 * 사용 예 : setFlag(nFlag, 1); <br>
	 * 또는 setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		if (value == 1){
			this.nixbpe |= flag;
		}
	}

	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 <br><br>
	 * 
	 * 사용 예 : getFlag(nFlag) <br>
	 * 또는 getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
