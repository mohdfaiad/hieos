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
package com.vangent.hieos.authutil.framework;

/**
 *
 * @author Anand Sastry
 */
public class AuthUtilException extends Exception {

    /**
     * Default CTOR
     */
    public AuthUtilException() {
        super();
    }

    /**
     *
     * @param message
     */
    public AuthUtilException(String message) {
        super(message);
    }

    /**
     *
     * @param message
     * @param t
     */
    public AuthUtilException(String message, Throwable t) {
        super(message, t);
    }
}