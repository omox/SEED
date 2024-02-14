/*
 * 作成日: 2007/10/26
 *
 */

package authentication.bean;

import java.io.Serializable;

/**
 * レポートを表すクラス <br />
 */
public class Report implements Serializable {

  /** レポート分類 */
  private int report_side_;

  /** レポートNo */
  private int report_no_;

  /** レポート分類表示順 */
  private String cd_disp_number_;

  /** レポート名称 */
  private String report_name_;

  /** レポート短縮名称 */
  private String report_shortname_;

  /** レポートJSP名 */
  private String report_jsp_;

  /** レポート有効無効 */
  private boolean report_boolean_;

  /** レポートON/OFF */
  private boolean enable_emnu_;

  /** レポートカスタムプロパティ */
  private String sys_users_custom_;

  private String report_name_custom_;

  private String report_side_custom_;

  private String group_custom_;

  private String posmst_custom_;

  /** レポートメニュー */
  private String report_name_decode_;

  /** 予備1 TODO: 特処理いなげや メニュー区分 */
  private String yobi1_;
  /** 予備2 TODO: 特処理いなげや */
  private String yobi2_;
  /** 予備3 TODO: 特処理いなげや */
  private String yobi3_;
  /** 予備4 TODO: 特処理いなげや */
  private String yobi4_;
  /** 予備5 TODO: 特処理いなげや */
  private String yobi5_;

  /**
   * コンストラクタ <br />
   * 全ての値にNull(int型は0)を設定する。 <br />
   */
  public Report() {}

  /**
   * コンストラクタ <br />
   *
   * @param id user_id
   * @param pass user_password
   * @param name user_name
   * @param auth user_auth
   * @param divide user_divide
   * @param ccl user_ccl_analyze
   */
  public Report(int report_side, int report_no, String report_name, String report_shortname, String report_jsp, String report_boolean, String enable_menu, String report_name_custom, String users_custom, String side_custom, String group_custom, String pos_custom, String yobi1, String yobi2,
      String yobi3, String yobi4, String yobi5) {
    this.report_side_ = report_side;
    this.report_no_ = report_no;
    this.report_name_ = report_name;
    this.report_shortname_ = report_shortname;
    this.report_jsp_ = report_jsp;
    this.sys_users_custom_ = users_custom;
    this.report_name_custom_ = report_name_custom;
    this.report_side_custom_ = side_custom;
    this.group_custom_ = group_custom;
    this.posmst_custom_ = pos_custom;
    this.yobi1_ = yobi1;
    this.yobi2_ = yobi2;
    this.yobi3_ = yobi3;
    this.yobi4_ = yobi4;
    this.yobi5_ = yobi5;

    if (report_boolean.equals("0")) {
      this.report_boolean_ = false;
    } else {
      this.report_boolean_ = true;
    }

    // レポートON/OFF設定
    if (enable_menu.equals("0")) {
      this.enable_emnu_ = false;
    } else {
      this.enable_emnu_ = true;
    }

  }

  /**
   * @param cd_report_no
   * @param nm_report
   */
  public Report(int cd_report_no, String nm_report) {
    this.report_no_ = cd_report_no;
    this.report_name_ = nm_report;
  }

  public Report(int cd_report_no, int cd_report_side, String cd_disp_number) {
    this.report_no_ = cd_report_no;
    this.report_side_ = cd_report_side;
    this.cd_disp_number_ = cd_disp_number;
  }

  public Report(int cd_report_no, int cd_report_side, String cd_disp_number, String era) {
    this.report_no_ = cd_report_no;
    this.report_side_ = cd_report_side;
    this.cd_disp_number_ = cd_disp_number;
  }

  /**
   * レポートマスタの名前を表示する
   *
   * @param report_side
   * @param report_no
   * @param report_name
   * @param report_shortname
   */
  public Report(int report_side, int report_no, String report_name, String report_name_decode, String report_shortname, String xx) {
    this.report_side_ = report_side;
    this.report_no_ = report_no;
    this.report_name_ = report_name;
    this.report_name_decode_ = report_name_decode;
    this.report_shortname_ = report_shortname;
  }

  /**
   * レポート管理 上書き登録
   *
   * @param cd_report_no
   * @param cd_report_side
   * @param cd_disp_number
   * @param upd
   * @param index
   */
  public Report(int cd_report_no, int cd_report_side, String cd_disp_number, String upd, int index) {
    this.report_no_ = cd_report_no;
    this.report_side_ = cd_report_side;
    this.cd_disp_number_ = cd_disp_number;
  }

  /**
   * レポートマスタメンテナンス
   *
   * @param cd_report_no
   * @param nm_report
   * @param nm_short
   * @param nm_report_jsp
   * @param custom_value
   */
  public Report(int cd_report_no, String nm_report, String nm_short, String nm_report_jsp, String custom_value) {
    this.report_no_ = cd_report_no;
    this.report_name_ = nm_report;
    this.report_shortname_ = nm_short;
    this.report_jsp_ = nm_report_jsp;
    this.report_name_custom_ = custom_value;
  }

  public Report(String nm_report, String nm_short, String nm_report_jsp, String custom_value) {
    this.report_name_ = nm_report;
    this.report_shortname_ = nm_short;
    this.report_jsp_ = nm_report_jsp;
    this.report_name_custom_ = custom_value;
  }

