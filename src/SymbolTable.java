import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�. section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;

	public SymbolTable() {
		symbolList = new ArrayList<String>();
		locationList = new ArrayList<Integer>();
	}

	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * 
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param location : �ش� symbol�� ������ �ּҰ� <br><br>
	 * 
	 */
	public void putSymbol(String symbol, int location) {
		symbolList.add(symbol);
		locationList.add(location);
	}

	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * 
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newLocation) {
		for (int i = 0; i < symbolList.size(); i++) {
			if (symbolList.get(i).equals(symbol)) {
				locationList.set(i, newLocation);
				break;
			}
		}
	}

	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�.
	 * 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int address = -1;
		for (int i = 0; i < symbolList.size(); i++) {
			if (symbolList.get(i).equals(symbol)) {
				address = locationList.get(i);
				break;
			}
		}
		return address;
	}
}
