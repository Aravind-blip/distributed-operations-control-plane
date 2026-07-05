package com.opscontrolplane.soapadapter;

import com.opscontrolplane.common.exception.InvalidRequestException;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Minimal DOM-based reader for the simulated SOAP envelope. Real SOAP integrations would
 * typically use JAXB against a generated WSDL contract; here a small, explicit DOM walk is
 * enough since the only goal is to demonstrate the legacy-to-event-driven bridge.
 */
public final class SoapEnvelopeParser {

    private SoapEnvelopeParser() {
    }

    public static ParsedHealthPayload parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setExpandEntityReferences(false);
            Document doc = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();

            String serviceName = readTag(doc, "serviceName");
            String status = readTag(doc, "status");
            String latencyMs = readTag(doc, "latencyMs");
            String errorRate = readTag(doc, "errorRate");

            if (serviceName == null || status == null) {
                throw new InvalidRequestException("SOAP payload missing required fields serviceName/status");
            }

            return new ParsedHealthPayload(
                    serviceName,
                    status,
                    latencyMs != null ? Double.parseDouble(latencyMs) : 0.0,
                    errorRate != null ? Double.parseDouble(errorRate) : 0.0);
        } catch (InvalidRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidRequestException("Unable to parse SOAP envelope: " + ex.getMessage());
        }
    }

    private static String readTag(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        Element element = (Element) nodes.item(0);
        return element.getTextContent() != null ? element.getTextContent().trim() : null;
    }

    public record ParsedHealthPayload(String serviceName, String status, double latencyMs, double errorRate) {
    }
}
