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
package com.vangent.hieos.services.xds.registry.mu.validation;

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.services.xds.registry.mu.command.MetadataUpdateCommand;
import com.vangent.hieos.services.xds.registry.mu.command.UpdateDocumentEntryMetadataCommand;
import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.storedquery.MetadataUpdateStoredQuerySupport;
import com.vangent.hieos.services.xds.registry.storedquery.RegistryObjectValidator;
import com.vangent.hieos.xutil.exception.XDSNonIdenticalHashException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateDocumentEntryMetadataCommandValidator extends MetadataUpdateCommandValidator {

    /**
     * 
     * @param metadataUpdateCommand
     */
    public UpdateDocumentEntryMetadataCommandValidator(MetadataUpdateCommand metadataUpdateCommand) {
        super(metadataUpdateCommand);
    }

    /**
     * 
     * @throws XdsException
     */
    public boolean validate() throws XdsException {
        UpdateDocumentEntryMetadataCommand cmd = (UpdateDocumentEntryMetadataCommand) this.getMetadataUpdateCommand();
        boolean validationSuccess = true;

        // Get metadata update context for use later.
        MetadataUpdateContext metadataUpdateContext = cmd.getMetadataUpdateContext();
        XLogMessage logMessage = metadataUpdateContext.getLogMessage();
        BackendRegistry backendRegistry = metadataUpdateContext.getBackendRegistry();
        RegistryResponse registryResponse = metadataUpdateContext.getRegistryResponse();
        XConfigActor configActor = metadataUpdateContext.getConfigActor();

        // Prepare to conduct validation.
        RegistryObjectValidator rov = new RegistryObjectValidator(registryResponse, logMessage, backendRegistry);
        Metadata submittedMetadata = cmd.getMetadata();

        OMElement targetObject = cmd.getTargetObject();
        String previousVersion = cmd.getPreviousVersion();

        // Save target patient id for later usage.
        cmd.setTargetPatientId(submittedMetadata.getPatientId(targetObject));

        // Get lid.
        String lid = submittedMetadata.getLID(targetObject);

        //
        // Look for an existing document that 1) matches the lid, 2) status is "Approved"
        // and 3) matches the previous version.
        //

        // Prepare to issue registry query.
        MetadataUpdateStoredQuerySupport muSQ = new MetadataUpdateStoredQuerySupport(
                metadataUpdateContext.getRegistryResponse(), logMessage,
                backendRegistry);

        // Issue query.
        muSQ.setReturnLeafClass(true);
        backendRegistry.setReason("Locate Previous Approved Document (by LID/Version)");
        OMElement queryResult = muSQ.getDocumentsByLID(lid, MetadataSupport.status_type_approved, previousVersion);
        backendRegistry.setReason("");

        // Convert response into Metadata instance.
        cmd.setCurrentMetadata(MetadataParser.parseNonSubmission(queryResult));
        Metadata currentMetadata = cmd.getCurrentMetadata();
        List<OMElement> currentDocuments = currentMetadata.getExtrinsicObjects();
        if (currentDocuments.isEmpty()) {
            throw new XdsException("Existing approved document entry not found for lid="
                    + lid + ", version=" + previousVersion);
        } else if (currentDocuments.size() > 1) {
            throw new XdsException("> 1 existing document entry found!");
        }

        // Fall through: we found a document that matches.
        cmd.setCurrentRegistryObject(currentMetadata.getExtrinsicObject(0));

        // Run further validations.
        this.validateUniqueIdMatch(targetObject, submittedMetadata, cmd.getCurrentRegistryObject(), currentMetadata);
        this.validateRepositoryUniqueId(submittedMetadata, cmd);
        this.validateHashAndSize(submittedMetadata, cmd);
        this.validateObjectType(cmd);
        rov.validateDocumentUniqueIds(submittedMetadata);
        rov.validatePatientId(submittedMetadata, configActor);
        return validationSuccess;
    }

    /**
     *
     * @param submittedMetadata
     * @param cmd
     * @throws XdsException
     */
    private void validateRepositoryUniqueId(Metadata submittedMetadata, UpdateDocumentEntryMetadataCommand cmd) throws XdsException {
        Metadata currentMetadata = cmd.getCurrentMetadata();
        OMElement currentDocumentEntry = cmd.getCurrentRegistryObject();
        String currentDocumentRepositoryUniqueId = currentMetadata.getSlotValue(currentDocumentEntry, "repositoryUniqueId", 0);
        String submittedDocumentRepositoryUniqueId = submittedMetadata.getSlotValue(cmd.getTargetObject(), "repositoryUniqueId", 0);
        if (!currentDocumentRepositoryUniqueId.equals(submittedDocumentRepositoryUniqueId)) {
            throw new XdsException("Submitted document and current document 'repositoryUniqueId' values do not match");
        }
    }

    /**
     * 
     * @param submittedMetadata
     * @param cmd
     * @throws XdsException
     */
    private void validateHashAndSize(Metadata submittedMetadata, UpdateDocumentEntryMetadataCommand cmd) throws XdsException {
        // NOTE (BHT): I believe that hash validation is already handled in the RegistryObjectValidator
        // but leaving here anyway.
        Metadata currentMetadata = cmd.getCurrentMetadata();
        OMElement currentDocumentEntry = cmd.getCurrentRegistryObject();

        // Validate that current document hash = submitted document hash
        String currentDocumentHash = currentMetadata.getSlotValue(currentDocumentEntry, "hash", 0);
        String submittedDocumentHash = submittedMetadata.getSlotValue(cmd.getTargetObject(), "hash", 0);
        if (!currentDocumentHash.equals(submittedDocumentHash)) {
            // FIXME: throw exceptions or add to registry response?
            throw new XDSNonIdenticalHashException("Submitted document and current document 'hash' value does not match");
        }

        // Validate that current document size = submitted document size
        String currentDocumentSize = currentMetadata.getSlotValue(currentDocumentEntry, "size", 0);
        String submittedDocumentSize = submittedMetadata.getSlotValue(cmd.getTargetObject(), "size", 0);
        if (!currentDocumentSize.equals(submittedDocumentSize)) {
            // FIXME: throw exceptions or add to registry response?
            throw new XdsException("Submitted document and current document 'size' value does not match");
        }
    }

    /**
     *
     * @param cmd
     * @throws XdsException
     */
    private void validateObjectType(UpdateDocumentEntryMetadataCommand cmd) throws XdsException {
        OMElement currentDocumentEntry = cmd.getCurrentRegistryObject();
        OMElement targetDocumentEntry = cmd.getTargetObject();

        // Validate that current document type = submitted document type
        String currentDocumentObjectType = currentDocumentEntry.getAttributeValue(MetadataSupport.object_type_qname);
        String submittedDocumentObjectType = targetDocumentEntry.getAttributeValue(MetadataSupport.object_type_qname);
        if (!currentDocumentObjectType.equals(submittedDocumentObjectType)) {
            // FIXME: throw exceptions or add to registry response?
            throw new XdsException("Submitted document and current document 'objectType' value does not match");
        }
    }
}