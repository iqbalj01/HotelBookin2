package examples;

// Note: The xsd schema viewer (in Eclipse) might report errors if you try to view your schemas with an 
// active internet connection, if so: view (and edit) your xml schemas offline please 

// This class uses the JAXB library to automatically read a xml file and 
// generate objects containing this data from pre-compiled classes

// Remember to generate the classes first from your xml schema and import them into your project which  
// might require a 'refresh' of your project directory (context menu > refresh)

// Generate the classes automatically with: Opening a command console and type:
// Path to YOUR-PROJECTROOT-IN-WORKSPACE\xjc.bat yourschemaname.xsd -d src -p yourclasspackagename


import java.io.*;

import javax.xml.bind.*;

import examples.HotelRental;

public class JAXB_XMLParser {

	private JAXBContext jaxbContext = null;     // generate a context to work in with JAXB											   
	private Unmarshaller unmarshaller = null;   // unmarshall = genrate objects from an xml file												
	
	// This is a candidate for a name change because you wont deal with a library any more in your conversion
	private HotelRental ourrental = null;            // the main object containing all data

	public JAXB_XMLParser() {

		try {
			jaxbContext = JAXBContext.newInstance("examples");  // Package that contains ouer classes																													
			unmarshaller = jaxbContext.createUnmarshaller();
		}
		catch (JAXBException e) {
		}
	}
	
	// Instance objects and return a list with this objects in it
	public HotelRental loadXML(InputStream fileinputstream) {

		try {
			Object xmltoobject = unmarshaller.unmarshal(fileinputstream);

			if (ourrental == null) {

				// generate the mynewlib object that conatins all info from the xml document
				ourrental = (HotelRental) (((JAXBElement) xmltoobject).getValue());
				// The above (Library) is a candidate for a name change because you wont deal with 
				// a library any more in your conversion
				
				return ourrental; // return Library Objekt
			}
		} // try

		catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}
}
