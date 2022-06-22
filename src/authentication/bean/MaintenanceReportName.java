package authentication.bean;

import authentication.bean.interfaces.MaintenanceRecord;

public class MaintenanceReportName extends Report implements MaintenanceRecord {
	// Listで扱われる際の、List内でのindex番号
	private int index_ = -1;

	private int state_ = STATE_NON; // 更新判定

	public MaintenanceReportName() {
		super();
	}

	public MaintenanceReportName(Report report) {
		super(report.getReport_no(), report.getReport_name(), report
				.getReport_shortname(), report.getReport_jsp(), report
				.getReport_custom());
	}

	public MaintenanceReportName(int report_no, String nm_report,
			String nm_short, String nm_report_jsp, String custom_value,
			int index) {
		super(report_no, nm_report, nm_short, nm_report_jsp, custom_value);
		this.index_ = index;
	}

	public MaintenanceReportName(String nm_report, String nm_short,
			String nm_report_jsp, String custom_value) {
		super(nm_report, nm_short, nm_report_jsp, custom_value);
	}

	/***/
	public MaintenanceReportName(String nm_report, String nm_short,
			String nm_report_jsp, String custom_value, int index) {
		super(nm_report, nm_short, nm_report_jsp, custom_value);
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
