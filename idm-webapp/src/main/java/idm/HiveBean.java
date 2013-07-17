package idm;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;

/** HiveBean
 * 
 * Class to screen control of the File System Hive
 * 
 * @author Igor.Souza
 */
public class HiveBean extends FileSystemBean {

	private static Logger logger = Logger.getLogger(HiveBean.class);

	/** openCanvasScreen
	 * 
	 * Methods to generating screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	@PostConstruct
	public void openCanvasScreen() {

		try {

			setDataStore(getHiveInterface());

			if(getListGrid().isEmpty()){

				mountTable(getDataStore());
			}

		}catch(Exception e){
			logger.error(e);
			getBundleMessage("error.mount.table");
		}

	}

	/** openCanvasScreen
	 * 
	 * Method that is executed when the screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	@PreDestroy
	public void closeCanvasScreen() {

	}
	
	@Override
	public String getFormatedString(String property, String value){
		if (property.equals("describe")){
			return value.replace(",", " ").replace(";", ", ");
		}
		return value;
	}
	
}