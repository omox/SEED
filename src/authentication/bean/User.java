/*
 * 作成日: 2013/12/18
 *
 */
package authentication.bean;

import org.apache.commons.lang.StringUtils;

/**
 * ユーザーを表すクラス <br />
 */
public class User {

	/** ユーザーコード */
	private int cd_user_;

	/** ID */
	private String id_;

	/** ﾊﾟｽﾜｰﾄﾞ */
	private String pass_;

	/** 名称 */
	private String name_;

	/** 権限 */
	private String cd_auth_;

	/** パスワード有効期限 */
	private String dt_pw_term_;

	/** 権限 */
	private int pos_;

	private String[] poslist_;

	private String[] pos_custom_value_list_;

	/** グループ */
	private int group_;

	private String[] grouplist_;

	/** 姓 */
	private String nm_family_;

	/** 名 */
	private String nm_name_;

	/** ロゴパス */
	private String logo_;

	/** カスタムプロパティ */
	private String custom_value_;

	/** 権限基本プロパティ TODO:特処理いなげや*/
	private String pos_custom_value_;
	/** 権限追加プロパティ TODO:特処理いなげや*/
	private String pos_add_custom_value_;

	/** 予備1 TODO: 特処理いなげや 取引先*/
	private String yobi1_;
	/** 予備2 TODO: 特処理いなげや 店舗*/
	private String yobi2_;
	/** 予備3 TODO: 特処理いなげや IP*/
	private String yobi3_;
	/** 予備4 */
	private String yobi4_;
	/** 予備5 */
	private String yobi5_;

	// TODO: 特処理いなげや 予備10に拡大
	/** 予備6 */
	private String yobi6_;
	/** 予備7 */
	private String yobi7_;
	/** 予備8 */
	private String yobi8_;
	/** 予備9 */
	private String yobi9_;
	/** 予備10 */
	private String yobi10_;

	/** 予備2 TODO: 特処理いなげや 店舗名称*/
	private String yobi2Nm_;

	/**
	 * コンストラクタ <br /> 全ての値にNull(int型は0)を設定する。 <br />
	 */
	public User() {
		this.cd_user_ = 0;
		this.id_ = null;
		this.pass_ = null;
		this.name_ = null;
		this.nm_family_ = null;
		this.nm_name_ = null;
		this.cd_auth_ = null;
		this.dt_pw_term_ = null;
		this.group_ = 0;
		this.pos_ = 0;
		this.poslist_ = null;
		this.grouplist_ = null;
		this.yobi1_= null;
		this.yobi2_= null;
		this.yobi3_= null;
		this.yobi4_= null;
		this.yobi5_= null;
	}

	/**
	 * コンストラクタ
	 * @param id
	 * @param pass
	 * @param name
	 * @param auth
	 * @param position
	 */
	public User(int user, String id, String pass, String name, int group,	int position) {
		this.cd_user_ = user;
		this.id_ = id;
		this.pass_ = pass;
		this.name_ = name;
		this.group_ = group;
		this.pos_ = position;
	}
	/**
	 * @param user
	 * @param id
	 * @param pass
	 * @param name
	 * @param cd_auth
	 * @param dt_pw_term
	 * @param group
	 * @param position
	 * @param custom
	 * @param yobi1
	 * @param yobi2
	 * @param yobi3
	 * @param yobi4
	 * @param yobi5
	 */
	public User(int user, String id, String pass, String name, String cd_auth, String dt_pw_term, int group, int position, String custom, String logo, String yobi1, String yobi2, String yobi3, String yobi4, String yobi5) {
		this.cd_user_ = user;
		this.id_ = id;
		this.pass_ = pass;
		this.name_ = name;
		this.cd_auth_ = cd_auth;
		this.dt_pw_term_ = dt_pw_term;
		this.group_ = group;
		this.pos_ = position;
		this.custom_value_ = custom;
		this.logo_ = logo;
		this.yobi1_= yobi1;
		this.yobi2_= yobi2;
		this.yobi3_= yobi3;
		this.yobi4_= yobi4;
		this.yobi5_= yobi5;
	}

	/**
	 * @param id
	 * @param pass
	 * @param name
	 * @param nm_family
	 * @param nm_name
	 * @param dt_pw_term パスワード有効期限
	 * @param i ?
	 */
	public User(int user, String id, String pass, String name,	String nm_family, String nm_name, String cd_auth, String dt_pw_term, int i) {
		this.cd_user_ = user;
		this.id_ = id;
		this.pass_ = pass;
		this.name_ = name;
		this.nm_family_ = nm_family;
		this.nm_name_ = nm_name;
		this.cd_auth_ = cd_auth;
		this.dt_pw_term_ = dt_pw_term;
	}

