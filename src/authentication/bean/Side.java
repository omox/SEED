/*
 *  作成日: 2007/10/26
 *
 */
package authentication.bean;

import java.util.ArrayList;

/**
 * レポート分類を表すクラス
 */
public class Side {
	/** 分類 */
	private int side_;

	/** 分類名 */
	private String sidename_;

	/** レポート */
	private ArrayList report_;

	/** レポート分類表示順 */
	private String cd_disp_number_;

	/** レポート分類表示行 */
	private String cd_disp_Column_;

	/** カスタムプロパティ */
	private String custom_value_;

	/**
	 * コンストラクタ <br />
	 *
	 */
	public Side() {
	}

	public Side(int side, String sidename, String custom_value, String report) {
		this.side_ = side;
		this.sidename_ = sidename;
		this.custom_value_ = custom_value;
		this.report_ = null;
	}

	public Side(int side, String sidename, String cd_disp_row) {
		this.side_ = side;
		this.sidename_ = sidename;
		this.cd_disp_Column_ = cd_disp_row;
		this.report_ = null;
	}

	public Side(int side, String sidename, String report, long disp) {
		this.side_ = side;
		this.sidename_ = sidename;
		this.report_ = null;
	}

	public Side(String sidename, String cd_disp, String cd_disp_row_no,
			String custom_value) {
		this.custom_value_ = custom_value;
		this.sidename_ = sidename;
		this.cd_disp_number_ = cd_disp;
		this.cd_disp_Column_ = cd_disp_row_no;
	}

	/**
	 * レポート分類マスタメンテナンス
	 *
	 * @param side
	 * @param sidename
	 * @param cd_disp_no
	 * @param custom_value
	 * @param maintenance
	 */
	public Side(int side, String sidename, String cd_disp_no,
			String disp_row_no, String custom_value, String maintenance) {
		this.side_ = side;
		this.sidename_ = sidename;
		this.custom_value_ = custom_value;
		this.cd_disp_number_ = cd_disp_no;
		this.cd_disp_Column_ = disp_row_no;
	}

	/**
	 * @return
	 */
	public String getCustom_value() {
		return custom_value_;
	}

	/**
	 * @param custom
	 */
	public void setCustom_value(String custom) {
		this.custom_value_ = custom;
	}

	/**
	 * @return
	 */
	public String getDisp_number() {
		return cd_disp_number_;
	}

	/**
	 * @param disp_no
	 */
	public void setDisp_number(String disp_no) {
		this.cd_disp_number_ = disp_no;
	}

	/**
	 * @return
	 */
	public String getDisp_Column() {
		return cd_disp_Column_;
	}

	/**
	 * @param disp_no
	 */
	public void setDisp_row_number(String cd_disp_row_no) {
		this.cd_disp_Column_ = cd_disp_row_no;
	}

	/**
	 * @return side_
	 */
	public int getSide() {
		return side_;
	}

	/**
	 * @param side_
	 *            設定する side_
	 */
	public void setSide(int side) {
		this.side_ = side;
	}

	/**
	 * @return sidename_
	 */
	public String getSidename() {

		return sidename_;
	}

	/**
	 * @param sidename_
	 *            設定する sidename_
	 */
	public void setSidename(String sidename) {
		this.sidename_ = sidename;
	}

	/**
	 * @return report_
	 */
	public ArrayList getReport() {
		return report_;
	}

	/**
	 * @param report_
	 *            設定する report_
	 */
	public void setReport(ArrayList report) {
		this.report_ = report;
	}

	/**
	 * @param report_
	 *            設定する report_
	 */
	public void setReportMst(ArrayList report) {
		this.report_ = report;
	}

	/**
	 * @return report_
	 */
	public ArrayList getReportMst() {
		return report_;
	}

}
