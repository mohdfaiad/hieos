--
--  This code is subject to the HIEOS License, Version 1.0
-- 
--  Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
-- 
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- 
--  See the License for the specific language governing permissions and
--  limitations under the License.
--

--
-- Oracle DDL for ADT Schema
--

--
--  DDL to Drop Tables
--
DROP TABLE patient;
DROP TABLE patientaddress;
DROP TABLE patientname;
DROP TABLE patientrace;

DROP TABLE mergedobjects;
DROP TABLE mergehistory;


--
-- Definition of table `patient`
--

CREATE TABLE patient (
  uuid VARCHAR2(64) NOT NULL,
  id VARCHAR2(100) NOT NULL,
  "TIMESTAMP" VARCHAR2(40),
  birthdatetime VARCHAR2(40),
  adminsex VARCHAR2(20),
  accountnumber VARCHAR2(100),
  bedid VARCHAR2(100),
  status CHAR(1) NOT NULL DEFAULT 'A',
  CONSTRAINT patient_pkey PRIMARY KEY (id)
);

--
-- Create index on patient uuid col
--
CREATE INDEX patient_uuid_idx ON patient (uuid);


--
-- Definition of table patientaddress
--

CREATE TABLE patientaddress (
  parent VARCHAR2(64) NOT NULL,
  streetaddress VARCHAR2(100) NOT NULL,
  otherdesignation VARCHAR2(64) NOT NULL,
  city VARCHAR2(32) NOT NULL,
  stateorprovince VARCHAR2(32) NOT NULL,
  zipcode VARCHAR2(12) NOT NULL,
  country VARCHAR2(32) NOT NULL,
  countyorparish VARCHAR2(32) NOT NULL,
  CONSTRAINT patientaddress_pkey PRIMARY KEY (parent)
);


--
-- Definition of table `patientname`
--

CREATE TABLE patientname (
  parent VARCHAR2(64) NOT NULL,
  familyname VARCHAR2(32) NOT NULL,
  givenname VARCHAR2(32) NOT NULL,
  secondandfurthername VARCHAR2(32) NOT NULL,
  suffix VARCHAR2(12) NOT NULL,
  prefix VARCHAR2(12) NOT NULL,
  degree VARCHAR2(12) NOT NULL,
  CONSTRAINT patientname_pkey PRIMARY KEY (parent)
);

--
-- Definition of table `patientrace`
--

CREATE TABLE patientrace (
  parent VARCHAR2(64) NOT NULL,
  race VARCHAR2(64) NOT NULL,
  CONSTRAINT patientrace_pkey PRIMARY KEY (parent)
);


--
-- Definition of table mergehistory 
--
CREATE TABLE mergehistory (
  uniqueid VARCHAR2(64) NOT NULL,
  survivingpatientid VARCHAR2(64) NOT NULL,
  subsumedpatientid VARCHAR2(64) NOT NULL,
  action CHAR(1) NOT NULL,
  datetimeperformed TIMESTAMP NOT NULL,
  CONSTRAINT mh_pkey PRIMARY KEY (uniqueid)
);

--
-- Create index on mergehistory
--
CREATE INDEX mh_patientids_idx ON mergehistory (survivingpatientid, subsumedpatientid);
CREATE INDEX mh_action_idx ON mergehistory (action);


--
-- Definition of table mergedobjects
--
CREATE TABLE mergedobjects (
  parentid VARCHAR2(64) NOT NULL,
  externalidentifierid VARCHAR2(64) NOT NULL,
  CONSTRAINT mo_pkey PRIMARY KEY (parentid, externalidentifierid),
  CONSTRAINT mo_mh_fkey FOREIGN KEY (parentid)
      REFERENCES mergehistory (uniqueid)
      ON DELETE CASCADE
);
