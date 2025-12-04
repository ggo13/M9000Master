package com.usi.m9000.actions;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fazecast.jSerialComm.SerialPort;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import com.usi.m9000.dao.interfaces.Dnp3ConfigurationDAO;
import com.usi.m9000.dao.interfaces.Dnp3OutstationDetailDAO;
import com.usi.m9000.dao.interfaces.Dnp3SourceTypeDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.Dnp3ConfigurationDTO;
import com.usi.m9000.dto.Dnp3OutstationDetailDTO;
import com.usi.m9000.dto.Dnp3SourceTypeDTO;
import com.usi.m9000.dto.ExportMeasurementDTO;
import com.usi.m9000.dto.ExportMeasurementListDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.util.M9kConstants;

public class M9kDnp3ConfigurationAction extends ActionSupport implements Preparable {
    private List<Dnp3ConfigurationDTO> lstDnp3Configurations;
    private List<Dnp3SourceTypeDTO> lstDnp3SourceTypes;
    private List<SerialPort> lstSerialPorts;
    private Dnp3OutstationDetailDTO dnp3OutstationDetail;
    private final StationDTO stationDetailDto;
    private final M9kDAOFactory m9kDAOFactory;
    private final Dnp3ConfigurationDAO dnp3ConfigurationDAO;
    private final Dnp3SourceTypeDAO dnp3SourceTypeDAO;
    private final Dnp3OutstationDetailDAO dnp3OutstationDetailDAO;
    private ExportMeasurementListDTO exportList;

    @Override
    public void prepare() throws Exception {
        // Only load from DB if not already set (e.g. after submission)
        if (lstDnp3Configurations == null || lstDnp3Configurations.isEmpty()) {
            lstDnp3Configurations = dnp3ConfigurationDAO.getDnp3Configurations();
            if (lstDnp3Configurations == null || lstDnp3Configurations.isEmpty()) {
                lstDnp3Configurations = new ArrayList<>();
                lstDnp3Configurations.add(new Dnp3ConfigurationDTO()); // add a blank row for first-time user
            }
        }
    }

    public M9kDnp3ConfigurationAction() {
        m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
        dnp3ConfigurationDAO = m9kDAOFactory.getDnp3ConfigurationDAO();
        dnp3SourceTypeDAO = m9kDAOFactory.getDnp3SourceTypeDAO();
        dnp3OutstationDetailDAO = m9kDAOFactory.getDnp3OutstationDetailDAO();
        stationDetailDto = M9kStationDBUtil.getLocalStationDetails();
    }

    @Override
    @Action(value = "displayDnp3Configuration", results = {
        @Result(name = "success", location = "/jsp/dnp3ConfigTab.jsp", params = {
                "includeProperties",
                "lstDnp3SourceTypes.*,exportList.*,lstDnp3Configurations.*"
        })
    })
    @SkipValidation
    public String execute() throws Exception {
        return fetchDnp3ConfigData();
    }

    @Action(value = "postDnp3Configuration", results = {
        @Result(name = SUCCESS, type = "redirectAction", params = {
            "actionName", "editConfiguration",
            "stationId", "${stationDetails.systemStationId}"
        }),
        @Result(name = ERROR, location = "/jsp/error.jsp")
    })
    public String postDnp3Configuration() {
        return saveDnp3Configuration(lstDnp3Configurations);
    }

