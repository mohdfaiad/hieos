/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.policyutil.pdp.client;

import com.sun.xacml.ctx.ResponseCtx;
import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.pdp.impl.PDPImpl;
import com.vangent.hieos.policyutil.pdp.model.PDPRequest;
import com.vangent.hieos.policyutil.pdp.model.PDPResponse;
import com.vangent.hieos.policyutil.pdp.model.SAMLResponseElement;
import com.vangent.hieos.policyutil.pdp.model.XACMLAuthzDecisionQueryElement;
import com.vangent.hieos.policyutil.pdp.model.XACMLRequestBuilder;
import com.vangent.hieos.policyutil.pdp.model.XACMLResponseBuilder;
import com.vangent.hieos.policyutil.pdp.resource.PIPResourceContentFinder;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.soap.WebServiceClient;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import java.io.ByteArrayOutputStream;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResponseType;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 * Client interface (proxy) to Policy Decision Point (PDP).
 *
 * @author Bernie Thuman
 */
public class PDPClient extends WebServiceClient {

    private final static Logger logger = Logger.getLogger(PDPClient.class);

    /**
     * PDPClient constructor.
     *
     * @param config
     */
    public PDPClient(XConfigActor config) {
        super(config);
    }

    /**
     * Simple client interface for issuing PDP requests and receiving PDP responses.
     *
     * @param pdpRequest
     * @return PDPResponse
     * @throws PolicyException
     */
    public PDPResponse authorize(PDPRequest pdpRequest) throws PolicyException {
        try {
            long start = System.currentTimeMillis();
            // Get configuration.
            XConfigActor config = this.getConfig();

            // See if we should use the embedded PDP.
            boolean embeddedMode = config.getPropertyAsBoolean("EmbeddedMode", true);
            PDPResponse pdpResponse;
            if (embeddedMode) {
                // Use embedded PDP (no SOAP call).
                pdpResponse = this.evaluate(pdpRequest);
            } else {
                // Perform SOAP call to PDP.
                XConfigTransaction txn = config.getTransaction("Authorize");
                String soapAction = config.getProperty("AuthorizeSOAPAction");
                pdpResponse = this.send(pdpRequest, soapAction, txn.getEndpointURL(), txn.isSOAP12Endpoint());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("PDP CLIENT TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
            }
            return pdpResponse;
        } catch (Exception ex) {
            throw new PolicyException("Unable to contact Policy Decision Point: " + ex.getMessage());
        }
    }

    /**
     * 
     * @param pdpRequest
     * @return
     * @throws PolicyException
     */
    private PDPResponse evaluate(PDPRequest pdpRequest) throws PolicyException {
        // Get PDPImpl singleton.
        PDPImpl pdpImpl = PDPImpl.getPDPImplInstance();

        // Add resource content (from PIP); if resource content does not exist.
        this.addResourceContent(pdpRequest);
        RequestType requestType = pdpRequest.getRequestType();

        // Conduct policy evaluation.
        ResponseCtx responseCtx = pdpImpl.evaluate(requestType);
        if (logger.isDebugEnabled()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            responseCtx.encode(baos);
            logger.debug("XACML Engine Response: " + baos.toString());
        }

        // Build PDPResponse.
        XACMLResponseBuilder builder = new XACMLResponseBuilder();
        ResponseType responseType = builder.buildResponseType(responseCtx);
        PDPResponse pdpResponse = new PDPResponse();
        pdpResponse.setRequestType(requestType);
        pdpResponse.setResponseType(responseType);

        return pdpResponse;
    }

    /**
     * 
     * @param requestType
     * @throws PolicyException
     */
    private void addResourceContent(PDPRequest pdpRequest) throws PolicyException {
        PIPResourceContentFinder resourceContentFinder = new PIPResourceContentFinder(this.getPIPConfig());
        // Get ResourceContent from the PIP.
        resourceContentFinder.addResourceContentToRequest(pdpRequest);
    }

    /**
     *
     * @return
     */
    private XConfigActor getPIPConfig() {
        XConfigActor pdpConfig = this.getConfig();
        return (XConfigActor) pdpConfig.getXConfigObjectWithName("pip", "PolicyInformationPointType");
    }

    /**
     * Issues SOAP request to PDP and returns PDP response.
     *
     * @param pdpRequest
     * @param soapAction
     * @param endpointURL
     * @param soap12
     * @return PDPResponse
     * @throws PolicyException
     */
    private PDPResponse send(PDPRequest pdpRequest, String soapAction, String endpointURL, boolean soap12) throws PolicyException {
        try {
            // Build XACML request.
            XACMLRequestBuilder requestBuilder = new XACMLRequestBuilder();
            XACMLAuthzDecisionQueryElement authzDecisionQuery = requestBuilder.buildXACMLAuthzDecisionQuery(pdpRequest);
            OMElement authzDecisionQueryNode = authzDecisionQuery.getElement();

            // Make SOAP call to PDP.
            Soap soap = new Soap();
            OMElement samlResponseNode;
            try {
                samlResponseNode = soap.soapCall(
                        authzDecisionQueryNode,
                        endpointURL,
                        false /* MTOM */,
                        soap12 /* Addressing - Only if SOAP 1.2 */,
                        soap12 /* SOAP 1.2 */,
                        soapAction, null);
            } catch (Exception ex) {
                throw new PolicyException(ex.getMessage());
            }
            if (samlResponseNode == null) {
                throw new PolicyException("No SOAP Response!");
            }

            // Build PDP response.
            XACMLResponseBuilder responseBuilder = new XACMLResponseBuilder();
            PDPResponse pdpResponse = responseBuilder.buildPDPResponse(new SAMLResponseElement(samlResponseNode));
            return pdpResponse;

        } catch (Exception ex) {
            throw new PolicyException(ex.getMessage());
        }
    }
}
