/*
 * 作成日: 2006/08/28
 *
 */
package authentication.parser;

import java.util.Date;
import java.text.SimpleDateFormat;

import authentication.defines.Defines;


/**
 * ユーザーを表すクラス <br />
 */
public class User {

	/** ID */
	private String id_; //User ID
	/** ﾊﾟｽﾜｰﾄﾞ */
	private String pass_; //User password
	/** ユーザー名称 */
	private String name_; //User Name
	/** ユーザー権限 */
	private String auth_; //User Auth
	/** ﾊﾟｽﾜｰﾄﾞ有効期限 */
	private String pwdate_; //User dt_pw_date
	/** ロゴパス */
	private String logo_; //User logo

	/** 各値の最大桁数:ID 半角12桁 */
	public final static int LENGTH_ID = 12;
	/** 各値の最大桁数:ﾊﾟｽﾜｰﾄﾞ 半角20桁 */
	public final static int LENGTH_PASS = 20;
	/** 各値の最大桁数:ﾕｰｻﾞｰ名称 半角12桁 */
	public final static int LENGTH_NAME = 12;

	/**
	 * コンストラクタ <br />
	 * 全ての値にNull(int型は0)を設定する。 <br />
	 */
	public User() {
		this.id_ = null;
		this.pass_ = null;
		this.name_ = null;
		this.auth_ = null;
		this.pwdate_ = null;
	}

	/**
	 * コンストラクタ <br />
	 *
	 * @param id user_id
	 * @param pass user_password
	 * @param name user_name
	 * @param auth user_auth
	 * @param divide user_divide
	 * @param ccl user_ccl_analyze
	 */
	public User(String id, String pass, String name, String auth,String pwdate) {
		this.id_ = id;
		this.pass_ = pass;
		this.name_ = name;
		this.auth_ = auth;
		this.setPwdate(pwdate);
	}

	/**
	 * @return auth を戻す。 <br />
	 */
	public String getAuth() {
		return auth_;
	}

	/**
	 * @param auth auth を設定。 <br />
	 */
	public void setAuth(String auth) {
		this.auth_ = auth;
	}

	/**
	 * 2次元配列より1列目をkeyとして、key指定で値を取り出す
	 * Defines.USER_ACCESS_LVXX 用
	 *
	 * @param authList
	 * @param auth
	 * @return

	public String getAuthName(String[][] authList,String auth){
		for(int i=0;i<authList.length; i++){
			String key = authList[i][0];
			if (key.equals(auth)){ return authList[i][1]; }
		}
		return new String("");
	}
		 */

	/**
	 * @return id_ を戻す。 <br />
	 */
	public String getId() {
		return id_;
	}

	/**
	 * @param id id を設定。 <br />
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
	 * @param name name を設定。 <br />
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
	 * @param pass pass を設定。 <br />
	 */
	public void setPass(String pass) {
		this.pass_ = pass;
	}

	/**
	 * @return pwdate_ を戻す。 <br />
	 */
	public String getPwdate() {
		return pwdate_;
	}
	public String getPwdate(String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
  	try{
  		Date dt = sdf.parse(this.pwdate_);
  		String sdt = sdf.format(dt);
  		sdf.applyPattern(Defines.DT_PW_FORMAT_DISP);
  		return sdt;
  	}catch(Exception e){
  		return this.getPwdate();
  	}
	}
	public Date getPwdatedt(String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
  	try{
  		Date dt = sdf.parse(this.pwdate_);
  		sdf.applyPattern(Defines.DT_PW_FORMAT_DISP);
  		return dt;
  	}catch(Exception e){

  		return new Date();
  	}
	}
	/**
	 * @param pwdate_ pwdate を設定。 <br />

	public void setPwdate(Date pwdate) {
		this.pwdate_ = pwdate;
	}*/
  public void setPwdate(String pwdate){
  	SimpleDateFormat sdf = new SimpleDateFormat(Defines.DT_PW_FORMAT_DISP);
		try {
			Date dt = sdf.parse(pwdate);
			sdf.applyPattern(Defines.DT_PW_FORMAT_SAVE);
			if(sdf.format(dt).toString().equals(pwdate.replaceAll("/",""))){
				this.pwdate_ = sdf.format(dt);
			}else{
				this.pwdate_ = pwdate;
			}
		} catch (Exception e) {
	  	this.pwdate_ = pwdate;
		}


  }
	/**
	 * @return logo_
	 */
	public String getLogo_() {
		return logo_;
	}

	/**
	 * @param logo_ the logo_ to set
	 */
	public void setLogo_(String logo_) {
		this.logo_ = logo_;
	}

}
