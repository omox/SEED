package servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import common.CmnDate;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import dao.ReportBW005Dao;
import dao.ReportSO003Dao;
import dao.ReportTG017Dao;
import dao.ReportTG017Dao.FtpFileInfo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * ファイル出力→FTP送信用クラス
 */
public class FileWriterForFtp extends HttpServlet {

	private static final int		PORT	= 21;								// 制御ポート
	private static final String		HOST	= "172.16.2.139";					// FTPサーバ
	private static final String		USER	= "administrator";					// ユーザ
	private static final String		PASS	= "Passw0rd";						// パスワード
	private static final String		DIR		= "/home/edipack/file/receive/";	// 初期ディレクトリ
	private static final String[]	ERRCD	= {	 "202" // コマンドは実装されていない。SITEコマンドでOSコマンドが適切でない場合など
												,"421" // サービスを提供できない。コントロールコネクションを終了する。サーバのシャットダウン時など
												,"425" // データコネクションをオープンできない
												,"426" // 何らかの原因により、コネクションをクローズし、データ転送も中止した
												,"450" // 要求されたリクエストはアクセス権限やファイルシステムの理由で実行できない
												,"451" // ローカルエラーのため処理を中止した
												,"452" // ディスク容量の問題で実行できない
												,"500" // コマンドの文法エラー
												,"501" // 引数やパラメータの文法エラー
												,"502" // コマンドは未実装である
												,"503" // コマンドを用いる順番が間違っている
												,"504" // 引数やパラメータが未実装
												,"530" // ユーザーはログインできなかった
												,"532" // ファイル送信には、ACCTコマンドで課金情報を確認しなくてはならない
												,"550" // 要求されたリクエストはアクセス権限やファイルシステムの理由で実行できない
												,"551" // ページ構造のタイプの問題で実行できない
												,"552" // ディスク容量の問題で実行できない
												,"553" // ファイル名が間違っているため実行できない
	};	// FTPレスポンスコードエラー一覧

	String addr		= "";	// クライアントIP
	String fileName	= "";	// ファイル名
	String seq		= "";	// FTPログ用シーケンス
	String user		= "";	// ユーザー情報
	String userid	= "";	// ユーザーID
	String status	= "0";	// FTP接続結果
	String ftpCode	= "";	// FTPリターンコード
	String ftpCom	= "";	// FTPリターンコメント

    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileWriterForFtp() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 文字変換コード設定【重要】
		request.setCharacterEncoding("UTF-8");

		// クライアントIPアドレス取得
		addr = request.getRemoteAddr();

		// JSON 戻り値格納
		JSONArray json = new JSONArray();

