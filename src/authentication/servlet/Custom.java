package authentication.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import authentication.bean.User;
import authentication.connection.DBConnection;
import authentication.defines.Consts;

/**
 * Servlet implementation class Custom
 */
public class Custom extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Custom() {
        super();
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("service");
		try {

			// 文字変換コード設定【重要】
			request.setCharacterEncoding("UTF-8");

			// パラメータ一覧【確認】
			HashMap<String,String> maps = new HashMap<String,String>();
			Enumeration<String> enums = request.getParameterNames();
			while( enums.hasMoreElements() ) {
				String name = enums.nextElement();
				System.out.println(name + "=" + request.getParameter( name ));
				maps.put(name, request.getParameter( name ));
			}
			// セッションの取得
			HttpSession session = request.getSession();

			// ユーザ情報の取得
			User loginusr = (User)session.getAttribute(Consts.STR_SES_LOGINUSER);

			// SQL構文
			String sqlcommand = "";

			// 戻り
			String Data="";

			// アクション指定
			if ("GET".equals(maps.get("ACTION"))) {

				// データのロード
				Data = (String)session.getAttribute("MENUDATA");
				if ( (Data == null) || (Data.length()==0) ) {

					// DB情報取得

					// SQL構文
					sqlcommand = "SELECT SNAPSHOT FROM KEYSYS.SYS_SNAPSHOT_MENU WHERE CD_USER=?";

					// 配列準備
					ArrayList<String> paramData = new ArrayList<String>();
					paramData.add(0, String.valueOf(loginusr.getCD_user()));

					// SQL実行
					Data = querySQL(sqlcommand, paramData);

				}

			} else if ("STORE".equals(maps.get("ACTION"))) {

				// 削除

				// SQL構文
				sqlcommand = "DELETE FROM KEYSYS.SYS_SNAPSHOT_MENU WHERE CD_USER=?";

				// 配列準備
				ArrayList<String> paramData = new ArrayList<String>();
				paramData.add(0, String.valueOf(loginusr.getCD_user()));

				// SQL実行
				updateSQL(sqlcommand, paramData);

				// 追加

				// SQL構文
				sqlcommand = "INSERT INTO KEYSYS.SYS_SNAPSHOT_MENU(CD_USER, SNAPSHOT, NM_CREATE, NM_UPDATE) VALUES(?, ?, ?, ?)";

				// 配列準備
				paramData.add(1, (String)maps.get("MENUDATA"));
				paramData.add(2, loginusr.getId());
				paramData.add(3, loginusr.getId());

				// SQL実行
				updateSQL(sqlcommand, paramData);


				// セッション保持
				session.setAttribute("MENUDATA", (String)maps.get("MENUDATA"));
				Data = "STORE";

			} else if ("DELETE".equals(maps.get("ACTION"))) {

				// 削除

				// SQL構文
				sqlcommand = "DELETE FROM KEYSYS.SYS_SNAPSHOT_MENU WHERE CD_USER=?";

				// 配列準備
				ArrayList<String> paramData = new ArrayList<String>();
				paramData.add(0, String.valueOf(loginusr.getCD_user()));

				// SQL実行
				updateSQL(sqlcommand, paramData);

				// セッションクリア
				session.removeAttribute("MENUDATA");

			} else {

				// データ・ストア（仮）
				session.setAttribute("MENUDATA", (String)maps.get("MENUDATA"));
				Data = "SET";

			}

			// レスポンス
			response.setContentType("text/html;charset=UTF-8");
			PrintWriter pw = response.getWriter();
			pw.print(Data.toString());
			pw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("doPost");
	}

	/**
	 * SQL実行
	 * @param sqlCommand
	 * @param paramData
	 * @throws Exception
	 */
	private void updateSQL(String sqlCommand, ArrayList<String> paramData) throws Exception {

		// 検索
		Connection con = null; // コネクション
		PreparedStatement stmt = null;	// ステートメント

		try {

			// コネクションの取得
			con = DBConnection.getConnection();

			// 実行SQL設定
			stmt = con.prepareStatement(sqlCommand);

			// 分類コードパラメータ判断
			for (int i=0; i < paramData.size(); i++) {
				System.out.println((String)paramData.get(i));
				stmt.setString((i+1), (String)paramData.get(i));
			}

			// SQL実行
			int updateCount = stmt.executeUpdate();
            System.out.println("updateSQL: updateCount: " + updateCount);

			con.commit();

		} catch (Exception e) {
			/* 接続解除 */
			if (con != null){
				try {
					con.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();

		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        }
	}

	/**
	 * SQL実行
	 * @param sqlCommand
	 * @param paramData
	 * @throws Exception
	 */
	private String querySQL(String sqlCommand, ArrayList<String> paramData) throws Exception {

		// 検索
		Connection con = null; // コネクション
		PreparedStatement stmt = null;	// ステートメント

		String data="";
		try {

			// コネクションの取得
			con = DBConnection.getConnection();

			// 実行SQL設定
			stmt = con.prepareStatement(sqlCommand);

			// 分類コードパラメータ判断
			for (int i=0; i < paramData.size(); i++) {
				stmt.setString((i+1), (String)paramData.get(i));
			}

			// SQL実行
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				data = rs.getString(1);
			}
			con.commit();

		} catch (Exception e) {
			/* 接続解除 */
			if (con != null){
				try {
					con.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();

		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        }
		return data;
	}

}