  /**
   */
  public String getReport_name_decode() {
    return report_name_decode_;
  }

  public void setReport_name_decode(String report_name_decode) {
    this.report_name_decode_ = report_name_decode;
  }

  /**
   * @return cd_disp_number_
   */
  public String getDispNumber() {
    return cd_disp_number_;
  }

  /**
   * @param cd_disp_number
   */
  public void setDispNumber(String cd_disp_number) {
    this.cd_disp_number_ = cd_disp_number;
  }

  /**
   * @return
   */
  public String getPosmst_custom() {
    return posmst_custom_;
  }

  /**
   * @param custom
   */
  public void setPosmast_custom(String custom) {
    this.posmst_custom_ = custom;
  }

  /**
   *
   * @return
   */
  public String getGroup_custom() {
    return group_custom_;
  }

  /**
   * @param custom
   */
  public void setGroup_custom(String custom) {
    this.group_custom_ = custom;
  }

  /**
   * @return report_side_custom_
   */
  public String getSide_custom() {
    return report_side_custom_;
  }

  /**
   * @param custom
   */
  public void setSide_cutom(String custom) {
    this.report_side_custom_ = custom;
  }

  /**
   * @return report_name_custom_
   */
  public String getReport_custom() {
    return report_name_custom_;
  }

  /**
   * @param custom
   */
  public void setReport_cutom(String custom) {
    this.report_name_custom_ = custom;
  }

  /**
   * @return sys_users_custom_
   */
  public String getUsers_custom() {
    return sys_users_custom_;
  }

  /**
   * @param custom
   */
  public void setUsers_custom(String custom) {
    this.sys_users_custom_ = custom;
  }

  /**
   * @return report_boolean_
   */
  public boolean getReport_boolean() {
    return report_boolean_;
  }

  /**
   * @param report_boolean_ 設定する report_boolean_
   */
  public void setReport_boolean(boolean report_boolean) {
    this.report_boolean_ = report_boolean;
  }

  public void setReport_boolean(String report_boolean) {
    this.report_boolean_ = report_boolean.equals("1") ? true : false;
  }

  /**
   * @return enable_menu_
   */
  public boolean getEnableMenu() {
    return this.enable_emnu_;
  }

  /**
   * @return report_jsp_
   */
  public String getReport_jsp() {
    return report_jsp_;
  }

  /**
   * @param report_jsp_ 設定する report_jsp_
   */
  public void setReport_jsp(String report_jsp) {
    this.report_jsp_ = report_jsp;
  }

  /**
   * @return report_nam_
   */
  public String getReport_name() {
    return report_name_;
  }

  /**
   * @param report_nam_ 設定する report_nam_
   */
  public void setReport_name(String report_name) {
    this.report_name_ = report_name;
  }

  /**
   * @return report_no_
   */
  public int getReport_no() {
    return report_no_;
  }

  /**
   * @param report_no_ 設定する report_no_
   */
  public void setReport_no(int report_no) {
    this.report_no_ = report_no;
  }

  /**
   * @return report_shortname_
   */
  public String getReport_shortname() {
    return report_shortname_;
  }

  /**
   * @param report_shortname_ 設定する report_shortname_
   */
  public void setReport_shortname(String report_shortname) {
    this.report_shortname_ = report_shortname;
  }

  /**
   * @return report_side_
   */
  public int getReport_side() {
    return report_side_;
  }

  /**
   * @param report_side_ 設定する report_side_
   */
  public void setReport_side(int report_side) {
    this.report_side_ = report_side;
  }

  /**
   * 予備1を取得します。
   *
   * @return 予備1
   */
  public String getYobi1_() {
    return yobi1_;
  }

  /**
   * 予備1を設定します。
   *
   * @param yobi1_ 予備1
   */
  public void setYobi1_(String yobi1_) {
    this.yobi1_ = yobi1_;
  }

  /**
   * 予備2を取得します。
   *
   * @return 予備2
   */
  public String getYobi2_() {
    return yobi2_;
  }

  /**
   * 予備2を設定します。
   *
   * @param yobi2_ 予備2
   */
  public void setYobi2_(String yobi2_) {
    this.yobi2_ = yobi2_;
  }

  /**
   * 予備3を取得します。
   *
   * @return 予備3
   */
  public String getYobi3_() {
    return yobi3_;
  }

  /**
   * 予備3を設定します。
   *
   * @param yobi3_ 予備3
   */
  public void setYobi3_(String yobi3_) {
    this.yobi3_ = yobi3_;
  }

  /**
   * 予備4を取得します。
   *
   * @return 予備4
   */
  public String getYobi4_() {
    return yobi4_;
  }

  /**
   * 予備4を設定します。
   *
   * @param yobi4_ 予備4
   */
  public void setYobi4_(String yobi4_) {
    this.yobi4_ = yobi4_;
  }

  /**
   * 予備5を取得します。
   *
   * @return 予備5
   */
  public String getYobi5_() {
    return yobi5_;
  }

  /**
   * 予備5を設定します。
   *
   * @param yobi5_ 予備5
   */
  public void setYobi5_(String yobi5_) {
    this.yobi5_ = yobi5_;
  }
}
