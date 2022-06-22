package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import common.CmnDate;
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
public class Reportx221Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();
	/** 登録時に受け取ったデータを加工する */
	JSONArray dataArrayNew = new JSONArray();
	/** テーブル検索時に必要なデータ */
	String szCenterCd	= "";	// センター
	String szSupplyNo	= "";	// 便
	String szStd		= "";	// 有効開始日
	String szEdd		= "";	// 有効終了日
	String szHandleEdd	= "";	// 取扱終了日
	String szUpdd		= "";	// 更新日付（排他チェック用）

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx221Dao(String JNDIname) {
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

	public JSONObject update(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {

		JSONObject option = new JSONObject();
		JSONArray msgList = this.check(map);

		if(msgList.size() > 0){
			option.put(MsgKey.E.getKey(), msgList);
			return option;
		}

		// 更新処理
		try {
			option = this.updateData(userInfo);
		} catch (Exception e) {
			e.printStackTrace();
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return option;
	}

	// 削除処理
	public JSONObject delete(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {

		JSONObject option = new JSONObject();
		JSONArray msgList = this.checkDel(map);

		if(msgList.size() > 0){
			option.put(MsgKey.E.getKey(), msgList);
			return option;
		}

		// 更新処理
		try {
			option = this.deleteData(userInfo);
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
	public JSONArray check(HashMap<String, String> map) {
		// パラメータ確認
		szCenterCd	= map.get("CENTERCD");								// センター
		szSupplyNo	= map.get("SUPPLYNO");								// 便
		szStd		= map.get("STD");									// 有効開始日
		szEdd		= map.get("EDD");									// 有効終了日
		szHandleEdd	= map.get("HANDLEEDD");								// 取扱終了日
		szUpdd		= map.get("UPDD");									// 更新日付（排他チェック用）
		JSONArray dataArrayT = JSONArray.fromObject(map.get("DATA"));	// 更新情報(コースマスタ情報)

		// 重複チェック用
		Set<String>	tencds = new HashSet<String>();

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();
		JSONArray		msg		= new JSONArray();
		ItemList		iL		= new ItemList();
		MessageUtility	mu		= new MessageUtility();
		JSONArray		dbDatas = new JSONArray();

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		// チェック処理
		// 対象件数チェック
		if (StringUtils.isEmpty(szCenterCd)	||
			StringUtils.isEmpty(szStd)		||
			StringUtils.isEmpty(szEdd)		||
			StringUtils.isEmpty(szSupplyNo)
		) {
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		if(dataArrayT.size() == 0 || dataArrayT.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 終了日にMAX値を代入
		if (szHandleEdd.equals("")) {
			szHandleEdd = "20501231";
		}

		// 有効開始～終了の範囲チェック
		if (Integer.valueOf(szStd) > Integer.valueOf(szEdd)) {
			// 有効開始日 ≦ 有効終了日の条件で入力してください。
			msg.add(mu.getDbMessageObj("EX1120", new String[]{}));
			return msg;
		}

		// 取扱終了日の範囲チェック
		if (Integer.valueOf(szStd) > Integer.valueOf(szHandleEdd) || Integer.valueOf(szEdd) < Integer.valueOf(szHandleEdd)) {
			// 有効開始日 ≦ 取扱終了日 ≦ 有効終了日を入力してください。
			msg.add(mu.getDbMessageObj("E30012", new String[]{"有効開始日 ≦ 取扱終了日 ≦ 有効終了日"}));
			return msg;
		}

		// センターコードマスタ存在チェック
		/*
		sqlWhere += "TENCD=?";
		paramData.add(szCenterCd);

		sbSQL.append("SELECT ");
		sbSQL.append("TENCD ");	// レコード件数
		sbSQL.append("FROM ");
		sbSQL.append("INAMS.MSTTEN ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere); // 入力されたセンターコードで検索

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() == 0){
			// マスタに登録のないセンター
			msg.add(mu.getDbMessageObj("EX1100", new String[]{"センターコード","店舗基本マスタ"}));
			return msg;
		}
		*/

		// 変数を初期化
		sbSQL	= new StringBuffer();
		iL		= new ItemList();
		dbDatas = new JSONArray();
		sqlWhere	= "";
		paramData	= new ArrayList<String>();

		// 排他チェック時に使用する更新日付を変更
		sqlWhere += "CENTERCD=? AND ";
		paramData.add(szCenterCd);

		sqlWhere += "SUPPLYNO=? AND ";
		paramData.add(szSupplyNo);

		sqlWhere += "EFFECTIVESTARTDATE=? AND ";
		paramData.add(CmnDate.dbDateFormat(szStd));
		sqlWhere += "LOGICALDELFLG=? ";
		paramData.add(DefineReport.ValUpdkbn.NML.getVal());

		sbSQL.append("SELECT ");
		sbSQL.append("CENTERCD ");	// レコード件数
		sbSQL.append("FROM ");
		sbSQL.append("INAORR.ORRCOURSEMASTER ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() == 0){
			// 今回登録するデータが新規レコードなら
			szUpdd = "";
		}

		// 同一センター・便で有効なレコードを検索

		// 変数を初期化
		sbSQL	= new StringBuffer();
		iL		= new ItemList();
		dbDatas = new JSONArray();
		sqlWhere	= "";
		paramData	= new ArrayList<String>();

		sqlWhere += "CENTERCD=? AND ";
		paramData.add(szCenterCd);

		sqlWhere += "SUPPLYNO=? AND ";
		paramData.add(szSupplyNo);

		// 入力の有効開始日+1日～入力の有効終了日内に有効なレコード
		sqlWhere += "(EFFECTIVESTARTDATE >= ? AND EFFECTIVEENDDATE <= ?) AND ";
		paramData.add(CmnDate.dbDateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(szStd),1)));
		paramData.add(CmnDate.dbDateFormat(szEdd));

		sqlWhere += "LOGICALDELFLG=? ";
		paramData.add(DefineReport.ValUpdkbn.NML.getVal());

		sbSQL.append("SELECT ");
		sbSQL.append("CENTERCD ");	// レコード件数
		sbSQL.append("FROM ");
		sbSQL.append("INAORR.ORRCOURSEMASTER ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere); // 入力された店コードで検索

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() >= 1){
			// 有効なレコードが存在している場合
			msg.add(mu.getDbMessageObj("EX1034", new String[]{}));
			return msg;
		}

		// 登録・更新の形式にデータを変換しつつ店舗チェック
		int course = 1;
		boolean courseFlg = false;

		for (int i=0; i<dataArrayT.size(); i++) {

			JSONObject dataT = dataArrayT.getJSONObject(i);

			int ten = 1;

			for (int j=0; j<dataT.size(); j++) {
				String filed = "F"+(j+1);

				if (!StringUtils.isEmpty(dataT.optString(filed))) {

					// 店コード重複チェック
					if ( j >= 8 ){

						JSONObject dataTC = new JSONObject();
						dataTC.accumulate("F1",szCenterCd);							// センター
						dataTC.accumulate("F2",szSupplyNo);							// 便
						dataTC.accumulate("F3",CmnDate.dbDateFormat(szStd));		// 有効開始日
						dataTC.accumulate("F4",CmnDate.dbDateFormat(szEdd));		// 有効終了日
						dataTC.accumulate("F5",CmnDate.dbDateFormat(szHandleEdd));	// 取扱終了日
						dataTC.accumulate("F6",course);								// コース
						dataTC.accumulate("F7",ten);								// 配送順
						dataTC.accumulate("F8",dataT.optString(filed));				// 店
						dataTC.accumulate("F9",szStd);								// 変換前有効開始日
						courseFlg = true;
						ten++;

						dataArrayNew.add(dataTC);

						if(!tencds.contains(dataT.optString(filed))) {
							tencds.add(dataT.optString(filed));
						} else {
							// エラー
							msg.add(mu.getDbMessageObj("E11141", new String[]{}));
							return msg;
						}
					}
				}
			}

			if (courseFlg) {
				course++;
				courseFlg = false;
			}
		}

		// 店コードが一件も入力されていない
		if (tencds.size() == 0) {
			// 有効なレコードが存在している場合
			msg.add(mu.getDbMessageObj("E30012", new String[]{"店コード"}));
			return msg;
		}

		Iterator<String> itencds = tencds.iterator();

		// 店舗基本マスタ存在チェック
		for (int i = 0; i < tencds.size(); i++) {

			// 変数を初期化
			sbSQL	= new StringBuffer();
			iL		= new ItemList();
			dbDatas = new JSONArray();
			sqlWhere	= "";
			paramData	= new ArrayList<String>();

			sqlWhere += "TENCD=?";
			paramData.add(itencds.next());

			sbSQL.append("SELECT ");
			sbSQL.append("TENCD ");	// レコード件数
			sbSQL.append("FROM ");
			sbSQL.append("INAMS.MSTTEN ");
			sbSQL.append("WHERE ");
			sbSQL.append(sqlWhere); // 入力された店コードで検索

			dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

			if (dbDatas.size() == 0){
				// マスタに登録のない店舗
				msg.add(mu.getDbMessageObj("E11096", new String[]{}));
				return msg;
			}
		}
		return msg;
	}

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(User userInfo) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();

		// 排他チェック用
		String				targetTable	= null;
		String				targetWhere	= null;
		ArrayList<String>	targetParam	= new ArrayList<String>();
		JSONArray			msg			= new JSONArray();

		// パラメータ確認
		// 必須チェック
		// 対象件数チェック
		if (StringUtils.isEmpty(szCenterCd)	||
			StringUtils.isEmpty(szStd)		||
			StringUtils.isEmpty(szEdd)		||
			StringUtils.isEmpty(szSupplyNo)
		) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// コースマスタINSERT/UPDATE処理
		createSqlCm(userInfo);

		// 排他チェック実行
		targetTable = "(SELECT CENTERCD,SUPPLYNO,EFFECTIVESTARTDATE,LOGICALDELFLG,UPDATEDATE AS UPDDT FROM INAORR.ORRCOURSEMASTER)";

		// 同一センター・便で有効なレコードを検索
		targetWhere = "CENTERCD=? AND ";
		targetParam.add(szCenterCd);

		if (!StringUtils.isEmpty(szSupplyNo)) {
			targetWhere += "SUPPLYNO=? AND ";
			targetParam.add(szSupplyNo);
		}

		targetWhere += "EFFECTIVESTARTDATE=? AND ";
		targetParam.add(CmnDate.dbDateFormat(szStd));
		targetWhere += "LOGICALDELFLG=? ";
		targetParam.add(DefineReport.ValUpdkbn.NML.getVal());

		if(!super.checkExclusion(targetTable, targetWhere, targetParam, szUpdd)){
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
	 * 	コースマスタINSERT/UPDATE処理(DELETE→INSERT)
	 *
	 * @param userInfo
	 */
	public String createSqlCm(User userInfo){

		// ログインユーザー情報取得
		String userId = userInfo.getId(); // ログインユーザー

		// DB検索用パラメータ
		ArrayList<String>	paramData	= new ArrayList<String>();
		ArrayList<String>	prmData		= new ArrayList<String>();
		StringBuffer		sbSQL		= new StringBuffer();
		String				sqlWhere	= "";
		Object[]			valueData	= new Object[]{};
		String				values		= "";

		String	centerCd	= dataArrayNew.getJSONObject(0).optString("F1");	// センター
		String	supplyNo	= dataArrayNew.getJSONObject(0).optString("F2");	// 便
		String	std			= dataArrayNew.getJSONObject(0).optString("F3");	// 有効開始日
		String	edd			= dataArrayNew.getJSONObject(0).optString("F4");	// 有効終了日
		String	std_s		= dataArrayNew.getJSONObject(0).optString("F9");	// 変換前有効開始日


		// 有効開始日が同一のレコードはDELETE
		sqlWhere += "CENTERCD=? AND ";
		paramData.add(centerCd);

		sqlWhere += "SUPPLYNO=? AND ";
		paramData.add(supplyNo);

		sqlWhere += "EFFECTIVESTARTDATE=? ";
		paramData.add(std);

		sbSQL = new StringBuffer();
		sbSQL.append("DELETE FROM INAORR.ORRCOURSEMASTER ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("コースマスタ");

		// 同一センター、便で有効なレコードは終了に
		// クリア
		sqlWhere = "";
		paramData = new ArrayList<String>();

		String sqlSet = "EFFECTIVEENDDATE=?";
		paramData.add(CmnDate.dbDateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(std_s),-1)));

		sqlWhere += "CENTERCD=? AND ";
		paramData.add(centerCd);

		if (!StringUtils.isEmpty(supplyNo)) {
			sqlWhere += "SUPPLYNO=? AND ";
			paramData.add(supplyNo);
		}

		sqlWhere += "(EFFECTIVESTARTDATE <= ? AND EFFECTIVEENDDATE >= ?) ";
		paramData.add(edd);
		paramData.add(std);

		// コースマスタの論理削除
		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE INAORR.ORRCOURSEMASTER ");
		sbSQL.append("SET ");
		sbSQL.append(sqlSet);
		sbSQL.append(",UPDATEBY='" + userId + "'");
		sbSQL.append(",UPDATEDATE=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);


		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("コースマスタ");

		int maxField = 8;		// Fxxの最大値
		for (int i = 0; i < dataArrayNew.size(); i++) {
			JSONObject data = dataArrayNew.getJSONObject(i);

			if(data.isEmpty()){
				continue;
			}

			for (int k = 1; k <= maxField; k++) {
				String key = "F" + String.valueOf(k);

				if(k == 1){
					values += "?";
					prmData.add("0001");
				}

				if(! ArrayUtils.contains(new String[]{""}, key)){
					String val = data.optString(key);
					if(StringUtils.isEmpty(val)){
						values += ", null";
					}else{
						values += ", ?";
						prmData.add(val);
					}
				}

				if(k == maxField){
					values += ",'','','','','','','','','','', ?, ?, ?, ?, current timestamp, current timestamp, current timestamp";
					prmData.add(CmnDate.dbDateFormat("20501231"));
					prmData.add(DefineReport.ValUpdkbn.NML.getVal());
					prmData.add(userId);
					prmData.add(userId);

					valueData = ArrayUtils.add(valueData, "("+values+")");
					values = "";
				}
			}

			if(valueData.length >= 100 || (i+1 == dataArrayNew.size() && valueData.length > 0)){
				sbSQL = new StringBuffer();
				sbSQL.append("INSERT INTO INAORR.ORRCOURSEMASTER");
				sbSQL.append("(CORPORATIONCODE");
				sbSQL.append(",CENTERCD");
				sbSQL.append(",SUPPLYNO");
				sbSQL.append(",EFFECTIVESTARTDATE");
				sbSQL.append(",EFFECTIVEENDDATE");
				sbSQL.append(",HANDLEENDDATE");
				sbSQL.append(",COURSENO");
				sbSQL.append(",STORESEQ");
				sbSQL.append(",STORECD");
				sbSQL.append(",ATTRIBUTE01");
				sbSQL.append(",ATTRIBUTE02");
				sbSQL.append(",ATTRIBUTE03");
				sbSQL.append(",ATTRIBUTE04");
				sbSQL.append(",ATTRIBUTE05");
				sbSQL.append(",ATTRIBUTE06");
				sbSQL.append(",ATTRIBUTE07");
				sbSQL.append(",ATTRIBUTE08");
				sbSQL.append(",ATTRIBUTE09");
				sbSQL.append(",ATTRIBUTE10");
				sbSQL.append(",LOGICALDELDATE");
				sbSQL.append(",LOGICALDELFLG");
				sbSQL.append(",CREATEBY");
				sbSQL.append(",UPDATEBY");
				sbSQL.append(",CREATEDATE");
				sbSQL.append(",UPDATEDATE");
				sbSQL.append(",SYSTEMROWVERSION");
				sbSQL.append(")SELECT * FROM(values "+StringUtils.join(valueData, ",")+")");

				if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("コースマスタ");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		return sbSQL.toString();
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray checkDel(HashMap<String, String> map) {
		// パラメータ確認
		szCenterCd	= map.get("CENTERCD");								// センター
		szSupplyNo	= map.get("SUPPLYNO");								// 便
		szStd		= map.get("STD");									// 有効開始日
		szEdd		= map.get("EDD");									// 有効終了日
		szHandleEdd	= map.get("HANDLEEDD");								// 取扱終了日
		szUpdd		= map.get("UPDD");									// 更新日付（排他チェック用）
		String syoriDt	= map.get("SHORIDT");	// 処理日

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();
		JSONArray		msg		= new JSONArray();
		ItemList		iL		= new ItemList();
		MessageUtility	mu		= new MessageUtility();
		JSONArray		dbDatas = new JSONArray();

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		// チェック処理
		// 対象件数チェック
		if (StringUtils.isEmpty(szCenterCd)	||
			StringUtils.isEmpty(szStd)		||
			StringUtils.isEmpty(szEdd)		||
			StringUtils.isEmpty(szSupplyNo)
		) {
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 変数を初期化
		sbSQL	= new StringBuffer();
		iL		= new ItemList();
		dbDatas = new JSONArray();
		sqlWhere	= "";
		paramData	= new ArrayList<String>();

		sqlWhere += "CENTERCD=? AND ";
		paramData.add(szCenterCd);

		sqlWhere += "SUPPLYNO=? AND ";
		paramData.add(szSupplyNo);

		sqlWhere += "EFFECTIVESTARTDATE <= ? AND EFFECTIVEENDDATE >=? AND ";
		paramData.add(CmnDate.dbDateFormat(syoriDt));
		paramData.add(CmnDate.dbDateFormat(syoriDt));
		sqlWhere += "LOGICALDELFLG=? ";
		paramData.add(DefineReport.ValUpdkbn.NML.getVal());

		sbSQL.append("SELECT ");
		sbSQL.append("TO_CHAR(EFFECTIVESTARTDATE,'yyyymmdd') AS EFFECTIVESTARTDATE ");	// レコード件数
		sbSQL.append(",TO_CHAR(EFFECTIVEENDDATE,'yyyymmdd') AS EFFECTIVEENDDATE ");		// レコード件数
		sbSQL.append(",TO_CHAR(HANDLEENDDATE,'yyyymmdd') AS HANDLEENDDATE ");		// レコード件数
		sbSQL.append("FROM ");
		sbSQL.append("INAORR.ORRCOURSEMASTER ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() > 0){
			szStd		= dbDatas.getJSONObject(0).optString("EFFECTIVESTARTDATE");
			szEdd		= dbDatas.getJSONObject(0).optString("EFFECTIVEENDDATE");
			szHandleEdd	= dbDatas.getJSONObject(0).optString("HANDLEENDDATE");
		}

		// 排他チェック時に使用する更新日付を変更
		sbSQL	= new StringBuffer();
		iL		= new ItemList();
		dbDatas = new JSONArray();
		sqlWhere	= "";
		paramData	= new ArrayList<String>();

		sqlWhere += "CENTERCD=? AND ";
		paramData.add(szCenterCd);

		sqlWhere += "SUPPLYNO=? AND ";
		paramData.add(szSupplyNo);

		// 終了日にMAX値を代入
		if (szHandleEdd.equals("")) {
			szHandleEdd = "20501231";
		}

		sqlWhere += "EFFECTIVESTARTDATE=? AND ";
		paramData.add(CmnDate.dbDateFormat(szStd));
		sqlWhere += "EFFECTIVEENDDATE=? AND ";
		paramData.add(CmnDate.dbDateFormat(szEdd));
		sqlWhere += "HANDLEENDDATE=? AND ";
		paramData.add(CmnDate.dbDateFormat(szHandleEdd));

		sqlWhere += "LOGICALDELFLG=? ";
		paramData.add(DefineReport.ValUpdkbn.NML.getVal());

		sbSQL.append("SELECT ");
		sbSQL.append("CENTERCD ");	// レコード件数
		sbSQL.append("FROM ");
		sbSQL.append("INAORR.ORRCOURSEMASTER ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() == 0){
			// 削除対象のデータが存在しません
			msg.add(mu.getDbMessageObj("E11021", new String[]{}));
			return msg;		}
		return msg;
	}

	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject deleteData(User userInfo) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();

		// 排他チェック用
		String				targetTable	= null;
		String				targetWhere	= null;
		ArrayList<String>	targetParam	= new ArrayList<String>();
		JSONArray			msg			= new JSONArray();

		// パラメータ確認
		// 必須チェック
		// 対象件数チェック
		if (StringUtils.isEmpty(szCenterCd)	||
			StringUtils.isEmpty(szStd)		||
			StringUtils.isEmpty(szEdd)		||
			StringUtils.isEmpty(szHandleEdd)
			//StringUtils.isEmpty(szSupplyNo)
		) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// コースマスタINSERT/UPDATE処理
		createDelSqlCm(userInfo);

		// 排他チェック実行
		targetTable = "(SELECT CENTERCD,SUPPLYNO,EFFECTIVESTARTDATE,EFFECTIVEENDDATE,HANDLEENDDATE,LOGICALDELFLG,UPDATEDATE AS UPDDT FROM INAORR.ORRCOURSEMASTER)";

		// 同一センター・便で有効なレコードを検索
		targetWhere = "CENTERCD=? AND ";
		targetParam.add(szCenterCd);

		if (!StringUtils.isEmpty(szSupplyNo)) {
			targetWhere += "SUPPLYNO=? AND ";
			targetParam.add(szSupplyNo);
		}

		targetWhere += "EFFECTIVESTARTDATE=? AND ";
		targetParam.add(CmnDate.dbDateFormat(szStd));
		targetWhere += "EFFECTIVEENDDATE=? AND ";
		targetParam.add(CmnDate.dbDateFormat(szEdd));
		targetWhere += "HANDLEENDDATE=? AND ";
		targetParam.add(CmnDate.dbDateFormat(szHandleEdd));
		targetWhere += "LOGICALDELFLG=? ";
		targetParam.add(DefineReport.ValUpdkbn.NML.getVal());

		if(!super.checkExclusion(targetTable, targetWhere, targetParam, szUpdd)){
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
	 * コースマスタDELETE(倫理削除)処理
	 *
	 * @param userInfo
	 */
	public String createDelSqlCm(User userInfo){

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();

		// ログインユーザー情報取得
		String userId	= userInfo.getId(); // ログインユーザー

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		// 対象のデータを論理削除
		String sqlSet = "LOGICALDELFLG=?";
		paramData.add(DefineReport.ValUpdkbn.DEL.getVal());

		sqlWhere += "CENTERCD=? AND ";
		paramData.add(szCenterCd);

		if (!StringUtils.isEmpty(szSupplyNo)) {
			sqlWhere += "SUPPLYNO=? AND ";
			paramData.add(szSupplyNo);
		}

		sqlWhere += "EFFECTIVESTARTDATE=? AND ";
		paramData.add(CmnDate.dbDateFormat(szStd));
		sqlWhere += "EFFECTIVEENDDATE=? AND ";
		paramData.add(CmnDate.dbDateFormat(szEdd));
		sqlWhere += "HANDLEENDDATE=? AND ";
		paramData.add(CmnDate.dbDateFormat(szHandleEdd));

		sqlWhere += "LOGICALDELFLG=? ";
		paramData.add(DefineReport.ValUpdkbn.NML.getVal());

		// コースマスタの論理削除
		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE INAORR.ORRCOURSEMASTER ");
		sbSQL.append("SET ");
		sbSQL.append(sqlSet);
		sbSQL.append(",LOGICALDELDATE=current timestamp");
		sbSQL.append(",UPDATEBY='" + userId + "'");
		sbSQL.append(",UPDATEDATE=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("コースマスタ");

		// 履歴の修正
		if (!szEdd.equals("20501231")) {
			// クリア
			sqlWhere	= "";
			paramData	= new ArrayList<String>();

			sqlSet = "EFFECTIVEENDDATE=?";
			paramData.add(CmnDate.dbDateFormat(szEdd));

			sqlWhere += "CENTERCD=? AND ";
			paramData.add(szCenterCd);

			sqlWhere += "SUPPLYNO=? AND ";
			paramData.add(szSupplyNo);

			sqlWhere += "EFFECTIVEENDDATE=? AND ";
			paramData.add(CmnDate.dbDateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(szStd),-1)));

			sqlWhere += "LOGICALDELFLG=? AND ";
			paramData.add(DefineReport.ValUpdkbn.NML.getVal());

			sqlWhere += "EXISTS(SELECT EFFECTIVESTARTDATE FROM INAORR.ORRCOURSEMASTER ";
			sqlWhere += "WHERE CENTERCD=? AND SUPPLYNO=? AND EFFECTIVESTARTDATE=? AND LOGICALDELFLG=?)";
			paramData.add(szCenterCd);
			paramData.add(szSupplyNo);
			paramData.add(CmnDate.dbDateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(szEdd),1)));
			paramData.add(DefineReport.ValUpdkbn.NML.getVal());

			// コースマスタの論理削除
			sbSQL = new StringBuffer();
			sbSQL.append("UPDATE INAORR.ORRCOURSEMASTER ");
			sbSQL.append("SET ");
			sbSQL.append(sqlSet);
			sbSQL.append(",UPDATEBY='" + userId + "'");
			sbSQL.append(",UPDATEDATE=current timestamp ");
			sbSQL.append("WHERE ");
			sbSQL.append(sqlWhere);

			if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(paramData);
			lblList.add("コースマスタ");
		}

		return sbSQL.toString();
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		// 入力値の取得
		szCenterCd	= getMap().get("CENTERCD");		// センターコード
		szSupplyNo	= getMap().get("SUPPLYNO");		// 便
		szStd		= getMap().get("STANDARDDATE");	// 基準日

		// DB検索用パラメータ
		String sqlWhere = "";

		ArrayList<String> paramData = new ArrayList<String>();

		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlcommand = "";
		int maxStoreSeq = 0;
		int maxCourseNo = 0;
		int cCourseno	= 0;
		int cStoreSeq	= 0;

		// コース、配送順のMAX値を取得
		sqlcommand = "SELECT NMAN AS VALUE FROM INAMS.MSTMEISHO WHERE MEISHOKBN="+DefineReport.MeisyoSelect.KBN910007.getCd();

		JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
		if(array.size() > 0){
			maxStoreSeq = array.getJSONObject(0).optInt("VALUE");
		} else {
			maxStoreSeq = Integer.valueOf(DefineReport.SubGridRowNumber.DEF.getVal());
		}

		sqlcommand = "SELECT NMAN AS VALUE FROM INAMS.MSTMEISHO WHERE MEISHOKBN="+DefineReport.MeisyoSelect.KBN910008.getCd();

		array = new JSONArray();
		array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
		if(array.size() > 0){
			maxCourseNo = array.getJSONObject(0).optInt("VALUE");
		} else {
			maxCourseNo = Integer.valueOf(DefineReport.SubGridRowNumber.DEF.getVal());
		}

		// 検索条件に紐づく配送順序、店舗コードを取得
		if(StringUtils.isEmpty(szCenterCd)){
			sqlWhere += "CENTERCD=null AND ";
		}else{
			sqlWhere += "CENTERCD=? AND ";
			paramData.add(szCenterCd);
		}

		if(!StringUtils.isEmpty(szSupplyNo) && !szSupplyNo.equals("-1")){
			sqlWhere += "SUPPLYNO=? AND ";
			paramData.add(szSupplyNo);
		}

		if(StringUtils.isEmpty(szStd)){
			sqlWhere += "EFFECTIVESTARTDATE=null AND ";
		} else {
			sqlWhere += "(EFFECTIVESTARTDATE<=? AND EFFECTIVEENDDATE>=?) AND ";
			paramData.add(CmnDate.dbDateFormat(szStd));
			paramData.add(CmnDate.dbDateFormat(szStd));
		}

		sqlWhere += "COURSENO <= ? AND STORESEQ <= ? AND ";
		paramData.add(String.valueOf(maxCourseNo));
		paramData.add(String.valueOf(maxStoreSeq));

		sqlWhere += "LOGICALDELFLG=? ";
		paramData.add(DefineReport.ValUpdkbn.NML.getVal());

		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("SELECT ");
		sbSQL.append("COURSENO ");														// コース
		sbSQL.append(",STORESEQ ");														// 配送順序
		sbSQL.append(",STORECD ");														// 店舗コード
		sbSQL.append(",TO_CHAR(EFFECTIVESTARTDATE,'YY/MM/DD') AS EFFECTIVESTARTDATE ");	// 有効開始日
		sbSQL.append(",TO_CHAR(EFFECTIVEENDDATE,'YY/MM/DD') AS EFFECTIVEENDDATE ");		// 有効終了日
		sbSQL.append(",TO_CHAR(HANDLEENDDATE,'YY/MM/DD') AS HANDLEENDDATE ");			// 取扱終了日
		sbSQL.append(",UPDATEBY ");														// F4	: オペレーター
		sbSQL.append(",TO_CHAR(CREATEDATE,'YY/MM/DD') AS ADDDT ");						// F5	: 登録日
		sbSQL.append(",TO_CHAR(UPDATEDATE,'YY/MM/DD') AS UPDDT ");						// F6	: 更新日
		sbSQL.append(",TO_CHAR(UPDATEDATE,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT ");	// F7	: 更新日時
		sbSQL.append("FROM ");
		sbSQL.append("INAORR.ORRCOURSEMASTER ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);
		sbSQL.append("ORDER BY ");
		sbSQL.append("COURSENO ");
		sbSQL.append(",STORESEQ ");
		sbSQL.append(",STORECD ");

		array = new JSONArray();
		array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
		paramData = new ArrayList<String>();
		sbSQL = new StringBuffer();
		if(array.size() > 0){
			for (int i=0; i<array.size(); i++) {

				int courseNo	= array.getJSONObject(i).optInt("COURSENO");
				int storeSeq	= array.getJSONObject(i).optInt("STORESEQ");
				int storeCd		= array.getJSONObject(i).optInt("STORECD");

				for (int j=(cCourseno+1); j<=courseNo; j++) {

					// 配送順序のselect句が作成途中でないことを確認
					if (cStoreSeq != 0) {

						for (int k=(cStoreSeq+1); k<=maxStoreSeq; k++) {
							sbSQL.append(", null AS STORECD_" + k + " ");
						}

						cStoreSeq = 0;
						sbSQL.append("FROM SYSIBM.SYSDUMMY1 UNION ALL ");
					}

					sbSQL.append("SELECT ");
					sbSQL.append(" cast( ? as varchar ) AS F1,");
					paramData.add(szStd.substring(2, 4) +"/" + szStd.substring(4, 6) + "/" + szStd.substring(6, 8));

					if (StringUtils.isEmpty(array.getJSONObject(i).optString("EFFECTIVEENDDATE"))) {
						sbSQL.append("'50/12/31' AS F2,");
					} else {
						sbSQL.append("'" + array.getJSONObject(i).optString("EFFECTIVEENDDATE") + "' AS F2,");
					}

					sbSQL.append("'" + array.getJSONObject(i).optString("HANDLEENDDATE") + "' AS F3,");
					sbSQL.append("'" + array.getJSONObject(i).optString("UPDATEBY") + "' AS F4,");
					sbSQL.append("'" + array.getJSONObject(i).optString("ADDDT") + "' AS F5,");
					sbSQL.append("'" + array.getJSONObject(i).optString("UPDDT") + "' AS F6,");
					sbSQL.append("'" + array.getJSONObject(i).optString("HDN_UPDDT") + "' AS F7,");
					sbSQL.append(j + " AS COURSENO "); // F8

					if (j == courseNo) {

						for (int k=(cStoreSeq+1); k<=storeSeq; k++) {

							if (k == storeSeq) {

								sbSQL.append(", " + storeCd + " AS STORECD_" + k + " ");

								// 現在どこまで作成したか番号を保持
								cStoreSeq = storeSeq;
							} else {
								sbSQL.append(", null AS STORECD_" + k + " ");
							}
						}

						// MAX値まで到達したらリセット
						if (cStoreSeq == maxStoreSeq) {
							cStoreSeq = 0;
						}

						// 現在どこまで作成したか番号を保持
						cCourseno = courseNo;

					} else {

						// 配送順序全てnullのselect句を作成
						for (int k=1; k<=maxStoreSeq; k++) {
							sbSQL.append(", null AS STORECD_" + k + " ");
						}

						if (cCourseno == maxCourseNo) {
							sbSQL.append("FROM SYSIBM.SYSDUMMY1 ");
						} else {
							sbSQL.append("FROM SYSIBM.SYSDUMMY1 UNION ALL ");
						}
					}
				}

				// コース用の項目が全て作成し終わり配送順序用の項目がまだ未作成の場合
				if (cStoreSeq != maxStoreSeq && cStoreSeq != storeSeq) {

					// 配送順序用項目を全て作成し終わりコースも全て作成済みの場合
					if (cStoreSeq == 0 && cCourseno == maxCourseNo) {
						sbSQL.append("FROM SYSIBM.SYSDUMMY1 ");
					} else {

						for (int k=(cStoreSeq+1); k<=storeSeq; k++) {

							if (k == storeSeq) {

								sbSQL.append(", " + storeCd + " AS STORECD_" + k + " ");

								// 現在どこまで作成したか番号を保持
								cStoreSeq = storeSeq;
							} else {
								sbSQL.append(", null AS STORECD_" + k + " ");
							}
						}

						// MAX値まで到達したらリセット
						if (cStoreSeq == maxStoreSeq) {
							if (cCourseno == maxCourseNo) {
								sbSQL.append("FROM SYSIBM.SYSDUMMY1 ");
							} else {
								sbSQL.append("FROM SYSIBM.SYSDUMMY1 UNION ALL ");
							}
							cStoreSeq = 0;
						}
					}
				}
			}
		}

		// 未作成のレコードがある場合
		if (cCourseno != maxCourseNo) {

			for (int j=(cCourseno+1); j<=maxCourseNo; j++) {

				// 配送順序のselect句が作成途中でないことを確認
				if (cStoreSeq != 0) {

					for (int k=(cStoreSeq+1); k<=maxStoreSeq; k++) {
						sbSQL.append(", null AS STORECD_" + k + " ");
					}

					cStoreSeq = 0;
					sbSQL.append("FROM SYSIBM.SYSDUMMY1 UNION ALL ");
				}

				sbSQL.append("SELECT ");
				sbSQL.append(" cast( ? as varchar ) as F1,");
				paramData.add(szStd.substring(2, 4) +"/" + szStd.substring(4, 6) + "/" + szStd.substring(6, 8));
				sbSQL.append("null AS F2,");
				sbSQL.append("null AS F3,");
				sbSQL.append("null AS F4,");
				sbSQL.append("null AS F5,");
				sbSQL.append("null AS F6,");
				sbSQL.append("null AS F7,");
				sbSQL.append(j + " AS COURSENO ");
				for (int k=1; k<=maxStoreSeq; k++) {
					sbSQL.append(", null AS STORECD_" + k + " ");
				}

				if (j == maxCourseNo) {
					sbSQL.append("FROM SYSIBM.SYSDUMMY1 ");
				} else {
					sbSQL.append("FROM SYSIBM.SYSDUMMY1 UNION ALL ");
				}
			}
		}

		// 配送順序のselect句が作成途中でないことを確認
		if (cStoreSeq != 0) {

			for (int k=(cStoreSeq+1); k<=maxStoreSeq; k++) {
				sbSQL.append(", null AS STORECD_" + k + " ");
			}

			cStoreSeq = 0;
			sbSQL.append("FROM SYSIBM.SYSDUMMY1 ");
		}
		sbSQL.append(" ORDER BY COURSENO");

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		for (int i = 0; i < maxStoreSeq; i++) {
			titleList.add(String.valueOf(i+1));
		}

		setParamData(paramData);
		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

	}
}

