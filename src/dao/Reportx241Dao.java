package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx241Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	// 削除時
	String in = "";

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx241Dao(String JNDIname) {
		super(JNDIname);
	}

	/**
	 * 検索実行
	 *
	 * @return
	 */
	public boolean selectBy() {

		// 検索コマンド生成
		String command = createCommand();

		// 出力用検索条件生成
		outputQueryList();

		// 検索実行
		return super.selectBySQL(command);
	}

	// 削除処理
	public JSONObject delete(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

		JSONObject option = new JSONObject();

		JSONArray msgList = this.checkDel(map, userInfo, sysdate);

		if(msgList.size() > 0){
			option.put(MsgKey.E.getKey(), msgList);
			return option;
		}

		// 更新処理
		try {
			option = this.deleteData(map, userInfo, sysdate);
		} catch (Exception e) {
			e.printStackTrace();
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return option;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {

		// 削除データ検索用コード
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		// 格納用変数
		JSONArray			msg		= new JSONArray();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}
		return msg;
	}

	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		// 排他チェック用
		String				targetTable	= null;
		String				targetWhere	= null;
		ArrayList<String>	targetParam	= new ArrayList<String>();
		JSONArray			msg			= new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// DB検索用パラメータ
		ArrayList<String>	paramData = new ArrayList<String>();
		ItemList			iL			= new ItemList();
		JSONArray			dbDatas 	= new JSONArray();
		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();
		sbSQL.append(" SELECT MAX(DATE_FORMAT(DT_UPDATE, '%Y%m%d%H%i%s%f')) as HDN_UPDDT,CD_USER FROM KEYSYS.SYS_USERS WHERE CD_USER IN (");
		for (int i = 0; i < dataArray.size(); i++) {

			JSONObject data = dataArray.getJSONObject(i);

			// ユーザーコード
			String userCd	= data.optString("F11");

			// 排他制御で使用する更新日付を取得
			if ((i+1) ==  dataArray.size()) {
				sbSQL.append(" ?");
			} else {
				sbSQL.append(" ?,");
			}
			paramData.add(userCd);
		}

		sbSQL.append(") GROUP BY CD_USER");

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		String cd_user		= "";
		String hdn_upddt	= "";
		if (dbDatas.size() != 0) {
			cd_user = dbDatas.getJSONObject(0).getString("CD_USER");
			hdn_upddt = dbDatas.getJSONObject(0).getString("HDN_UPDDT");
		}

		// 配送グループマスタ、配送店グループマスタINSERT/UPDATE処理
		createDelSqlUsers(dataArray,userInfo);

		// 排他チェック
		targetTable = "(SELECT CD_USER,DT_UPDATE AS UPDDT FROM KEYSYS.SYS_USERS)";
		targetWhere = " CD_USER = ? ";
		targetParam.add(cd_user);
		if(! super.checkExclusion(targetTable, targetWhere, targetParam, hdn_upddt)){
 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
 			option.put(MsgKey.E.getKey(), msg);
			return option;
		}

		ArrayList<Integer> countList  = new ArrayList<Integer>();
		if(sqlList.size() > 0){
			countList =  super.executeSQLs(sqlList, prmList);
		}

		if(StringUtils.isEmpty(getMessage())){
			int count = 0;
			for (int i = 0; i < countList.size(); i++) {
				count += countList.get(i);
				if (DefineReport.ID_DEBUG_MODE)	System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{lblList.get(i), Integer.toString(countList.get(i))}));
			}
			if(count==0){
				option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
			}else{
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}
		return option;
	}

	/**
	 * ユーザーマスタ削除処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createDelSqlUsers(JSONArray dataArray, User userInfo){

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();

		// ログインユーザー情報取得
		String userId	= userInfo.getId(); // ログインユーザー

		// DB検索用パラメータ
		ArrayList<String>	paramData = new ArrayList<String>();
		ItemList			iL			= new ItemList();
		JSONArray			dbDatas 	= new JSONArray();

		Reportx242Dao dao = new Reportx242Dao(JNDIname);

		for (int i = 0; i < dataArray.size(); i++) {

			JSONObject data = dataArray.getJSONObject(i);

			// ユーザーコード
			String userCd	= data.optString("F11");

			// 排他制御で使用する更新日付を取得
			in += (i+1) ==  dataArray.size() ? userCd : userCd+",";

			// SYS_USERS_JNLの挿入
			String jnlSeq = dao.getCSVTOK_SEQ();
			sbSQL = new StringBuffer();
			paramData = new ArrayList<String>();
			sbSQL.append(" INSERT INTO KEYSYS.SYS_USERS_JNL ");
			sbSQL.append(" SELECT");
			sbSQL.append(" ? AS SEQ");
			paramData.add(jnlSeq);
			sbSQL.append(" , current_timestamp AS INF_DATE");
			sbSQL.append(" , ? AS INF_OPERATOR");
			paramData.add(userId);
			sbSQL.append(" ,'1' AS INF_TABLEKBN");
			sbSQL.append(" ,'9' AS INF_TRNKBN");
			sbSQL.append(" , ? AS CD_USER");
			paramData.add(userCd);
			sbSQL.append(" ,USER_ID");
			sbSQL.append(" ,PASSWORDS");
			sbSQL.append(" ,NM_FAMILY");
			sbSQL.append(" ,NM_NAME");
			sbSQL.append(" ,CUSTOM_VALUE");
			sbSQL.append(" ,NM_CREATE");
			sbSQL.append(" ,DT_CREATE");
			sbSQL.append(" ,NM_UPDATE");
			sbSQL.append(" ,DT_UPDATE");
			sbSQL.append(" ,CD_AUTH");
			sbSQL.append(" ,DT_PW_TERM");
			sbSQL.append(" ,LOGO");
			sbSQL.append(" ,YOBI_1");
			sbSQL.append(" ,YOBI_2");
			sbSQL.append(" ,YOBI_3");
			sbSQL.append(" ,YOBI_4");
			sbSQL.append(" ,YOBI_5");
			sbSQL.append(" ,YOBI_6");
			sbSQL.append(" ,YOBI_7");
			sbSQL.append(" ,YOBI_8");
			sbSQL.append(" ,YOBI_9");
			sbSQL.append(" ,YOBI_10");
			sbSQL.append(" ,PASSWORDS_1");
			sbSQL.append(" ,PASSWORDS_2");
			sbSQL.append(" ,PASSWORDS_3");
			sbSQL.append(" ,PASSWORDS_4");
			sbSQL.append(" ,PASSWORDS_5");
			sbSQL.append(" FROM");
			sbSQL.append(" KEYSYS.SYS_USERS");
			sbSQL.append(" WHERE");
			sbSQL.append(" CD_USER=?");
			paramData.add(userCd);

			if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			dao.sqlList.add(sbSQL.toString());
			dao.prmList.add(paramData);
			dao.lblList.add("SYS_USERS_JNL");

			// ユーザーマスタ物理削除
			sbSQL = new StringBuffer();
			paramData = new ArrayList<String>();
			sbSQL.append("DELETE FROM ");
			sbSQL.append("KEYSYS.SYS_USERS ");
			sbSQL.append("WHERE CD_USER = ? ");
			paramData.add(userCd);

			if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			dao.sqlList.add(sbSQL.toString());
			dao.prmList.add(paramData);
			dao.lblList.add("ユーザーマスタ");

			// その他テーブル更新
			dao.createSqlUserPos(userId,userCd,"1","24321",jnlSeq,"1");
			dao.createSqlUserPos(userId,userCd,"1","24320",jnlSeq,"1");
			dao.createSqlUserPos(userId,userCd,"1","24322",jnlSeq,"1");
		}
		sqlList = dao.sqlList;
		prmList = dao.prmList;
		lblList = dao.lblList;

		return "";
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szId		= getMap().get("ID");	// ID
		String szName	= getMap().get("NAME");	// NAME
		String szTen	= getMap().get("TEN");	// TEN

		// 配送グループ名称（漢字）に入力があった場合部分一致検索を行う
		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();
		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(" with WKUSER as ( ");
		sbSQL.append(" SELECT T1.* ");
		sbSQL.append(" , T2.TENCD ");
		sbSQL.append(" , T2.TENKN ");
		sbSQL.append(" FROM ( ");
		sbSQL.append(" select ");
		sbSQL.append(" T1.USER_ID ");
		sbSQL.append(" , LPAD('',LENGTH(T1.PASSWORDS),'*') AS PASSWORDS ");
		sbSQL.append(" , T1.NM_FAMILY || T1.NM_NAME AS USERNM ");
		sbSQL.append(" , DATE_FORMAT( ");
		sbSQL.append(" DATE_FORMAT(T1.DT_PW_TERM, '%Y%m%d') ");
		sbSQL.append(" , '%Y/%m/%d' ");
		sbSQL.append(" ) as DT_PW_TERM ");
		sbSQL.append(" , case ");
		sbSQL.append(" when T1.YOBI_2 is null ");
		sbSQL.append(" or T1.YOBI_2 = '' ");
		sbSQL.append(" then null ");
		sbSQL.append(" else cast(T1.YOBI_2 as SIGNED) ");
		sbSQL.append(" end YOBI_2 ");
		sbSQL.append(" , T1.YOBI_6 ");
		sbSQL.append(" , T1.YOBI_7 ");
		sbSQL.append(" , T1.YOBI_8 ");
		sbSQL.append(" , T1.NM_UPDATE || '　' || T2.NM_FAMILY || T2.NM_NAME AS UPDUSER ");
		sbSQL.append(" , DATE_FORMAT(T1.DT_UPDATE ,'%Y/%m/%d %H:%i') AS DT_UPDATE ");
		sbSQL.append(" , T1.CD_USER ");
		sbSQL.append(" from KEYSYS.SYS_USERS T1 ");
		sbSQL.append(" left join KEYSYS.SYS_USERS T2 ");
		sbSQL.append(" on T1.NM_UPDATE = T2.USER_ID ) T1 ");
		sbSQL.append(" left join INAMS.MSTTEN T2 ");
		sbSQL.append(" on T1.YOBI_2 = T2.TENCD ");

		// ユーザーID
		if (!StringUtils.isEmpty(szId)) {
			sqlWhere = " where T1.USER_ID like ? ";
			paramData.add("%" + szId + "%");
		}

		// 姓名
		if (!StringUtils.isEmpty(szName)) {
			if (!StringUtils.isEmpty(sqlWhere)) {
				sqlWhere += " and T1.USERNM like ? ";
			} else {
				sqlWhere += " where T1.USERNM like ? ";
			}
			paramData.add("%" + szName + "%");
		}

		// 店コード
		if (!StringUtils.isEmpty(szTen) && !szTen.equals("-1")) {
			if (!StringUtils.isEmpty(sqlWhere)) {
				sqlWhere += " and T2.TENCD =? ";
			} else {
				sqlWhere += " where T2.TENCD =? ";
			}
			paramData.add(szTen);
		}

		sbSQL.append(sqlWhere+" ) ");
		sbSQL.append(" , WKMS as ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" CD_USER AS CD_USER_WK ");
		sbSQL.append(" FROM ");
		sbSQL.append(" KEYSYS.SYS_USER_POS ");
		sbSQL.append(" WHERE ");
		sbSQL.append(" CD_POSITION = 24321) ");
		sbSQL.append(" , WKTK as ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" CD_USER AS CD_USER_WK ");
		sbSQL.append(" FROM ");
		sbSQL.append(" KEYSYS.SYS_USER_POS ");
		sbSQL.append(" WHERE");
		sbSQL.append(" CD_POSITION = 24320) ");
		sbSQL.append(" , WKTN as ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" CD_USER AS CD_USER_WK ");
		sbSQL.append(" FROM ");
		sbSQL.append(" KEYSYS.SYS_USER_POS ");
		sbSQL.append(" WHERE ");
		sbSQL.append(" CD_POSITION = 24322) ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" USER_ID ");								// F1:ユーザーID
		sbSQL.append(" , PASSWORDS ");							// F2:パスワード
		sbSQL.append(" , USERNM ");								// F3:姓名
		sbSQL.append(" , case ");								// F4:所属
		sbSQL.append(" when TENCD is null ");
		sbSQL.append(" then '本部' ");
		sbSQL.append(" else YOBI_2 || '　' || TENKN ");
		sbSQL.append(" end ");
		sbSQL.append(" , DT_PW_TERM ");							// F5:有効期限
		sbSQL.append(" , case ");								// F6:本部マスタ
		sbSQL.append(" when NOT EXISTS ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" 1 ");
		sbSQL.append(" FROM ");
		sbSQL.append(" WKMS T1 ");
		sbSQL.append(" WHERE ");
		sbSQL.append(" CD_USER = T1.CD_USER_WK) ");
		sbSQL.append(" then '参照不可' ");
		sbSQL.append(" when YOBI_6 = '1' ");
		sbSQL.append(" and YOBI_7 = '1' ");
		sbSQL.append(" then '登録・削除可' ");
		sbSQL.append(" when YOBI_6 = '1' ");
		sbSQL.append(" then '登録可' ");
		sbSQL.append(" else '参照可' end ");
		sbSQL.append(" , case ");								// F7:本部特売
		sbSQL.append(" when NOT EXISTS ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" 1 ");
		sbSQL.append(" FROM ");
		sbSQL.append(" WKTK T1 ");
		sbSQL.append(" WHERE ");
		sbSQL.append(" CD_USER = T1.CD_USER_WK) ");
		sbSQL.append(" then '参照不可' ");
		sbSQL.append(" when YOBI_8 = '1' ");
		sbSQL.append(" then '登録・削除可' ");
		sbSQL.append(" else '参照可' end ");
		sbSQL.append(" , case ");								// F8:店舗画面
		sbSQL.append(" when NOT EXISTS ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" 1 ");
		sbSQL.append(" FROM ");
		sbSQL.append(" WKTN T1 ");
		sbSQL.append(" WHERE ");
		sbSQL.append(" CD_USER = T1.CD_USER_WK) ");
		sbSQL.append(" then '参照不可' ");
		sbSQL.append(" else '登録・削除可' end ");
		sbSQL.append(" , UPDUSER ");							// F9:更新ユーザー
		sbSQL.append(" , DT_UPDATE ");							// F10:更新日
		sbSQL.append(" , CD_USER ");							// F11:ユーザーコード(hidden)
		sbSQL.append(" FROM WKUSER ORDER BY USER_ID ");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
		List<String> cells = new ArrayList<String>();

		// 共通箇所設定
		createCmnOutput(jad);
	}

	// 最小週Noを取得する
	public String getMinShuNo() {

		// 格納用変数
		StringBuffer		sbSQL	= new StringBuffer();
		ArrayList<String> paramData	= new ArrayList<String>();
		String				shuNo	= "";
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas = new JSONArray();

		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(", SHORIDT as (select");
		sbSQL.append(" right (YYYY, 2) || right ('00' || (case when WEEK_NO in (2, 3) then SHUNO else SHUNO + 1 end), 2) as SHUNO");
		sbSQL.append(" from (select");
		sbSQL.append(" SHORIDT as value");
		sbSQL.append(", DATE_FORMAT(DATE_FORMAT(SHORIDT, '%Y%m%d'), '%Y') as YYYY");
		sbSQL.append(", WEEK_ISO(DATE_FORMAT(SHORIDT, '%Y%m%d')) as SHUNO");
		sbSQL.append(", DAYOFWEEK(DATE_FORMAT(SHORIDT, '%Y%m%d')) as WEEK_NO");
		sbSQL.append(" from INAAD.SYSSHORIDT where COALESCE(UPDKBN, 0) <> 1");
		sbSQL.append(" order by ID desc fetch first 1 rows only))");
		sbSQL.append(" select *");
		sbSQL.append(" from (select");
		sbSQL.append(" right ('0000' || MIN(T1.SHUNO), 4) as value");
		sbSQL.append(" from INAAD.SYSSHUNO T1");
		sbSQL.append(" inner join (select distinct SHUNO from INATK.HATJTR_TEN) T2 on T1.SHUNO = T2.SHUNO, SHORIDT");
		sbSQL.append(" where SHORIDT.SHUNO < T1.SHUNO) T order by replace (value, '-1', '9999') desc");

		@SuppressWarnings("static-access")
		JSONArray array  = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() >= 0){
			shuNo =  array.optJSONObject(0).optString("VALUE");
		}
		return shuNo;
	}

	/**
	 * 排他判断<br>
	 * 更新日時が変わっていないことを判断する<br>
	 * @param targetTable 対象テーブル
	 * @param targetWhere テーブルのキー条件
	 * @param targetParam テーブルのキー
	 * @param inp_upddt 画面入力開始時の更新日時
	 * @return true:一致/false:不一致(排他発生)
	 */
	public boolean checkExclusion(HashMap<String, String> map, String inp_upddt) {

		ItemList iL = new ItemList();

		String szShncd	= map.get("SHNCD");		// 商品コード
		String szBumon	= map.get("BUMON");		// 部門
		String szDaibun	= map.get("DAIBUN");	// 大分類
		String szChubun	= map.get("CHUBUN");	// 中分類
		String szTencd	= map.get("TENCD");		// 店コード
		String szShuno	= map.get("SHUNO");		// 週No.
		String szSeiki	= map.get("SEIKI");		// 正規
		String szJisyu	= map.get("JISYU");		// 次週

		// 配送グループ名称（漢字）に入力があった場合部分一致検索を行う
		// DB検索用パラメータ
		String sqlFrom = "";
		String sqlWhere = "";
		String sqlWith = "";
		ArrayList<String> paramData = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		JSONArray		dataArrayT	= JSONArray.fromObject(map.get("DATA_HATSTR"));	// 正規定量_店別数量(次週も含む)
		int len = dataArrayT.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataT = dataArrayT.getJSONObject(i);

			String UPDDT	 = dataT.optString("F13");
			String JSEIKBN	 = dataT.optString("F2");
			String SHUNO	 = dataT.optString("F3");
			String SHNCD	 = dataT.optString("F4");
			String TENCD	 = dataT.optString("F5");

			paramData.add(UPDDT);
			paramData.add(JSEIKBN);
			paramData.add(SHUNO);
			paramData.add(SHNCD);
			paramData.add(TENCD);

			sbSQL.append(" select");
			sbSQL.append(" COUNT(UPDDT) as value");
			sbSQL.append(" from (select * from (select");
			sbSQL.append(" TK.JSEIKBN");
			sbSQL.append(", TK.SHUNO");
			sbSQL.append(", TK.SHNCD");
			sbSQL.append(", TK.TENCD");
			sbSQL.append(", DATE_FORMAT(TK.UPDDT, '%Y%m%d%H%i%s%f') as UPDDT");
			sbSQL.append(" from INATK.HATTR_CSV TK");
			sbSQL.append(" where TK.UPDKBN = 0))");
			sbSQL.append(" where UPDDT = ?");
			sbSQL.append(" and JSEIKBN = ?");
			sbSQL.append(" and SHUNO = ?");
			sbSQL.append(" and SHNCD = ?");
			sbSQL.append(" and TENCD = ?");

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0){
				return false;
			}

		}
		return true;
	}
}
