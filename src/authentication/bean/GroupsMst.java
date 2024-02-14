package authentication.bean;

import java.io.Serializable;
import authentication.bean.interfaces.MaintenanceRecord;

public class GroupsMst extends Maintenance implements MaintenanceRecord, Serializable {
  // Listで扱われる際の、List内でのindex番号
  private int index_ = -1;

  private int state_ = STATE_NON; // 更新判定

  public GroupsMst() {
    super();
  }

  /** ユーザーグループ管理 */
  public GroupsMst(int cd_user, int cd_group, String nm_create, String dt_create, String nm_update, String dt_update, int index) {
    super(cd_user, cd_group, nm_create, dt_create, nm_update, dt_update);
    this.index_ = index;
  }

  /** ユーザーレポート管理 */
  public GroupsMst(int cd_group, int cd_position, int cd_report_no, String nm_create, String dt_create, String nm_update, String dt_update, int index) {
    super(cd_group, cd_position, cd_report_no, nm_create, dt_create, nm_update, dt_update);
    this.index_ = index;
  }

  // -----抽象メソッドを実装するのに必要なところ------
  @Override
  public int getIndex() {
    return index_;
  }

  @Override
  public void setIndex(int index) {
    this.index_ = index;
  }

  @Override
  public int getState() {
    return state_;
  }

  @Override
  public void setDel() {
    this.state_ = STATE_DEL;

  }

  @Override
  public void setUpd() {
    this.state_ = STATE_UPD;
  }
  // -----//抽象メソッド//------
}
