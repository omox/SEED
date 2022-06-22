package authentication.bean;

import authentication.bean.interfaces.MaintenanceRecord;

public class MaintenanceReportAuth extends Report implements MaintenanceRecord {
	// Listで扱われる際の、List内でのindex番号
	private int index_ = -1;

	private int state_ = STATE_NON; // 更新判定

	public MaintenanceReportAuth() {
		super();
	}

	public MaintenanceReportAuth(Report report) {
		super(report.getReport_no(), report.getReport_side(), report
				.getDispNumber());
	}

	public MaintenanceReportAuth(int cd_report_no, int cd_report_side,
			String cd_disp_number, int index) {
		super(cd_report_no, cd_report_side, cd_disp_number);
		this.index_ = index;
	}

	/**
	 * エラー時値保持
	 *
	 */
	public MaintenanceReportAuth(int cd_report, int cd_report_side,
			String cd_disp_number) {
		super(cd_report, cd_report_side, cd_disp_number, "era");
	}

	/**
	 * 上書き登録用値保持
	 *
	 * @param cd_report_no
	 * @param cd_report_side
	 * @param cd_disp_number
	 * @param upd
	 * @param index
	 */
	public MaintenanceReportAuth(int cd_report_no, int cd_report_side,
			String cd_disp_number, String upd, int index) {
		super(cd_report_no, cd_report_side, cd_disp_number, upd, index);
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
