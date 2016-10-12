package com.zs;

import javax.xml.namespace.QName;

import net.sf.json.JSONObject;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebserviceResultService {
	public static JSONObject result;
	static Log log = LogFactory.getLog(WebserviceResultService.class);

	public static String testListInvoke(String json, String flg, String weIp,String wePort,String machineCode) {
		String url = "http://" + weIp + ":" + wePort + "/services/AxisService";
		String rs = "";
		try{
			RPCServiceClient serviceClient = new RPCServiceClient();
			Options options = serviceClient.getOptions();
			EndpointReference targetEPR = new EndpointReference(url);
			options.setTo(targetEPR);
			QName opAddEntry = new QName("http://action.web.oa.zs.com", "smpTongXin");
			Object[] opAddEntryArgs = null;
			if(flg.equals("receive")){
				opAddEntryArgs = new Object[] { json, flg, machineCode };
			}else if (flg.equals("response")) {
				opAddEntryArgs = new Object[] { json, flg, machineCode };
			}
			Class[] classes = new Class[] { String.class };
			rs = (String)serviceClient.invokeBlocking(opAddEntry,opAddEntryArgs, classes)[0];
		} catch (Exception e) {
			log.info(" smp  call webservice is bad ,and the error Detail is : " + e);
		}
		return rs;

	}

	public static JSONObject getResult() {
		return result;
	}
	public static void setResult(JSONObject result) {
		WebserviceResultService.result = result;
	}
}
