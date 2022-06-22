<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="authentication.bean.Side" %>
<%@ page import="authentication.bean.Report" %>
<%@ page import="authentication.bean.User" %>
<%@ page import="authentication.defines.*" %>
<div id="tabs">
<%
	// ���[�U���擾��CD_AUTH�̒l��ǉ��inull�̏ꍇ�́A�^�u�\���j
	User lusr = (User)session.getAttribute(Consts.STR_SES_LOGINUSER);
	String initParamName = "";
	if (lusr.getCd_auth_() != null) {
		initParamName += lusr.getCd_auth_();
	}

	// ���j���[���X�g�ƕ��ޔԍ�
	ArrayList menu = (ArrayList)session.getAttribute(Consts.STR_SES_REPSIDE);
	int repside = Integer.parseInt(request.getParameter(Form.REPORT_SIDE).toString().trim());

	// ���j���[���ޔԍ��ƈ�v���郌�|�[�g���X�g�擾
	ArrayList report = null;
	for(int i = 0; i < menu.size(); i++){
		Side m = (Side)menu.get(i);
		if (m.getSide() == repside) {
			report = m.getReport();
		}
	}

	// ���s�̃��|�[�g�ԍ��擾
	String current = ((String)request.getParameter(Form.REPORT_NO));
	String currentno = current!=null?current:"0";

	// �\���`��
	String repview = ((String)request.getParameter(Form.REPORT_NAMETYPE));
	String no = ((String)request.getParameter(Form.REPORT_SIDE_ARRAY));
	String HiddenReport = (request.getParameter("HiddenReport") == null) ? "" :((String)request.getParameter("HiddenReport"));

	// ���|�[�g����
	String reportName = "";
	// �p�����[�^�[��
	String parameterURL = "";
	String href="";
	int tabSelected=0;

	String hostname = request.getRequestURL().toString();
	String context = request.getContextPath();
	hostname = "";//hostname.substring(0, hostname.indexOf(context)); 2015/04/07 comment out by omoto

	// �\������
	for(int i = 0; i< report.size(); i++){
		Report rep = (Report)report.get(i);

		if (rep.getReport_boolean()) {

			// �\�����̂̐ݒ�
			if (repview == null) {
				reportName = rep.getReport_shortname();
			} else {
				reportName = rep.getReport_name();
			}

			parameterURL = hostname + context + "/Servlet/Report.do?" +
				Form.REPORT_SIDE_ARRAY + "="  + no + "&" +
				Form.REPORT_NO_ARRAY + "=" + Integer.toString(i) + "&" +
				Form.REPORT_SIDE + "=" + rep.getReport_side() + "&" +
				Form.REPORT_NO + "=" + rep.getReport_no();

			// �J�X�^���v���p�e�B�̊m�F
			String[] inclurepnames = rep.getReport_custom().toString().split(",");

			String parentReportName = rep.getReport_custom();
			if (inclurepnames.length >= 2) {
				// �ďo���i�߂��j�̎w��
				parentReportName = inclurepnames[1];
			}

			// ��\���^�u�ݒ�
			String inclurepname = (String)request.getSession().getAttribute(Defines.ID_REQUEST_JSP_REPORT);
			String classHide = "";
			if (inclurepnames.length >= 3) {
				// �\��/��\��(hide)�̎w��
				classHide = inclurepnames[2];
			}
			if(!rep.getEnableMenu()){
				// �\��/��\��(hide)�̎w��
				classHide = "hide";
			}

			if (currentno.equals(Integer.toString(rep.getReport_no()))) {
				if ("".equals(classHide)) {
					href += ","+parameterURL;
				} else {
					href += ","+parameterURL;	// (2013/01/24 �ύX��������Ȃ��ꍇ�͍폜�\��) href += ",#";

					reportName = "�@�@";
				}
				classHide = "";// �J�����g�^�u�͏펞�\��
				// href �L��
				parameterURL = hostname + context + "/jsp/Out_Report.jsp";
				// �J�����g�^�u�ԍ�
				tabSelected = i ;
			} else {
				href += ","+parameterURL;
				// href ����
				parameterURL = "";
			}

			// ���j���[�\����(HiddenReport=1)�ɔ�\���w��̏ꍇ�́A�ǉ����Ȃ��B
			// �^�u�\����(HiddenReport=0)�́A��ɒǉ�����B
			// ���|�[�gON/OFF�ݒ�m�F
			if ((("".equals(classHide) && "1".equals(HiddenReport)) || ("0".equals(HiddenReport)))) {
%>
				<div id="parent_<%=parentReportName %>"
					title="<%=reportName %>"
					class="<%=classHide %>"
					href="<%=parameterURL %>"
					cache=false
					closable=false
					style="overflow:hidden">
				</div>
<%
			}
		}
	}
%>
</div>
<input type="hidden" id="tabSelected" name="tabSelected" value="<%=tabSelected %>"/>
<input type="hidden" id="tabContent" name="tabContent" value="<%=href %>"/>
<input type="hidden" id="tabView" name="tabView" value="<%=initParamName %>"/>
<script type="text/javascript" src="../js/jquery.report.tabs.js"></script>