<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="authentication.bean.Side" %>
<%@ page import="authentication.bean.Report" %>
<%@ page import="authentication.defines.*" %>

<%
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

			if (currentno.equals(Integer.toString(rep.getReport_no()))) {

%>
				<li>
					<%=reportName %>
				</li>
<%
			}else{

				parameterURL = "../Servlet/Report.do?" +
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

				String classHide = "";
				if (inclurepnames.length >= 3) {
					// �\��/��\��(hide)�̎w��
					classHide = inclurepnames[2];
				}

				// ���j���[�\����(HiddenReport=1)�ɔ�\���w��̏ꍇ�́A�ǉ����Ȃ��B
				// �^�u�\����(HiddenReport=0)�́A��ɒǉ�����B
				if (("".equals(classHide) && "1".equals(HiddenReport)) ||("0".equals(HiddenReport))) {
					if (rep.getEnableMenu()) {	// ���|�[�gON/OFF�ݒ�m�F
%>
				<li class="<%=classHide %>">
					<a href="<%=parameterURL %>" class="jump" id="parent_<%=parentReportName %>" name="parent_<%=parentReportName %>"><%=reportName %></a>
				</li>
<%
					} else { // �����N�Ȃ�
%>
				<li class="<%=classHide %>">
					<pre id="parent_<%=parentReportName %>" name="parent_<%=parentReportName %>" class="disableMenu"><%=reportName %></pre>
				</li>
<%
					}
				}
			}
		}
	}
%>