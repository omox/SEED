package authentication.bean;

import authentication.bean.interfaces.MaintenanceRecord;

public class MaintenanceReportSide extends Side implements MaintenanceRecord {
	// Listで扱われる際の、List内でのindex番号
	private int index_ = -1;

	private int state_ = STATE_NON; // 更新判定

	public MaintenanceReportSide() {
		super();
	}

	public MaintenanceReportSide(Side side) {
		super(side.getSide(), side.getSidename(), side.getDisp_number(), side.getDisp_Column(), side.getCustom_value(),
				"maintenancereportside");
	}

	public MaintenanceReportSide(String sidename, String disp_no,
			String disp_row_no, String custom_value) {
		super(sidename, disp_no, disp_row_no, custom_value);
	}

	public MaintenanceReportSide(int side, String sidename, String disp_no,
			String disp_row_no, String custom_value, int index) {
		super(side, sidename, disp_no, disp_row_no, custom_value, "");
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
	// -----//抽象メソッド//-----
}