    // GET handler
    public String fetchDnp3ConfigData() {
        try {
            System.setProperty("jSerialComm.library.randomizeNativeName", "true");
            lstSerialPorts = Arrays.asList(SerialPort.getCommPorts());
            lstDnp3Configurations = dnp3ConfigurationDAO.getDnp3Configurations();
            lstDnp3SourceTypes = dnp3SourceTypeDAO.getSourceTypes();
            dnp3OutstationDetail = dnp3OutstationDetailDAO.getDnp3OutstationDetailByStationId(stationDetailDto.getSystemStationId());
            if(dnp3OutstationDetail.getTransportMethod() == null){
                dnp3OutstationDetail.setTransportMethod("TCP/IP");
            } if(dnp3OutstationDetail.getBaudRate() == 0){
                dnp3OutstationDetail.setBaudRate(9600);
            }   

            // 💡 Auto-add one blank row if list is empty
            if (lstDnp3Configurations == null || lstDnp3Configurations.isEmpty()) {
                lstDnp3Configurations = new ArrayList<>();
                lstDnp3Configurations.add(new Dnp3ConfigurationDTO());
            }

            String exportXml = extractExportsTag(stationDetailDto.getConfigXml());
            exportList = parseExportXmlToDataModel(exportXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("XML: " + stationDetailDto.getConfigXml());

        return SUCCESS;
    }

    // POST handler
    public String saveDnp3Configuration(List<Dnp3ConfigurationDTO> dnp3Configurations) {
        try {
            dnp3OutstationDetail.setStationId(stationDetailDto.getSystemStationId());
            if(dnp3OutstationDetail.getPortNumber() == 0){
                dnp3OutstationDetail.setPortNumber(20000);
            }
            dnp3OutstationDetailDAO.addDnp3OutstationDetail(dnp3OutstationDetail);
            
            // Remove null entries from the list
            dnp3Configurations = dnp3Configurations.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            dnp3ConfigurationDAO.addDnp3Configuration(dnp3Configurations);
        } catch (SQLException e) {
            e.printStackTrace();
            return ERROR;
        }

        // Reload data to re-display
        fetchDnp3ConfigData();
        return SUCCESS;
    }

    public Dnp3ConfigurationDTO getLstDnp3Configurations(int index) {
        if (lstDnp3Configurations == null) {
            lstDnp3Configurations = new ArrayList<>();
        }

        // Expand list for dynamic binding
        while (lstDnp3Configurations.size() <= index) {
            lstDnp3Configurations.add(new Dnp3ConfigurationDTO());
        }

        return lstDnp3Configurations.get(index);
    }

    public List<Dnp3ConfigurationDTO> getLstDnp3Configurations() {
        return lstDnp3Configurations;
    }

    public void setLstDnp3Configurations(int index, Dnp3ConfigurationDTO config) {
        // Ensure the list is initialized
        if (lstDnp3Configurations == null) {
            lstDnp3Configurations = new ArrayList<>();
        }

        // Grow the list if needed
        while (lstDnp3Configurations.size() <= index) {
            lstDnp3Configurations.add(new Dnp3ConfigurationDTO());
        }

        lstDnp3Configurations.set(index, config);
    }

    public void setLstDnp3Configurations(List<Dnp3ConfigurationDTO> lstDnp3Configurations) {
        this.lstDnp3Configurations = lstDnp3Configurations;
    }

    public Dnp3OutstationDetailDTO getDnp3OutstationDetail() {
        return dnp3OutstationDetail;
    }

    public void setDnp3OutstationDetail(Dnp3OutstationDetailDTO dnp3OutstationDetail) {
        this.dnp3OutstationDetail = dnp3OutstationDetail;
    }

    public List<Dnp3SourceTypeDTO> getLstDnp3SourceTypes() {
        return lstDnp3SourceTypes;
    }

    public List<SerialPort> getLstSerialPorts() {
        return lstSerialPorts;
    }

    public String getConfigXml() {
        return extractExportsTag(stationDetailDto.getConfigXml());
    }

    public List<ExportMeasurementDTO> getExportList() {
        return exportList.getExports();
    }

    public StationDTO getStationDetails() {
        return stationDetailDto;
    }

    private static String extractExportsTag(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            XPathExpression expr = xpath.compile("//Exports");

            Node exportsNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (exportsNode == null)
                return "No <Exports> tag found.";

            // Convert Node back to string
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(exportsNode), new StreamResult(writer));
            return writer.toString();

        } catch (IOException | IllegalArgumentException | ParserConfigurationException | TransformerException
                | XPathExpressionException | SAXException e) {
            return "Error parsing XML.";
        }
    }

    private static ExportMeasurementListDTO parseExportXmlToDataModel(String xml) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ExportMeasurementListDTO.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (ExportMeasurementListDTO) unmarshaller.unmarshal(new StringReader(xml));
    }
}