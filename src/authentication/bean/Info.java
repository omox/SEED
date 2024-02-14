package authentication.bean;

import java.io.Serializable;

/**
 * レポートを表すクラス <br />
 */
public class Info implements Serializable {

  /** お知らせコード */
  private int cd_info_;

  /** 表示開始日 */
  private String dt_start_;

  /** 表示終了日 */
  private String dt_end_;

  /** 常時表示フラグ */
  private String flg_disp_always_;

  /** 表示順 */
  private String no_disp_;

  /** タイトル */
  private String title_;

  /** お知らせ */
  private String information_;

  /** グループコード */
  private int cd_group_;

  /** ロールコード */
  private int cd_pos_;

  /**
   * コンストラクタ <br />
   *
   */
  public Info() {}

  public Info(int cd_info, String dt_start, String dt_end, String flg_disp_always, String no_disp, String title, String information) {
    this.cd_info_ = cd_info;
    this.dt_start_ = dt_start;
    this.dt_end_ = dt_end;
    this.flg_disp_always_ = flg_disp_always;
    this.no_disp_ = no_disp;
    this.title_ = title;
    this.information_ = information;
  }

  public Info(String dt_start, String dt_end, String flg_disp_always, String no_disp, String title, String information) {
    this.dt_start_ = dt_start;
    this.dt_end_ = dt_end;
    this.flg_disp_always_ = flg_disp_always;
    this.no_disp_ = no_disp;
    this.title_ = title;
    this.information_ = information;
  }

  public Info(int cd_info, String title) {
    this.cd_info_ = cd_info;
    this.title_ = title;
  }

  public Info(int cd_group, int cd_pos, int cd_info) {
    this.cd_group_ = cd_group;
    this.cd_pos_ = cd_pos;
    this.cd_info_ = cd_info;
  }

  /**
   * @return cd_info_
   */
  public int getCd_info() {
    return cd_info_;
  }

  /**
   * @param cd_info the cd_info_ to set
   */
  public void setCd_info(int cd_info) {
    this.cd_info_ = cd_info;
  }

  /**
   * @return dt_start_
   */
  public String getDt_start() {
    return dt_start_;
  }

  /**
   * @param dt_start the dt_start_ to set
   */
  public void setDt_start(String dt_start) {
    this.dt_start_ = dt_start;
  }

  /**
   * @return dt_end_
   */
  public String getDt_end() {
    return dt_end_;
  }

  /**
   * @param dt_end the dt_end_ to set
   */
  public void setDt_end(String dt_end) {
    this.dt_end_ = dt_end;
  }

  /**
   * @return flg_disp_always_
   */
  public String getFlg_disp_always() {
    return flg_disp_always_;
  }

  /**
   * @param flg_disp_always the flg_disp_always_ to set
   */
  public void setFlg_disp_always(String flg_disp_always) {
    this.flg_disp_always_ = flg_disp_always;
  }

  /**
   * @return no_disp_
   */
  public String getNo_disp() {
    return no_disp_;
  }

  /**
   * @param no_disp the no_disp_ to set
   */
  public void setNo_disp(String no_disp) {
    this.no_disp_ = no_disp;
  }

  /**
   * @return title_
   */
  public String getTitle() {
    return title_;
  }

  /**
   * @param title the title_ to set
   */
  public void setTitle(String title) {
    this.title_ = title;
  }

  /**
   * @return information_
   */
  public String getInformation() {
    return information_;
  }

  /**
   * @param information the information_ to set
   */
  public void setInformation(String information) {
    this.information_ = information;
  }

  /**
   * @return cd_group_
   */
  public int getCd_group() {
    return cd_group_;
  }

  /**
   * @param cd_group the cd_group_ to set
   */
  public void setCd_group(int cd_group) {
    this.cd_group_ = cd_group;
  }

  /**
   * @return cd_pos_
   */
  public int getCd_pos() {
    return cd_pos_;
  }

  /**
   * @param cd_pos the cd_pos_ to set
   */
  public void setCd_pos(int cd_pos) {
    this.cd_pos_ = cd_pos;
  }

}
