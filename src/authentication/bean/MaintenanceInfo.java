package authentication.bean;

import authentication.bean.interfaces.MaintenanceRecord;

public class MaintenanceInfo extends Info implements MaintenanceRecord {
	// Listで扱われる際の、List内でのindex番号
	private int index_ = -1;

	private int state_ = STATE_NON; // 更新判定

	public MaintenanceInfo() {
		super();
	}

	public MaintenanceInfo(int cd_info, String dt_start, String dt_end, String flg_disp_always,
							String no_disp, String title, String information) {
		super(cd_info, dt_start, dt_end, flg_disp_always, no_disp, title, information);
	}

	public MaintenanceInfo(int cd_info, String dt_start, String dt_end, String flg_disp_always,
							String no_disp, String title, String information, int index) {
		super(cd_info, dt_start, dt_end, flg_disp_always, no_disp, title, information);
		this.index_ = index;
	}

	public MaintenanceInfo(String dt_start, String dt_end, String flg_disp_always,
							String no_disp, String title, String information) {
		super(dt_start, dt_end, flg_disp_always, no_disp, title, information);
	}

	public MaintenanceInfo(String dt_start, String dt_end, String flg_disp_always,
							String no_disp, String title, String information, int index) {
		super(dt_start, dt_end, flg_disp_always, no_disp, title, information);
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
