<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="authentication.bean.Side" %>
<%@ page import="authentication.bean.Report" %>
<%@ page import="authentication.defines.*" %>

<%
	// ���j���[���X�g�ƕ��ޔԍ�
	ArrayList menu = (ArrayList)session.getAttribute(Consts.STR_SES_REPSIDE);
	int repside = Integer.parseInt(request.getParameter(Form.REPORT_SIDE));

	// ���j���[���ޔԍ��ƈ�v���郌�|�[�g���X�g�擾
	ArrayList report = null;
	for(int i = 0; i < menu.size(); i++){
		Side m = (Side)menu.get(i);
		if (m.getSide() == repside) {
			report = m.getReport();
			break;
		}
	}

	// ���s�̃��|�[�g�ԍ��擾
	String current = ((String)request.getParameter(Form.REPORT_NO));
	String currentno = current!=null?current:"0";

	// �Y�����|�[�g�̏��擾
	Report rep=null;
	for(int i = 0; i< report.size(); i++){
		Report r = (Report)report.get(i);
		if (r.getReport_boolean()){
			if (currentno.equals(Long.toString(r.getReport_no()))){
				rep=r;
				break;
			}
		}
	}

	if (rep != null) {
		String reportjsp = "/jsp/" + rep.getReport_jsp() + ".jsp";
%>
		<jsp:include page="<%=reportjsp%>" flush="true"></jsp:include>
<%
	}
%>