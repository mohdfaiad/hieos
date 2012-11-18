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
package com.vangent.hieos.empi.transform;

import org.apache.commons.codec.language.RefinedSoundex;

/**
 *
 * @author Bernie Thuman
 */
public class RefinedSoundexTransformFunction extends TransformFunction {

    /**
     * 
     * @param obj
     * @return
     */
    public Object transform(Object obj) {
        RefinedSoundex encoder = new RefinedSoundex();
        return encoder.encode((String)obj);
    }
}
