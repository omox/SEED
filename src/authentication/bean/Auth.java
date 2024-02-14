/*
 * 作成日: 2007/10/26
 *
 */

package authentication.bean;

import java.io.Serializable;

/**
 * 所属を表すクラス
 */
public class Auth implements Serializable {
  /** 所属 */
  private int group_;

  /** 所属名 */
  private String authname_;

  /** ユーザーコード */
  private int cd_user_;

  /** カスタムプロパティ */
  private String custom_value_;

  /**
   * コンストラクタ <br />
   *
   */
  public Auth() {}

  public Auth(int group, String authname) {
    this.group_ = group;
    this.authname_ = authname;
  }

  public Auth(int cd_user, int group, String groupname) {
    this.cd_user_ = cd_user;
    this.group_ = group;
    this.authname_ = groupname;
  }

  public Auth(String groupname, String custom_value) {
    this.authname_ = groupname;
    this.custom_value_ = custom_value;
  }

  /***
   * グループマスタ管理
   *
   * @param cd_user
   * @param group
   * @param groupname
   */
  public Auth(int group, String groupname, String custom_value, int index) {
    this.group_ = group;
    this.authname_ = groupname;
    this.custom_value_ = custom_value;
  }

  public String getCustom_value() {
    return custom_value_;
  }

  public void setCustom_value(String custom) {
    this.custom_value_ = custom;
  }

  /**
   * @return
   */
  public int getCd_user() {
    return cd_user_;
  }

  /**
   * @param cd_user
   */
  public void setCd_user(int cd_user) {
    this.cd_user_ = cd_user;
  }

  /**
   * @return auth_
   */
  public int getGroup() {
    return group_;
  }

  /**
   * @param auth_ 設定する auth_
   */
  public void setGroup(int group) {
    this.group_ = group;
  }

  /**
   * @return pos_
   */
  public String getGroupName() {
    return authname_;
  }

  /**
   * @param pos_ 設定する pos_
   */
  public void setGroupName(String authname) {
    this.authname_ = authname;
  }

}
