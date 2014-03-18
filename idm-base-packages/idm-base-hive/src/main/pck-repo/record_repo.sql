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
      '#{ARTIFACTID}',
      '#{NAME}',
      '#{VERSION}',
      'Apache',
      '',
      '#{DESCRIPTION}',
      '#{ARTIFACTID}-#{VERSION}.html',
      '',
      str_to_date('#{TIMESTAMP}','%Y-%m-%d'),
      '#{ARTIFACTID}-#{VERSION}.zip'
      );
