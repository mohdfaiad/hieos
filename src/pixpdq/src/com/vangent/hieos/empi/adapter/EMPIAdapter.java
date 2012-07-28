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
package com.vangent.hieos.empi.adapter;

import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.SubjectMergeRequest;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 *
 * @author Bernie Thuman
 */
public interface EMPIAdapter {

    /**
     * 
     * @param configActor
     */
    public void setConfig(XConfigActor configActor);

    /**
     *
     * @return
     */
    public XConfigActor getConfigActor();

    /**
     *
     * @param senderDeviceInfo
     */
    public void setSenderDeviceInfo(DeviceInfo senderDeviceInfo);

    /**
     * 
     * @return
     */
    public DeviceInfo getSenderDeviceInfo();

    /**
     *
     * @param classLoader
     */
    public void startup(ClassLoader classLoader);

    /**
     *
     * @param subject
     * @return
     * @throws EMPIException
     */
    public EMPINotification addSubject(Subject subject) throws EMPIException;

    /**
     *
     * @param subject
     * @return
     * @throws EMPIException
     */
    public EMPINotification updateSubject(Subject subject) throws EMPIException;

    /**
     *
     * @param subjectMergeRequest
     * @return
     * @throws EMPIException
     */
    public EMPINotification mergeSubjects(SubjectMergeRequest subjectMergeRequest) throws EMPIException;

    /**
     * 
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    public SubjectSearchResponse findSubjects(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException;

    /**
     * 
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    public SubjectSearchResponse getBySubjectIdentifiers(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException;
}
