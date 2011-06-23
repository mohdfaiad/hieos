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

package com.vangent.hieos.services.xds.bridge.utils;

import java.math.BigInteger;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-23
 * @author         Jim Horner
 */
public class UUIDUtils {

    /** Field description */
    public final static String OID_ROOT = "2.25";

    /** Field description */
    private final static Logger logger = Logger.getLogger(UUIDUtils.class);

    /**
     * Method description
     *
     *
     * @param uuidstr
     *
     * @return
     */
    public static boolean isUUID(String uuidstr) {

        boolean result = false;

        try {

            UUID.fromString(uuidstr);

            result = true;

        } catch (IllegalArgumentException e) {

            // don't care
        }

        return result;
    }

    /**
     * IHE_ITI_TF_Rev7-0_Vol2x_FT_2010-08-10<br />
     * B.6: Representing UUIDs as OIDs<br />
     * The standards ITU X.667 and ISO 9834-8 defined a particular OID root
     * for the UUIDs, and define the translation between these two formats.
     * The top node 2.25 is assigned for all UUIDs. This means that the UUID
     * that can be written as urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6
     * (using hexadecimal notation) is also
     * 2.25.329800735698586629295641978511506172918 (using dotted decimal
     * notation).
     *
     * @param uuid
     *
     * @return
     */
    public static String toOID(UUID uuid) {

        return toOID(uuid, OID_ROOT);
    }

    /**
     * Method description
     *
     *
     * @param uuid
     * @param prefix
     *
     * @return
     */
    public static String toOID(UUID uuid, String prefix) {

        String result = null;

        if ((uuid != null) && StringUtils.isNotBlank(prefix)) {

            String uuidstr = uuid.toString().replaceAll("-", "");
            BigInteger bi = new BigInteger(uuidstr, 16);

            result = String.format("%s.%s", prefix, bi.toString());
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param uuidstr
     *
     * @return
     */
    public static String toOIDFromUUIDString(String uuidstr) {

        return toOIDFromUUIDString(uuidstr, OID_ROOT);
    }

    /**
     * Method description
     *
     *
     * @param uuidstr
     * @param prefix
     *
     * @return
     */
    public static String toOIDFromUUIDString(String uuidstr, String prefix) {

        String result = null;

        try {

            UUID uuid = UUID.fromString(uuidstr);

            result = toOID(uuid, prefix);

        } catch (IllegalArgumentException e) {

            logger.warn(String.format("String[%s] is not a UUID.", uuidstr));
        }

        return result;
    }
}
