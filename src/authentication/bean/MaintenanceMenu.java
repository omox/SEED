/*
 * 作成日: 2007/11/08
 */
package authentication.bean;

import authentication.bean.interfaces.MaintenanceRecord;

/**
 * メンテナンス画面のメニューを表すクラス <br />
 */
public class MaintenanceMenu extends Menu implements MaintenanceRecord {

	// Listで扱われる際の、List内でのindex番号
	private int index_ = -1;

	private int state_ = STATE_NON; // 更新判定

	public MaintenanceMenu() {
		super();
	}

	public MaintenanceMenu(Menu menu) {
		super(menu.getPos(), menu.getGroup());
	}

	public MaintenanceMenu(int pos, int group, int index) {
		super(pos, group);
		this.index_ = index;
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
