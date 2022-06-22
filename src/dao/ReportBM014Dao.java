package dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportBM014Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportBM014Dao(String JNDIname) {
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

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		String szSeq	= getMap().get("SEQ");			// SEQ
		String btnId	= getMap().get("BTN");			// 実行ボタン

		// パラメータ確認
		// 必須チェック
		if ((btnId == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		if(StringUtils.isEmpty(szSeq)){
			sqlWhere += "H.SEQ=null AND ";
		} else {
			sqlWhere += "H.SEQ=? AND ";
			paramData.add(szSeq);
		}

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(" SELECT ");
		sbSQL.append("MOYSKBN||MOYSSTDT||right('000'||MOYSRBAN, 3) AS MOYSCD, ");
		sbSQL.append("MOYKN, ");
		sbSQL.append("MSDT || MSDTW || DECODE(MEDT,null,null, '～') || MEDT || MEDTW AS MOYPERIOD, ");
		sbSQL.append("BMNNO, ");
		sbSQL.append("BMNMAN, ");
		sbSQL.append("HBSTDT || HSTW || DECODE(HBEDDT,null,null, '～') || HBEDDT || HEDW AS HBPERIOD, ");
		sbSQL.append("INPUTNO, ");
		sbSQL.append("E.SHNCD, ");
		sbSQL.append("SHNKN, ");
		sbSQL.append("ERRFLD, ");
		sbSQL.append("ERRTBLNM, ");
		sbSQL.append("ERRVL, ");
		sbSQL.append("OPERATOR_HEAD, ");
		sbSQL.append("INPUT_DATE, ");
		sbSQL.append("INPUT_TIME, ");
		sbSQL.append("COMMENTKN ");
		sbSQL.append("FROM( ");
		sbSQL.append("SELECT ");
		sbSQL.append("C.MOYSKBN, ");
		sbSQL.append("C.MOYSSTDT, ");
		sbSQL.append("C.MOYSRBAN, ");
		sbSQL.append("M.MOYKN, ");
		sbSQL.append("DECODE(M.HBSTDT,null,null,TO_CHAR(TO_DATE(M.HBSTDT,'YYYYMMDD'),'YY/MM/DD')) MSDT, ");
		sbSQL.append("DECODE(M.HBEDDT,null,null,TO_CHAR(TO_DATE(M.HBEDDT,'YYYYMMDD'),'YY/MM/DD')) MEDT, ");
		sbSQL.append("(select JWEEK from WEEK where CWEEK=DECODE(M.HBSTDT,null,null,DAYOFWEEK(TO_DATE(M.HBSTDT,'YYYYMMDD')))) MSDTW, ");
		sbSQL.append("(select JWEEK from WEEK where CWEEK=DECODE(M.HBEDDT,null,null,DAYOFWEEK(TO_DATE(M.HBSTDT,'YYYYMMDD')))) MEDTW, ");
		sbSQL.append("C.BMNNO, ");
		sbSQL.append("C.BMNMAN, ");
		sbSQL.append("DECODE(C.HBSTDT,null,null,TO_CHAR(TO_DATE(C.HBSTDT,'YYYYMMDD'),'YY/MM/DD')) HBSTDT, ");
		sbSQL.append("DECODE(C.HBEDDT,null,null,TO_CHAR(TO_DATE(C.HBEDDT,'YYYYMMDD'),'YY/MM/DD')) HBEDDT, ");
		sbSQL.append("(select JWEEK from WEEK where CWEEK=DECODE(C.HBSTDT,null,null,DAYOFWEEK(TO_DATE(C.HBSTDT,'YYYYMMDD')))) HSTW, ");
		sbSQL.append("(select JWEEK from WEEK where CWEEK=DECODE(C.HBEDDT,null,null,DAYOFWEEK(TO_DATE(C.HBEDDT,'YYYYMMDD')))) HEDW, ");
		sbSQL.append("C.INPUTNO, ");
		sbSQL.append("C.SHNCD, ");
		sbSQL.append("C.ERRFLD, ");
		sbSQL.append("C.ERRTBLNM, ");
		sbSQL.append("C.ERRVL, ");
		sbSQL.append("H.OPERATOR AS OPERATOR_HEAD, ");
		sbSQL.append("to_char(H.INPUT_DATE, 'YYMMDD') AS INPUT_DATE, ");
		sbSQL.append("to_char(H.INPUT_DATE, 'HH24MISS') AS INPUT_TIME, ");
		sbSQL.append("H.COMMENTKN ");
		sbSQL.append(" FROM INATK.CSVTOKHEAD H, INATK.CSVTOK_BM C LEFT JOIN INATK.TOKMOYCD M ");
		sbSQL.append(" ON C.MOYSKBN=M.MOYSKBN AND ");
		sbSQL.append("C.MOYSSTDT=M.MOYSSTDT AND ");
		sbSQL.append("C.MOYSRBAN=M.MOYSRBAN ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);
		sbSQL.append("H.SEQ=C.SEQ ");
		sbSQL.append(") E LEFT JOIN INAMS.MSTSHN S ON E.SHNCD=S.SHNCD ");
		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
		List<String> cells = new ArrayList<String>();

		// タイトル名称
		cells.add("商品マスタ");// jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
		getWhere().add(cells);

		// 空白行
		cells = new ArrayList<String>();
		cells.add("");
		getWhere().add(cells);

		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.KIKAN.getTxt() );
		cells.add( jad.getJSONText(DefineReport.Select.KIKAN_F.getObj()));
		cells.add( DefineReport.Select.TENPO.getTxt());
		cells.add( jad.getJSONText( DefineReport.Select.TENPO.getObj()) );
		cells.add( DefineReport.Select.BUMON.getTxt());
		cells.add( jad.getJSONText( DefineReport.Select.BUMON.getObj()) );
		getWhere().add(cells);

		// 空白行
		cells = new ArrayList<String>();
		cells.add("");
		getWhere().add(cells);
	}
}