	/**
	 * @param id
	 * @param pass
	 * @param name
	 * @param nm_family
	 * @param nm_name
	 * @param auth
	 * @param position
	 */
	public User(int user, String id, String pass, String name,	String nm_family, String nm_name, int group, int position) {
		this.cd_user_ = user;
		this.id_ = id;
		this.pass_ = pass;
		this.name_ = name;
		this.nm_family_ = nm_family;
		this.nm_name_ = nm_name;
		this.group_ = group;
		this.pos_ = position;
	}

	// ロール配列
	public User(int user, String id, String pass, String name,	String nm_family, String nm_name, String[] grouplist, String[] position) {
		this.cd_user_ = user;
		this.id_ = id;
		this.pass_ = pass;
		this.name_ = name;
		this.nm_family_ = nm_family;
		this.nm_name_ = nm_name;
		this.grouplist_ = grouplist;
		this.poslist_ = position;
	}

	// ロール配列
	public User(String id, String pass, String name, String nm_family,	String nm_name, String[] grouplist, String[] position) {
		this.id_ = id;
		this.pass_ = pass;
		this.name_ = name;
		this.nm_family_ = nm_family;
		this.nm_name_ = nm_name;
		this.grouplist_ = grouplist;
		this.poslist_ = position;
	}

	/**
	 * コンストラクタ
	 * @param user
	 * @param id
	 * @param pass
	 * @param name
	 * @param cd_auth
	 * @param dt_pw_term
	 * @param group
	 * @param position
	 * @param custom
	 * @param logo
	 * @param yobi1_
	 * @param yobi2_
	 * @param yobi3_
	 * @param yobi4_
	 * @param yobi5_
	 * @param yobi6_
	 * @param yobi7_
	 * @param yobi8_
	 * @param yobi9_
	 * @param yobi10_
	 *
	 */
	public User(int user, String id, String pass, String name, String cd_auth, String dt_pw_term, int group, int position, String custom, String logo,
			String yobi1_, String yobi2_, String yobi3_, String yobi4_, String yobi5_, String yobi6_, String yobi7_, String yobi8_, String yobi9_, String yobi10_) {
		this.cd_user_ = user;
		this.id_ = id;
		this.pass_ = pass;
		this.name_ = name;
		this.cd_auth_ = cd_auth;
		this.dt_pw_term_ = dt_pw_term;
		this.group_ = group;
		this.pos_ = position;
		this.custom_value_ = custom;
		this.logo_ = logo;
		this.yobi1_ = yobi1_;
		this.yobi2_ = yobi2_;
		this.yobi3_ = yobi3_;
		this.yobi4_ = yobi4_;
		this.yobi5_ = yobi5_;
		this.yobi6_ = yobi6_;
		this.yobi7_ = yobi7_;
		this.yobi8_ = yobi8_;
		this.yobi9_ = yobi9_;
		this.yobi10_ = yobi10_;
	}

	/**
	 * @return
	 */
	public int getCD_user() {
		return cd_user_;
	}

	/**
	 * @param user
	 */
	public void setCD_user(int user) {
		this.cd_user_ = user;
	}

	/**
	 * @return custom_value_
	 */
	public String getUsers_custom() {
		return custom_value_;
	}

	public void setUsers_custom(String custom) {
		this.custom_value_ = custom;
	}

	/**
	 * @return group_
	 */
	public String[] getGrouplist() {
		return grouplist_;
	}

	/**
	 * @param group
	 */
	public void setGrouplist(String[] grouplist) {
		this.grouplist_ = grouplist;
	}

	/**
	 * @return poslist_
	 */
	public String[] getPoslist() {
		return poslist_;
	}

	/**
	 * @param position
	 */
	public void setPoslist(String[] position) {
		this.poslist_ = position;
	}


	/**
	 * @return pos_custom_value_list_
	 */
	public String[] getPos_custom_value_list() {
		return pos_custom_value_list_;
	}

	/**
	 * @param pos_custom_value_list_
	 */
	public void setPos_custom_value_list(String[] pos_custom_value_list_) {
		this.pos_custom_value_list_ = pos_custom_value_list_;
	}


	/**
	 * @return nm_family_
	 */
	public String getNm_family() {
		return nm_family_;
	}

	/**
	 * @param nm_family
	 */
	public void setNm_family(String nm_family) {
		this.nm_family_ = nm_family;
	}

	/**
	 * @return nm_name_
	 */
	public String getNm_name() {
		return nm_name_;
	}

	/**
	 * @param nm_name
	 */
	public void setNm_name(String nm_name) {
		this.nm_name_ = nm_name;
	}

	/**
	 * @return id_ を戻す。 <br />
	 */
	public String getId() {
		return id_;
	}

	/**
	 * @param id
	 *			id を設定。 <br />
	 */
	public void setId(String id) {
		this.id_ = id;
	}

