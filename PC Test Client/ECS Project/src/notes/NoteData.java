package notes;

public class NoteData {
	private int ID;
	private String data;
	
	public int getID() {
		return ID;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	public NoteData(int iD, String data) {
		super();
		ID = iD;
		this.data = data;
	}
	
	public NoteData(int iD) {
		super();
		ID = iD;
	}
	
	public void insertText(int insertPosition, String insert){
		data = data.substring(0, insertPosition).concat(insert).concat(data.substring(insertPosition));
	}
}
