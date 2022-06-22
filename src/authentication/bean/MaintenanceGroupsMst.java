package authentication.bean;

import authentication.bean.interfaces.MaintenanceRecord;

public class MaintenanceGroupsMst extends Auth implements MaintenanceRecord {
	// Listで扱われる際の、List内でのindex番号
	private int index_ = -1;

	private int state_ = STATE_NON; // 更新判定

	public MaintenanceGroupsMst() {
		super();
	}

	public MaintenanceGroupsMst(Auth auth) {
		super(auth.getGroup(), auth.getGroupName());
	}

	public MaintenanceGroupsMst(String nm_group, String custom_value) {
		super(nm_group, custom_value);
	}

	public MaintenanceGroupsMst(String nm_group, String custom_value, int index) {
		super(nm_group, custom_value);
		this.index_ = index;
	}

	public MaintenanceGroupsMst(int cd_group, String nm_group,
			String custom_value, int index) {
		super(cd_group, nm_group, custom_value, index);
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
