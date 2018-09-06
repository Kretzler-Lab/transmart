package edu.harvard.i2b2.ontology.ws;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Paths;

import javax.xml.bind.JAXBElement;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptsType;
import edu.harvard.i2b2.ontology.util.OntologyJAXBUtil;

public class OntologyServiceRESTTest extends OntologyAxisAbstract{
	private static String testFileDir = "";

	private static String ontologyTargetEPR = 
			"http://localhost:9090/i2b2/services/OntologyService/getSchemes";			
	//	"http://127.0.0.1:8080/i2b2/services/PMService/getServices";			

	//swc20160722 added following DBlookup related
	private static String ontTargetEPR = "http://localhost:9090/i2b2/services/OntologyService/";
	private static String getAllDBlookups = ontTargetEPR + "getAllDblookups";
	private static String setDBlookup = ontTargetEPR + "setDblookup";
	private static String getDBlookup = ontTargetEPR + "getDblookup";
	private static String deleteDBlookup = ontTargetEPR + "deleteDblookup";
	private static String ontMsg;

	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(OntologyServiceRESTTest.class);
	}

	public static void setMsgSkeleton() { //swc20160725 added
		StringBuffer sb = new StringBuffer("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n");
		sb.append("<ns3:request xmlns:ns3='http://www.i2b2.org/xsd/hive/msg/1.1/' xmlns:ns4='http://www.i2b2.org/xsd/cell/ont/1.1/' xmlns:ns2='http://www.i2b2.org/xsd/hive/plugin/'>\n");
		sb.append("	  <message_header>\n");
		sb.append("		 <i2b2_version_compatible>1.1</i2b2_version_compatible>\n");
		sb.append("		 <hl7_version_compatible>2.4</hl7_version_compatible>\n");
		sb.append("      <sending_application>\n");
		sb.append("            <application_name>i2b2 Ontology</application_name>\n");
		sb.append("            <application_version>1.6</application_version>\n");
		sb.append("      </sending_application>\n");
		sb.append("      <sending_facility>\n");
		sb.append("            <facility_name>i2b2 Hive</facility_name>\n");
		sb.append("      </sending_facility>\n");
		sb.append("      <receiving_application>\n");
		sb.append("            <application_name>Ontology Cell</application_name>\n");
		sb.append("            <application_version>1.6</application_version>\n");
		sb.append("      </receiving_application>\n");
		sb.append("      <receiving_facility>\n");
		sb.append("            <facility_name>i2b2 Hive</facility_name>\n");
		sb.append("      </receiving_facility>\n");
		sb.append("		 <security>\n");
		sb.append("			   <domain>i2b2demo</domain>\n"); 
		sb.append("			   <username>{{$USER$}}</username>\n"); 
		sb.append("			   <password>demouser</password>\n"); 
		sb.append("		 </security>\n");
		sb.append("      <message_control_id>\n");
		sb.append("            <message_num>aJpNM5zV07sXBCyyvd4h5</message_num>\n"); 
		sb.append("            <instance_num>0</instance_num>\n");
		sb.append("      </message_control_id>\n");
		sb.append("      <processing_id>\n");
		sb.append("            <processing_id>P</processing_id>\n");
		sb.append("            <processing_mode>I</processing_mode>\n");
		sb.append("      </processing_id>\n");
		sb.append("      <accept_acknowledgement_type>messageId</accept_acknowledgement_type>\n");
		sb.append("      <project_id></project_id>\n"); 
		sb.append("   </message_header>\n");
		sb.append("   <request_header>\n");
		sb.append("        <result_waittime_ms>180000</result_waittime_ms>\n");
		sb.append("   </request_header>\n");
		sb.append("   <message_body>\n");
		sb.append("        {{$PAYLOAD$}}\n");
		sb.append("   </message_body>\n");
		sb.append("</ns3:request>\n");
		ontMsg = sb.toString();
	}
	
	@BeforeClass
	public static void setUp() throws Exception {
		testFileDir = "test"; //System.getProperty("testfiledir");
		//if (!java.nio.file.Files.exists(java.nio.file.Paths.get(testFileDir))) {
		//	throw new Exception("testFileDir '" + testFileDir + "' non-existent!");
		//}
		System.out.println("test file dir " + testFileDir);
		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}
		
		setMsgSkeleton(); //swc20160725 added
	}

	@Test
	public void GetSchemes() throws Exception {
		String filename = testFileDir + "/schemes.xml";
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(ontologyTargetEPR).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			ConceptsType folders = (ConceptsType)helper.getObjectByClass(r.getMessageBody().getAny(),ConceptsType.class);
			assertNotNull(folders);
			assertTrue(folders.getConcept().size() > 5);



		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	

	@Test
	public void GetCategories() throws Exception {
		String filename = testFileDir + "/categories.xml";
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(ontologyTargetEPR).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			ConceptsType folders = (ConceptsType)helper.getObjectByClass(r.getMessageBody().getAny(),ConceptsType.class);
			assertNotNull(folders);
			assertTrue(folders.getConcept().size() > 10);



		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	

	@Test
	public void GetAllDBlookups_admin() throws Exception { //swc20160722
		String requestString = ontMsg.replace("{{$USER$}}", "i2b2").replace("{{$PAYLOAD$}}", "<ns4:get_all_dblookups type='default'/>");
		try { 
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getAllDBlookups).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void GetAllDBlookups_non_admin() throws Exception { //swc20160722
		String requestString = ontMsg.replace("{{$USER$}}", "demo").replace("{{$PAYLOAD$}}", "<ns4:get_all_dblookups type='default'/>");
		try { 
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getAllDBlookups).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public String GetDBlookupPayload() { //swc20160725 (not a test)
		StringBuffer sb = new StringBuffer();
		sb.append("        <ns4:set_dblookup project_path='test20160721'>\n");
		sb.append("            <domain_id>i2b2demo</domain_id>\n");
		sb.append("            <owner_id>@</owner_id>\n");
		sb.append("            <db_fullschema>i2b2metadata</db_fullschema>\n");
		sb.append("            <db_datasource>java:/OntologyDemoDS</db_datasource>\n");
		sb.append("            <db_servertype>ORACLE</db_servertype>\n");
		sb.append("            <db_nicename>Metadata</db_nicename>\n");
		sb.append("            <db_tooltip>testing, ..., 1, 2, 3</db_tooltip>\n");
		sb.append("            <comment>JUNIT did it.</comment>\n");
		sb.append("            <status_cd></status_cd>\n");
		sb.append("        </ns4:set_dblookup>");
		return sb.toString();
	}
	
	@Test
	public void SetDBlookup_admin() throws Exception { //swc20160722
		String requestString = ontMsg.replace("{{$USER$}}", "i2b2").replace("{{$PAYLOAD$}}", GetDBlookupPayload());
		try { 
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);			
			DeleteDBlookup_admin(); //clean it up (in case this gets run after the DeleteDBlookup_admin()
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void SetDBlookup_non_admin() throws Exception { //swc20160722
		String requestString = ontMsg.replace("{{$USER$}}", "demo").replace("{{$PAYLOAD$}}", GetDBlookupPayload());
		try { 
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(setDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void GetDBlookup_admin() throws Exception { //swc20160722
		String requestString = ontMsg.replace("{{$USER$}}", "i2b2").replace("{{$PAYLOAD$}}", "<ns4:get_dblookup value='test20160721'/>");
		try { 
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void GetDBlookup_schema_admin() throws Exception { //swc20160722
		String requestString = ontMsg.replace("{{$USER$}}", "i2b2").replace("{{$PAYLOAD$}}", "<ns4:get_dblookup field='db_fullschema' value='i2b2metadata'/>");
		try { 
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void GetDBlookup_non_admin() throws Exception { //swc20160722
		String requestString = ontMsg.replace("{{$USER$}}", "demo").replace("{{$PAYLOAD$}}", "<ns4:get_dblookup value='test20160721'/>");
		try { 
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(getDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void DeleteDBlookup_admin() throws Exception { //swc20160722
		String requestString = ontMsg.replace("{{$USER$}}", "i2b2").replace("{{$PAYLOAD$}}", "<ns4:delete_dblookup project_path='test20160721' domain_id='i2b2demo' owner_id='@'/>");
		try { 
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("DONE", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void DeleteDBlookup_non_admin() throws Exception { //swc20160722
		String requestString = ontMsg.replace("{{$USER$}}", "demo").replace("{{$PAYLOAD$}}", "<ns4:delete_dblookup project_path='test20160721' domain_id='i2b2demo' owner_id='@'/>");
		try { 
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void DeleteDBlookup_nonexist_admin() throws Exception { //swc20160722
		String requestString = ontMsg.replace("{{$USER$}}", "i2b2").replace("{{$PAYLOAD$}}", "<ns4:delete_dblookup project_path='bogus' domain_id='i2b2demo' owner_id='@'/>");
		try { 
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			StatusType st = r.getResponseHeader().getResultStatus().getStatus();
			assertEquals("DONE", st.getType());
			assertEquals("no dblookup row was deleted (could be due to no target row found)! - Ontology processing completed", st.getValue());
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void DeleteDBlookup_missingAttrib_admin() throws Exception { //swc20160722
		String requestString = ontMsg.replace("{{$USER$}}", "i2b2").replace("{{$PAYLOAD$}}", "<ns4:delete_dblookup project_path='test20160721' owner_id='@'/>");
		try { 
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(deleteDBlookup).sendReceive(requestElement);
			JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			String msg = r.getResponseHeader().getResultStatus().getStatus().getType();
			assertEquals("ERROR", msg);
		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}	

}





