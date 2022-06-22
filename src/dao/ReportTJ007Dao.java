package dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTJ007Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportTJ007Dao(String JNDIname) {
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

	/**
	 * 他画面からの呼び出し検索実行
	 *
	 * @return
	 */
	public String createCommandSub(HashMap<String, String> map, User userInfo) {

		// ユーザー情報を設定
		super.setUserInfo(userInfo);

		// 検索条件などの情報を設定
		super.setMap(map);

		// 検索コマンド生成
		String command = createCommand();

		// 出力用検索条件生成
		outputQueryList();

		// 検索実行
		return command;
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		// メインデータ作成
		JSONArray wkArray = getTOKTJSHNDTWKData(getMap());	// 発注明細(ワーク)
		// ワークにデータが存在しない場合空を返却
		if (wkArray.size() == 0) {
			return "";
		}

		ReportTJ003Dao dao = new ReportTJ003Dao(JNDIname);
		dao.setUserInfo(getUserInfo());
		dao.setMap(getMap());

		JSONArray bmnysanArray		= dao.getBMNYSANData(getMap());		// 部門予算
		JSONArray cmprtArray		= dao.getCMPRTData(getMap());		// 構成比
		JSONArray avgptankaArray	= getAVGPTANKAData(getMap());		// 平均パック単価

		ArrayList<String>	paramData	= new ArrayList<String>();

		// 部門予算のデータを key:日付、value:部門予算 に変更
		HashMap<String,String> bmnysanMap = new HashMap<String,String>();
		for(int i =0 ;i<bmnysanArray.size();i++){
			String key = bmnysanArray.optJSONObject(i).optString("TJDT");
			String val = StringUtils.isEmpty(bmnysanArray.optJSONObject(i).optString("BMNYSANAM")) ? "0" : bmnysanArray.optJSONObject(i).optString("BMNYSANAM");
			bmnysanMap.put(key,val);
		}

		// 構成比のデータを key:大分類+-+日付、value:部門予算 に変更
		HashMap<String,String> cmprtMap = new HashMap<String,String>();
		for(int i =0 ;i<cmprtArray.size();i++){
			String key = cmprtArray.optJSONObject(i).optString("DAICD") + "-" + cmprtArray.optJSONObject(i).optString("TJDT");
			String val = StringUtils.isEmpty(cmprtArray.optJSONObject(i).optString("URICMPRT")) ? "0.0" : cmprtArray.optJSONObject(i).optString("URICMPRT");
			cmprtMap.put(key,val);
		}

		// 平均パック単価のデータを key:大分類、value:部門予算 に変更
		HashMap<String,String> avgptankaMap = new HashMap<String,String>();
		for(int i =0 ;i<avgptankaArray.size();i++){
			String key = avgptankaArray.optJSONObject(i).optString("DAICD");
			String val = StringUtils.isEmpty(avgptankaArray.optJSONObject(i).optString("AVGPTANKA")) ? "0.0" : avgptankaArray.optJSONObject(i).optString("AVGPTANKA");
			avgptankaMap.put(key,val);
		}

		// 全ての情報を集約
		String values = "(values";

		// 10日分の発注売価と売上予算を kye:日付、value:金額 で保持
		HashMap<String,BigDecimal> yoteisu	= new HashMap<String,BigDecimal>();
		HashMap<String,BigDecimal> baikakei	= new HashMap<String,BigDecimal>();
		HashMap<String,BigDecimal> genkakei	= new HashMap<String,BigDecimal>();
		BigDecimal hatsukei		= new BigDecimal("0");
		BigDecimal yoteisuhi	= new BigDecimal("0");
		String getNhKetaiKbn	= "";
		String getDaiCd			= "";
		String getDaiBruiKn		= "";

		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("select * from ");

		for (int i = 0; i < wkArray.size(); i++) {

			JSONObject	data = wkArray.getJSONObject(i);
			if (getNhKetaiKbn.equals("1") && data.optString("NHKETAIKBN").equals("1") || getNhKetaiKbn.equals("2") && data.optString("NHKETAIKBN").equals("2")) {
				getNhKetaiKbn = getNhKetaiKbn.equals("1") ? "2" : "1";
				values += getBlank(getDaiCd, getDaiBruiKn, getNhKetaiKbn, values) + ",";
			}

			getDaiCd		= data.optString("DAICD");
			getDaiBruiKn	= data.optString("DAIBRUIKN");
			getNhKetaiKbn	= data.optString("NHKETAIKBN");

			BigDecimal avegptanka	= avgptankaMap.containsKey(getDaiCd) ? BigDecimal.valueOf(NumberUtils.toLong(avgptankaMap.get(getDaiCd))) : BigDecimal.valueOf(NumberUtils.toLong("0.0"));

			for (int sortNum = 1; sortNum <= 5; sortNum++) {

				// ソート番号と大分類
				values += "("+getDaiCd+",";

				// センターパック
				if (getNhKetaiKbn.equals("1")) {
					if (sortNum == 1 || sortNum == 2) {
						values += "'"+getDaiBruiKn+"','0','',";
					} else if (sortNum == 3 || sortNum == 4 || sortNum == 5) {
						values += "'','"+getNhKetaiKbn+"','',";
					}

					// 項目名
					values += "'',";

				// 原料
				} else {
					if (sortNum == 1 || sortNum == 2 || sortNum == 3) {
						values += "'','"+getNhKetaiKbn+"','',";
					} else if (sortNum == 4 || sortNum == 5) {
						values += "'','3','',";
					}

					// 項目名
					values += "'',";
				}

				values += sortNum + ",";

				// 日付10日分作成
				BigDecimal kikanKei = new BigDecimal("0");
				for (int jtDt = 1; jtDt <= 10; jtDt++) {
					String getJtDt	= jtDt == 10 ? data.optString("JTDT_10") : data.optString("JTDT_0" + String.valueOf(jtDt));

					// センターパック
					if (getNhKetaiKbn.equals("1")) {
						// 売上予算作成
						if (sortNum == 1) {
							BigDecimal bmnyosan = bmnysanMap.containsKey(getJtDt) ? new BigDecimal(bmnysanMap.get(getJtDt)) : new BigDecimal("0.0");
							BigDecimal uricmprt = cmprtMap.containsKey(getDaiCd + "-" +getJtDt) ? new BigDecimal(cmprtMap.get(getDaiCd + "-" +getJtDt)) : new BigDecimal("0.0");

							// 小数点第一位四捨五入
							values += "'"+String.valueOf(bmnyosan.multiply(uricmprt).divide(new BigDecimal("100"),0,BigDecimal.ROUND_HALF_UP))+"',";

							yoteisu.put(getJtDt, bmnyosan.multiply(uricmprt).divide(new BigDecimal("100"),0,BigDecimal.ROUND_HALF_UP));

							// 期間計
							kikanKei = kikanKei.add(bmnyosan.multiply(uricmprt).divide(new BigDecimal("100"),0,BigDecimal.ROUND_HALF_UP));

						// 予定数量
						} else if (sortNum == 2) {

							BigDecimal getValA = new BigDecimal("0");

							if (yoteisu.containsKey(getJtDt)) {
								getValA = yoteisu.get(getJtDt);
							}

							// 小数点第二位で四捨五入
							if (String.valueOf(avegptanka).equals("0")) {
								values += "'"+String.valueOf(avegptanka)+"',";
								// 期間計
								kikanKei = kikanKei.add(avegptanka);
							} else {
								values += "'"+String.valueOf(getValA.divide(avegptanka,5,BigDecimal.ROUND_DOWN).multiply(new BigDecimal("1000")).setScale(1, BigDecimal.ROUND_HALF_UP))+"',";
								// 期間計
								kikanKei = kikanKei.add(getValA.divide(avegptanka,5,BigDecimal.ROUND_DOWN).multiply(new BigDecimal("1000")).setScale(1, BigDecimal.ROUND_HALF_UP));
							}

						// 発注数量
						} else if (sortNum == 3) {
							BigDecimal val = jtDt == 10 ? new BigDecimal(data.optString("HTSU_10")) : new BigDecimal(data.optString("HTSU_0" + String.valueOf(jtDt)));
							values += "'"+String.valueOf(val.setScale(0,BigDecimal.ROUND_HALF_UP))+"',";

							// 期間計
							kikanKei = kikanKei.add(val.setScale(0,BigDecimal.ROUND_HALF_UP));

						// 発注売価
						} else if (sortNum == 4) {
							BigDecimal val = jtDt == 10 ? new BigDecimal(data.optString("NNBAIKA_10")) : new BigDecimal(data.optString("NNBAIKA_0" + String.valueOf(jtDt)));
							values += "'"+String.valueOf(val.setScale(0,BigDecimal.ROUND_HALF_UP))+"',";

							// 期間計、売価計
							kikanKei = kikanKei.add(val.setScale(0,BigDecimal.ROUND_HALF_UP));
							baikakei.put(getJtDt, val.setScale(0,BigDecimal.ROUND_HALF_UP));

						// 発注原価
						} else if (sortNum == 5) {

							BigDecimal val = jtDt == 10 ? new BigDecimal(data.optString("GENKAAM_MAE_10")) : new BigDecimal(data.optString("GENKAAM_MAE_0" + String.valueOf(jtDt)));
							values += "'"+String.valueOf(val.setScale(0,BigDecimal.ROUND_HALF_UP))+"',";

							// 期間計、原価計
							kikanKei = kikanKei.add(val.setScale(0,BigDecimal.ROUND_HALF_UP));
							genkakei.put(getJtDt, val.setScale(0,BigDecimal.ROUND_HALF_UP));
						}
					} else {
						// 発注数量
						if (sortNum == 1) {
							BigDecimal val = jtDt == 10 ? new BigDecimal(data.optString("HTSU_10")) : new BigDecimal(data.optString("HTSU_0" + String.valueOf(jtDt)));
							values += "'"+String.valueOf(val.setScale(1,BigDecimal.ROUND_HALF_UP))+"',";

							// 期間計
							kikanKei = kikanKei.add(val.setScale(1,BigDecimal.ROUND_HALF_UP));

						// 発注売価
						} else if (sortNum == 2) {
							BigDecimal val = jtDt == 10 ? new BigDecimal(data.optString("NNBAIKA_10")) : new BigDecimal(data.optString("NNBAIKA_0" + String.valueOf(jtDt)));
							values += "'"+String.valueOf(val.setScale(0,BigDecimal.ROUND_HALF_UP))+"',";

							// 期間計、売価計
							kikanKei = kikanKei.add(val.setScale(0,BigDecimal.ROUND_HALF_UP));
							if (baikakei.containsKey(getJtDt)) {
								BigDecimal addVal = baikakei.get(getJtDt).add(val.setScale(0,BigDecimal.ROUND_HALF_UP));
								baikakei.replace(getJtDt, addVal);
							} else {
								baikakei.put(getJtDt, val.setScale(0,BigDecimal.ROUND_HALF_UP));
							}

						// 発注原価
						} else if (sortNum == 3) {
							BigDecimal val = jtDt == 10 ? new BigDecimal(data.optString("GENKAAM_MAE_10")) : new BigDecimal(data.optString("GENKAAM_MAE_0" + String.valueOf(jtDt)));
							values += "'"+String.valueOf(val.setScale(0,BigDecimal.ROUND_HALF_UP))+"',";

							// 期間計、原価計
							kikanKei = kikanKei.add(val.setScale(0,BigDecimal.ROUND_HALF_UP));
							if (genkakei.containsKey(getJtDt)) {
								BigDecimal addVal = genkakei.get(getJtDt).add(val.setScale(0,BigDecimal.ROUND_HALF_UP));
								genkakei.replace(getJtDt, addVal);
							} else {
								genkakei.put(getJtDt, val.setScale(0,BigDecimal.ROUND_HALF_UP));
							}

						// 売価計
						} else if (sortNum == 4) {

							if (baikakei.containsKey(getJtDt)) {
								values += "'"+String.valueOf(baikakei.get(getJtDt).setScale(0,BigDecimal.ROUND_HALF_UP))+"',";

								// 期間計
								kikanKei = kikanKei.add(baikakei.get(getJtDt).setScale(0,BigDecimal.ROUND_HALF_UP));
							} else {
								values += "'0',";

								// 期間計
								kikanKei = kikanKei.add(new BigDecimal("0"));
							}

						// 原価計
						} else if (sortNum == 5) {

							if (genkakei.containsKey(getJtDt)) {
								values += "'"+String.valueOf(genkakei.get(getJtDt).setScale(0,BigDecimal.ROUND_HALF_UP))+"',";

								// 期間計
								kikanKei = kikanKei.add(genkakei.get(getJtDt).setScale(0,BigDecimal.ROUND_HALF_UP));
							} else {
								values += "'0',";

								// 期間計
								kikanKei = kikanKei.add(new BigDecimal("0"));
							}
						}

					}
				}

				// センターパック
				if (getNhKetaiKbn.equals("1")) {

					// 発注数計
					if (sortNum == 3) {
						hatsukei = hatsukei.add(kikanKei);

					// 予定数比
					} else if (sortNum == 2) {
						yoteisuhi = yoteisuhi.add(kikanKei);
					}
					values += "'"+String.valueOf(kikanKei)+"','')";

				// 原料
				} else {
					values += "'"+String.valueOf(kikanKei)+"'";

					// 発注数計
					if (sortNum == 2) {
						hatsukei = hatsukei.add(kikanKei);
					}

					// 発注数計、予定数比特殊処理
					if (sortNum == 4) {

						if (avegptanka.intValue() == 0) {
							hatsukei = hatsukei.add(new BigDecimal("0"));
						} else {
							hatsukei = hatsukei.divide(avegptanka,5,BigDecimal.ROUND_DOWN).multiply(new BigDecimal("1000").setScale(0, BigDecimal.ROUND_HALF_UP));
						}
						values += ",'"+String.valueOf(hatsukei)+"')";

					} else if (sortNum == 5) {
						if (yoteisuhi.intValue() == 0) {
							yoteisuhi = yoteisuhi.add(new BigDecimal("0"));
						} else {
							yoteisuhi = hatsukei.divide(yoteisuhi,5,BigDecimal.ROUND_DOWN).multiply(new BigDecimal("100").setScale(1, BigDecimal.ROUND_HALF_UP));
						}
						values += ",'"+String.valueOf(yoteisuhi)+"')";
						hatsukei = new BigDecimal("0");
					} else {
						values += ",'')";
					}
				}

				// 一番最後のレコード以外カンマをつける
				if (sortNum == 5 && i+1 == wkArray.size()) {
					if (getNhKetaiKbn.equals("1")) {
						values += "," + getBlank(getDaiCd, getDaiBruiKn, "2", "");
					}
					values += ")";
				} else {
					values += ",";
				}
				sbSQL.append(values);
				values = "";
			}

			// センターパック
			if (getNhKetaiKbn.equals("1")) {
				yoteisu = new HashMap<String,BigDecimal>();

			// 原料
			} else {
				baikakei = new HashMap<String,BigDecimal>();
				genkakei = new HashMap<String,BigDecimal>();
			}
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		sbSQL.append(" as (F1 ,F2 ,F3 ,F4 ,F5 ,F6 ,F7 ,F8 ,F9 ,F10 ,F11 ,F12 ,F13 ,F14 ,F15, F16 ,F17, F18) ");
		sbSQL.append("order by F1,F3,F6 ");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	public String getBlank(String getDaiCd, String daibruikn, String getNhKetaiKbn, String values) {

		BigDecimal hatsukei		= new BigDecimal("0");
		BigDecimal yoteisuhi	= new BigDecimal("0");

		for (int sortNum = 1; sortNum <= 5; sortNum++) {

			// ソート番号と大分類
			values += "("+getDaiCd+",";

			// センターパック
			if (getNhKetaiKbn.equals("1")) {
				if (sortNum == 1 || sortNum == 2) {
					values += "'"+daibruikn+"','0','',";
				} else if (sortNum == 3 || sortNum == 4 || sortNum == 5) {
					values += "'','"+getNhKetaiKbn+"','',";
				}

				// 項目名
				values += "'',";

			// 原料
			} else {
				if (sortNum == 1 || sortNum == 2 || sortNum == 3) {
					values += "'','"+getNhKetaiKbn+"','',";
				} else if (sortNum == 4 || sortNum == 5) {
					values += "'','3','',";
				}

				// 項目名
				values += "'',";
			}

			values += sortNum + ",";

			// 日付10日分作成
			BigDecimal kikanKei = new BigDecimal("0");
			for (int jtDt = 1; jtDt <= 10; jtDt++) {

				// センターパック
				if (getNhKetaiKbn.equals("1")) {
					// 売上予算作成
					if (sortNum == 1) {
						// 小数点第一位四捨五入
						values += "'0',";

					// 予定数量
					} else if (sortNum == 2) {
						values += "'0.0',";
						kikanKei = new BigDecimal("0.0");

					// 発注数量
					} else if (sortNum == 3) {
						values += "'0',";

					// 発注売価
					} else if (sortNum == 4) {
						values += "'0',";

					// 発注原価
					} else if (sortNum == 5) {
						values += "'0',";
					}
				} else {
					// 発注数量
					if (sortNum == 1) {
						values += "'0.0',";
						kikanKei = new BigDecimal("0.0");

					// 発注売価
					} else if (sortNum == 2) {
						values += "'0',";

					// 発注原価
					} else if (sortNum == 3) {
						values += "'0',";

					// 売価計
					} else if (sortNum == 4) {
						values += "'0',";

					// 原価計
					} else if (sortNum == 5) {
						values += "'0',";
					}

				}
			}

			// センターパック
			if (getNhKetaiKbn.equals("1")) {
				values += "'"+kikanKei+"',''),";

			// 原料
			} else {
				values += "'"+kikanKei+"'";

				// 発注数計、予定数比特殊処理
				if (sortNum == 4) {
					values += ",'"+hatsukei+"')";

				} else if (sortNum == 5) {
					values += ",'"+yoteisuhi+"')";
				} else {
					values += ",'')";
				}
			}
			if (getNhKetaiKbn.equals("2") && sortNum != 5) {
				values += ",";
			}
		}

		return values;
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

	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getAVGPTANKAData(HashMap<String, String> map) {

		ArrayList<String> paramData = new ArrayList<String>();

		User userInfo 	= getUserInfo();
		String tenpo 	= userInfo.getTenpo();
		String szBmncd	= getMap().get("BMNCD");				// 部門コード
		String szLstno	= getMap().get("LSTNO");				// リスト№
		String listno	= szLstno.replace("-", "");
		String bmncd 	= szBmncd.substring(0,2);

		paramData.add(listno);
		paramData.add(tenpo);
		paramData.add(bmncd);

		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("  select T1.DAICD");
		sbSQL.append(" ,SUM(T1.AVGPTANKA) AS AVGPTANKA ");
		sbSQL.append(" from");
		sbSQL.append(" INATK.TOKTJ_AVGPTANKA T1");
		sbSQL.append(" where");
		sbSQL.append("  T1.LSTNO = ?");
		sbSQL.append("  and T1.TENCD = ?");
		sbSQL.append("  and T1.BMNCD = ?");
		sbSQL.append("  GROUP BY T1.DAICD");

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
	}


	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getTOKTJSHNDTWKData(HashMap<String, String> map) {

		ArrayList<String> paramData = new ArrayList<String>();

		User userInfo 	= getUserInfo();
		String tenpo 	= userInfo.getTenpo();
		String szBmncd			= getMap().get("BMNCD");		// 部門コード
		String szLstno			= getMap().get("LSTNO");		// リスト№
		String szOutRowIndez	= getMap().get("OUTROWINDEX");	// 出力対象行
		int start	= Integer.valueOf(szOutRowIndez) - 4;
		int end		= Integer.valueOf(szOutRowIndez);
		JSONArray arr	= JSONArray.fromObject(getMap().get("INPDAYARR"));
		String bmncd 	= szBmncd.substring(0,2);
		String listno	= szLstno.replace("-", "");

		paramData.add(bmncd);
		paramData.add(String.valueOf(start));
		paramData.add(String.valueOf(end));
		paramData.add(listno);
		paramData.add(tenpo);

		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" WITH WK AS(SELECT T1.LSTNO");
		sbSQL.append(" , T1.TENCD");
		sbSQL.append(" , T1.BMNCD");
		sbSQL.append(" , T1.DAICD");
		sbSQL.append(" , CASE WHEN T1.NHKETAIKBN <> '2' THEN '1' ELSE T1.NHKETAIKBN END AS NHKETAIKBN");
		sbSQL.append(" , T1.IRISU_TB");
		sbSQL.append(" , NVL(T1.JRYO,0) JRYO ");
		sbSQL.append(" , CASE WHEN T2.BAIKAAM_PACK IS NOT NULL ");
		sbSQL.append(" AND T2.BAIKAAM_PACK <> 0 THEN T2.BAIKAAM_PACK ");
		sbSQL.append(" ELSE T1.BAIKAAM_TB END AS BAIKAAM_TB");
		sbSQL.append(" , CASE WHEN T2.GENKAAM_PACK IS NOT NULL ");
		sbSQL.append(" AND T2.GENKAAM_PACK <> 0 THEN T2.GENKAAM_PACK ");
		sbSQL.append(" ELSE T1.GENKAAM_MAE END AS GENKAAM_MAE");
		sbSQL.append(" ,CASE WHEN T1.HTSU_01=99999 THEN 0 ELSE T1.HTSU_01 END AS HTSU_01 ");
		sbSQL.append(" ,CASE WHEN T1.HTSU_02=99999 THEN 0 ELSE T1.HTSU_02 END AS HTSU_02 ");
		sbSQL.append(" ,CASE WHEN T1.HTSU_03=99999 THEN 0 ELSE T1.HTSU_03 END AS HTSU_03 ");
		sbSQL.append(" ,CASE WHEN T1.HTSU_04=99999 THEN 0 ELSE T1.HTSU_04 END AS HTSU_04 ");
		sbSQL.append(" ,CASE WHEN T1.HTSU_05=99999 THEN 0 ELSE T1.HTSU_05 END AS HTSU_05 ");
		sbSQL.append(" ,CASE WHEN T1.HTSU_06=99999 THEN 0 ELSE T1.HTSU_06 END AS HTSU_06 ");
		sbSQL.append(" ,CASE WHEN T1.HTSU_07=99999 THEN 0 ELSE T1.HTSU_07 END AS HTSU_07 ");
		sbSQL.append(" ,CASE WHEN T1.HTSU_08=99999 THEN 0 ELSE T1.HTSU_08 END AS HTSU_08 ");
		sbSQL.append(" ,CASE WHEN T1.HTSU_09=99999 THEN 0 ELSE T1.HTSU_09 END AS HTSU_09 ");
		sbSQL.append(" ,CASE WHEN T1.HTSU_10=99999 THEN 0 ELSE T1.HTSU_10 END AS HTSU_10 ");
		// 日付は固定値
		for (int i = 0; i < arr.size(); i++) {
			String col = (i+1) == 10 ? "JTDT_10" : "JTDT_0" + String.valueOf(i+1);
			sbSQL.append(" , '"+ arr.getString(i).split("-")[0] +"' AS "+col);
		}
		if (arr.size()==0) {
			sbSQL.append(" , T2.JTDT_01");
			sbSQL.append(" , T2.JTDT_02");
			sbSQL.append(" , T2.JTDT_03");
			sbSQL.append(" , T2.JTDT_04");
			sbSQL.append(" , T2.JTDT_05");
			sbSQL.append(" , T2.JTDT_06");
			sbSQL.append(" , T2.JTDT_07");
			sbSQL.append(" , T2.JTDT_08");
			sbSQL.append(" , T2.JTDT_09");
			sbSQL.append(" , T2.JTDT_10");
		} else if (arr.size() != 10) {
			for (int i = arr.size()+1; i <= 10; i++) {
				String col = i == 10 ? "JTDT_10" : "JTDT_0"+(i+1);
				sbSQL.append(" , null AS "+col);
			}
		}
		sbSQL.append(" FROM");
		sbSQL.append(" INATK.TOKTJ_SHNDT_WK T1 ");
		sbSQL.append(" LEFT JOIN INATK.TOKTJ_TEN T2 ");
		sbSQL.append(" ON T1.LSTNO=T2.LSTNO AND T1.TENCD=T2.TENCD AND T1.BMNCD=T2.BMNCD AND T1.HYOSEQNO=T2.HYOSEQNO), ");
		sbSQL.append(" DAI AS (SELECT * FROM ");
		sbSQL.append("(SELECT BMNCD,DAICD,DAIBRUIKN,ROWNUMBER() OVER (ORDER BY DAICD) AS ROWNUM FROM INAMS.MSTDAIBRUI WHERE BMNCD=? AND UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + ")");
		sbSQL.append(" WHERE ROWNUM BETWEEN ? AND ?)");
		sbSQL.append(" SELECT T0.DAICD,NVL(T1.NHKETAIKBN,'1') AS NHKETAIKBN ");
		sbSQL.append(" ,T0.DAIBRUIKN");
		sbSQL.append(" ,T1.LSTNO");
		sbSQL.append(" ,T1.TENCD");
		sbSQL.append(" ,T1.BMNCD");
		sbSQL.append(" ,T1.JTDT_01");
		sbSQL.append(" ,T1.JTDT_02");
		sbSQL.append(" ,T1.JTDT_03");
		sbSQL.append(" ,T1.JTDT_04");
		sbSQL.append(" ,T1.JTDT_05");
		sbSQL.append(" ,T1.JTDT_06");
		sbSQL.append(" ,T1.JTDT_07");
		sbSQL.append(" ,T1.JTDT_08");
		sbSQL.append(" ,T1.JTDT_09");
		sbSQL.append(" ,T1.JTDT_10");
		sbSQL.append(" ,NVL(SUM(T1.HTSU_01*T1.IRISU_TB* CASE WHEN T1.NHKETAIKBN = '2' THEN T1.JRYO ELSE 1 END),'0') AS HTSU_01");
		sbSQL.append(" ,NVL(SUM(T1.HTSU_02*T1.IRISU_TB* CASE WHEN T1.NHKETAIKBN = '2' THEN T1.JRYO ELSE 1 END),'0') AS HTSU_02");
		sbSQL.append(" ,NVL(SUM(T1.HTSU_03*T1.IRISU_TB* CASE WHEN T1.NHKETAIKBN = '2' THEN T1.JRYO ELSE 1 END),'0') AS HTSU_03");
		sbSQL.append(" ,NVL(SUM(T1.HTSU_04*T1.IRISU_TB* CASE WHEN T1.NHKETAIKBN = '2' THEN T1.JRYO ELSE 1 END),'0') AS HTSU_04");
		sbSQL.append(" ,NVL(SUM(T1.HTSU_05*T1.IRISU_TB* CASE WHEN T1.NHKETAIKBN = '2' THEN T1.JRYO ELSE 1 END),'0') AS HTSU_05");
		sbSQL.append(" ,NVL(SUM(T1.HTSU_06*T1.IRISU_TB* CASE WHEN T1.NHKETAIKBN = '2' THEN T1.JRYO ELSE 1 END),'0') AS HTSU_06");
		sbSQL.append(" ,NVL(SUM(T1.HTSU_07*T1.IRISU_TB* CASE WHEN T1.NHKETAIKBN = '2' THEN T1.JRYO ELSE 1 END),'0') AS HTSU_07");
		sbSQL.append(" ,NVL(SUM(T1.HTSU_08*T1.IRISU_TB* CASE WHEN T1.NHKETAIKBN = '2' THEN T1.JRYO ELSE 1 END),'0') AS HTSU_08");
		sbSQL.append(" ,NVL(SUM(T1.HTSU_09*T1.IRISU_TB* CASE WHEN T1.NHKETAIKBN = '2' THEN T1.JRYO ELSE 1 END),'0') AS HTSU_09");
		sbSQL.append(" ,NVL(SUM(T1.HTSU_10*T1.IRISU_TB* CASE WHEN T1.NHKETAIKBN = '2' THEN T1.JRYO ELSE 1 END),'0') AS HTSU_10");
		sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_01*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_01");
		sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_02*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_02");
		sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_03*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_03");
		sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_04*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_04");
		sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_05*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_05");
		sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_06*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_06");
		sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_07*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_07");
		sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_08*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_08");
		sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_09*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_09");
		sbSQL.append(" ,NVL(SUM((T1.BAIKAAM_TB*T1.HTSU_10*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS NNBAIKA_10");
		sbSQL.append(" ,NVL(SUM((T1.GENKAAM_MAE*T1.HTSU_01*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS GENKAAM_MAE_01");
		sbSQL.append(" ,NVL(SUM((T1.GENKAAM_MAE*T1.HTSU_02*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS GENKAAM_MAE_02");
		sbSQL.append(" ,NVL(SUM((T1.GENKAAM_MAE*T1.HTSU_03*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS GENKAAM_MAE_03");
		sbSQL.append(" ,NVL(SUM((T1.GENKAAM_MAE*T1.HTSU_04*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS GENKAAM_MAE_04");
		sbSQL.append(" ,NVL(SUM((T1.GENKAAM_MAE*T1.HTSU_05*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS GENKAAM_MAE_05");
		sbSQL.append(" ,NVL(SUM((T1.GENKAAM_MAE*T1.HTSU_06*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS GENKAAM_MAE_06");
		sbSQL.append(" ,NVL(SUM((T1.GENKAAM_MAE*T1.HTSU_07*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS GENKAAM_MAE_07");
		sbSQL.append(" ,NVL(SUM((T1.GENKAAM_MAE*T1.HTSU_08*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS GENKAAM_MAE_08");
		sbSQL.append(" ,NVL(SUM((T1.GENKAAM_MAE*T1.HTSU_09*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS GENKAAM_MAE_09");
		sbSQL.append(" ,NVL(SUM((T1.GENKAAM_MAE*T1.HTSU_10*T1.IRISU_TB))/CAST(1000 as float),CAST(0 as float)) AS GENKAAM_MAE_10");
		sbSQL.append(" FROM");
		sbSQL.append(" DAI T0 LEFT JOIN WK T1 ");
		sbSQL.append(" ON T0.BMNCD=T1.BMNCD and T0.DAICD=T1.DAICD AND T1.LSTNO = ? AND T1.TENCD = ? ");
		sbSQL.append(" AND T0.DAICD IS NOT NULL ");
		sbSQL.append(" GROUP BY T0.DAICD,T1.NHKETAIKBN");
		sbSQL.append(" ,T0.DAIBRUIKN");
		sbSQL.append(" ,T1.LSTNO");
		sbSQL.append(" ,T1.TENCD");
		sbSQL.append(" ,T1.BMNCD");
		sbSQL.append(" ,T1.JTDT_01");
		sbSQL.append(" ,T1.JTDT_02");
		sbSQL.append(" ,T1.JTDT_03");
		sbSQL.append(" ,T1.JTDT_04");
		sbSQL.append(" ,T1.JTDT_05");
		sbSQL.append(" ,T1.JTDT_06");
		sbSQL.append(" ,T1.JTDT_07");
		sbSQL.append(" ,T1.JTDT_08");
		sbSQL.append(" ,T1.JTDT_09");
		sbSQL.append(" ,T1.JTDT_10");
		sbSQL.append(" ORDER BY T0.DAICD,T1.NHKETAIKBN");

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
	}
}
