/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.pixpdqv2.v2handler;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.ERL;
import ca.uhn.hl7v2.model.v25.message.RSP_K23;
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.empi.adapter.EMPIAdapter;
import com.vangent.hieos.empi.adapter.EMPIAdapterFactory;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownIdentifierDomain;
import com.vangent.hieos.empi.exception.EMPIExceptionUnknownSubjectIdentifier;
import com.vangent.hieos.hl7v2util.acceptor.impl.Connection;
import com.vangent.hieos.hl7v2util.atna.ATNAAuditEventHelper;
import com.vangent.hieos.hl7v2util.model.message.PIXQueryResponseMessageBuilder;
import com.vangent.hieos.hl7v2util.model.subject.SubjectSearchCriteriaBuilder;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import com.vangent.hieos.subjectmodel.SubjectSearchResponse;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAHL7v2AuditEventQuery;
import com.vangent.hieos.xutil.atna.XATNALogger;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIXQueryMessageHandler extends HL7V2MessageHandler {

    private final static Logger logger = Logger.getLogger(PIXQueryMessageHandler.class);

    /**
     * 
     * @param connection
     * @param inMessage
     * @return
     * @throws ApplicationException
     * @throws HL7Exception
     */
    @Override
    public Message processMessage(Connection connection, Message inMessage) throws ApplicationException, HL7Exception {
        logger.info("Processing PIX Query message ...");
        Message outMessage;
        try {
            // Get Terser.
            Terser terser = new Terser(inMessage);

            // FIXME: Create DeviceInfo properly; receiver device info not yet used.
            DeviceInfo senderDeviceInfo = getSenderDeviceInfo(terser);
            DeviceInfo receiverDeviceInfo = getReceiverDeviceInfo(terser);

            // Build SubjectSearchCriteria from HL7v2 message.
            SubjectSearchCriteriaBuilder subjectSearchCriteriaBuilder =
                    new SubjectSearchCriteriaBuilder(getBuilderConfig(), terser);
            SubjectSearchCriteria subjectSearchCriteria = subjectSearchCriteriaBuilder.buildSubjectSearchCriteriaFromPIXQuery();

            // Clone identifiers (for audit later).
            //List<SubjectIdentifier> subjectIdentifiers = SubjectIdentifier.clone(subject.getSubjectIdentifiers());

            // Get subject cross references from EMPI.
            EMPIAdapter adapter = EMPIAdapterFactory.getInstance();
            adapter.setSenderDeviceInfo(senderDeviceInfo);
            SubjectSearchResponse subjectSearchResponse = adapter.getBySubjectIdentifiers(subjectSearchCriteria);

            // Build response.
            outMessage = this.buildResponse(inMessage, subjectSearchResponse);

            // Perform ATNA audit.
            this.performAuditPIXQueryProvider(
                    senderDeviceInfo, receiverDeviceInfo, terser,
                    connection.getRemoteAddress().getHostAddress(),
                    subjectSearchResponse);

        } catch (EMPIExceptionUnknownIdentifierDomain ex) {
            outMessage = this.buildErrorResponse(inMessage, ex);
        } catch (EMPIExceptionUnknownSubjectIdentifier ex) {
            outMessage = this.buildErrorResponse(inMessage, ex);
        } catch (EMPIException ex) {
            outMessage = this.buildErrorResponse(inMessage, ex.getMessage());
        } catch (Exception ex) {
            outMessage = this.buildErrorResponse(inMessage, "Exception: " + ex.getClass().getName() + " - " + ex.getMessage());
        }
        return outMessage;
    }

    /**
     *
     * @param inMessage
     * @param subjectSearchResponse
     * @return
     */
    private Message buildResponse(Message inMessage, SubjectSearchResponse subjectSearchResponse) throws HL7Exception {
        PIXQueryResponseMessageBuilder responseBuilder =
                new PIXQueryResponseMessageBuilder(getBuilderConfig(), inMessage);
        return responseBuilder.buildPIXQueryResponse(subjectSearchResponse);
    }

    /**
     *
     * @param inMessage
     * @param errorText
     * @return
     * @throws HL7Exception
     */
    private Message buildErrorResponse(Message inMessage, String errorText) throws HL7Exception {
        PIXQueryResponseMessageBuilder responseBuilder =
                new PIXQueryResponseMessageBuilder(getBuilderConfig(), inMessage);
        return responseBuilder.buildErrorResponse(errorText);
    }

    /**
     *
     * @param inMessage
     * @param ex
     * @return
     * @throws HL7Exception
     */
    private Message buildErrorResponse(Message inMessage, EMPIExceptionUnknownSubjectIdentifier ex) throws HL7Exception {
        PIXQueryResponseMessageBuilder responseBuilder =
                new PIXQueryResponseMessageBuilder(getBuilderConfig(), inMessage);
        RSP_K23 outMessage = responseBuilder.buildBaseErrorResponse();

        // SEGMENT: Error Segment [ERR]
        ERR err = outMessage.getERR();
        err.getSeverity().setValue("E");  // E - Error, F - Fatal, I - Informational, W - Warning
        ERL erl = err.getErrorLocation(0);
        erl.getSegmentID().setValue("QPD");
        erl.getSegmentSequence().setValue("1");
        erl.getFieldPosition().setValue("3");
        erl.getFieldRepetition().setValue("1");
        erl.getComponentNumber().setValue("1");
        err.getHL7ErrorCode().getIdentifier().setValue(EMPIExceptionUnknownSubjectIdentifier.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        err.getHL7ErrorCode().getText().setValue(ex.getMessage());
        return outMessage;
    }

    /**
     *
     * @param inMessage
     * @param ex
     * @return
     * @throws HL7Exception
     */
    private Message buildErrorResponse(Message inMessage, EMPIExceptionUnknownIdentifierDomain ex) throws HL7Exception {
        PIXQueryResponseMessageBuilder responseBuilder =
                new PIXQueryResponseMessageBuilder(getBuilderConfig(), inMessage);
        RSP_K23 outMessage = responseBuilder.buildBaseErrorResponse();

        // SEGMENT: Error Segment [ERR]
        ERR err = outMessage.getERR();
        err.getSeverity().setValue("E");  // E - Error, F - Fatal, I - Informational, W - Warning
        ERL erl = err.getErrorLocation(0);
        erl.getSegmentID().setValue("QPD");
        if (ex.getIdentifierDomainType() == EMPIExceptionUnknownIdentifierDomain.IdentifierDomainType.SUBJECT_IDENTIFIER_DOMAIN) {
            erl.getSegmentSequence().setValue("1");
            erl.getFieldPosition().setValue("3");
            erl.getFieldRepetition().setValue("1");
            erl.getComponentNumber().setValue("4");
        } else {
            erl.getSegmentSequence().setValue("1");
            erl.getFieldPosition().setValue("4");
            int listPosition = ex.getListPosition();
            int ordinalPosition = listPosition + 1;
            erl.getFieldRepetition().setValue(new Integer(ordinalPosition).toString());
        }
        err.getHL7ErrorCode().getIdentifier().setValue(EMPIExceptionUnknownIdentifierDomain.UNKNOWN_KEY_IDENTIFIER_ERROR_CODE);
        err.getHL7ErrorCode().getText().setValue(ex.getMessage());
        return outMessage;
    }

    /**
     * 
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     * @param terser
     * @param sourceIP
     * @param subjectIdentifiers
     */
    private void performAuditPIXQueryProvider(
            DeviceInfo senderDeviceInfo,
            DeviceInfo receiverDeviceInfo,
            Terser terser,
            String sourceIP,
            SubjectSearchResponse subjectSearchResponse) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                ATNAHL7v2AuditEventQuery auditEvent =
                        ATNAAuditEventHelper.getATNAAuditEventPIXQueryProvider(
                        ATNAAuditEvent.ActorType.PIX_MANAGER_V2,
                        senderDeviceInfo,
                        receiverDeviceInfo,
                        terser,
                        sourceIP,
                        subjectSearchResponse);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            logger.error("PIXv2Manager EXCEPTION: Could not perform ATNA audit", ex);
        }
    }

    /**
     *
     * @param connection
     * @param in
     * @return
     */
    @Override
    public boolean canProcess(Connection connection, Message in) {
        return true;
    }
}
