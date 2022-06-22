package authentication.bean;

import authentication.bean.interfaces.MaintenanceRecord;

public class MaintenanceInfoAuth extends Info implements MaintenanceRecord {
	// Listで扱われる際の、List内でのindex番号
	private int index_ = -1;

	private int state_ = STATE_NON; // 更新判定

	public MaintenanceInfoAuth() {
		super();
	}

	public MaintenanceInfoAuth(int cd_group, int cd_pos, int cd_info) {
		super(cd_group, cd_pos, cd_info);
	}

	public MaintenanceInfoAuth(int cd_group, int cd_pos, int cd_info, int index) {
		super(cd_group, cd_pos, cd_info);
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
