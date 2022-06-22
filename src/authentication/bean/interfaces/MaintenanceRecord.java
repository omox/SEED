/*
 * 作成日: 2007/11/08
 *
 */
package authentication.bean.interfaces;

/**
 * メンテナンス画面のレコードbean用インターフェース
 * 各レコードbeanに対しての更新・削除状態と、
 * インデックス番号取得設定が可能なようにしなければならない
 */
public interface MaintenanceRecord {
	/** レコード状態 */
	final int STATE_NON = 0; // 未設定(更新なし)

	final int STATE_UPD = 1; // 更新

	final int STATE_DEL = 2; // 削除

	/**
	 * @return index_ を戻す。 <br />
	 */
	public int getIndex();

	/**
	 * @param index_
	 *            index を設定。 <br />
	 */
	public void setIndex(int index);

	/**
	 * @return state_ を戻す。 <br />
	 */
	public int getState();

	/**
	 * state_Upd を設定。 <br />
	 */
	public void setUpd();

	/**
	 * state_Del を設定。 <br />
	 */
	public void setDel();

}
