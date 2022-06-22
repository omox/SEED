package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx152Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx152Dao(String JNDIname) {
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
	 * 更新処理
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JSONObject update(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {

		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.check(map);

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}

		// 更新処理
		try {
			msgObj = this.updateData(map, userInfo);
		} catch (Exception e) {
			e.printStackTrace();
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return msgObj;
	}

	/**
	 * 削除処理
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JSONObject delete(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {

		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.check(map);

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}

		// 削除処理
		try {
			msgObj = this.deleteData(map, userInfo);
		} catch (Exception e) {
			e.printStackTrace();
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
		}
		return msgObj;
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szSircd	 = getMap().get("SIRCD");				// 選択メーカーコード
		String sendBtnid	 = getMap().get("SENDBTNID");			// 呼出しボタン

		// パラメータ確認
		// 必須チェック
		if ( szSircd == null || sendBtnid == null) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();

		StringBuffer sbSQL = new StringBuffer();

		if(StringUtils.equals(DefineReport.Button.SEL_CHANGE.getObj(), sendBtnid)
				|| StringUtils.equals(DefineReport.Button.SEL_REFER.getObj(), sendBtnid)
				|| StringUtils.equals("btn_id_change", sendBtnid)
				|| StringUtils.equals("btn_id_sel_refer", sendBtnid)
				|| StringUtils.equals(DefineReport.Button.SEARCH.getObj(), sendBtnid)

				){

			sbSQL.append("select");
			sbSQL.append(" SIR.SIRCD");											// F1	： 仕入先コード
			sbSQL.append(", SIR.SIRKN");										// F2	： 仕入先名（カナ）
			sbSQL.append(", SIR.SIRAN");										// F3	： 仕入先名（漢字）
			sbSQL.append(", SIR.YUBINNO_U");									// F4	： 郵便番号_上桁
			sbSQL.append(", SIR.YUBINNO_S");									// F5	： 郵便番号_下桁
			sbSQL.append(", SIR.ADDRKN_T");										// F6	： 住所_都道府県（漢字）
			sbSQL.append(", SIR.ADDRKN_S");										// F7	： 住所_市区町村（漢字）
			sbSQL.append(", SIR.ADDRKN_M");										// F8	： 住所_町字（漢字）
			sbSQL.append(", SIR.ADDR_B");										// F9	： 住所_番地（漢字）
			sbSQL.append(", SIR.BUSHOKN");										// F10	： 部署名（漢字）
			sbSQL.append(", SIR.TEL");											// F11	： 電話番号
			sbSQL.append(", SIR.NAISEN");										// F12	： 内線番号
			sbSQL.append(", SIR.FAX");											// F13	： FAX番号
			sbSQL.append(", case");												// F14	： 代表仕入先コード
			sbSQL.append("  when SIR.STARTDT = 0 then null");
			sbSQL.append("  else right (SIR.STARTDT, 6)");
			sbSQL.append("  end ");
			sbSQL.append(", SIR.SIRYOTOKBN");									// F15	： 伝送先親仕入先コード
			sbSQL.append(", SIR.INAZAIKOKBN");									// F16	： 開始日
			sbSQL.append(", SIR.EDI_RKBN");										// F17	： EDI受信
			sbSQL.append(", SIR.KAKGAKEKBN");									// F18	： 買掛区分
			sbSQL.append(", SIR.SYORTANKAAM");									// F19	： 処理単価
			sbSQL.append(", SIR.NOZEISHANO");									// F20	： 納税者番号
			sbSQL.append(", SIR.DF_IDENKBN");									// F21	： デフォルト_一括区分
			sbSQL.append(", case");												// F22	： 代表仕入先コード
			sbSQL.append("  when trim(SIR.DSIRCD) = '000000' then null");
			sbSQL.append("  else trim(SIR.DSIRCD) end");
			sbSQL.append(", SIR.SIRKN");										// F23	： 仕入先名（漢字）
			sbSQL.append(", SIR.DDENPKBN");										// F24	： 同報配信先_伝票区分
			sbSQL.append(", SIR.DSHUHKBN");										// F25	： 同報配信先_集計表
			sbSQL.append(", SIR.DWAPNKBN");										// F26	： 同報配信先_ワッペン
			sbSQL.append(", SIR.BMSKBN");										// F27	： BMS対象区分
			sbSQL.append(", SIR.AUTOKBN");										// F28	： 自動検収区分
			sbSQL.append(", SIR.STOPFLG");										// F29	： 取引停止フラグ
			sbSQL.append(", SIR.DF_VANKBN");									// F30	： デフォルト_計算センター
			sbSQL.append(", SIR.DF_DENPKBN");									// F31	： デフォルト_伝票区分
			sbSQL.append(", SIR.DF_TENDENFLG");									// F32	： デフォルト_店別伝票フラグ
			sbSQL.append(", SIR.DF_SHUHKBN");									// F33	： デフォルト_集計表
			sbSQL.append(", SIR.DF_UNYOKBN");									// F34	： デフォルト_運用区分
			sbSQL.append(", SIR.DF_PICKDKBN");									// F35	： デフォルト_ピッキングデータ
			sbSQL.append(", SIR.DF_RYUTSUKBN");									// F36	： デフォルト_流通区分
			sbSQL.append(", SIR.DF_PICKLKBN");									// F37	： デフォルト_ピッキングリスト
			sbSQL.append(", SIR.DF_WAPNKBN");									// F38	： デフォルト_ワッペン
			sbSQL.append(", SIR.DF_IDENPKBN");									// F39	： デフォルト_一括伝票
			sbSQL.append(", SIR.DF_KAKOSJKBN");									// F40	： デフォルト_加工指示
			sbSQL.append(", case");												// F41	： デフォルト_実仕入先コード
			sbSQL.append("  when trim(SIR.DF_RSIRCD) = '000000' then null");
			sbSQL.append("  else trim(SIR.DF_RSIRCD) end");
			sbSQL.append(", SIR.SIRKN");										// F42	： 仕入先名（漢字）
			sbSQL.append(", SIR.DF_ZDENPKBN");									// F43	： デフォルト_在庫内訳_伝票区分
			sbSQL.append(", SIR.DF_ZSHUHKBN");									// F44	： デフォルト_在庫内訳_集計表
			sbSQL.append(", SIR.DF_ZPICKDKBN");									// F45	： デフォルト_在庫内訳_ピッキングデータ
			sbSQL.append(", SIR.DF_ZPICKLKBN");									// F46	： デフォルト_在庫内訳_ピッキングリスト
			sbSQL.append(", SIR.DF_YKNSHKBN");									// F47	： デフォルト_横持先_検収区分
			sbSQL.append(", SIR.DF_YDENPKBN");									// F48	： デフォルト_横持先_伝票区分
			sbSQL.append(", SIR.DF_YSHUHKBN");									// F49	： デフォルト_横持先_集計表
			sbSQL.append(", TO_CHAR(SIR.ADDDT, 'yy/mm/dd')");					// F50	： 登録日
			sbSQL.append(", TO_CHAR(SIR.UPDDT, 'yy/mm/dd')");					// F51	： 更新日
			sbSQL.append(", SIR.OPERATOR");										// F52	： オペレータ
			sbSQL.append(" from INAMS.MSTSIR SIR");
			sbSQL.append(" where NVL(SIR.UPDKBN, 0) = 0");
			sbSQL.append(" and SIR.SIRCD = ?");
			sbSQL.append(" order by SIR.SIRCD");
			paramData.add(szSircd);

		}else if(StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			sbSQL.append("select");
			sbSQL.append(" null");												// F1	： 仕入先コード
			sbSQL.append(", null");												// F2	： 仕入先名（カナ）
			sbSQL.append(", null");												// F3	： 仕入先名（漢字）
			sbSQL.append(", null");												// F4	： 郵便番号_上桁
			sbSQL.append(", null");												// F5	： 郵便番号_下桁
			sbSQL.append(", null");												// F6	： 住所_都道府県（漢字）
			sbSQL.append(", null");												// F7	： 住所_市区町村（漢字）
			sbSQL.append(", null");												// F8	： 住所_町字（漢字）
			sbSQL.append(", null");												// F9	： 住所_番地（漢字）
			sbSQL.append(", null");												// F10	： 部署名（漢字）
			sbSQL.append(", null");												// F11	： 電話番号
			sbSQL.append(", null");												// F12	： 内線番号
			sbSQL.append(", null");												// F13	： FAX番号
			sbSQL.append(", null");												// F14	： 代表仕入先コード
			sbSQL.append(", 0");												// F15	： 伝送先親仕入先コード
			sbSQL.append(", 0");												// F16	： 開始日
			sbSQL.append(", 0");												// F17	： EDI受信
			sbSQL.append(", 0");												// F18	： 買掛区分
			sbSQL.append(", 0");												// F19	： 処理単価
			sbSQL.append(", null");												// F20	： 納税者番号
			sbSQL.append(", 1");												// F21	： 代表仕入先コード
			sbSQL.append(", null");												// F22	： 代表仕入先コード
			sbSQL.append(", null");												// F23	： 仕入先名（漢字）
			sbSQL.append(", 0");												// F24	： 同報配信先_伝票区分
			sbSQL.append(", 0");												// F25	： 同報配信先_集計表
			sbSQL.append(", 0");												// F26	： 同報配信先_ワッペン
			sbSQL.append(", 0");												// F27	： BMS対象区分
			sbSQL.append(", 0");												// F28	： 自動検収区分
			sbSQL.append(", 0");												// F39	： 取引停止フラグ
			sbSQL.append(", 0");												// F30	： デフォルト_計算センター
			sbSQL.append(", 0");												// F31	： デフォルト_伝票区分
			sbSQL.append(", 0");												// F32	： デフォルト_店別伝票フラグ
			sbSQL.append(", 0");												// F33	： デフォルト_集計表
			sbSQL.append(", 0");												// F34	： デフォルト_運用区分
			sbSQL.append(", 0");												// F35	： デフォルト_ピッキングデータ
			sbSQL.append(", 0");												// F36	： デフォルト_流通区分
			sbSQL.append(", 0");												// F37	： デフォルト_ピッキングリスト
			sbSQL.append(", 0");												// F38	： デフォルト_ワッペン
			sbSQL.append(", 0");												// F39	： デフォルト_一括伝票
			sbSQL.append(", 0");												// F40	： デフォルト_加工指示
			sbSQL.append(", null");												// F41	： デフォルト_実仕入先コード
			sbSQL.append(", null");												// F42	： 仕入先名（漢字）
			sbSQL.append(", 0");												// F43	： デフォルト_在庫内訳_伝票区分
			sbSQL.append(", 0");												// F44	： デフォルト_在庫内訳_集計表
			sbSQL.append(", 0");												// F45	： デフォルト_在庫内訳_ピッキングデータ
			sbSQL.append(", 0");												// F46	： デフォルト_在庫内訳_ピッキングリスト
			sbSQL.append(", 0");												// F47	： デフォルト_横持先_検収区分
			sbSQL.append(", 0");												// F48	： デフォルト_横持先_伝票区分
			sbSQL.append(", 0");												// F49	： デフォルト_横持先_集計表
			sbSQL.append(", null");												// F50	： 登録日
			sbSQL.append(", null");												// F51	： 更新日
			sbSQL.append(", null");												// F52	： オペレータ
			sbSQL.append(" from SYSIBM.SYSDUMMY1");
		}

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

		// 共通箇所設定
		createCmnOutput(jad);

	}

	boolean isTest = true;

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/**
	 * 仕入先マスタINSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlSIR(JSONObject data, HashMap<String, String> map, User userInfo){

		StringBuffer sbSQL			= new StringBuffer();
		JSONArray dataArrayHSPTN	= JSONArray.fromObject(map.get("DATA_HSPTN"));		// 更新情報(予約発注_納品日)
		JSONArray dataArrayEHSPTN	= JSONArray.fromObject(map.get("DATA_EHSPTN"));		// 更新情報(予約発注_納品日)

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー

		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		int maxField = 51;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			if(k == 1){
				values += String.valueOf(0 + 1);

			}

			if(! ArrayUtils.contains(new String[]{""}, key)){
				String val = data.optString(key);
				if(StringUtils.isEmpty(val)){

					if(StringUtils.equals(key, "F22")){
						// デフォルト値設定:納税者番号
						values += ", ''";

					}else if(StringUtils.equals(key, "F12")){
						// デフォルト値設定:内線番号
						values += ", ''";

					}else if(StringUtils.equals(key, "F14")){
						// デフォルト値設定:代表仕入先コード
						values += ", '000000'";

					}else if(StringUtils.equals(key, "F15")){
						// デフォルト値設定:伝送先親仕入先コード
						values += ", '0'";

					}else if(StringUtils.equals(key, "F16")){
						// デフォルト値設定:開始日
						values += ", '0'";

					}else if(StringUtils.equals(key, "F18")){
						// デフォルト値設定:EDI送信
						values += ", '0'";

					}else if(StringUtils.equals(key, "F24")){
						// デフォルト値設定:基本料金
						values += ", '0'";

					}else if(StringUtils.equals(key, "F26")){
						// デフォルト値設定:同報配信先コード
						values += ", '      '";

					}else if(StringUtils.equals(key, "F46")){
						// デフォルト値設定:デフォルト_実仕入先コード
						values += ", '000000'";

					}else{
						values += ", null";
					}
				}else{
					if(StringUtils.equals(key, "F30")){
						if("-1".equals(val)){
							values += ", null";
						}else{
							values += ", ?";
							prmData.add(val);
						}
					}else{
						values += ", ?";
						prmData.add(val);
					}
				}
			}

			if(k == maxField){
				valueData = ArrayUtils.add(valueData, "("+values+")");
				values = "";
			}
		}

		// 仕入先マスタの登録・更新
		sbSQL = new StringBuffer();
		sbSQL.append(" merge into INAMS.MSTSIR as T using (select");
		sbSQL.append(" SIRCD");														// 仕入先コード
		sbSQL.append(", SIRAN");													// 仕入先名（カナ）
		sbSQL.append(", SIRKN");													// 仕入先名（漢字）
		sbSQL.append(", ADDRKN_T");													// 住所_都道府県（漢字）
		sbSQL.append(", ADDRKN_S");													// 住所_市区町村（漢字）
		sbSQL.append(", ADDRKN_M");													// 住所_町字（漢字）
		sbSQL.append(", ADDR_B");													// 住所_番地（漢字）
		sbSQL.append(", BUSHOKN");													// 部署名（漢字）
		sbSQL.append(", YUBINNO_U");												// 郵便番号_上桁
		sbSQL.append(", YUBINNO_S");												// 郵便番号_下桁
		sbSQL.append(", TEL");														// 電話番号
		sbSQL.append(", NAISEN");													// 内線番号
		sbSQL.append(", FAX");														// FAX番号
		sbSQL.append(", DSIRCD");													// 代表仕入先コード
		sbSQL.append(", DOYASIRCD");												// 伝送先親仕入先コード
		sbSQL.append(", STARTDT");													// 開始日
		sbSQL.append(", EDI_RKBN");													// EDI受信
		sbSQL.append(", EDI_SKBN");													// EDI送信
		sbSQL.append(", SIRYOTOKBN");												// 仕入先用途
		sbSQL.append(", INAZAIKOKBN");												// いなげや在庫
		sbSQL.append(", KAKGAKEKBN");												// 買掛区分
		sbSQL.append(", NOZEISHANO");												// 納税者番号
		sbSQL.append(", SYORTANKAAM");												// 処理単価
		sbSQL.append(", KHNRYOKINAM");												// 基本料金
		sbSQL.append(", STOPFLG");													// 取引停止フラグ
		sbSQL.append(", DOHOCD");													// 同報配信先コード
		sbSQL.append(", DDENPKBN");													// 同報配信先_伝票区分
		sbSQL.append(", DSHUHKBN");													// 同報配信先_集計表
		sbSQL.append(", DWAPNKBN");													// 同報配信先_ワッペン
		sbSQL.append(", DF_IDENKBN");												// デフォルト_一括区分
		sbSQL.append(", DF_TENDENFLG");												// デフォルト_店別伝票フラグ
		sbSQL.append(", DF_VANKBN");												// デフォルト_計算センター
		sbSQL.append(", DF_UNYOKBN");												// デフォルト_運用区分
		sbSQL.append(", DF_DENPKBN");												// デフォルト_伝票区分
		sbSQL.append(", DF_SHUHKBN");												// デフォルト_集計表
		sbSQL.append(", DF_PICKDKBN");												// デフォルト_ピッキングデータ
		sbSQL.append(", DF_PICKLKBN");												// デフォルト_ピッキングリスト
		sbSQL.append(", DF_WAPNKBN");												// デフォルト_ワッペン
		sbSQL.append(", DF_IDENPKBN");												// デフォルト_一括伝票
		sbSQL.append(", DF_KAKOSJKBN");												// デフォルト_加工指示
		sbSQL.append(", DF_RYUTSUKBN");												// デフォルト_流通区分
		sbSQL.append(", DF_ZDENPKBN");												// デフォルト_在庫内訳_伝票区分
		sbSQL.append(", DF_ZSHUHKBN");												// デフォルト_在庫内訳_集計表
		sbSQL.append(", DF_ZPICKDKBN");												// デフォルト_在庫内訳_ピッキングデータ
		sbSQL.append(", DF_ZPICKLKBN");												// デフォルト_在庫内訳_ピッキングリスト
		sbSQL.append(", DF_RSIRCD");												// デフォルト_実仕入先コード
		sbSQL.append(", DF_YKNSHKBN");												// デフォルト_横持先_検収区分
		sbSQL.append(", DF_YDENPKBN");												// デフォルト_横持先_伝票区分
		sbSQL.append(", DF_YSHUHKBN");												// デフォルト_横持先_集計表
		sbSQL.append(", BMSKBN");													// BMS対象区分
		sbSQL.append(", AUTOKBN");													// 自動検収区分
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分
		sbSQL.append(", 0 AS SENDFLG");												// 送信フラグ
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日
		sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM, ");
		sbSQL.append(" SIRCD");
		sbSQL.append(", SIRAN");
		sbSQL.append(", SIRKN");
		sbSQL.append(", ADDRKN_T");
		sbSQL.append(", ADDRKN_S");
		sbSQL.append(", ADDRKN_M");
		sbSQL.append(", ADDR_B");
		sbSQL.append(", BUSHOKN");
		sbSQL.append(", YUBINNO_U");
		sbSQL.append(", YUBINNO_S");
		sbSQL.append(", TEL");
		sbSQL.append(", NAISEN");
		sbSQL.append(", FAX");
		sbSQL.append(", DSIRCD");
		sbSQL.append(", DOYASIRCD");
		sbSQL.append(", STARTDT");
		sbSQL.append(", EDI_RKBN");
		sbSQL.append(", EDI_SKBN");
		sbSQL.append(", SIRYOTOKBN");
		sbSQL.append(", INAZAIKOKBN");
		sbSQL.append(", KAKGAKEKBN");
		sbSQL.append(", NOZEISHANO");
		sbSQL.append(", SYORTANKAAM");
		sbSQL.append(", KHNRYOKINAM");
		sbSQL.append(", STOPFLG");
		sbSQL.append(", DOHOCD");
		sbSQL.append(", DDENPKBN");
		sbSQL.append(", DSHUHKBN");
		sbSQL.append(", DWAPNKBN");
		sbSQL.append(", DF_IDENKBN");
		sbSQL.append(", DF_TENDENFLG");
		sbSQL.append(", DF_VANKBN");
		sbSQL.append(", DF_UNYOKBN");
		sbSQL.append(", DF_DENPKBN");
		sbSQL.append(", DF_SHUHKBN");
		sbSQL.append(", DF_PICKDKBN");
		sbSQL.append(", DF_PICKLKBN");
		sbSQL.append(", DF_WAPNKBN");
		sbSQL.append(", DF_IDENPKBN");
		sbSQL.append(", DF_KAKOSJKBN");
		sbSQL.append(", DF_RYUTSUKBN");
		sbSQL.append(", DF_ZDENPKBN");
		sbSQL.append(", DF_ZSHUHKBN");
		sbSQL.append(", DF_ZPICKDKBN");
		sbSQL.append(", DF_ZPICKLKBN");
		sbSQL.append(", DF_RSIRCD");
		sbSQL.append(", DF_YKNSHKBN");
		sbSQL.append(", DF_YDENPKBN");
		sbSQL.append(", DF_YSHUHKBN");
		sbSQL.append(", BMSKBN");
		sbSQL.append(", AUTOKBN");
		sbSQL.append(" ))as RE on (T.SIRCD = RE.SIRCD)");
		sbSQL.append(" when matched then update set");
		sbSQL.append(" SIRCD=RE.SIRCD");
		sbSQL.append(", SIRAN=RE.SIRAN");
		sbSQL.append(", SIRKN=RE.SIRKN");
		sbSQL.append(", ADDRKN_T=RE.ADDRKN_T");
		sbSQL.append(", ADDRKN_S=RE.ADDRKN_S");
		sbSQL.append(", ADDRKN_M=RE.ADDRKN_M");
		sbSQL.append(", ADDR_B=RE.ADDR_B");
		sbSQL.append(", BUSHOKN=RE.BUSHOKN");
		sbSQL.append(", YUBINNO_U=RE.YUBINNO_U");
		sbSQL.append(", YUBINNO_S=RE.YUBINNO_S");
		sbSQL.append(", TEL=RE.TEL");
		sbSQL.append(", NAISEN=RE.NAISEN");
		sbSQL.append(", FAX=RE.FAX");
		sbSQL.append(", DSIRCD=RE.DSIRCD");
		//sbSQL.append(", DOYASIRCD=RE.DOYASIRCD");
		sbSQL.append(", STARTDT=RE.STARTDT");
		sbSQL.append(", EDI_RKBN=RE.EDI_RKBN");
		//sbSQL.append(", EDI_SKBN=RE.EDI_SKBN");
		sbSQL.append(", SIRYOTOKBN=RE.SIRYOTOKBN");
		sbSQL.append(", INAZAIKOKBN=RE.INAZAIKOKBN");
		sbSQL.append(", KAKGAKEKBN=RE.KAKGAKEKBN");
		sbSQL.append(", NOZEISHANO=RE.NOZEISHANO");
		sbSQL.append(", SYORTANKAAM=RE.SYORTANKAAM");
		//sbSQL.append(", KHNRYOKINAM=RE.KHNRYOKINAM");
		sbSQL.append(", STOPFLG=RE.STOPFLG");
		//sbSQL.append(", DOHOCD=RE.DOHOCD");
		sbSQL.append(", DDENPKBN=RE.DDENPKBN");
		sbSQL.append(", DSHUHKBN=RE.DSHUHKBN");
		sbSQL.append(", DWAPNKBN=RE.DWAPNKBN");
		sbSQL.append(", DF_IDENKBN=RE.DF_IDENKBN");
		sbSQL.append(", DF_TENDENFLG=RE.DF_TENDENFLG");
		sbSQL.append(", DF_VANKBN=RE.DF_VANKBN");
		sbSQL.append(", DF_UNYOKBN=RE.DF_UNYOKBN");
		sbSQL.append(", DF_DENPKBN=RE.DF_DENPKBN");
		sbSQL.append(", DF_SHUHKBN=RE.DF_SHUHKBN");
		sbSQL.append(", DF_PICKDKBN=RE.DF_PICKDKBN");
		sbSQL.append(", DF_PICKLKBN=RE.DF_PICKLKBN");
		sbSQL.append(", DF_WAPNKBN=RE.DF_WAPNKBN");
		sbSQL.append(", DF_IDENPKBN=RE.DF_IDENPKBN");
		sbSQL.append(", DF_KAKOSJKBN=RE.DF_KAKOSJKBN");
		sbSQL.append(", DF_RYUTSUKBN=RE.DF_RYUTSUKBN");
		sbSQL.append(", DF_ZDENPKBN=RE.DF_ZDENPKBN");
		sbSQL.append(", DF_ZSHUHKBN=RE.DF_ZSHUHKBN");
		sbSQL.append(", DF_ZPICKDKBN=RE.DF_ZPICKDKBN");
		sbSQL.append(", DF_ZPICKLKBN=RE.DF_ZPICKLKBN");
		sbSQL.append(", DF_RSIRCD=RE.DF_RSIRCD");
		sbSQL.append(", DF_YKNSHKBN=RE.DF_YKNSHKBN");
		sbSQL.append(", DF_YDENPKBN=RE.DF_YDENPKBN");
		sbSQL.append(", DF_YSHUHKBN=RE.DF_YSHUHKBN");
		sbSQL.append(", UPDKBN=RE.UPDKBN");
		sbSQL.append(", SENDFLG=RE.SENDFLG");
		sbSQL.append(", OPERATOR=RE.OPERATOR");
		sbSQL.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
		sbSQL.append(", UPDDT=RE.UPDDT");
		sbSQL.append(", BMSKBN=RE.BMSKBN");
		sbSQL.append(", AUTOKBN=RE.AUTOKBN");
		sbSQL.append(" when not matched then insert (");
		sbSQL.append(" SIRCD");
		sbSQL.append(", SIRAN");
		sbSQL.append(", SIRKN");
		sbSQL.append(", ADDRKN_T");
		sbSQL.append(", ADDRKN_S");
		sbSQL.append(", ADDRKN_M");
		sbSQL.append(", ADDR_B");
		sbSQL.append(", BUSHOKN");
		sbSQL.append(", YUBINNO_U");
		sbSQL.append(", YUBINNO_S");
		sbSQL.append(", TEL");
		sbSQL.append(", NAISEN");
		sbSQL.append(", FAX");
		sbSQL.append(", DSIRCD");
		sbSQL.append(", DOYASIRCD");
		sbSQL.append(", STARTDT");
		sbSQL.append(", EDI_RKBN");
		sbSQL.append(", EDI_SKBN");
		sbSQL.append(", SIRYOTOKBN");
		sbSQL.append(", INAZAIKOKBN");
		sbSQL.append(", KAKGAKEKBN");
		sbSQL.append(", NOZEISHANO");
		sbSQL.append(", SYORTANKAAM");
		sbSQL.append(", KHNRYOKINAM");
		sbSQL.append(", STOPFLG");
		sbSQL.append(", DOHOCD");
		sbSQL.append(", DDENPKBN");
		sbSQL.append(", DSHUHKBN");
		sbSQL.append(", DWAPNKBN");
		sbSQL.append(", DF_IDENKBN");
		sbSQL.append(", DF_TENDENFLG");
		sbSQL.append(", DF_VANKBN");
		sbSQL.append(", DF_UNYOKBN");
		sbSQL.append(", DF_DENPKBN");
		sbSQL.append(", DF_SHUHKBN");
		sbSQL.append(", DF_PICKDKBN");
		sbSQL.append(", DF_PICKLKBN");
		sbSQL.append(", DF_WAPNKBN");
		sbSQL.append(", DF_IDENPKBN");
		sbSQL.append(", DF_KAKOSJKBN");
		sbSQL.append(", DF_RYUTSUKBN");
		sbSQL.append(", DF_ZDENPKBN");
		sbSQL.append(", DF_ZSHUHKBN");
		sbSQL.append(", DF_ZPICKDKBN");
		sbSQL.append(", DF_ZPICKLKBN");
		sbSQL.append(", DF_RSIRCD");
		sbSQL.append(", DF_YKNSHKBN");
		sbSQL.append(", DF_YDENPKBN");
		sbSQL.append(", DF_YSHUHKBN");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT");
		sbSQL.append(", BMSKBN");
		sbSQL.append(", AUTOKBN");
		sbSQL.append(") values (");
		sbSQL.append(" RE.SIRCD");
		sbSQL.append(", RE.SIRAN");
		sbSQL.append(", RE.SIRKN");
		sbSQL.append(", RE.ADDRKN_T");
		sbSQL.append(", RE.ADDRKN_S");
		sbSQL.append(", RE.ADDRKN_M");
		sbSQL.append(", RE.ADDR_B");
		sbSQL.append(", RE.BUSHOKN");
		sbSQL.append(", RE.YUBINNO_U");
		sbSQL.append(", RE.YUBINNO_S");
		sbSQL.append(", RE.TEL");
		sbSQL.append(", RE.NAISEN");
		sbSQL.append(", RE.FAX");
		sbSQL.append(", RE.DSIRCD");
		sbSQL.append(", RE.DOYASIRCD");
		sbSQL.append(", RE.STARTDT");
		sbSQL.append(", RE.EDI_RKBN");
		sbSQL.append(", RE.EDI_SKBN");
		sbSQL.append(", RE.SIRYOTOKBN");
		sbSQL.append(", RE.INAZAIKOKBN");
		sbSQL.append(", RE.KAKGAKEKBN");
		sbSQL.append(", RE.NOZEISHANO");
		sbSQL.append(", RE.SYORTANKAAM");
		sbSQL.append(", RE.KHNRYOKINAM");
		sbSQL.append(", RE.STOPFLG");
		sbSQL.append(", RE.DOHOCD");
		sbSQL.append(", RE.DDENPKBN");
		sbSQL.append(", RE.DSHUHKBN");
		sbSQL.append(", RE.DWAPNKBN");
		sbSQL.append(", RE.DF_IDENKBN");
		sbSQL.append(", RE.DF_TENDENFLG");
		sbSQL.append(", RE.DF_VANKBN");
		sbSQL.append(", RE.DF_UNYOKBN");
		sbSQL.append(", RE.DF_DENPKBN");
		sbSQL.append(", RE.DF_SHUHKBN");
		sbSQL.append(", RE.DF_PICKDKBN");
		sbSQL.append(", RE.DF_PICKLKBN");
		sbSQL.append(", RE.DF_WAPNKBN");
		sbSQL.append(", RE.DF_IDENPKBN");
		sbSQL.append(", RE.DF_KAKOSJKBN");
		sbSQL.append(", RE.DF_RYUTSUKBN");
		sbSQL.append(", RE.DF_ZDENPKBN");
		sbSQL.append(", RE.DF_ZSHUHKBN");
		sbSQL.append(", RE.DF_ZPICKDKBN");
		sbSQL.append(", RE.DF_ZPICKLKBN");
		sbSQL.append(", RE.DF_RSIRCD");
		sbSQL.append(", RE.DF_YKNSHKBN");
		sbSQL.append(", RE.DF_YDENPKBN");
		sbSQL.append(", RE.DF_YSHUHKBN");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT");
		sbSQL.append(", RE.BMSKBN");
		sbSQL.append(", RE.AUTOKBN");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕入先マスタ");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";


		maxField = 21;		// Fxxの最大値
		int len = dataArrayHSPTN.size();
		// 配送パターン仕入先マスタ 削除
		this.createDeleteSqlHsptnSir(map, userInfo);
		for (int i = 0; i < len; i++) {
			JSONObject dataT = dataArrayHSPTN.getJSONObject(i);
			if(! dataT.isEmpty()){
				for (int k = 1; k <= maxField; k++) {
					String key = "F" + String.valueOf(k);

					if(k == 1){
						values += String.valueOf(0 + 1);

					}

					if(! ArrayUtils.contains(new String[]{""}, key)){
						String val = dataT.optString(key);
						if(StringUtils.isEmpty(val)){
							values += ", null";
						}else{
							values += ", ?";
							prmData.add(val);
						}
					}

					if(k == maxField){
						valueData = ArrayUtils.add(valueData, "("+values+")");
						values = "";
					}
				}
			}

			if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){

				// 配送パターンの登録・更新
				sbSQL = new StringBuffer();
				sbSQL.append(" merge into INAMS.MSTHSPTNSIR as T using (select");
				sbSQL.append(" SIRCD");														// 仕入先コード
				sbSQL.append(", HSPTN");													// 配送パターン
				sbSQL.append(", TENDENFLG");												// 店別伝票フラグ
				sbSQL.append(", VANKBN");													// 計算センター
				sbSQL.append(", UNYOKBN");													// 運用区分
				sbSQL.append(", DENPKBN");													// 伝票区分
				sbSQL.append(", SHUHKBN");													// 集計表
				sbSQL.append(", PICKDKBN");													// ピッキングデータ
				sbSQL.append(", PICKLKBN");													// ピッキングリスト
				sbSQL.append(", WAPNKBN");													// ワッペン
				sbSQL.append(", IDENPKBN");													// 一括伝票
				sbSQL.append(", KAKOSJKBN");												// 加工指示
				sbSQL.append(", RYUTSUKBN");												// 流通区分
				sbSQL.append(", ZDENPKBN");													// 在庫内訳_伝票区分
				sbSQL.append(", ZSHUHKBN");													// 在庫内訳_集計表
				sbSQL.append(", ZPICKDKBN");												// 在庫内訳_ピッキングデータ
				sbSQL.append(", ZPICKLKBN");												// 在庫内訳_ピッキングリスト
				sbSQL.append(", RSIRCD");													// 実仕入先コード
				sbSQL.append(", YKNSHKBN");													// 横持先_検収区分
				sbSQL.append(", YDENPKBN");													// 横持先_伝票区分
				sbSQL.append(", DSHUHKBN");													// 横持先_集計表
				sbSQL.append(", 0 as SENDFLG");												// 送信フラグ
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター
				sbSQL.append(", current timestamp AS ADDDT ");								// 登録日
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM, ");
				sbSQL.append(" SIRCD");
				sbSQL.append(", HSPTN");
				sbSQL.append(", TENDENFLG");
				sbSQL.append(", VANKBN");
				sbSQL.append(", UNYOKBN");
				sbSQL.append(", DENPKBN");
				sbSQL.append(", SHUHKBN");
				sbSQL.append(", PICKDKBN");
				sbSQL.append(", PICKLKBN");
				sbSQL.append(", WAPNKBN");
				sbSQL.append(", IDENPKBN");
				sbSQL.append(", KAKOSJKBN");
				sbSQL.append(", RYUTSUKBN");
				sbSQL.append(", ZDENPKBN");
				sbSQL.append(", ZSHUHKBN");
				sbSQL.append(", ZPICKDKBN");
				sbSQL.append(", ZPICKLKBN");
				sbSQL.append(", RSIRCD");
				sbSQL.append(", YKNSHKBN");
				sbSQL.append(", YDENPKBN");
				sbSQL.append(", DSHUHKBN");
				sbSQL.append(" ))as RE on (T.SIRCD = RE.SIRCD and T.HSPTN = RE.HSPTN)");
				sbSQL.append(" when matched then update set");
				sbSQL.append(" SIRCD=RE.SIRCD");
				sbSQL.append(", HSPTN=RE.HSPTN");
				sbSQL.append(", TENDENFLG=RE.TENDENFLG");
				sbSQL.append(", VANKBN=RE.VANKBN");
				sbSQL.append(", UNYOKBN=RE.UNYOKBN");
				sbSQL.append(", DENPKBN=RE.DENPKBN");
				sbSQL.append(", SHUHKBN=RE.SHUHKBN");
				sbSQL.append(", PICKDKBN=RE.PICKDKBN");
				sbSQL.append(", PICKLKBN=RE.PICKLKBN");
				sbSQL.append(", WAPNKBN=RE.WAPNKBN");
				sbSQL.append(", IDENPKBN=RE.IDENPKBN");
				sbSQL.append(", KAKOSJKBN=RE.KAKOSJKBN");
				sbSQL.append(", RYUTSUKBN=RE.RYUTSUKBN");
				sbSQL.append(", ZDENPKBN=RE.ZDENPKBN");
				sbSQL.append(", ZSHUHKBN=RE.ZSHUHKBN");
				sbSQL.append(", ZPICKDKBN=RE.ZPICKDKBN");
				sbSQL.append(", ZPICKLKBN=RE.ZPICKLKBN");
				sbSQL.append(", RSIRCD=RE.RSIRCD");
				sbSQL.append(", YKNSHKBN=RE.YKNSHKBN");
				sbSQL.append(", YDENPKBN=RE.YDENPKBN");
				sbSQL.append(", DSHUHKBN=RE.DSHUHKBN");
				sbSQL.append(", SENDFLG=RE.SENDFLG");
				sbSQL.append(", OPERATOR=RE.OPERATOR");
				//sbSQL.append(", ADDDT=RE.ADDDT");
				sbSQL.append(", UPDDT=RE.UPDDT");
				sbSQL.append(" when not matched then insert (");
				sbSQL.append(" SIRCD");
				sbSQL.append(", HSPTN");
				sbSQL.append(", TENDENFLG");
				sbSQL.append(", VANKBN");
				sbSQL.append(", UNYOKBN");
				sbSQL.append(", DENPKBN");
				sbSQL.append(", SHUHKBN");
				sbSQL.append(", PICKDKBN");
				sbSQL.append(", PICKLKBN");
				sbSQL.append(", WAPNKBN");
				sbSQL.append(", IDENPKBN");
				sbSQL.append(", KAKOSJKBN");
				sbSQL.append(", RYUTSUKBN");
				sbSQL.append(", ZDENPKBN");
				sbSQL.append(", ZSHUHKBN");
				sbSQL.append(", ZPICKDKBN");
				sbSQL.append(", ZPICKLKBN");
				sbSQL.append(", RSIRCD");
				sbSQL.append(", YKNSHKBN");
				sbSQL.append(", YDENPKBN");
				sbSQL.append(", DSHUHKBN");
				sbSQL.append(", SENDFLG");
				sbSQL.append(", OPERATOR");
				sbSQL.append(", ADDDT");
				sbSQL.append(", UPDDT");
				sbSQL.append(") values (");
				sbSQL.append(" RE.SIRCD");
				sbSQL.append(", RE.HSPTN");
				sbSQL.append(", RE.TENDENFLG");
				sbSQL.append(", RE.VANKBN");
				sbSQL.append(", RE.UNYOKBN");
				sbSQL.append(", RE.DENPKBN");
				sbSQL.append(", RE.SHUHKBN");
				sbSQL.append(", RE.PICKDKBN");
				sbSQL.append(", RE.PICKLKBN");
				sbSQL.append(", RE.WAPNKBN");
				sbSQL.append(", RE.IDENPKBN");
				sbSQL.append(", RE.KAKOSJKBN");
				sbSQL.append(", RE.RYUTSUKBN");
				sbSQL.append(", RE.ZDENPKBN");
				sbSQL.append(", RE.ZSHUHKBN");
				sbSQL.append(", RE.ZPICKDKBN");
				sbSQL.append(", RE.ZPICKLKBN");
				sbSQL.append(", RE.RSIRCD");
				sbSQL.append(", RE.YKNSHKBN");
				sbSQL.append(", RE.YDENPKBN");
				sbSQL.append(", RE.DSHUHKBN");
				sbSQL.append(", RE.SENDFLG");
				sbSQL.append(", RE.OPERATOR");
				sbSQL.append(", RE.ADDDT");
				sbSQL.append(", RE.UPDDT");
				sbSQL.append(")");

				if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("配送パターン仕入先マスタ");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}

		maxField = 22;		// Fxxの最大値
		len = dataArrayEHSPTN.size();
		// エリア別配送パターン仕入先マスタ 削除
		this.createDeleteSqlEhsptnSir(map, userInfo);
		for (int i = 0; i < len; i++) {
			JSONObject dataT = dataArrayEHSPTN.getJSONObject(i);
			if(! dataT.isEmpty()){
				for (int k = 1; k <= maxField; k++) {
					String key = "F" + String.valueOf(k);

					if(k == 1){
						values += String.valueOf(0 + 1);

					}

					if(! ArrayUtils.contains(new String[]{""}, key)){
						String val = dataT.optString(key);
						if(StringUtils.isEmpty(val)){
							values += ", null";
						}else{
							values += ", ?";
							prmData.add(val);
						}
					}

					if(k == maxField){
						valueData = ArrayUtils.add(valueData, "("+values+")");
						values = "";
					}
				}
			}

			if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){

				// エリア配送パターンの登録・更新
				sbSQL = new StringBuffer();
				sbSQL.append(" merge into INAMS.MSTAREAHSPTNSIR as T using (select");
				sbSQL.append(" SIRCD");														// 仕入先コード
				sbSQL.append(", HSPTN");													// 配送パターン
				sbSQL.append(", TENGPCD");													// 店グループコード
				sbSQL.append(", TENDENFLG");												// 店別伝票フラグ
				sbSQL.append(", VANKBN");													// 計算センター
				sbSQL.append(", UNYOKBN");													// 運用区分
				sbSQL.append(", DENPKBN");													// 伝票区分
				sbSQL.append(", SHUHKBN");													// 集計表
				sbSQL.append(", PICKDKBN");													// ピッキングデータ
				sbSQL.append(", PICKLKBN");													// ピッキングリスト
				sbSQL.append(", WAPNKBN");													// ワッペン
				sbSQL.append(", IDENPKBN");													// 一括伝票
				sbSQL.append(", KAKOSJKBN");												// 加工指示
				sbSQL.append(", RYUTSUKBN");												// 流通区分
				sbSQL.append(", ZDENPKBN");													// 在庫内訳_伝票区分
				sbSQL.append(", ZSHUHKBN");													// 在庫内訳_集計表
				sbSQL.append(", ZPICKDKBN");												// 在庫内訳_ピッキングデータ
				sbSQL.append(", ZPICKLKBN");												// 在庫内訳_ピッキングリスト
				sbSQL.append(", RSIRCD");													// 実仕入先コード
				sbSQL.append(", YKNSHKBN");													// 横持先_検収区分
				sbSQL.append(", YDENPKBN");													// 横持先_伝票区分
				sbSQL.append(", DSHUHKBN");													// 横持先_集計表
				sbSQL.append(", 0 as SENDFLG");												// 送信フラグ
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
				sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM, ");
				sbSQL.append(" SIRCD");
				sbSQL.append(", HSPTN");
				sbSQL.append(", TENGPCD");
				sbSQL.append(", TENDENFLG");
				sbSQL.append(", VANKBN");
				sbSQL.append(", UNYOKBN");
				sbSQL.append(", DENPKBN");
				sbSQL.append(", SHUHKBN");
				sbSQL.append(", PICKDKBN");
				sbSQL.append(", PICKLKBN");
				sbSQL.append(", WAPNKBN");
				sbSQL.append(", IDENPKBN");
				sbSQL.append(", KAKOSJKBN");
				sbSQL.append(", RYUTSUKBN");
				sbSQL.append(", ZDENPKBN");
				sbSQL.append(", ZSHUHKBN");
				sbSQL.append(", ZPICKDKBN");
				sbSQL.append(", ZPICKLKBN");
				sbSQL.append(", RSIRCD");
				sbSQL.append(", YKNSHKBN");
				sbSQL.append(", YDENPKBN");
				sbSQL.append(", DSHUHKBN");
				sbSQL.append(" ))as RE on (T.SIRCD = RE.SIRCD and T.HSPTN = RE.HSPTN)");
				sbSQL.append(" when matched then update set");
				sbSQL.append(" SIRCD=RE.SIRCD");
				sbSQL.append(", HSPTN=RE.HSPTN");
				sbSQL.append(", TENGPCD=RE.TENGPCD");
				sbSQL.append(", TENDENFLG=RE.TENDENFLG");
				sbSQL.append(", VANKBN=RE.VANKBN");
				sbSQL.append(", UNYOKBN=RE.UNYOKBN");
				sbSQL.append(", DENPKBN=RE.DENPKBN");
				sbSQL.append(", SHUHKBN=RE.SHUHKBN");
				sbSQL.append(", PICKDKBN=RE.PICKDKBN");
				sbSQL.append(", PICKLKBN=RE.PICKLKBN");
				sbSQL.append(", WAPNKBN=RE.WAPNKBN");
				sbSQL.append(", IDENPKBN=RE.IDENPKBN");
				sbSQL.append(", KAKOSJKBN=RE.KAKOSJKBN");
				sbSQL.append(", RYUTSUKBN=RE.RYUTSUKBN");
				sbSQL.append(", ZDENPKBN=RE.ZDENPKBN");
				sbSQL.append(", ZSHUHKBN=RE.ZSHUHKBN");
				sbSQL.append(", ZPICKDKBN=RE.ZPICKDKBN");
				sbSQL.append(", ZPICKLKBN=RE.ZPICKLKBN");
				sbSQL.append(", RSIRCD=RE.RSIRCD");
				sbSQL.append(", YKNSHKBN=RE.YKNSHKBN");
				sbSQL.append(", YDENPKBN=RE.YDENPKBN");
				sbSQL.append(", DSHUHKBN=RE.DSHUHKBN");
				sbSQL.append(", SENDFLG=RE.SENDFLG");
				sbSQL.append(", OPERATOR=RE.OPERATOR");
				//sbSQL.append(", ADDDT=RE.ADDDT");
				sbSQL.append(", UPDDT=RE.UPDDT");
				sbSQL.append(" when not matched then insert (");
				sbSQL.append(" SIRCD");
				sbSQL.append(", HSPTN");
				sbSQL.append(", TENGPCD");
				sbSQL.append(", TENDENFLG");
				sbSQL.append(", VANKBN");
				sbSQL.append(", UNYOKBN");
				sbSQL.append(", DENPKBN");
				sbSQL.append(", SHUHKBN");
				sbSQL.append(", PICKDKBN");
				sbSQL.append(", PICKLKBN");
				sbSQL.append(", WAPNKBN");
				sbSQL.append(", IDENPKBN");
				sbSQL.append(", KAKOSJKBN");
				sbSQL.append(", RYUTSUKBN");
				sbSQL.append(", ZDENPKBN");
				sbSQL.append(", ZSHUHKBN");
				sbSQL.append(", ZPICKDKBN");
				sbSQL.append(", ZPICKLKBN");
				sbSQL.append(", RSIRCD");
				sbSQL.append(", YKNSHKBN");
				sbSQL.append(", YDENPKBN");
				sbSQL.append(", DSHUHKBN");
				sbSQL.append(", SENDFLG");
				sbSQL.append(", OPERATOR");
				sbSQL.append(", ADDDT");
				sbSQL.append(", UPDDT");
				sbSQL.append(") values (");
				sbSQL.append(" RE.SIRCD");
				sbSQL.append(", RE.HSPTN");
				sbSQL.append(", RE.TENGPCD");
				sbSQL.append(", RE.TENDENFLG");
				sbSQL.append(", RE.VANKBN");
				sbSQL.append(", RE.UNYOKBN");
				sbSQL.append(", RE.DENPKBN");
				sbSQL.append(", RE.SHUHKBN");
				sbSQL.append(", RE.PICKDKBN");
				sbSQL.append(", RE.PICKLKBN");
				sbSQL.append(", RE.WAPNKBN");
				sbSQL.append(", RE.IDENPKBN");
				sbSQL.append(", RE.KAKOSJKBN");
				sbSQL.append(", RE.RYUTSUKBN");
				sbSQL.append(", RE.ZDENPKBN");
				sbSQL.append(", RE.ZSHUHKBN");
				sbSQL.append(", RE.ZPICKDKBN");
				sbSQL.append(", RE.ZPICKLKBN");
				sbSQL.append(", RE.RSIRCD");
				sbSQL.append(", RE.YKNSHKBN");
				sbSQL.append(", RE.YDENPKBN");
				sbSQL.append(", RE.DSHUHKBN");
				sbSQL.append(", RE.SENDFLG");
				sbSQL.append(", RE.OPERATOR");
				sbSQL.append(", RE.ADDDT");
				sbSQL.append(", RE.UPDDT");
				sbSQL.append(")");

				if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("エリア別配送パターン仕入先マスタ");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		return sbSQL.toString();
	}

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {

		// パラメータ確認
		JSONArray dataArray		 = JSONArray.fromObject(map.get("DATA"));			// 対象情報

		JSONObject option = new JSONObject();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		// パラメータ確認
		// 必須チェック
		if (dataArray.isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);
		// 仕入先マスタINSERT/UPDATE処理
		this.createSqlSIR(data, map, userInfo);

		ArrayList<Integer> countList  = new ArrayList<Integer>();
		if(sqlList.size() > 0){
			countList =  super.executeSQLs(sqlList, prmList);
		}

		if(StringUtils.isEmpty(getMessage())){
			int count = 0;
			for (int i = 0; i < countList.size(); i++) {
				count += countList.get(i);
				if (DefineReport.ID_DEBUG_MODE) System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{lblList.get(i), Integer.toString(countList.get(i))}));
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
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報

		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		// 更新情報
		String values = "";
		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			//szRTPattern = data.optString("F1");								// リードタイムターン
		}
		values = StringUtils.removeStart(values, ",");

		return msgObj;
	}

	/**
	 * 配送パターンマスタ仕入先DELETE処理
	 *
	 * @param dataArray
	 * @param map
	 * @param userInfo
	 */
	public String createDeleteSqlHsptnSir(HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows = "";											// 更新データ

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String sircd	 = map.get("SIRCD");							// 仕入先コード

		StringBuffer sbSQL;

		sbSQL = new StringBuffer();
		sbSQL.append("delete from INAMS.MSTHSPTNSIR where SIRCD = ?");
		prmData.add(sircd);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("配送パターンマスタ仕入先");

		return sbSQL.toString();
	}

	/**
	 * エリア別配送パターンマスタ仕入先DELETE処理
	 *
	 * @param dataArray
	 * @param map
	 * @param userInfo
	 */
	public String createDeleteSqlEhsptnSir(HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows = "";											// 更新データ

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String sircd	 = map.get("SIRCD");							// 仕入先コード

		StringBuffer sbSQL;

		sbSQL = new StringBuffer();
		sbSQL.append("delete from INAMS.MSTAREAHSPTNSIR where SIRCD = ?");
		prmData.add(sircd);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("エリア別配送パターンマスタ仕入先");

		return sbSQL.toString();
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// パラメータ確認
		JSONArray dataArray				 = JSONArray.fromObject(map.get("DATA"));				// 更新情報
		JSONArray dataArray_HSPTN_DEL	 = JSONArray.fromObject(map.get("DATA_HSPTN_DEL"));		// 更新情報

		ArrayList<String> paramData		 = new ArrayList<String>();
		String sqlcommand				 = "";

		String sircd	 = map.get("SIRCD");							// 仕入先コード

		JSONArray msg = new JSONArray();
		MessageUtility	mu	 = new MessageUtility();

		// 入力値を取得
		JSONObject data = dataArray.getJSONObject(0);

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		String sendBtnid	= map.get("SENDBTNID");			// 呼出しボタン

		// 新規登録重複チェック
		if(StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			if(dataArray.size() > 0){
				data = dataArray.getJSONObject(0);

				paramData	 = new ArrayList<String>();
				paramData.add(data.getString("F1"));
				sqlcommand = "select COUNT(SIRCD) as value from INAMS.MSTSIR where UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal()+" and SIRCD = ? ";

				@SuppressWarnings("static-access")
				JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
					JSONObject o = mu.getDbMessageObj("E00004", "仕入先コード");
					msg.add(o);
					return msg;
				}
			}
		}
		return msg;
	}
}
