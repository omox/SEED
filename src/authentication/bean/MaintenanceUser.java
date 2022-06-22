/*
 * 作成日: 2007/10/26
 */
package authentication.bean;

import authentication.bean.interfaces.MaintenanceRecord;

/**
 * メンテナンス画面のユーザーを表すクラス <br />
 */
public class MaintenanceUser extends User implements MaintenanceRecord {

	// Listで扱われる際の、List内でのindex番号
	private int index_ = -1;

	private int state_ = STATE_NON; // 更新判定

	public MaintenanceUser() {
		super();
	}

	public MaintenanceUser(User user) {
		super(user.getCD_user(), user.getId(), user.getPass(), user.getName(),
				user.getNm_family(), user.getNm_name(), user.getGroup(), user
						.getPos());
	}

	public MaintenanceUser(int cd_user, String id, String pass, String name,
			int group, int position, int index) {
		super(cd_user, id, pass, name, group, position);
		this.index_ = index;
	}

	public MaintenanceUser(int cd_user, String id, String pass, String name, String nm_family, String nm_name, String cd_auth, String dt_pw_term, String flg, int index) {
		super(cd_user, id, pass, name, nm_family, nm_name, cd_auth, dt_pw_term, 1);
		this.index_ = index;
	}

	public MaintenanceUser(int cd_user, String id, String pass, String name,
			String nm_family, String nm_name, int group, int position, int index) {
		super(cd_user, id, pass, name, nm_family, nm_name, group, position);
		this.index_ = index;
	}

	// ロールを配列
	public MaintenanceUser(int cd_user, String id, String pass, String name,
			String nm_family, String nm_name, String[] group,
			String[] position, int index) {
		super(cd_user, id, pass, name, nm_family, nm_name, group, position);
		this.index_ = index;
	}

	// ロールを配列
	public MaintenanceUser(String id, String pass, String name,
			String nm_family, String nm_name, String[] group, String[] position) {
		super(id, pass, name, nm_family, nm_name, group, position);
	}

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

}
