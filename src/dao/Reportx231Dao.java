package dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx231Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx231Dao(String JNDIname) {
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

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}
		String szHtdt			= getMap().get("HTDT");		// 商品コード
		String szErrordiv		= getMap().get("ERRORDIV");		// エラー区分
		String szTenkn			= getMap().get("TENKN");		// 店コード
		String szSsrccd			= getMap().get("SRCCD");		//取引先コード
		String szCentercd		= getMap().get("CENTERCD");		// センターコード
		String szShnkn			= getMap().get("SHNKN");		// 商品区分
		JSONArray bumonArray	= JSONArray.fromObject(getMap().get("BUMON"));		// 部門
		JSONArray daiBunArray	= JSONArray.fromObject(getMap().get("DAI_BUN"));		// 部門
		JSONArray chuBunArray	= JSONArray.fromObject(getMap().get("CHU_BUN"));		// 部門
		String szSelBumon		= StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(bumonArray.join(","),"\"0","\""),"\"",""),",");
		String szSelDaiBun		= StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(daiBunArray.join(","),"\"0","\""),"\"",""),",");
		String szSelChuBun		= StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(chuBunArray.join(","),"\"0","\""),"\"",""),",");		// 中分類
		String btnId 			= getMap().get("BTN");			// 実行ボタン
		// パラメータ確認
		// 必須チェック
		if ((btnId == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();

		// 一覧表情報
		// TODO:天気・気温情報今適当
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(" select ");
		sbSQL.append(" SUBSTRING(right ('0000000' || RTRIM(char (T1.PRODUCTSCD)), 8), 1, 4) || SUBSTRING(right ('0000000' || RTRIM(char (T1.PRODUCTSCD)), 8), 5, 4) ");				// 取引先コード
		sbSQL.append(" ,T1.PRODUCTSKANJI ");																// 商品名
		sbSQL.append(" ,T2.NMKN ");																			// 商品区分
		sbSQL.append(" ,FLOOR(T1.ORDERUNITQTY) ");															// 入数
		sbSQL.append(" ,FLOOR(T1.QTYPERUNIT) ");															// 発注数
		sbSQL.append(" ,FLOOR(T1.QTYPERUNIT) * FLOOR(T1.ORDERUNITQTY) ");									// 発注バラ数
		sbSQL.append(" ,TO_CHAR(TO_DATE(T1.DELIVERYDATE, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.DELIVERYDATE, 'YYYYMMDD'))) ");				// 納品日
		sbSQL.append(" ,case when  ERRORDIV = 0 then ' ' when ERRORDIV <> 0 then ERRORDIV END ");			// エラー区分
		sbSQL.append(" ,case when  ERRORDIV = 0 then ' ' when ERRORDIV <> 0 then ERRORMESSAGE END ");		// エラーメッセージ
		sbSQL.append(" ,right ('000' || RTRIM(CHAR (T1.CENTERCD)), 3) ");																		// センターコード
		sbSQL.append(" ,right ('000000' || RTRIM(CHAR (T1.SUPPLIERCD)), 6) ");								// 取引先コード
		sbSQL.append(" from INAAD.HATKEKKA T1");
		sbSQL.append(" left join INAMS.MSTMEISHO T2");
		sbSQL.append(" on T2.MEISHOCD = T1.PRODUCTDIV");
		sbSQL.append(" and T2.MEISHOKBN = 910010");
		sbSQL.append(" where " );
		sbSQL.append(" cast(T1.ORDERSTORECD as INTEGER ) = ? " );	// 発注企業店舗コード
		paramData.add(String.format("%3s", szTenkn).replace(" ", "0"));
		if(!StringUtils.isEmpty(szHtdt)){
			sbSQL.append(" and  T1.ORDERDATE = ? " );			// 発注年月日
			paramData.add(szHtdt);
		}

		if(!szErrordiv.equals("-1")){
			if(szErrordiv.equals("1")){
				sbSQL.append(" and  T1.ERRORDIV <> 0 " );		// エラー区分
			}else{
				sbSQL.append(" and  T1.ERRORDIV = 0 " );		// エラー区分
			}
		}
		if( !szShnkn.equals("-1")){
			sbSQL.append(" and  T1.PRODUCTDIV = ? " );	// 商品区分
			paramData.add(szShnkn);
		}
		if(!StringUtils.isEmpty(szCentercd)){
			sbSQL.append(" and  T1.CENTERCD = ? " );		// センターコード
			paramData.add(szCentercd);
		}
		if(!StringUtils.isEmpty(szSsrccd)){
			sbSQL.append(" and  T1.SUPPLIERCD = ? " );		// 取引先コード
			paramData.add(szSsrccd);
		}
		if( !szSelBumon.equals("-1")){
			sbSQL.append(" and  T1.CATEGORY1CD = ? " );	// 部門
			paramData.add(szSelBumon);
		}
		if( !szSelDaiBun.equals("-1")){
			sbSQL.append(" and  T1.CATEGORY2CD = ? " );	// 部門
			String daiBun = String.format("%4s", szSelDaiBun).replace(" ", "0");
			paramData.add(daiBun);
		}
		if( !szSelChuBun.equals("-1")){
			sbSQL.append(" and  T1.CATEGORY3CD = ? " );	// 部門
			String chuBun = String.format("%6s", szSelChuBun).replace(" ", "0");
			paramData.add(chuBun);
		}

		sbSQL.append(" order by ");
		sbSQL.append("  T1.CENTERCD,T1.SUPPLIERCD,T1.PRODUCTDIV,T1.ERRORDIV ");

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
		LocalDateTime d = LocalDateTime.now();
		DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		String s = df1.format(d); //format(d)のdは、LocalDateTime dのd
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(s); // 出力結果：2018/02/11 13:02:49　日
		// タイトル名称
		cells.add("発注結果");// jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
		cells.add("");
		cells.add("");
		cells.add("");
		cells.add("");
		cells.add("");
		cells.add("");
		cells.add("");
		cells.add("出力日時 :"+s);
		getWhere().add(cells);

		// 空白行
		cells = new ArrayList<String>();
		cells.add("");
		getWhere().add(cells);

		cells = new ArrayList<String>();
		cells.add( DefineReport.InpText.HTDT.getTxt()+" :"+jad.getJSONText(DefineReport.InpText.HTDT.getObj()) );
		cells.add("");
		cells.add( DefineReport.MeisyoSelect.KBN910009.getTxt()+" :"+jad.getJSONText(DefineReport.MeisyoSelect.KBN910009.getObj()) );
		cells.add( "店舗 :"+jad.getJSONText( DefineReport.Select.TENKN.getObj()));
		cells.add( DefineReport.MeisyoSelect.KBN10002.getTxt()+" :"+jad.getJSONText( DefineReport.MeisyoSelect.KBN10002.getObj()));
		cells.add("");
		cells.add( DefineReport.InpText.CENTERCD.getTxt()+" :"+jad.getJSONText( DefineReport.InpText.CENTERCD.getObj()));
		getWhere().add(cells);
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.BUMON.getTxt()+" :"+jad.getJSONText( DefineReport.Select.BUMON.getObj()));
		cells.add("");
		cells.add( DefineReport.Select.DAI_BUN.getTxt()+" :"+jad.getJSONText( DefineReport.Select.DAI_BUN.getObj()));
		cells.add("");
		cells.add( DefineReport.Select.CHU_BUN.getTxt()+" :"+jad.getJSONText( DefineReport.Select.CHU_BUN.getObj()));
		cells.add("");
		cells.add( DefineReport.InpText.SSIRCD.getTxt()+" :"+jad.getJSONText( DefineReport.InpText.SSIRCD.getObj()));
		getWhere().add(cells);

		// 空白行
		cells = new ArrayList<String>();
		cells.add("");
		getWhere().add(cells);
	}
}
