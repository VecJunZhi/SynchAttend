package com.zs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hanvon.faceid.sdk.FaceId;
import com.hanvon.faceid.sdk.FaceIdAnswer;
import com.hanvon.faceid.sdk.FaceId_ErrorCode;


import com.zs.data.Employee;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SmpReceiveRsponseService {

	public static Log log = LogFactory.getLog(SmpReceiveRsponseService.class);
	public static String webserviceIp;
	public static String webservicePort;
	public static String machineCode;
	
	static {
		Properties p = new Properties();
		InputStream inStream = SmpReceiveRsponseService.class.getResourceAsStream("/context.properties");
		try {
			p.load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		webserviceIp = p.getProperty("webServiceIp");
		webservicePort = p.getProperty("webServicePort");
		machineCode=p.getProperty("machineCode");
	}

	// 接受数据
	public static String smpReceiveData(String json, String flg,String machineCode) {
		if (flg.equals("receive")) {
			String rs = WebserviceResultService.testListInvoke(json, "receive",webserviceIp, webservicePort,machineCode);
			return rs;
		} else {
			String rs = WebserviceResultService.testListInvoke(json,"response", webserviceIp, webservicePort,machineCode);
			return rs;
		}
	}
	// 考勤机数据进行封装
	public static List<Employee> getEmployeeList(String answer)throws Exception {
		List<String> listime = new ArrayList<String>();
		List<String> listid = new ArrayList<String>();
		List<String> listname = new ArrayList<String>();
		List<Employee> list = new ArrayList<Employee>();
		// 时间匹配出来
		Pattern p = Pattern.compile("([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))\\s\\d+:\\d+:\\d+");
		Matcher m = p.matcher(answer);
		// 名称匹配出来
		Pattern pname = Pattern.compile("name=\"[\u4e00-\u9fa5]{1,}\"");
		Matcher mname = pname.matcher(answer);
		// ID号匹配出来
		Pattern pid = Pattern.compile("id=\"[0-9]{2,}\"");
		Matcher mid = pid.matcher(answer);
		String time = null;
		String id = null;
		String name = null;
		String id2 = null;
		String name2 = null;
		while (m.find()) {
			time = m.group();
			listime.add(time);
		}
		while (mname.find()) {
			name = mname.group();
			name2 = name.substring(6, name.length() - 1);
			listname.add(name2);
		}
		while (mid.find()) {
			id = mid.group();
			id2 = id.substring(4, id.length() - 1);
			listid.add(id2);
		}
		log.info("step 2 :smp encapsulate data to list ");
		log.info("stpe 2.1 :theList: time is " + listime.size() + " name list " + listname.size() + " id list " + listid.size());
		Employee employee = null;
		try{
			for (int i = 0; i < listime.size(); i++) {
				employee = new Employee(listime.get(i), listid.get(i),listname.get(i));
				list.add(employee);
			}
		}catch (Exception e) {
			log.info(" encapsulate employ list is wrong and the Detail  " + e);
		}
		return list;
	}
	// 封装数据
	public static String smpEncapsulateData(String answer)throws Exception {
		List<Employee> list = getEmployeeList(answer);
		JSONArray ja = JSONArray.fromObject(list);
		JSONObject data = new JSONObject();
		data.put("data", ja);
		return data.toString();

	}
	// 获取考勤机数据
	public static String getAttendData(String startTime,String attendIp,int port,String secretKey,String deviceCharset) {
		String answer = null;
		try {
			FaceId tcpClient = new FaceId(attendIp, port);
			String sdfend = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String Command = "GetRecord(start_time=\""+startTime+"\" end_time=\""+sdfend+"\")";
			FaceIdAnswer output = new FaceIdAnswer();
			tcpClient.setSecretKey(secretKey);
			FaceId_ErrorCode ErrorCode = tcpClient.Execute(Command, output,deviceCharset);
			if (ErrorCode.equals(FaceId_ErrorCode.Success)) {
				answer = output.answer;
				tcpClient.close();
				log.info("step 1 : connection the attendMachine successly");
			} else {
				answer = "getDataFormAttendBad";
				tcpClient.close();
				log.info(" can not connection the attendMachine "+ErrorCode);
			}
		} catch (Exception e) {
			answer = "getDataFormAttendBad";
			log.info(" can not connection the attendMachine .Error Detial : " + e);
		}
		return answer;
	}
	public static void main(String[] args) throws Exception {
		/**
		 * 1	smp接受该考勤机的最后节点时间
		 * 2	根据此节点时间获取考勤机数据
		 * 3	smp返回数据
		 */
		
		Calendar starCal=Calendar.getInstance();
		String nodeData = smpReceiveData(machineCode, "receive",machineCode);
		JSONObject b = JSONObject.fromObject(nodeData);
		String startTime =(String) b.get("lastTime");
		String attendIp=(String) b.get("machineIP");
		String sport=(String) b.get("machinePort");
		int port=Integer.parseInt(sport);
		String secretKey=(String) b.get("machineSecretkey");
		String deviceCharset=(String) b.get("machineCharset");
		String locked=(String) b.get("machineStatus");
		if("1".equals(locked)){
			Calendar endCal=Calendar.getInstance();
			Long start=starCal.getTime().getTime();
			Long end=endCal.getTime().getTime();
			Long subSecond=(end-start)/1000;
			log.info(" step 0 : smp receive the last attend data from db"+" "+startTime+" "+attendIp+" "+sport+" "+secretKey+" "+deviceCharset);
			String answer = getAttendData(startTime, attendIp, port, secretKey, deviceCharset);
			   File file = new File("machineData.txt");
			   FileOutputStream fos = new FileOutputStream(file);
			   OutputStreamWriter osw = new OutputStreamWriter(fos);
			   BufferedWriter bw = new BufferedWriter(osw);
			   bw.write("client request takes times "+subSecond+" seconds \n"+answer+"\n"+b.toString());
			   bw.flush();
			   bw.close();
			   osw.close();
			   fos.close();
			   log.info("machineData.txt has generated");
			if (!"getDataFormAttendBad".equals(answer)) {
				if (!"".equals(nodeData)) {
					String listString = smpEncapsulateData(answer);//smp封装数据
					if (!"".equals(listString)) {
						smpReceiveData(listString, "response",machineCode);//smp发送数据
						log.info(" step 3: smp send data to the webservice");
					}
				}else{
					log.info("get the attend machine data is bad");
				}
			}
		}else{
			log.info("the attend machine status is off or zero ,canot attend the machine data");
		}	
	}
}
