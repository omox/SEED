package common;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate.DATE_FORMAT;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ItemDao;
import dao.Reportx242Dao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Pass extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Pass(String JNDIname) {
		super(JNDIname);
	}

	public JSONObject update(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

		JSONObject option = new JSONObject();

		// 更新処理
		try {
			option = this.updateData(map, userInfo, sysdate);
		} catch (Exception e) {
			e.printStackTrace();
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return option;
	}

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();

		// ユーザー情報取得
		StringBuffer		sbSQL		= new StringBuffer();
		ArrayList<String>	paramData	= new ArrayList<String>();
		JSONArray			dbDatas		= new JSONArray();
		ItemList			iL			= new ItemList();
		String				userCd		= String.valueOf(userInfo.getCD_user());
		String				yobi6		= "";
		String				yobi7		= "";
		String				yobi8		= "";
		String				yobi9		= "";
		String				authMs		= "0";
		String				authTk		= "0";
		String				authTn		= "0";

		sbSQL.append("SELECT ");
		sbSQL.append("YOBI_6,YOBI_7,YOBI_8,YOBI_9 ");
		sbSQL.append("FROM KEYSYS.SYS_USERS ");
		sbSQL.append("WHERE CD_USER=?");
		paramData.add(userCd);

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() > 0) {
			yobi6 = dbDatas.getJSONObject(0).containsKey("YOBI_6") ? dbDatas.getJSONObject(0).getString("YOBI_6") : "";
			yobi7 = dbDatas.getJSONObject(0).containsKey("YOBI_7") ? dbDatas.getJSONObject(0).getString("YOBI_7") : "";
			yobi8 = dbDatas.getJSONObject(0).containsKey("YOBI_8") ? dbDatas.getJSONObject(0).getString("YOBI_8") : "";
			yobi9 = dbDatas.getJSONObject(0).containsKey("YOBI_9") ? dbDatas.getJSONObject(0).getString("YOBI_9") : "";
		}

		map.put("USERCD", userCd);
		map.put("YOBI6", yobi6);
		map.put("YOBI7", yobi7);
		map.put("YOBI8", yobi8);
		map.put("YOBI9", yobi9);
		map.put("AUTHMS", authMs);
		map.put("AUTHTK", authTk);
		map.put("AUTHTN", authTn);
		map.put("DTPWTERM", CmnDate.dateFormat(CmnDate.getDayAddedDate(new Date(),90),DATE_FORMAT.DB_DATE8));
		map.put(DefineReport.ID_PARAM_PAGE, "Pass");

		Reportx242Dao dao = new Reportx242Dao(JNDIname);
		dao.createSqlUser(map,userInfo);

		sqlList = dao.sqlList;
		prmList = dao.prmList;
		lblList = dao.lblList;

		ArrayList<Integer> countList  = new ArrayList<Integer>();
		if(sqlList.size() > 0){
			countList =  super.executeSQLs(sqlList, prmList);
		}

		if(StringUtils.isEmpty(getMessage())){
			int count = 0;
			for (int i = 0; i < countList.size(); i++) {
				count += countList.get(i);
				System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{lblList.get(i), Integer.toString(countList.get(i))}));
			}
			if(count==0){
				option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
			}else{
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00006.getVal(), "データ", String.valueOf(1)));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}
		return option;
	}

	/**
	 * パスワード更新処理
	 *
	 * @param pass
	 * @param userCd
	 * @param userId
	 */
	public String createSqlPass(String pass, String userCd, String userId){

		StringBuffer sbSQL = new StringBuffer();
		ArrayList<String> paramData = new ArrayList<String>();

		// パスワードの更新
		sbSQL = new StringBuffer();
		sbSQL.append("MERGE INTO KEYSYS.SYS_USERS AS T USING (SELECT ");
		sbSQL.append(" CD_USER");											// ユーザーID
		sbSQL.append(",USER_ID");											// ユーザーコード
		sbSQL.append(",DT_PW_TERM");										// 有効期限
		sbSQL.append(",PASSWORDS");											// パスワード
		sbSQL.append(", '"+userId+"' AS OPERATOR ");						// オペレーター：
		sbSQL.append(", current timestamp AS ADDDT ");						// 登録日：
		sbSQL.append(", current timestamp AS UPDDT ");						// 更新日：
		sbSQL.append(" FROM (values (?,?,?,?)) as T1(");
		sbSQL.append("CD_USER");											// ユーザーID
		sbSQL.append(",USER_ID");											// ユーザーコード
		sbSQL.append(",DT_PW_TERM");										// 有効期限
		sbSQL.append(",PASSWORDS");											// パスワード
		sbSQL.append(")) as RE on (");
		sbSQL.append("T.CD_USER=RE.CD_USER AND ");
		sbSQL.append("T.USER_ID=RE.USER_ID ");
		sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
		sbSQL.append("DT_PW_TERM=RE.DT_PW_TERM ");
		sbSQL.append(",PASSWORDS=RE.PASSWORDS ");
		sbSQL.append(",PASSWORDS_1=PASSWORDS");
		sbSQL.append(",PASSWORDS_2=PASSWORDS_1");
		sbSQL.append(",PASSWORDS_3=PASSWORDS_2");
		sbSQL.append(",PASSWORDS_4=PASSWORDS_3");
		sbSQL.append(",PASSWORDS_5=PASSWORDS_4");
		sbSQL.append(",NM_UPDATE=RE.OPERATOR");
		sbSQL.append(",DT_UPDATE=RE.UPDDT");
		sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
		sbSQL.append(" CD_USER");
		sbSQL.append(",USER_ID");
		sbSQL.append(",DT_PW_TERM");
		sbSQL.append(",PASSWORDS");
		sbSQL.append(",NM_CREATE");
		sbSQL.append(",NM_UPDATE");
		sbSQL.append(",DT_CREATE");
		sbSQL.append(",DT_UPDATE");
		sbSQL.append(") VALUES (");
		sbSQL.append(" RE.CD_USER");
		sbSQL.append(",RE.USER_ID");
		sbSQL.append(",RE.DT_PW_TERM");
		sbSQL.append(",RE.PASSWORDS");
		sbSQL.append(",RE.OPERATOR");
		sbSQL.append(",RE.OPERATOR");
		sbSQL.append(",RE.ADDDT");
		sbSQL.append(",RE.UPDDT");
		sbSQL.append(")");

		// ユーザーコード
		paramData.add(userCd);
		// ユーザーID
		paramData.add(userId);
		// 有効期限 システム日付の翌日から90日後の日付を算出
		paramData.add(CmnDate.dateFormat(CmnDate.getDayAddedDate(new Date(),90),DATE_FORMAT.DB_DATE8));
		// パスワード
		paramData.add(pass);

		System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("SYS_USERS");

		return sbSQL.toString();
	}
}
