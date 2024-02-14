/*
 * 作成日: 2007/10/26
 *
 */

package authentication.bean;

import java.io.Serializable;

/**
 * 権限を表すクラス
 */
public class Position implements Serializable {
  /** 権限 */
  private int pos_;

  /** 権限名 */
  private String posname_;

  /** ユーザーコード */
  private int cd_user_;

  /** カスタムプロパティ */
  private String custom_value_;

  /**
   * コンストラクタ <br />
   *
   */
  public Position() {}

  public Position(int pos, String posname) {
    this.pos_ = pos;
    this.posname_ = posname;
  }

  public Position(int cd_user, int pos, String posname) {
    this.cd_user_ = cd_user;
    this.pos_ = pos;
    this.posname_ = posname;
  }

  /**
   * ロールマスタ管理メンテナンス画面で使用
   *
   * @param cd_user
   * @param pos
   * @param posname
   */
  public Position(int cd_pos, String nm_pos, String custom_value, int index) {
    this.pos_ = cd_pos;
    this.posname_ = nm_pos;
    this.custom_value_ = custom_value;
  }

  public Position(String nm_pos, String custom_value) {
    this.posname_ = nm_pos;
    this.custom_value_ = custom_value;
  }

  /**
   * @return
   */
  public String getCustom_value() {
    return custom_value_;
  }

  /**
   * @param custom_value
   */
  public void setCustom_vlaue(String custom_value) {
    this.custom_value_ = custom_value;
  }

  /**
   * @return cd_user_
   */
  public int getCd_user() {
    return cd_user_;
  }

  /**
   * @param cd_user
   */
  public void serCd_user(int cd_user) {
    this.cd_user_ = cd_user;
  }

  /**
   * @return auth_
   */
  public int getPosition() {
    return pos_;
  }

  /**
   * @param auth_ 設定する auth_
   */
  public void setPosition(int position) {
    this.pos_ = position;
  }

  /**
   * @return pos_
   */
  public String getPositionName() {
    return posname_;
  }

  /**
   * @param pos_ 設定する pos_
   */
  public void setPositionName(String positionname) {
    this.posname_ = positionname;
  }

}
