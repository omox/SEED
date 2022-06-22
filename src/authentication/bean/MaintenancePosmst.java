package authentication.bean;

import authentication.bean.interfaces.MaintenanceRecord;

public class MaintenancePosmst extends Position implements MaintenanceRecord {
	// Listで扱われる際の、List内でのindex番号
	private int index_ = -1;

	private int state_ = STATE_NON; // 更新判定

	public MaintenancePosmst() {
		super();
	}

	public MaintenancePosmst(Position pos) {
		super(pos.getPosition(), pos.getPositionName());
	}

	public MaintenancePosmst(String nm_pos, String custom_value) {
		super(nm_pos, custom_value);
	}

	public MaintenancePosmst(String nm_pos, String custom_value, int index) {
		super(nm_pos, custom_value);
		this.index_ = index;
	}

	public MaintenancePosmst(int cd_position, String nm_pos,
			String custom_value, int index) {
		super(cd_position, nm_pos, custom_value, index);
		this.index_ = index;
	}

	// -----抽象メソッドを実装するのに必要なところ------
	public int getIndex() {
		return index_;
	}

	public void setIndex(int index) {
		this.index_ = index;
	}

	public int getState() {
		return state_;
	}

	public void setDel() {
		this.state_ = STATE_DEL;

	}

	public void setUpd() {
		this.state_ = STATE_UPD;
	}
	// -----//抽象メソッド//------
}
