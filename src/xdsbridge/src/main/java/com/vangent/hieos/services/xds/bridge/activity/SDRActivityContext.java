/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vangent.hieos.services.xds.bridge.activity;

import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentRequest;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse;
import com.vangent.hieos.services.xds.bridge.message.XDSPnRMessage;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-22
 * @author         Vangent
 */
public class SDRActivityContext {

    /** Field description */
    private final Document document;

    /** Field description */
    private final SubmitDocumentRequest submitDocumentRequest;

    /** Field description */
    private final SubmitDocumentResponse submitDocumentResponse;

    /** Field description */
    private XDSPnRMessage xdspnr;

    /**
     * Constructs ...
     *
     *
     *
     * @param request
     * @param document
     * @param response
     */
    public SDRActivityContext(SubmitDocumentRequest request, Document document,
                              SubmitDocumentResponse response) {

        super();
        this.document = document;
        this.submitDocumentRequest = request;
        this.submitDocumentResponse = response;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public SubjectIdentifier getPatientId() {

        return getSubmitDocumentRequest().getPatientId();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public SubmitDocumentRequest getSubmitDocumentRequest() {
        return submitDocumentRequest;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public SubmitDocumentResponse getSubmitDocumentResponse() {
        return submitDocumentResponse;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public XDSPnRMessage getXdspnr() {
        return xdspnr;
    }

    /**
     * Method description
     *
     *
     * @param xdspnr
     */
    public void setXdspnr(XDSPnRMessage xdspnr) {
        this.xdspnr = xdspnr;
    }
}
