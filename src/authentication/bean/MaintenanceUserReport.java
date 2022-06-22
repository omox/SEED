package authentication.bean;

import authentication.bean.interfaces.MaintenanceRecord;

public class MaintenanceUserReport extends Menu implements MaintenanceRecord {
	// Listで扱われる際の、List内でのindex番号
	private int index_ = -1;

	private int state_ = STATE_NON; // 更新判定

	public MaintenanceUserReport() {
		super();
	}

	public MaintenanceUserReport(Menu menu) {
		super(menu.getGroup(), menu.getPos(), menu.getReport_no());
	}

	public MaintenanceUserReport(int cd_group, int cd_positon, int cd_report_no) {
		super(cd_group, cd_positon, cd_report_no, "erro");
	}

	public MaintenanceUserReport(int cd_group, int cd_positon, int cd_report_no, String enableMenu) {
		super(cd_group, cd_positon, cd_report_no, enableMenu);
	}

	public MaintenanceUserReport(int cd_group, int cd_positon,
			int cd_report_no, int index) {
		super(cd_group, cd_positon, cd_report_no);
		this.index_ = index;
	}
	public MaintenanceUserReport(int cd_group, int cd_positon,
			int cd_report_no, int index, String enableMenu) {
		super(cd_group, cd_positon, cd_report_no, enableMenu);
		this.index_ = index;
	}

	public MaintenanceUserReport(int cd_group, int cd_positon,
			int cd_report_no, String enableMenu, String upd, int index) {
		super(cd_group, cd_positon, cd_report_no, enableMenu, upd, index);
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