	/**
	 * @return name_ を戻す。 <br />
	 */
	public String getName() {
		return name_;
	}

	/**
	 * @param name
	 *			name を設定。 <br />
	 */
	public void setName(String name) {
		this.name_ = name;
	}

	/**
	 * @return pass_ を戻す。 <br />
	 */
	public String getPass() {
		return pass_;
	}

	/**
	 * @param pass
	 *			pass を設定。 <br />
	 */
	public void setPass(String pass) {
		this.pass_ = pass;
	}

	/**
	 * @return group を戻す。 <br />
	 */
	public int getGroup() {
		return group_;
	}

	/**
	 * @param auth
	 *			group を設定。 <br />
	 */
	public void setGroup(int group) {
		this.group_ = group;
	}

	/**
	 * @return pos_
	 */
	public int getPos() {
		return pos_;
	}

	/**
	 * @param pos_
	 *			設定する pos_
	 */
	public void setPos(int pos) {
		this.pos_ = pos;
	}

	/**
	 * パスワード有効期限(YYYYMMDD)を取得
	 * @return パスワード有効期限
	 */
	public String getDt_pw_term_() {
		if (dt_pw_term_ == null) {
			return "20401231";
		} else {
			return dt_pw_term_;
		}
	}

	/**
	 * パスワード有効期限を設定
	 * @param dt_pw_term_
	 */
	public void setDt_pw_term_(String dt_pw_term_) {
		this.dt_pw_term_ = dt_pw_term_;
	}

	/**
	 * 権限の取得
	 * @return
	 */
	public String getCd_auth_() {
		return cd_auth_;
	}
	/**
	 * 権限の設定
	 * @param cd_auth_
	 */
	public void setCd_auth_(String cd_auth_) {
		this.cd_auth_ = cd_auth_;
	}

	/**
	 * ロゴパスの取得
	 * @return logo_
	 */
	public String getLogo_() {
		return logo_;
	}

	/**
	 * ロゴパスの設定
	 * @param logo_ the logo_ to set
	 */
	public void setLogo_(String logo_) {
		this.logo_ = logo_;
	}

	/**
	 * 予備1を取得します。
	 * @return 予備1
	 */
	public String getYobi1_() {
		return yobi1_;
	}

	/**
	 * 予備1を設定します。
	 * @param yobi1_ 予備1
	 */
	public void setYobi1_(String yobi1_) {
		this.yobi1_ = yobi1_;
	}

	/**
	 * 予備2を取得します。
	 * @return 予備2
	 */
	public String getYobi2_() {
		return yobi2_;
	}

	/**
	 * 予備2名称を取得します。
	 * @return 予備2
	 */
	public String getYobi2Nm_() {
		return yobi2Nm_;
	}

	/**
	 * 予備2を設定します。
	 * @param yobi2_ 予備2
	 */
	public void setYobi2_(String yobi2_) {
		this.yobi2_ = yobi2_;
	}

	/**
	 * 予備2に紐付く名称を設定します。
	 * @param yobi2_ 予備2
	 */
	public void setYobi2Nm_(String yobi2Nm_) {
		this.yobi2Nm_ = yobi2Nm_;
	}

	/**
	 * 予備3を取得します。
	 * @return 予備3
	 */
	public String getYobi3_() {
		return yobi3_;
	}

	/**
	 * 予備3を設定します。
	 * @param yobi3_ 予備3
	 */
	public void setYobi3_(String yobi3_) {
		this.yobi3_ = yobi3_;
	}

	/**
	 * 予備4を取得します。
	 * @return 予備4
	 */
	public String getYobi4_() {
		return yobi4_;
	}

	/**
	 * 予備4を設定します。
	 * @param yobi4_ 予備4
	 */
	public void setYobi4_(String yobi4_) {
		this.yobi4_ = yobi4_;
	}

	/**
	 * 予備5を取得します。
	 * @return 予備5
	 */
	public String getYobi5_() {
		return yobi5_;
	}

	/**
	 * 予備5を設定します。
	 * @param yobi5_ 予備5
	 */
	public void setYobi5_(String yobi5_) {
		this.yobi5_ = yobi5_;
	}

	/**
	 * 予備6を取得します。
	 * @return 予備6
	 */
	public String getYobi6_() {
		return yobi6_;
	}

	/**
	 * 予備6を設定します。
	 * @param yobi6_ 予備6
	 */
	public void setYobi6_(String yobi6_) {
		this.yobi6_ = yobi6_;
	}

	/**
	 * 予備7を取得します。
	 * @return 予備7
	 */
	public String getYobi7_() {
		return yobi7_;
	}

