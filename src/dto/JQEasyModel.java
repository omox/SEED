package dto;

import java.util.List;

import net.sf.json.JSONObject;
/**
 * jQuery jqGrid の JSON 型 ロードモデル
 *
 */
public class JQEasyModel {

	/**
	 * 宣言
	 */
	private Integer total;
	private List<JSONObject> rows;
	private String[] titles;
	/** オプション情報(JSONObject) */
	private JSONObject opts;
	private String message;

	/**
	 * 総レコード数を戻す
	 * @return total
	 */
	public Integer getTotal() {
		return total;
	}
	/**
	 * 総レコード数を設定する
	 * @param total the total to set
	 */
	public void setTotal(Integer total) {
		this.total = total;
	}

	/**
	 * レコード情報(JSONObject)を戻す
	 * @return レコード情報
	 */
	public List<JSONObject> getRows() {
		return rows;
	}
	/**
	 * レコード情報(JSONObject)を設定する
	 * @param rows
	 */
	public void setRows(List<JSONObject> rows) {
		this.rows = rows;
	}

	/**
	 * タイトル行情報を戻す
	 * @return レコード情報
	 */
	public String[] getTitles() {
		return titles;
	}
	/**
	 * タイトル行情報を設定する
	 * @return レコード情報
	 */
	public void setTitles(String[] titles) {
		this.titles = titles;
	}

	/**
	 * オプション情報(JSONObject)を取得します。
	 *
	 * @return オプション情報(JSONObject)
	 */
	public JSONObject getOpts() {
		return opts;
	}

	/**
	 * オプション情報(JSONObject)を設定します。
	 *
	 * @param opts オプション情報(JSONObject)
	 */
	public void setOpts(JSONObject opts) {
		this.opts = opts;
	}
	/**
	 * メッセージを取得します。
	 * @return メッセージ（SQL実行時エラーメッセージ）
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * メッセージを設定します。
	 * @param message メッセージ（SQL実行時エラーメッセージ）
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
