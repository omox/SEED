package authentication.bean;

public class Maintenance {

	/** ユーザーコード */
	private int cd_user_;

	/** グループコード */
	private int cd_group_;

	/** ロールコード */
	private int cd_position_;

	/** レポートコード */
	private int cd_report_no_;

	/** レポート分類コード */
	private int cd_report_side_;

	/** 登録者 */
	private String nm_create_;

	/** 登録日時 */
	private String dt_create_;

	/** 更新者 */
	private String nm_update_;

	/** 更新日時 */
	private String dt_update_;

	/**
	 * コンストラクタ <br />
	 *
	 */
	public Maintenance() {
	}

	/**
	 * ユーザーグループ管理
	 *
	 * @param cd_user
	 * @param cd_group
	 * @param nm_create
	 * @param dt_create
	 * @param nm_update
	 * @param dt_update
	 */
	public Maintenance(int cd_user, int cd_group, String nm_create,
			String dt_create, String nm_update, String dt_update) {
		this.cd_user_ = cd_user;
		this.cd_group_ = cd_group;
		this.nm_create_ = nm_create;
		this.dt_create_ = dt_create;
		this.nm_update_ = nm_update;
		this.dt_update_ = dt_update;
	}

	/**
	 * ユーザーレポート管理
	 *
	 * @param cd_group
	 * @param cd_position
	 * @param cd_report_no
	 * @param nm_create
	 * @param dt_create
	 * @param nm_update
	 * @param dt_update
	 */
	public Maintenance(int cd_group, int cd_position, int cd_report_no,
			String nm_create, String dt_create, String nm_update,
			String dt_update) {
		this.cd_group_ = cd_group;
		this.cd_position_ = cd_position;
		this.cd_report_no_ = cd_report_no;
		this.nm_create_ = nm_create;
		this.dt_create_ = dt_create;
		this.nm_update_ = nm_update;
		this.dt_update_ = dt_update;
	}

	public int getCd_user() {
		return cd_user_;
	}

	public void setCd_user(int cd_user) {
		this.cd_user_ = cd_user;
	}

	public int getCd_group() {
		return cd_group_;
	}

	public void setCd_group(int cd_group) {
		this.cd_group_ = cd_group;
	}

	public int getCd_position() {
		return cd_position_;
	}

	public void setCd_position(int cd_pos) {
		this.cd_position_ = cd_pos;
	}

	public int getCd_report_no() {
		return cd_report_no_;
	}

	public void setCd_report_no(int cd_report_no) {
		this.cd_report_no_ = cd_report_no;
	}

	public int getCd_report_side() {
		return cd_report_side_;
	}

	public void setCd_report_side(int cd_report_side) {
		this.cd_report_side_ = cd_report_side;
	}

	public String getNm_create() {
		return nm_create_;
	}

	public void setNm_create(String nm_create) {
		this.nm_create_ = nm_create;
	}

	public String getDt_create() {
		return dt_create_;
	}

	public void setDt_create(String dt_create) {
		this.dt_create_ = dt_create;
	}

	public String getNm_update() {
		return nm_update_;
	}

	public void setNm_update(String nm_update) {
		this.nm_update_ = nm_update;
	}

	public String getDt_update() {
		return dt_update_;
	}

	public void setDt_update(String dt_update) {
		this.dt_update_ = dt_update;
	}

}
