/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xds.registry.storedquery;

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XDSRegistryOutOfResourcesException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.response.ErrorLogger;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.SQCodedTerm;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.List;
import org.apache.axiom.om.OMElement;
import org.freebxml.omar.server.persistence.rdb.RegistryCodedValueMapper;

/**
 * 
 * @author NIST (Adapted by Bernie Thuman).
 */
public class FindDocuments extends StoredQuery {

    /**
     * 
     * @param response
     * @param logMessage
     * @param backendRegistry
     */
    public FindDocuments(ErrorLogger response, XLogMessage logMessage, BackendRegistry backendRegistry) {
        super(response, logMessage, backendRegistry);
    }

    /**
     * 
     * @param params
     * @param returnLeafClass
     * @param response
     * @param logMessage
     * @param backendRegistry
     * @throws MetadataValidationException
     */
    public FindDocuments(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);
        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSDocumentEntryPatientId", true, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryClassCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryTypeCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryPracticeSettingCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryCreationTimeFrom", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryCreationTimeTo", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStartTimeFrom", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStartTimeTo", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStopTimeFrom", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryServiceStopTimeTo", false, false, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryHealthcareFacilityTypeCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryEventCodeList", false, true, true, true, true, (String[]) null);
        validateQueryParam("$XDSDocumentEntryConfidentialityCode", false, true, true, true, true, (String[]) null);
        validateQueryParam("$XDSDocumentEntryFormatCode", false, true, true, true, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryStatus", true, true, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryAuthorPerson", false, true, true, false, false, (String[]) null);
        validateQueryParam("$XDSDocumentEntryDocumentAvailability", false, true, true, false, false, (String[]) null);
        validateQueryParam("$MetadataLevel", false, false, false, false, false, (String[]) null);
        if (this.hasValidationErrors()) {
            throw new MetadataValidationException("Metadata Validation error present");
        }
    }

    /**
     *
     * @return
     * @throws XdsInternalException
     * @throws XdsException
     * @throws XDSRegistryOutOfResourcesException
     */
    public Metadata runInternal() throws XdsInternalException, XdsException, XDSRegistryOutOfResourcesException {
        if (this.isReturnLeafClass()) {
            this.setReturnLeafClass(false);
            OMElement refs = impl();
            Metadata m = MetadataParser.parseNonSubmission(refs);
            int objectRefsSize = m.getObjectRefs().size();
            // Guard against large leaf class queries
            if (objectRefsSize > this.getMaxLeafObjectsAllowedFromQuery()) {
                throw new XDSRegistryOutOfResourcesException(
                        "FindDocuments Stored Query for LeafClass is limited to " + this.getMaxLeafObjectsAllowedFromQuery() + " documents on this Registry. Your query targeted " + m.getObjectRefs().size() + " documents");
            }
            this.setReturnLeafClass(true);
            if (objectRefsSize == 0) {
                return m;  // No need to go further and issue another query.
            }
        }
        OMElement results = impl();
        Metadata m = MetadataParser.parseNonSubmission(results);
        if (this.getLogMessage() != null) {
            this.getLogMessage().addOtherParam("Results structure", m.structure());
        }
        return m;
    }

    /**
     *
     * @return
     * @throws XdsInternalException
     * @throws XdsException
     */
    private OMElement impl() throws XdsInternalException, XdsException {

        // Parse query parameters:
        SqParams params = this.getSqParams();
        String metadataLevel = params.getIntParm("$MetadataLevel");
        String patientId = params.getStringParm("$XDSDocumentEntryPatientId");
        SQCodedTerm classCodes = params.getCodedParm("$XDSDocumentEntryClassCode");
        SQCodedTerm typeCodes = params.getCodedParm("$XDSDocumentEntryTypeCode");
        SQCodedTerm practiceSettingCodes = params.getCodedParm("$XDSDocumentEntryPracticeSettingCode");
        String creationTimeFrom = params.getIntParm("$XDSDocumentEntryCreationTimeFrom");
        String creationTimeTo = params.getIntParm("$XDSDocumentEntryCreationTimeTo");
        String serviceStartTimeFrom = params.getIntParm("$XDSDocumentEntryServiceStartTimeFrom");
        String serviceStartTimeTo = params.getIntParm("$XDSDocumentEntryServiceStartTimeTo");
        String serviceStopTimeFrom = params.getIntParm("$XDSDocumentEntryServiceStopTimeFrom");
        String serviceStopTimeTo = params.getIntParm("$XDSDocumentEntryServiceStopTimeTo");
        SQCodedTerm facilityTypeCodes = params.getCodedParm("$XDSDocumentEntryHealthcareFacilityTypeCode");
        SQCodedTerm eventCodes = params.getCodedParm("$XDSDocumentEntryEventCodeList");
        SQCodedTerm confidentialityCodes = params.getCodedParm("$XDSDocumentEntryConfidentialityCode");
        SQCodedTerm formatCodes = params.getCodedParm("$XDSDocumentEntryFormatCode");
        List<String> status = params.getListParm("$XDSDocumentEntryStatus");
        List<String> authorPerson = params.getListParm("$XDSDocumentEntryAuthorPerson");
        List<String> documentAvailabilityStatus = params.getListParm("$XDSDocumentEntryDocumentAvailability");

        StoredQueryBuilder sqb = new StoredQueryBuilder(this.isReturnLeafClass());
        sqb.select("obj");
        sqb.append("FROM ExtrinsicObject obj, ExternalIdentifier patId");
        sqb.appendClassificationDeclaration(classCodes);
        sqb.appendClassificationDeclaration(typeCodes);
        sqb.appendClassificationDeclaration(practiceSettingCodes);
        sqb.appendClassificationDeclaration(facilityTypeCodes);
        sqb.appendClassificationDeclaration(eventCodes);
        sqb.appendClassificationDeclaration(confidentialityCodes);
        sqb.appendClassificationDeclaration(formatCodes);
        if (creationTimeFrom != null) {
            sqb.append(", Slot crTimef");
        }
        if (creationTimeTo != null) {
            sqb.append(", Slot crTimet");
        }
        if (serviceStartTimeFrom != null) {
            sqb.append(", Slot serStartTimef");
        }
        if (serviceStartTimeTo != null) {
            sqb.append(", Slot serStartTimet");
        }
        if (serviceStopTimeFrom != null) {
            sqb.append(", Slot serStopTimef");
        }
        if (serviceStopTimeTo != null) {
            sqb.append(", Slot serStopTimet");
        }
        if (documentAvailabilityStatus != null) {
            sqb.append(", Slot das");
        }
        if (authorPerson != null) {
            sqb.append(", Classification author");
            sqb.append(", Slot authorperson");
        }

        // WHERE clause ...
        sqb.append(" WHERE ");
        // patientID
        sqb.append("(obj.id = patId.registryobject AND ");
        sqb.append("patId.identificationScheme='"
                + RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSDocumentEntry_patientid_uuid)
                + "' AND ");
        sqb.append(" patId.value = '");
        sqb.append(patientId);
        sqb.append("' ) ");

        sqb.addCode(classCodes);
        sqb.addCode(typeCodes);
        sqb.addCode(practiceSettingCodes);

        sqb.addTimes("creationTime", "crTimef", "crTimet", creationTimeFrom, creationTimeTo, "obj");
        sqb.addTimes("serviceStartTime", "serStartTimef", "serStartTimet", serviceStartTimeFrom, serviceStartTimeTo, "obj");
        sqb.addTimes("serviceStopTime", "serStopTimef", "serStopTimet", serviceStopTimeFrom, serviceStopTimeTo, "obj");

        sqb.addCode(facilityTypeCodes);
        sqb.addCode(eventCodes);
        sqb.addCode(confidentialityCodes);
        sqb.addCode(formatCodes);

        if (authorPerson != null) {
            for (String ap : authorPerson) {
                sqb.append(" AND ");
                sqb.append("(obj.id = author.classifiedObject AND ");
                sqb.append(" author.classificationScheme='urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d' AND ");
                sqb.append(" authorperson.parent=author.id AND");
                sqb.append(" authorperson.name_='authorPerson' AND");
                sqb.append(" authorperson.value LIKE '" + ap + "' ) ");
            }
        }
        sqb.addSlot("documentAvailability", documentAvailabilityStatus, "das", "obj");
        sqb.append("AND obj.status IN ");
        sqb.append(RegistryCodedValueMapper.convertStatus_ValueToCode(status));
        return runQuery(sqb);
    }
}