	/**
	 * 予備7を設定します。
	 * @param yobi7_ 予備7
	 */
	public void setYobi7_(String yobi7_) {
		this.yobi7_ = yobi7_;
	}

	/**
	 * 予備8を取得します。
	 * @return 予備8
	 */
	public String getYobi8_() {
		return yobi8_;
	}

	/**
	 * 予備8を設定します。
	 * @param yobi8_ 予備8
	 */
	public void setYobi8_(String yobi8_) {
		this.yobi8_ = yobi8_;
	}

	/**
	 * 予備9を取得します。
	 * @return 予備9
	 */
	public String getYobi9_() {
		return yobi9_;
	}

	/**
	 * 予備9を設定します。
	 * @param yobi9_ 予備9
	 */
	public void setYobi9_(String yobi9_) {
		this.yobi9_ = yobi9_;
	}

	/**
	 * 予備10を取得します。
	 * @return 予備10
	 */
	public String getYobi10_() {
		return yobi10_;
	}

	/**
	 * 予備10を設定します。
	 * @param yobi10_ 予備10
	 */
	public void setYobi10_(String yobi10_) {
		this.yobi10_ = yobi10_;
	}

	/**
	 * TODO:いなげや
	 * 店舗情報を返します。
	 */
	public String getTenpo(){
		return this.getYobi2_();
	}

	/**
	 * TODO:いなげや
	 * 店舗配列を返します。
	 */
	public String[] getTenpos(){
		return StringUtils.split(this.getYobi2_(), ",");
	}

	/**
	 * TODO:いなげや
	 * 部門情報を返します。
	 */
	public String getBumon(){
		if(isBumonTanto()){
			return "true";
		}
		return "";
	}

	/**
	 * TODO:いなげや
	 * 権限基本情報識別子(管理権限)
	 */
	String admin_prefix = "k";

	/**
	 * TODO:いなげや
	 * 権限基本情報識別子(店舗権限)
	 */
	String tenpo_prefix = "t";

	/**
	 * TODO:いなげや
	 * 権限追加情報識別子(特殊権限)
	 */
	String add_prefix = "s";

	/**
	 * TODO:いなげや
	 * 権限基本情報(店舗権限-店長・主任)
	 */
	String tentyo = tenpo_prefix + "1";

	/**
	 * TODO:いなげや
	 * 権限基本情報(店舗権限-部門担当者)
	 */
	String bumon = tenpo_prefix + "2";

	/**
	 * TODO:いなげや
	 * 権限基本情報の取得
	 * @return pos_custom_value_
	 */
	public String getPos_custom_value_() {
		return pos_custom_value_;
	}

	/**
	 * TODO:いなげや
	 * 権限追加情報の取得
	 * @return pos_add_custom_value_
	 */
	public String getPos_add_custom_value_() {
		return pos_add_custom_value_;
	}

	/**
	 * TODO:いなげや
	 * 権限補足情報の設定
	 * @param logo_ the logo_ to set
	 */
	public void setPos_custom_value_() {
		// 基本情報
		String pos_custom_value = "";
		for(String val : this.pos_custom_value_list_){
			if(admin_prefix.equals(StringUtils.left(val, 2))){
				pos_custom_value = val;
				break;
			}else if(tenpo_prefix.equals(StringUtils.left(val, 2))){
				pos_custom_value = val;
				break;
			}
		}
		this.pos_custom_value_ = pos_custom_value;
		// 追加情報
		String pos_add_custom_value = "";
		for(String val : this.pos_custom_value_list_){
			if(add_prefix.equals(StringUtils.left(val, 2))){
				pos_add_custom_value = val;
				break;
			}
		}
		this.pos_add_custom_value_ = pos_add_custom_value;
	}

	/**
	 * TODO:いなげや
	 * 権限ユーザーの場合Trueを返します。
	 */
	public boolean isAdminUser(){
		return StringUtils.isEmpty(this.getTenpo());
	}

	/**
	 * TODO:いなげや
	 * 店舗ユーザーの場合Trueを返します。
	 */
	public boolean isTenpoUser(){
		return StringUtils.isNotEmpty(this.getTenpo());
	}

	/**
	 * TODO:いなげや
	 * 店長・主任の場合Trueを返します。
	 */
	public boolean isTentyo() {
		return tentyo.equals(getPos_custom_value_());
	}

	/**
	 * TODO:いなげや
	 * 部門担当者の場合Trueを返します。
	 */
	public boolean isBumonTanto() {
		return bumon.equals(getPos_custom_value_());
	}

	/**
	 * TODO:いなげや
	 * 特殊権限を持つ、店舗ユーザーの場合Trueを返します。
	 */
	public boolean isSpecialTenpoUser(){
		return isTenpoUser() && add_prefix.equals(StringUtils.left(getPos_add_custom_value_(), 2));
	}
}
