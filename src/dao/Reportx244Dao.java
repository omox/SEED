package dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.CmnDate;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx244Dao extends ItemDao {

    /**
     * インスタンスを生成します。
     * @param source
     */
    public Reportx244Dao(String JNDIname) {
        super(JNDIname);
    }

    /**
     * 検索実行
     *
     * @return
     */
    @Override
    public boolean selectBy() {

        // 検索コマンド生成
        String command = createCommand();

        // 出力用検索条件生成
        outputQueryList();

        // 検索実行
        return super.selectBySQL(command);
    }

    /**
     * 検索実行
     *
     * @return
     */
    private String createCommand() {
        // ログインユーザー情報取得
        User userInfo = getUserInfo();
        if(userInfo==null){
            return "";
        }

        String btnId = getMap().get("BTN");         // 実行ボタン

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

        String szWhereCmd = this.getSqlWhere(paramData);

        // 一覧表情報
        // TODO:天気・気温情報今適当
        StringBuffer sbSQL = new StringBuffer();
        sbSQL.append(" select ");
        sbSQL.append(" trim(left(T1.SHNCD, 4) || '-' || SUBSTR(T1.SHNCD, 5)) as SHNCD");    // 商品コード
        sbSQL.append(" ,T1.SHOHINKN");              // 商品名
        sbSQL.append(" ,T1.SSIRCD || ' ' || T2.SIRKN as SSIRCD");           // 標準仕入先
        sbSQL.append(" ,T1.RG_GENKAAM");        // 原価
        sbSQL.append(" ,T1.RG_BAIKAAM");        // 本体売価
        sbSQL.append(" ,T1.RG_IRISU");          // 店入数
        sbSQL.append(" ,T1.INF_OPERATOR || ' ' || T3.NM_FAMILY || T3.NM_NAME as INF_OPERATOR");     // 更新情報_オペレータ
        sbSQL.append(" ,COALESCE(DATE_FORMAT(T1.INF_DATE, '%Y/%m/%d %H:%i'),'__/__/__')");          // 更新情報_更新日時

        sbSQL.append(" from INAAD.JNLSHN T1");
        sbSQL.append(" left outer join INAMS.MSTSIR T2 on T1.SSIRCD = T2.SIRCD");
        sbSQL.append(" left outer join KEYSYS.SYS_USERS T3 on T1.INF_OPERATOR = T3.USER_ID");
        sbSQL.append(" where " + szWhereCmd + " and COALESCE(T1.UPDKBN, 0) <> 1 ");

        sbSQL.append(" order by ");
        sbSQL.append("  T1.SHNCD ");
        sbSQL.append("  ,T1.INF_DATE DESC ");


        // オプション情報（タイトル）設定
        JSONObject option = new JSONObject();
        option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
        setOption(option);

        // DB検索用パラメータ設定
        setParamData(paramData);

        if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
        return sbSQL.toString();
    }


    private String getSqlWhere(ArrayList<String> paramData) {
        String szShncd  = getMap().get("SHNCD");    // 商品コード
        String szShohinkn = getMap().get("SHOHINKN");   // 商品名（漢字）

        JSONArray bumonArray    = JSONArray.fromObject(getMap().get("BUMON"));      // 部門
        JSONArray bumonAllArray = JSONArray.fromObject(getMap().get("BUMON_DATA")); // 全部門
        JSONArray daiBunArray   = JSONArray.fromObject(getMap().get("DAI_BUN"));    // 大分類
        JSONArray chuBunArray   = JSONArray.fromObject(getMap().get("CHU_BUN"));    // 中分類

        String daiBun = bumonArray.optString(0) + daiBunArray.optString(0);
        String chuBun = bumonArray.optString(0) + daiBunArray.optString(0) + chuBunArray.optString(0);

        String szSsircd     = getMap().get("SSIRCD");       // 仕入先コード
        String szMakercd    = getMap().get("MAKERCD");      // メーカーコード

        String szFromDate = getMap().get("FROM_DATE"); // 開始日
        String szToDate = getMap().get("TO_DATE"); // 終了日


        String btnId = getMap().get("BTN");         // 実行ボタン

        String szWhereCmd = "";

        // *** その他条件
        // 商品コード
        if (!StringUtils.isEmpty(szShncd)){
            szWhereCmd = " T1.SHNCD = '" + szShncd + "'";
        } else {
            // *** 分類条件
            // 部門
            if (bumonArray.optString(0).equals(DefineReport.Values.ALL.getVal())){
                szWhereCmd = " CAST(SUBSTR(T1.SHNCD,1,2) AS SIGNED) IN ("+StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(bumonAllArray.join(","),"\"0","\""),"\"",""),",")+")";
                szWhereCmd += " and T1.BMNCD IN ("+StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(bumonAllArray.join(","),"\"0","\""),"\"",""),",")+")";
            }else{
                szWhereCmd = " CAST(SUBSTR(T1.SHNCD,1,2) AS SIGNED) = "+StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(bumonArray.join(","),"\"0","\""),"\"",""),",");
                szWhereCmd += " and T1.BMNCD = "+StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(bumonArray.join(","),"\"0","\""),"\"",""),",");
            }
        }

        String szWhereBun = "";
        // 大分類設定
        if (!daiBunArray.optString(0).equals(DefineReport.Values.ALL.getVal())){
            szWhereBun = " and RIGHT('0'||T1.BMNCD,2)||RIGHT('0'||T1.DAICD,2) = "+ daiBun;
        }
        // 中分類指定
        if (!chuBunArray.optString(0).equals(DefineReport.Values.ALL.getVal())){
            szWhereBun = " and RIGHT('0'||T1.BMNCD,2)||RIGHT('0'||T1.DAICD,2)||RIGHT('0'||T1.CHUCD,2) = "+ chuBun;
        }
        szWhereCmd += szWhereBun;

        // *** その他条件
        if (!StringUtils.isEmpty(szShohinkn)){
            szWhereCmd += " and T1.SHOHINKN like ?";
            paramData.add("%" + szShohinkn + "%");

        }
        if (!StringUtils.isEmpty(szSsircd)){
            szWhereCmd += " and T1.SSIRCD = " + szSsircd;
        }
        if (!StringUtils.isEmpty(szMakercd)){
            szWhereCmd += " and T1.MAKERCD = " + szMakercd;
        }

        if ((!StringUtils.isEmpty(szFromDate)) && (StringUtils.isEmpty(szToDate))) {
            String convdt = CmnDate.getConvInpDate(szFromDate);
            Calendar convdt_c = Calendar.getInstance();

            String y_f = convdt.substring(0, 4);
            String m_f = convdt.substring(4, 6);
            String d_f = convdt.substring(6);
            String NewFromDate = y_f + m_f + d_f;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

            try {
                Date date = dateFormat.parse(NewFromDate);
                convdt_c.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int convdt_y_f = Integer.parseInt(y_f);
            int convdt_m_f = Integer.parseInt(m_f) - 1;
            int convdt_d_f = Integer.parseInt(d_f);

            convdt_c.add(Calendar.MONTH, 1);

            int convdt_c_end = convdt_c.getActualMaximum(Calendar.DAY_OF_MONTH);
            convdt_c.clear();

            if(convdt_d_f > convdt_c_end){
                convdt_c.set(convdt_y_f, convdt_m_f, convdt_c_end);
            }else{
                convdt_c.set(convdt_y_f, convdt_m_f, convdt_d_f);
                convdt_c.add(Calendar.DAY_OF_MONTH, -1);
            }
            convdt_c.add(Calendar.MONTH, 1);
            Date NewToDate = convdt_c.getTime();
            String convdt_to = dateFormat.format(NewToDate);

            szWhereCmd += " and DATE_FORMAT(T1.INF_DATE, '%Y%m%d') <= '" + convdt_to + "'";
        }
        if ((StringUtils.isEmpty(szFromDate)) && (!StringUtils.isEmpty(szToDate))) {
            String convdt = CmnDate.getConvInpDate(szToDate);
            Calendar convdt_c = Calendar.getInstance();

            String y_t = convdt.substring(0, 4);
            String m_t = convdt.substring(4, 6);
            String d_t = convdt.substring(6);
            String NewToDate = y_t + m_t + d_t;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

            try {
                Date date = dateFormat.parse(NewToDate);
                convdt_c.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int convdt_y_t = Integer.parseInt(y_t);
            int convdt_m_t = Integer.parseInt(m_t) - 1;
            int convdt_d_t = Integer.parseInt(d_t);

            convdt_c.add(Calendar.MONTH, -1);
            int convdt_c_end = convdt_c.getActualMaximum(Calendar.DAY_OF_MONTH);
            convdt_c.clear();

            if(convdt_d_t > convdt_c_end){
                convdt_c.set(convdt_y_t, convdt_m_t, convdt_c_end);
            }else{
                convdt_c.set(convdt_y_t, convdt_m_t, convdt_d_t);
                convdt_c.add(Calendar.DAY_OF_MONTH, 1);
            }
            convdt_c.add(Calendar.MONTH, -1);

            Date NewFromDate = convdt_c.getTime();
            String convdt_from = dateFormat.format(NewFromDate);

            szWhereCmd += " and DATE_FORMAT(T1.INF_DATE, '%Y%m%d') >= '" + convdt_from + "'";
        }

        if (!StringUtils.isEmpty(szFromDate)){
            String convdt = CmnDate.getConvInpDate(szFromDate);
            szWhereCmd += " and DATE_FORMAT(T1.INF_DATE, '%Y%m%d') >= '" + convdt + "'";
        }
        if (!StringUtils.isEmpty(szToDate)){
            String convdt = CmnDate.getConvInpDate(szToDate);
            szWhereCmd += " and DATE_FORMAT(T1.INF_DATE, '%Y%m%d') <= '" + convdt + "'";
        }

        return szWhereCmd;
    }


    private void outputQueryList() {

        // 検索条件の加工クラス作成
        JsonArrayData jad = new JsonArrayData();
        jad.setJsonString(getJson());

        // 保存用 List (検索情報)作成
        setWhere(new ArrayList<List<String>>());
        List<String> cells = new ArrayList<String>();

        // タイトル名称
        cells.add("商品マスタ履歴管理");
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
