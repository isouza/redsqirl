-- Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
-- Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
-- 
-- This file is part of Red Sqirl
-- 
-- User agrees that use of this software is governed by: 
-- (1) the applicable user limitations and specified terms and conditions of 
--    the license agreement which has been entered into with Red Sqirl; and 
-- (2) the proprietary and restricted rights notices included in this software.
-- 
-- WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
-- INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
-- OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
-- 
-- If you have received this software in error please contact Red Sqirl at 
-- support@redsqirl.com
--

INSERT INTO idm_pck_mng.idm_packages( 
      id,
      name,
      version,
      license,
      price,
      short_description,
      html_file,
      release_notes,
      package_date,
      zip_file)
VALUES(
      'test',
      'test',
      '0.1-SNAPSHOT',
      'Apache',
      '',
      '',
      'test-0.1-SNAPSHOT.html',
      '',
      str_to_date('2014-04-17','%Y-%m-%d'),
      'test-0.1-SNAPSHOT.zip'
      );