        try {
        	// パラメータ一覧【確認】
			HashMap<String,String> maps = new HashMap<String,String>();
			Enumeration<String> enums = request.getParameterNames();
			while( enums.hasMoreElements() ) {
				String name = enums.nextElement();
				//System.out.println(name + "=" + request.getParameter( name ));
				maps.put(name, request.getParameter( name ));
			}
			user = maps.get("user") == null ? "0" : maps.get("user");
			userid = maps.get("userid") == null ? "0" : maps.get("userid");
			String outpage	= maps.get(DefineReport.ID_PARAM_PAGE)==null	? "" : maps.get(DefineReport.ID_PARAM_PAGE);
			String outjson	= maps.get(DefineReport.ID_PARAM_JSON)==null	? "" : maps.get(DefineReport.ID_PARAM_JSON);

			// 戻り値
			response.setContentType("text/html;charset=UTF-8");

			// JSONパラメータの解析
			JSONArray map = new JSONArray();
			if (!"".equals(outjson)) {
				map = (JSONArray) JSONSerializer.toJSON( outjson );
			}

			JSONObject obj = (JSONObject)map.get(0);

			// データレコードの種類数
			fileName = obj.optString("FILE");		// ファイル名
			int dReqKind = obj.optInt("DREQKIND");	// データレコード数
			int reqLen = obj.optInt("REQLEN");		// 1レコードのbyte数

			File file = new File(fileName);

			// ファイル作成
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw = writeHead(outpage,pw,reqLen);
			for (int i = 0; i < dReqKind; i++) {
				pw = writeData(outpage,pw,obj,i+1,reqLen);
			}
			pw.close();

			// FTP送信
			obj = FtpSocket();

			file.delete();

			// JSON データのロード
			response.setContentType("text/html;charset=UTF-8");
			pw = response.getWriter();

			// 行データ格納
			json.add(obj);
			pw.print(json.toString());
			pw.close();

        } catch (Exception e) {
			e.printStackTrace();
        }
	}

	/**
	 * FTP送信用ファイルヘッダ書き込みクラス
	 *
	 * @param outpage
	 * @param pw
	 * @param reqLen
	 * @return pw
	 */
	public PrintWriter writeHead(String outpage, PrintWriter pw, int reqLen) {

		int blank		= reqLen-46;			// 余白用ブランク
		int fileBlnak	= 8-fileName.length();	// ファイル名後ろスペース埋め用変数
		int userBlank	= 20-userid.length();	// オペレータ後ろスペース埋め用変数

		CmnDate cdate = new CmnDate();
		pw.print("H1");
		pw.print(getPrintStr(fileName,fileBlnak));
		pw.print(getPrintStr(userid,userBlank));
		pw.print(cdate.getToday());
		pw.print(cdate.getNowTime());
		pw.println(getPrintStr("",blank));
		return pw;
	}

	public String getPrintStr(String str, int len) {
		if (len >= 1) {
			return str+String.format("%" + len + "s", " ");
		} else {
			return str;
		}
	}

	/**
	 * FTP送信用ファイル実データ書き込みクラス
	 *
	 * @param outpage
	 * @param pw
	 * @param obj
	 * @param reqLen
	 * @return pw
	 */
	public PrintWriter writeData(String outpage, PrintWriter pw, JSONObject obj, int num, int reqLen) throws IOException {

		// JSON 戻り値格納
		JSONArray json = new JSONArray();

		// SQLベース
		ItemList il = new ItemList();

		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();

		// SQL構文
		String sqlcommand = "";

		// 最後尾の余白の数を求める(初期値は1レコードのbyte数-改行コード分2byte)
		int blank = reqLen-2;

		// キーに特殊な設定がある場合はここにセット
		String key = "";

		try {

			if (DefineReport.ID_PAGE_BM003.equals(outpage)) {

				// 検索条件の設定
				String moyskbn	= obj.optString("MOYSKBN");
				String moysstdt	= obj.optString("MOYSSTDT");
				String moysrban	= obj.optString("MOYSRBAN");
				paramData.add(moyskbn);
				paramData.add(moysstdt);
				paramData.add(moysrban);

				if (num == 1) {
					sqlcommand = DefineReport.ID_SQL_TOKBM_D1;
				} else if (num == 3) {
					sqlcommand = DefineReport.ID_SQL_TOKBM_D3;
				} else {
					sqlcommand = DefineReport.ID_SQL_TOKBM_D2;
					json = il.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);

					String changeReq = "";
					String printReq = "";
					String zero = "000";
					int addTen = 0;
					int delTen = 0;

					int cnt = 1;
					for (int i = 0; i < json.size(); i++) {
						JSONObject data = json.getJSONObject(i);

						// レコードのキー項目を取得
						if (StringUtils.isEmpty(changeReq)) {
							cnt = 1;
							while (data.containsKey("D"+num+"_"+cnt)) {
								changeReq += data.getString("D"+num+"_"+cnt);
								cnt++;
							}
						}

						// キー項目と現在のレコードの値が一致するか
						if (changeReq.substring(2,16).equals(
								data.getString("D"+num+"_2")+data.getString("D"+num+"_3")+data.getString("D"+num+"_4")+data.getString("D"+num+"_5"))
						) {

							// 対象店か除外店か判定
							if (data.getString("D"+num+"_"+cnt+"_2").equals("1")) {
								if (StringUtils.isEmpty(printReq)) {
									printReq = changeReq + data.getString("D"+num+"_"+cnt+"_1");
								} else {
									printReq += data.getString("D"+num+"_"+cnt+"_1");
								}
								addTen++;
							} else if (data.getString("D"+num+"_"+cnt+"_2").equals("0")) {

								for (int j = addTen; j < 10; j++) {

									if (j == 0) {
										printReq = changeReq + zero;
									} else {
										printReq += zero;
									}
									addTen++;
								}

								printReq += data.getString("D"+num+"_"+cnt+"_1");
								delTen++;
							}
						} else {

							for (int j = addTen; j < 10; j++) {

								if (j == 0) {
									printReq = changeReq + zero;
								} else {
									printReq += zero;
								}
								addTen++;
							}

							for (int j = delTen; j < 10; j++) {
								printReq += zero;
								delTen++;
							}

							pw.println(printReq);

							cnt = 1;
							changeReq = "";
							while (data.containsKey("D"+num+"_"+cnt)) {
								changeReq += data.getString("D"+num+"_"+cnt);
								cnt++;
							}

							printReq = "";
							addTen = 0;
							delTen = 0;
						}


						if (i+1 == json.size()) {

							// 対象店か除外店か判定
							if (data.getString("D"+num+"_"+cnt+"_2").equals("1")) {
								if (StringUtils.isEmpty(printReq)) {
									printReq = changeReq + data.getString("D"+num+"_"+cnt+"_1");
								} else {
									printReq += data.getString("D"+num+"_"+cnt+"_1");
								}
								addTen++;
							} else if (data.getString("D"+num+"_"+cnt+"_2").equals("0")) {

								for (int j = addTen; j < 10; j++) {

									if (j == 0) {
										printReq = changeReq + zero;
									} else {
										printReq += zero;
									}
									addTen++;
								}

								printReq += data.getString("D"+num+"_"+cnt+"_1");
								delTen++;
							}

							for (int j = addTen; j < 10; j++) {

								if (j == 0) {
									printReq = changeReq + zero;
								} else {
									printReq += zero;
								}
								addTen++;
							}

							for (int j = delTen; j < 10; j++) {
								printReq += zero;
								delTen++;
							}

							pw.println(printReq);
							changeReq = "";
							printReq = "";
						}
					}

					// 下の処理でSQLが実行されないようにクリア
					sqlcommand = "";
					paramData = new ArrayList<String>();
				}
			}else if (DefineReport.ID_PAGE_TG017.equals(outpage)) {

				String btnId	= obj.optString("BTN");
				String moyskbn	= obj.optString("MOYSKBN");
				String moysstdt	= obj.optString("MOYSSTDT");
				String moysrban	= obj.optString("MOYSRBAN");

				if (!StringUtils.isEmpty(btnId) && (btnId.equals(DefineReport.Button.CSV.getObj() + "1") || btnId.equals(DefineReport.Button.CSV.getObj() + "2"))) {

					ReportTG017Dao dao = new ReportTG017Dao(Defines.STR_JNDI_DS);
					JSONArray dataArray = JSONArray.fromObject(obj.optString("DATA"));	// 選択情報
					boolean isTOKTG = dao.isTOKTG(moyskbn, moysrban);
					key = "REC";

					// ファイル情報
					FtpFileInfo fio = null;
					if(StringUtils.equals(btnId,  DefineReport.Button.CSV.getObj() + "1")){
						if(isTOKTG){
							fio = FtpFileInfo.CSV1_TG;
						}else{
							fio = FtpFileInfo.CSV1_SP;
						}
					}else if(StringUtils.equals(btnId,  DefineReport.Button.CSV.getObj() + "2")){
						fio = FtpFileInfo.CSV2;
					}

					sqlcommand = new ReportTG017Dao(Defines.STR_JNDI_DS).createCommandForDl(user, dataArray, fio, false);
				} else {
					// 検索条件の設定
					String bmncd	= obj.optString("BMNCD");
					paramData.add(bmncd);
					paramData.add(moyskbn);
					paramData.add(moysstdt);
					paramData.add(moysrban);

					sqlcommand = new ReportTG017Dao(Defines.STR_JNDI_DS).createCommandCheckList(user, obj);
				}

			}else if (DefineReport.ID_PAGE_SO001.equals(outpage) || DefineReport.ID_PAGE_SO003.equals(outpage)) {

				// 検索条件の設定
				String moyskbn	= obj.optString("MOYSKBN");
				String moysstdt	= obj.optString("MOYSSTDT");
				String moysrban	= obj.optString("MOYSRBAN");
				String bmncd	= obj.optString("BMNCD");
				paramData.add(bmncd);
				paramData.add(moyskbn);
				paramData.add(moysstdt);
				paramData.add(moysrban);

				sqlcommand = new ReportSO003Dao(Defines.STR_JNDI_DS).createCommandFTP(user, obj, outpage);
			}else if (DefineReport.ID_PAGE_BW005.equals(outpage) || DefineReport.ID_PAGE_BW005.equals(outpage) ||DefineReport.ID_PAGE_BW002.equals(outpage) || DefineReport.ID_PAGE_BW002.equals(outpage)) {

				// 検索条件の設定
				String bsstdt	= obj.optString("HBSSTDT");
				String bmncd	= obj.optString("BMNCD");
				String waribiki	= obj.optString("WARIBIKI");
				String seiki	= obj.optString("SEIKI");
				String dummy	= obj.optString("DUMMY");
				paramData.add(bsstdt);
				paramData.add(bmncd);
				paramData.add(waribiki);
				paramData.add(seiki);
				paramData.add(dummy);

				sqlcommand = new ReportBW005Dao(Defines.STR_JNDI_DS).createCommandFTP(user, obj, outpage);
			}

			if (!StringUtils.isEmpty(sqlcommand)) {
				json = il.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);

				for (int i = 0; i < json.size(); i++) {
					JSONObject data = json.getJSONObject(i);
					String req = "";
					int cnt = 1;

					if (StringUtils.isEmpty(key)) {
						while (data.containsKey("D"+num+"_"+cnt)) {
							req += data.getString("D"+num+"_"+cnt);
							cnt++;
						}
					} else {
						if (data.containsKey(key)) {
							req += data.getString(key);
						}
					}

					// 結合文字列を書き込み
					pw.print(req);

					// 全角・半角判断
					blank -= getBytes(req);
					pw.println(getPrintStr("",blank));
					blank = reqLen-2;
				}
			}
			return pw;
		} catch (Exception e) {
		    e.printStackTrace();
		    return pw;
		}
	}

	/**
	 * FTP接続
	 *
	 */
	@SuppressWarnings("resource")
	public JSONObject FtpSocket() {

		try {
			// 開始ログ
			ftpLog(status,ftpCode,ftpCom);

			// 接続
			Socket			socket			= new Socket(HOST, PORT);
			byte[]			localHostAdr	= socket.getLocalAddress().getAddress();
			PrintWriter		outWriter		= new PrintWriter(socket.getOutputStream());
			BufferedReader	inReader		= new BufferedReader(new InputStreamReader(socket.getInputStream()));

			ftpCom = inReader.readLine();
			if (!isReady()) {
				// 結果をセット
				return setFtp();
			}

			// ユーザー認証
			outWriter.println("USER " + USER);
			outWriter.flush();
			ftpCom = inReader.readLine();
			if (!isReady()) {
				// 結果をセット
				return setFtp();
			}
			outWriter.println("PASS " + PASS);
			outWriter.flush();
			ftpCom = inReader.readLine();
			if (!isReady()) {
				// 結果をセット
				return setFtp();
			}

			// ディレクトリ移動
			outWriter.println("CWD " + DIR);
			outWriter.flush();

			ftpCom = inReader.readLine();
			if (!isReady()) {
				// 結果をセット
				return setFtp();
			}

			// バイナリモードに設定(アスキーモードは'TYPE A')
			outWriter.println("TYPE I");
			outWriter.flush();
			ftpCom = inReader.readLine();
			if (!isReady()) {
				// 結果をセット
				return setFtp();
			}

			// アップロード
			FileInputStream	fis			= new FileInputStream(fileName);
			Socket			dataSocket	= connection("STOR " + fileName, outWriter, localHostAdr);
			OutputStream	outstr		= dataSocket.getOutputStream();
			int l;
			byte[] buff = new byte[1024];
			while ((l = fis.read(buff)) > 0) {
			    outstr.write(buff, 0, l);
			}
			dataSocket.close();
			fis.close();

			ftpCom = inReader.readLine();
			if (!isReady()) {
				// 結果をセット
				return setFtp();
			}

			// 終了ログ
			ftpLog(status,ftpCode,ftpCom);

			// 切断／クローズ
			outWriter.close();
			inReader.close();
			socket.close();

			// 結果をセット
			return setFtp();
		}catch (Exception e) {
		    e.printStackTrace();
		    status = "-1";
		    ftpCom = "Exception:致命的エラーの発生";

		    // 終了ログ
			ftpLog("1",ftpCode,ftpCom);

			// 結果をセット
			return setFtp();
		}
	}

	/**
	 * 送受信用ソケット取得
	 *
	 * @param ctrlcmd
	 * @param outWriter
	 * @param localHostAdr
	 * @return dataSocket
	 */
	public static Socket connection(String ctrlcmd, PrintWriter outWriter, byte[] localHostAdr) throws IOException,UnknownHostException {

		String cmd = "PORT ";
		ServerSocket serverDataSocket = new ServerSocket(0,1);
		for (int i = 0; i < 4; i++) {
			cmd = cmd + (localHostAdr[i] & 0xff) + ",";
		}

		cmd = cmd + (((serverDataSocket.getLocalPort())/256) & 0xff) + "," + (serverDataSocket.getLocalPort() & 0xff);

		outWriter.println(cmd);
		outWriter.flush();
		outWriter.println(ctrlcmd);
		outWriter.flush();

		Socket dataSocket = serverDataSocket.accept();
		serverDataSocket.close();
		return dataSocket;
	}

	/**
	 * FTP送信ログ
	 *
	 * @param status 0:OK 1:NG
	 * @param errCode
	 * @param errCom
	 */
	public void ftpLog (String status, String errCode, String errCom) {

		// SQL構文
		String				sqlcommand	= "";
		ArrayList<String>	paramData	= new ArrayList<String>();
		ItemList			iL			= new ItemList();

		// 開始ログ
		if (StringUtils.isEmpty(seq)) {

			// シーケンス取得
			seq = getFtp_SEQ();
			sqlcommand = DefineReport.ID_SQL_INSERT_FTPLOGS;
			paramData.add(seq);
			paramData.add(addr);
			paramData.add(HOST);
			paramData.add(fileName);
			paramData.add(user);

		// 終了ログ
		} else {

			sqlcommand = DefineReport.ID_SQL_UPDATE_FTPLOGS;
			if (status.equals("0")) {
				paramData.add("OK");
			} else {
				paramData.add("NG");
			}
			paramData.add(errCode);
			paramData.add(errCom);
			paramData.add(seq);
		}

		// SQL構文の実行（コマンド指定あり）
		if (!"".equals(sqlcommand)) {
			if (DefineReport.ID_DEBUG_MODE) System.out.println(sqlcommand);

			try {
				iL.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * SEQ情報取得処理(No)
	 *
	 * @throws Exception
	 */
	public String getFtp_SEQ() {
		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ008";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("1");
		}
		return value;
	}

	// リターンコードの確認
	public boolean isReady()
	{
		for (int i = 0; i < ERRCD.length; i++) {

			ftpCode = ftpCom.substring(0,3);

			if (ERRCD[i].equals(ftpCode)) {
				// 終了ログ
				ftpLog("1",ftpCode,ftpCom);
				status = "-1";
				return false;
			}
		}
		return true;
	}

	// 結果セット
	public JSONObject setFtp() {

		JSONObject obj = new JSONObject();

		obj.put("status",status);
		obj.put("code",ftpCode);
		obj.put("com","<br>["+ftpCom+"]<br><br>");

		return obj;
	}

	/**
	 * バイト数返却
	 * 全角文字はUTF-8では一文字3byteとなるため
	 * 一文字を2byteとして数えたバイト数を返却する
	 *
	 * @param val
	 * @return bytes
	 */
	public int getBytes(String val) {

		String[] strArray = val.split("");
		int bytes = 0;

		for (int i = 0; i < strArray.length; i++) {

			int strByte = strArray[i].getBytes().length;

			// 一文字3byteのものは2byteで計算
			if (strByte == 3) {
				bytes += 2;
			} else {
				bytes += strByte;
			}
		}
		return bytes;
	}
}
