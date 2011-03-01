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
package com.vangent.hieos.hl7v3util.model.message;

import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectGender;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.hl7v3util.model.subject.SubjectName;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import java.util.Date;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class PRPA_IN201305UV02_Message_Builder extends HL7V3MessageBuilderHelper {

    /**
     * 
     */
    private PRPA_IN201305UV02_Message_Builder() {
    }

    /**
     *
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     */
    public PRPA_IN201305UV02_Message_Builder(DeviceInfo senderDeviceInfo, DeviceInfo receiverDeviceInfo) {
        super(senderDeviceInfo, receiverDeviceInfo);
    }

    /**
     * 
     * @param subjectSearchCriteria
     * @param sendCommunityPatientId
     * @return
     */
    public PRPA_IN201305UV02_Message getPRPA_IN201305UV02_Message(
            SubjectSearchCriteria subjectSearchCriteria) {

        String messageName = "PRPA_IN201305UV02";

        // PRPA_IN201305UV02
        OMElement rootNode = this.createOMElement(messageName);
        this.setAttribute(rootNode, "ITSVersion", "XML_1.0");
        this.addMessageId(rootNode);

        // <id root="1.2.840.114350.1.13.0.1.7.1.1" extension="35423"/>
        // <creationTime value="20090417150301"/>
        // <interactionId root="2.16.840.1.113883.1.6" extension="PRPA_IN201305UV02"/>
        // <processingCode code="T"/>
        // <processingModeCode code="I"/>
        // <acceptAckCode code="NE"/>
        this.addCreationTime(rootNode);
        this.addInteractionId(messageName, rootNode);
        this.addCode(rootNode, "processingCode", "T");
        this.addCode(rootNode, "processingModeCode", "I");
        this.addCode(rootNode, "acceptAckCode", "NE");

        // PRPA_IN201305UV02/receiver
        // PRPA_IN201305UV02/sender
        this.addReceiver(rootNode);
        this.addSender(rootNode);

        // PRPA_IN201305UV02/controlActProcess
        OMElement controlActProcessNode = this.addChildOMElement(rootNode, "controlActProcess");
        this.setAttribute(controlActProcessNode, "moodCode", "EVN");
        this.setAttribute(controlActProcessNode, "classCode", "CACT");

        // PRPA_IN201305UV02/controlActProcess/code
        OMElement codeNode = this.addCode(controlActProcessNode, "code", "PRPA_TE201305UV02");
        this.setAttribute(codeNode, "codeSystem", "2.16.840.1.113883.1.6");

        // PRPA_IN201305UV02/controlActProcess/authorOrPerformer
        // <authorOrPerformer typeCode="AUT">
        //       <assignedDevice classCode="ASSIGNED">
        //          <id root="1.2.840.114350.1.13.99997.2.3412"/>
        //       </assignedDevice>
        // </authorOrPerformer>
        if (subjectSearchCriteria.getCommunityAssigningAuthority() != null) {
            OMElement authorOrPerformerNode = this.addChildOMElement(controlActProcessNode, "authorOrPerformer");
            this.setAttribute(authorOrPerformerNode, "typeCode", "AUT");
            OMElement assignedDeviceNode = this.addChildOMElement(authorOrPerformerNode, "assignedDevice");
            this.setAttribute(assignedDeviceNode, "classCode", "ASSIGNED");
            OMElement idNode = this.addChildOMElement(assignedDeviceNode, "id");
            SubjectIdentifierDomain identifierDomain = subjectSearchCriteria.getCommunityAssigningAuthority();
            this.setAttribute(idNode, "root", identifierDomain.getUniversalId());
        }

        // PRPA_IN201305UV02/controlActProcess/queryAck
        //this.addQueryAck(requestNode, controlActProcessNode, subjects, errorText);

        // PRPA_IN201305UV02/controlActProcess/queryByParameter
        OMElement queryByParameterNode =
                this.addQueryByParameter(controlActProcessNode,
                subjectSearchCriteria);

        // TBD ... queryByParameter ... this was on the request .... not valid in this case.
        // PRPA_IN201305UV02/controlActProcess/queryByParameter
        //this.addQueryByParameter(requestNode, controlActProcessNode);

        return new PRPA_IN201305UV02_Message(rootNode);
    }

    /**
     * 
     * @param controlActProcessNode
     * @param subjectSearchCriteria
     * @return
     */
    private OMElement addQueryByParameter(
            OMElement controlActProcessNode,
            SubjectSearchCriteria subjectSearchCriteria) {

        // PRPA_IN201305UV02/controlActProcess/queryByParameter
        OMElement queryByParameterNode = this.addChildOMElement(controlActProcessNode, "queryByParameter");

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/queryId
        OMElement queryIdNode = this.addChildOMElement(queryByParameterNode, "queryId");
        this.setAttribute(queryIdNode, "root", "1.2.840.114350.1.13.28.1.18.5.999");  // FIXME: ???
        this.setAttribute(queryIdNode, "extension", "18204");  // FIXME: ???

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/statusCode
        this.addCode(queryByParameterNode, "statusCode", "new");

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/responseModalityCode
        this.addCode(queryByParameterNode, "responseModalityCode", "R");

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/responsePriorityCode
        this.addCode(queryByParameterNode, "responsePriorityCode", "I");

        // FIXME (Pull from subjectSearchCriteria) ....
        // PRPA_IN201305UV02/controlActProcess/queryByParameter/matchCriterionList
        // <matchCriterionList>
        //   <minimumDegreeMatch>
        //     <value xsi:type="INT" value="75"/>
        //     <semanticsText>Degree of match requested</semanticsText>
        //   </minimumDegreeMatch>
        // </matchCriterionList>

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList
        this.addParameterList(queryByParameterNode, subjectSearchCriteria);

        return queryByParameterNode;
    }

    /**
     *
     * @param rootNode
     * @param subjectSearchCriteria
     * @return
     */
    private OMElement addParameterList(
            OMElement rootNode,
            SubjectSearchCriteria subjectSearchCriteria) {
        Subject searchSubject = subjectSearchCriteria.getSubject();

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList
        OMElement parameterListNode = this.addChildOMElement(rootNode, "parameterList");

        // Now add parameters.

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList/livingSubjectAdministrativeGender
        //  <livingSubjectAdministrativeGender>
        //    <value code="M"/>
        //    <semanticsText>LivingSubject.administrativeGender</semanticsText>
        SubjectGender subjectGender = searchSubject.getGender();
        if (subjectGender != null) {
            OMElement genderNode = this.addChildOMElement(parameterListNode, "livingSubjectAdministrativeGender");
            this.addCode(genderNode, "value", subjectGender.getCode());
            this.addChildOMElementWithValue(genderNode, "semanticsText", "LivingSubject.administrativeGender");
        }

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList/livingSubjectBirthTime
        // <livingSubjectBirthTime>
        //   <value value="19640610"/>
        //   <semanticsText>LivingSubject.birthTime</semanticsText>
        // </livingSubjectBirthTime>-->
        Date birthTime = searchSubject.getBirthTime();
        if (birthTime != null) {
            OMElement birthTimeNode = this.addChildOMElement(parameterListNode, "livingSubjectBirthTime");
            this.addValue(birthTimeNode, "value", Hl7Date.toHL7format(birthTime));
            this.addChildOMElementWithValue(birthTimeNode, "semanticsText", "LivingSubject.birthTime");
        }

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList/livingSubjectId
        // <livingSubjectId>
        //   <value root="1.3.6.1.4.1.21367.13.20.2000" extension="GREEN_GLOBAL_05"/>
        //   <semanticsText>LivingSubject.id</semanticsText>
        // </livingSubjectId>
        //String communityAssigningAuthority = subjectSearchCriteria.getCommunityAssigningAuthority();
        for (SubjectIdentifier subjectIdentifier : searchSubject.getSubjectIdentifiers()) {
            //String assigningAuthority = subjectIdentifier.getIdentifierDomain().getUniversalId();
            //if (sendCommunityPatientId == true ||
            //        !assigningAuthority.equals(communityAssigningAuthority)) {
            OMElement subjectIdNode = this.addChildOMElement(parameterListNode, "livingSubjectId");
            OMElement valueNode = this.addChildOMElement(subjectIdNode, "value");
            this.addSubjectIdentifier(valueNode, subjectIdentifier);
            this.addChildOMElementWithValue(subjectIdNode, "semanticsText", "LivingSubject.id");
            //}
        }

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList/livingSubjectName
        // <livingSubjectName>
        //   <value>
        //     <given>John</given>
        //     <family>Jones</family>
        //   </value>
        //   <semanticsText>LivingSubject.name</semanticsText>
        // </livingSubjectName>-->
        for (SubjectName subjectName : searchSubject.getSubjectNames()) {
            OMElement subjectNameNode = this.addChildOMElement(parameterListNode, "livingSubjectName");
            OMElement valueNode = this.addChildOMElement(subjectNameNode, "value");
            this.addSubjectName(valueNode, subjectName);
            this.addChildOMElementWithValue(subjectNameNode, "semanticsText", "LivingSubject.name");
        }

        // PRPA_IN201305UV02/controlActProcess/queryByParameter/parameterList/otherIDsScopingOrganization
        // <otherIDsScopingOrganization>
        //    <value root="1.2.840.114350.1.13.99997.2.3412"/>
        //    <semanticsText>OtherIDs.scopingOrganization.id</semanticsText>
        // </otherIDsScopingOrganization>
        for (SubjectIdentifierDomain subjectIdentifierDomain : subjectSearchCriteria.getScopingAssigningAuthorities()) {
            String assigningAuthority = subjectIdentifierDomain.getUniversalId();
            OMElement otherIDsScopingOrganizationNode = this.addChildOMElement(parameterListNode, "otherIDsScopingOrganization");
            OMElement valueNode = this.addChildOMElement(otherIDsScopingOrganizationNode, "value");
            this.setAttribute(valueNode, "root", assigningAuthority);
            this.addChildOMElementWithValue(otherIDsScopingOrganizationNode, "semanticsText", "OtherIDs.scopingOrganization.id");
        }

        // FIXME: How about addresses and other parameters????
        return parameterListNode;
    }
}
