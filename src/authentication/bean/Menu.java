/*
 * 作成日: 2007/10/26
 *
 */
package authentication.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * メニューを表すクラス
 */
public class Menu implements Serializable {
  /** 所属 */
  private int group_;

  /** 権限 */
  private int pos_;

  /** 分類 */
  private ArrayList side_;

  /** レポートコード */
  private int report_;

  /** 有効無効 */
  private String enableMenu_;

  /**
   * コンストラクタ <br />
   *
   */
  public Menu() {}

  public Menu(int pos, int group) {
    this.pos_ = pos;
    this.group_ = group;
  }

  public Menu(int group, int pos, int report, String erro) {
    this.group_ = group;
    this.pos_ = pos;
    this.report_ = report;
    this.enableMenu_ = erro;
  }

  public Menu(int group, int pos, int report) {
    this.group_ = group;
    this.pos_ = pos;
    this.report_ = report;
  }


  public Menu(int group, int pos, int report, String enableMenu, String upd, int index) {
    this.group_ = group;
    this.pos_ = pos;
    this.report_ = report;
    this.enableMenu_ = enableMenu;
  }

  /**
   * @return auth_ 所属を返す
   */
  public int getGroup() {
    return group_;
  }

  /**
   * @param auth_ 所属を設定する
   */
  public void setGroup(int auth) {
    this.group_ = auth;
  }

  /**
   * @return pos_ 権限を返す
   */
  public int getPos() {
    return pos_;
  }

  /**
   * @param pos_ 権限を設定する
   */
  public void setPos(int pos) {
    this.pos_ = pos;
  }

  /**
   * @return side_
   */
  public ArrayList getSide() {
    return side_;
  }

  /**
   * @param side 設定する side_
   */
  public void setSide(ArrayList side) {
    this.side_ = side;
  }

  /**
   *
   * @return report_
   */
  public int getReport_no() {
    return report_;
  }

  /**
   * @param report_no
   */
  public void setReport_no(int report_no) {
    this.report_ = report_no;
  }

  /**
   * @param repoenableMenu_
   */
  public void setEnableMenu(String enableMenu) {
    this.enableMenu_ = enableMenu;
  }

  /**
   *
   * @return repoenableMenu_
   */
  public String getEnableMenu() {
    return this.enableMenu_;
  }
}
