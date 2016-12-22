package examples;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.Scanner;
import java.util.Calendar;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import examples.Hotel;
import examples.HotelRental;
import examples.Catalog;
import examples.Customer;
import examples.Employee;
import examples.Insurance;
import examples.Lending;
import examples.Location;
import examples.ObjectFactory;
import examples.Payment;
import examples.Reservation;
import examples.SimpleGUI;
public class Reasoner {


	public HotelRental ourrental; 
	static SimpleGUI Myface;
	
	// The lists holding the class instances of all domain entities

	public List ourrentalList = new ArrayList(); 									
	public List thehotelList = new ArrayList(); 
	public List theEmployeeList = new ArrayList(); 
	public List theCatalogList = new ArrayList(); 
	public List theLendingList = new ArrayList(); 
	public List theRecentThing = new ArrayList();
	public List theCustomerList = new ArrayList();

	// Vectors to store all the synonyms

	public Vector<String> officesyn = new Vector<String>(); 
	public Vector<String> hotelsyn = new Vector<String>();
	public Vector<String> customersyn = new Vector<String>();
	public Vector<String> lendingsyn = new Vector<String>(); 
	public Vector<String> recentobjectsyn = new Vector<String>();

	public String questiontype = ""; //this variable is used to determine the question types for example if user types in "i want", the question type changes to 'intent'

	public List classtype = new ArrayList(); //creating the arraylist, this will store the classtype

	public String attributetype = ""; //attribute type selects the attribute to check for in the query

	public Object Currentitemofinterest; //the last object used, 'IT'

	public Integer Currentindex; //Last Index used
	
	public String brand ="";
	
	//***************Ensure the path is correct or the code won't work
	String filepath = "C:\\RentMe - Helpdesk\\Library.xml"; //filepath to the XML file, this is used to write data onto a pre-existing XML
	//***************Ensure the path is correct or the code won't work
	
	public String tooltipstring = "";

	public String URL3 = ""; //URL for our website which stores images and description

	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd-HH-mm"; //date format we will be using for our timestamp at booking start date

	public static String now() { //used to obtain todays and time in the format displayed above
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	String result = ""; //variable used to the result
	public Reasoner(SimpleGUI myface) {

		Myface = myface; //reference to GUI 

	}

	public void initknowledge() { //load all the knowledge/data from XML

		JAXB_XMLParser xmlhandler = new JAXB_XMLParser(); //instance of the XML parser

		File xmlfiletoload = new File("Library.xml"); //this identifies what file to load as the XML file

		// Init synonyms

		officesyn.add("office"); 
		officesyn.add("place"); 
		officesyn.add("hotelstore"); 
		officesyn.add("hotelhouse"); 
		officesyn.add("garage");
		officesyn.add("collection point");
		officesyn.add("forecourt"); 

		hotelsyn.add("hotel"); 

		hotelsyn.add("hotels");
		hotelsyn.add("hotel");
		hotelsyn.add(" it");
		hotelsyn.add("hotel");
		hotelsyn.add("vehicle");


		customersyn.add("customer"); 
		customersyn.add("reader");
		customersyn.add("follower");
		customersyn.add("client");
		customersyn.add("member");
		customersyn.add("guy");

		lendingsyn.add(" lending");
		lendingsyn.add(" a week"); 
		lendingsyn.add(" a month"); 
		lendingsyn.add(" a year"); 
		lendingsyn.add(" itworks");
		lendingsyn.add(" 0");

		recentobjectsyn.add(" this"); 
		recentobjectsyn.add(" that");
		recentobjectsyn.add(" him");
		recentobjectsyn.add(" her"); //spaces to prevent collision with "wHERe"
		recentobjectsyn.add(" it");

		try {
			FileInputStream readthatfile = new FileInputStream(xmlfiletoload); //initiate input stream
			ourrental = xmlhandler.loadXML(readthatfile);

			//populating the lists with data obtained from the XML file

			thehotelList = ourrental.gethotel();
			theEmployeeList = ourrental.getEmployee(); 
			theCatalogList = ourrental.getCatalog();
			theLendingList = ourrental.getLending();
			theCustomerList = ourrental.getCustomer();
			ourrentalList.add(ourrental); //force it to be a List

		}

		catch (Exception e) {
			e.printStackTrace();
			System.out.println("error in populating from XML");
		}
	}

	public Vector<String> generateAnswer(String input) { //generate an answer (String Vector)

		Vector<String> out = new Vector<String>();
		out.clear(); //clearing the vector for fresh use

		questiontype = "none"; //default status of questiontype

		Integer Answered = 0; //default status of answered, this is changed if the question is answered

		Integer subjectcounter = 0; //counter to keep track of # of identified subjects (classes)

		// Answer Generation Idea: content = Questiontype-method(classtype class) (+optional attribute)

		// ___________________________ IMPORTANT _____________________________

		input = input.toLowerCase(); // forcing all input into lowercase to prevent any issues with handling the input

		// ___________________________________________________________________

		String answer = ""; // the answer we return
		
		//string variables to store the customers use for later use

		String customername1 = "";
		String customername01 = "";
		String customerchoice = "";
		String daysofrental1 = "";
		String filepath = "C:\\RentMe - Helpdesk\\Library.xml"; //filepath to the XML file, this is used to write data onto a pre-existing XML

		// ----- Identifying type of question asked ------------------------------

		if (input.contains("how many")) { questiontype = "amount"; input = input.replace("how many", "<b>how many</b>"); }
		if (input.contains("number of")) { questiontype = "amount";input = input.replace("number of", "<b>number of</b>");}
		if (input.contains("amount of")) { questiontype = "amount";input = input.replace("amount of", "<b>amount of</b>"); }
		if (input.contains("count")) { questiontype = "amount";input = input.replace("count", "<b>count</b>"); }
		//######################################################################################################################################
		
		if (input.contains("what hotel")) { questiontype = "list";input = input.replace("what hotel", "<b>what hotel</b>"); }
		if (input.contains("diplay all")) { questiontype = "list";input = input.replace("diplay all", "<b>diplay all</b>"); }
		//######################################################################################################################################
		
		if (input.contains("is there a")) { questiontype = "checkfor";input = input.replace("is there a", "<b>is there a</b>"); }
		if (input.contains("i am searching")) { questiontype = "checkfor";input = input.replace("i am searching", "<b>i am searching</b>"); }
		if (input.contains("i am looking for")) { questiontype = "checkfor";input = input.replace("i am looking for", "<b>i am looking for</b>"); }
		if (input.contains("do you have") && !input.contains("how many")) { questiontype = "checkfor";input = input.replace("do you have", "<b>do you have</b>"); }
		if (input.contains("i look for")) { questiontype = "checkfor"; input = input.replace("i look for", "<b>i look for</b>"); }
		if (input.contains("is there")) { questiontype = "checkfor";input = input.replace("is there", "<b>is there</b>"); }
		//######################################################################################################################################
		
		if (input.contains("list all") 
				|| input.contains("view all") 
				|| input.contains("show all")) 
		{ 
			questiontype = "list";
			input = input.replace("what hotel", "<b>what hotel</b>");
		}
		//######################################################################################################################################
		
		if (input.contains("where is") 
				|| input.contains("can't find") 
				|| input.contains("can i find")
				|| input.contains("way to"))
		{
			questiontype = "location";
		}
		//######################################################################################################################################
		
		if (input.contains("can i lend") 
				|| input.contains("can i borrow") 
				|| input.contains("can i get the hotel")
				|| input.contains("am i able to") 
				|| input.contains("could i lend") 
				|| input.contains("i want to lend")
				|| input.contains("can i rent") 
				|| input.contains("can i Rent") 
				|| input.contains("i want to borrow"))
		{
			questiontype = "intent";
		}
		//######################################################################################################################################
		
		if (input.contains("thank you") 
				|| input.contains("bye") 
				|| input.contains("thanks")
				|| input.contains("cool thank"))
		{
			questiontype = "farewell";
		}
		//######################################################################################################################################
		
		if (input.contains("hello") 
				|| input.contains("hi") 
				|| input.contains("hanji")
				|| input.contains("Hi") 
				|| input.contains("Hey")
				|| input.contains("yo") 
				|| input.contains("Hello")
				|| input.contains("hey"))

		{
			questiontype = "intro";
		}
		//######################################################################################################################################
		
		if (input.contains("a hotel") 
				|| input.contains("A hotel") 
				|| input.contains("im looking for a hotel")
				|| input.contains("I am looking for a hotel"))

		{
			questiontype = "statingvehicle";
		}
		//######################################################################################################################################
		
		if (input.contains("no i dont") 
				|| input.contains("no, i dont") 
				|| input.contains("No i dont")
				|| input.contains("no i dont know") 
				|| input.contains("i dont know") 
				|| input.contains("no i don't")
				|| input.contains("no"))

		{
			questiontype = "dontknowhotel";
		}
		//######################################################################################################################################
		
		if (input.contains("who has it") 
				|| input.contains("by who") 
				|| input.contains("who"))

		{
			questiontype = "location";
		}
		//######################################################################################################################################
		
		if (input.contains("my name is") 
				|| input.contains("my names"))
		{
			questiontype = "name";
			customername1 = Myface.Input.getText();
			customername01 = customername1.substring(11);
		}
		//######################################################################################################################################

//		if (input.contains("3 series") 
//				|| input.contains("5 series")
//				|| input.contains("e230")
//				|| input.contains("sdf")
//				|| input.contains("audi")
//				|| input.contains("porsche")
//				|| input.contains("e230")
//				|| input.contains("e230")
//				|| input.contains("e230")
//				|| input.contains("e230")
//				|| input.contains("c220"))
//
//		{
//			questiontype = "pickmodel";
//
//
//			if (input.contains("3 series")) {brand = "3series";}
//			if (input.contains("3 series")) {brand = "3series";}									
//			if (input.contains("e230")){brand = "e230";}
//			if (input.contains("mercedes")){brand = "mercedes";}
//			if (input.contains("bmw")){brand = "bmw";}
//			if (input.contains("audi")){brand = "audi";}
//
//			// .... add more brands
//
//		}
		//######################################################################################################################################
		
		if (input.contains("days") 
				|| input.contains("day"))

		{
			questiontype = "periodOfRental";
			daysofrental1 = Myface.Input.getText();

		}
		//######################################################################################################################################
		
		if (input.contains("yes") 
				|| input.contains("that is correct"))

		{
			questiontype = "confirmation";
			
		}
		//######################################################################################################################################
		
		if (input.contains("exit") 
				|| input.contains("bye"))

		{
			questiontype = "exit";
			
		}
		//######################################################################################################################################
		

		// ------- Checking the Subject of the Question
		// --------------------------------------
		classtype = thehotelList;
		for (int x = 0; x < hotelsyn.size(); x++) { 
			if (input.contains(hotelsyn.get(x))) { 
				classtype = thehotelList;
				input = input.replace(hotelsyn.get(x), "<b>" + hotelsyn.get(x) + "</b>");
				subjectcounter = 1;
				System.out.println("Class type hotel recognised.");
			}
		}
		//######################################################################################################################################
		
		for (int x = 0; x < customersyn.size(); x++) { 
			if (input.contains(customersyn.get(x))) { 
				classtype = theCustomerList; 
				input = input.replace(customersyn.get(x), "<b>" + customersyn.get(x) + "</b>");
				subjectcounter = 1;
				System.out.println("Class type customer recognised.");
			}
		}
		//######################################################################################################################################
		

		//######################################################################################################################################
		
		for (int x = 0; x < lendingsyn.size(); x++) {
			if (input.contains(lendingsyn.get(x))) { 
				classtype = theLendingList; 
				input = input.replace(lendingsyn.get(x), "<b>" + lendingsyn.get(x) + "</b>");
				subjectcounter = 1;
				System.out.println("Class type Lending recognised.");
			}
		}
		//######################################################################################################################################
		
		if (subjectcounter == 0) {
			for (int x = 0; x < recentobjectsyn.size(); x++) {
				if (input.contains(recentobjectsyn.get(x))) {
					classtype = theRecentThing;
					input = input.replace(recentobjectsyn.get(x), "<b>" + recentobjectsyn.get(x) + "</b>");
					subjectcounter = 1;
					System.out.println("Class type recognised as" + recentobjectsyn.get(x));
				}
			}
		}
		//######################################################################################################################################
		
		System.out.println("subjectcounter = " + subjectcounter);
		for (int x = 0; x < officesyn.size(); x++) { 
			if (input.contains(officesyn.get(x))) { 

				if (subjectcounter == 0) { 
					input = input.replace(officesyn.get(x), "<b>" + officesyn.get(x) + "</b>");
					classtype = ourrentalList; 
				}
			}
		}
		//######################################################################################################################################
		
		// Compose Method call and generate answerVector

		if (questiontype == "amount") { // Number of Subject
			Integer numberof = Count(classtype);
			answer = ("The number of " + classtype.get(0).getClass().getSimpleName() + "s is " + numberof + ".");
			Answered = 1; // An answer was given
		}

		if (questiontype == "list") { // List all Subjects of a kind
			answer = ("All the Available " + classtype.get(0).getClass().getSimpleName() + "s. <br>"
					+ classtype.get(0).getClass().getSimpleName() + "s:" + ListAll(classtype));
			Answered = 1; // An answer was given
		}

		if (questiontype == "checkfor") { // test for a certain Subject instance

			Vector<String> check = CheckFor(classtype, input);
			answer = (check.get(0));
			Answered = 1; // An answer was given
			if (check.size() > 1) {
				Currentitemofinterest = classtype.get(Integer.valueOf(check.get(1)));
				System.out.println("Classtype List = " + classtype.getClass().getSimpleName());
				System.out.println("Index in Liste = " + Integer.valueOf(check.get(1)));
				Currentindex = Integer.valueOf(check.get(1));
				theRecentThing.clear(); // Clear it before adding (changing) the
				// now recent thing
				theRecentThing.add(classtype.get(Currentindex));
			}
		}

		//question regarding location
		if (questiontype == "location") { // We always expect a pronomial question to refer to the last object questioned for
			answer = ("You can find the " + classtype.get(0).getClass().getSimpleName() + " " + "at "
					+ Location(classtype, input));
			Answered = 1; // An answer was given
		}
		//######################################################################################################################################
		
		if ((questiontype == "intent" && classtype == thehotelList)
				|| (questiontype == "intent" && classtype == theRecentThing)) {
			// Can I lend the hotel or not (Can I lend "it" or not)
			
			answer = (hotelAvailable(classtype, input) );
			
			Answered = 1; // An answer was given
		}
		//######################################################################################################################################
		
		if (questiontype == "farewell") { // Reply to a farewell
			answer = ("You are welcome.");
			Answered = 1; // An answer was given
		}
		//######################################################################################################################################
		
		if (questiontype == "introduction") { // Reply to a farewell
			answer = ("Hi " + customername01 + ", to start off do you know what type of vehicle you are after?");
			Answered = 1; // An answer was given
		}
		//######################################################################################################################################
		
		if (questiontype == "name") { // Reply to a farewell
			answer = ("Hi " + customername01 + ", to start off do you know what type of vehicle you are after?");
			Answered = 1; // An answer was given"
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//																														//
			//									               XML WRITER
			//																														//
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			try { //writing the customer name into the XML file
				
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(filepath);

				// Get the root element
				Node reservation = doc.getFirstChild();

				// Get the customer element by tag name directly
				Node customer = doc.getElementsByTagName("customers").item(0);

				// update customer attribute
				NamedNodeMap attr = customer.getAttributes();
				Node nodeAttr = attr.getNamedItem("id");
				nodeAttr.setTextContent(customername01 + now()); //updating attribute using the customer name + todays date as the reservation ID

				// loop the customer child node
				NodeList list = customer.getChildNodes();

				for (int i = 0; i < list.getLength(); i++) {

		                   Node node = list.item(i);

				   // updates the name attribute with the customers name
				   if ("name".equals(node.getNodeName())) {
					node.setTextContent(customername01);
				   }

		           //remove a attributes
				   //if ("firstname".equals(node.getNodeName())) {
					//customer.removeChild(node);
				   //}

				}

				// write the content into XML file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(filepath));
				transformer.transform(source, result);

			   } catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			   } catch (TransformerException tfe) {
				tfe.printStackTrace();
			   } catch (IOException ioe) {
				ioe.printStackTrace();
			   } catch (SAXException sae) {
				sae.printStackTrace();
			   }
			}
		
		//######################################################################################################################################
		
		if (questiontype == "dontknowhotel") { // Reply to a farewell
			answer = ("Would you like to <b>view all hotels</b>, alternatively simply type the make of a hotel (i.e BMW)");
			Answered = 1; // An answer was given
		}
		//######################################################################################################################################
		
		if (Answered == 0) { // No answer was given
			answer = ("Sorry I didn't understand that. Would you like try again");
		}
		//######################################################################################################################################
		
		if (questiontype == "intro") { 
			answer = ("Please enter your name: ");
			Answered = 1; // An answer was given
		}
		//######################################################################################################################################
		
//		if (questiontype == "pickmodel") { // Reply to a farewell
//			
//			
//			
//			
//			
//			answer = ("How many days would you like to book the " + brand + " for?");
//			//String customername = input;
//			System.out.println(brand);
//			URL3 = "https://soc.uwl.ac.uk/~21226018/" + brand +".html";
//			System.out.println("URL = " + URL3);
//			tooltipstring = readwebsite(URL3);
//			String html = "<html>" + tooltipstring + "</html>";
//			Myface.setmytooltip(html);
//			Myface.setmyinfobox(URL3);
//
//			Answered = 1; // An answer was given
//		}

		if (questiontype == "periodOfRental") { 
			answer = ("So, just to confirm " + customername01 + " you want to rent a " + customerchoice + " for a total of " + daysofrental1);
			Answered = 1; // An answer was given
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//																														//
			//									               XML WRITER
			//																														//
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			try { //
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(filepath);

				// Get the root element
				Node reservation = doc.getFirstChild();
				// Get the customer element by tag name directly
				Node customer = doc.getElementsByTagName("customer").item(0);
				// update customer attribute
				NamedNodeMap attr = customer.getAttributes();
				// loop the customer child node
				NodeList list = customer.getChildNodes();
				for (int i = 0; i < list.getLength(); i++) {
		                   Node node = list.item(i);
				   //updates the rental start date
				   if ("rentalstartdate".equals(node.getNodeName())) {
					node.setTextContent(now().substring(0,10)); //trimming the date to only show the date rather than date + time
				   }
				   //updates the time stamp
				   if ("timestamp".equals(node.getNodeName())) {
					   node.setTextContent(now().substring(11,16));
				   }				  
				   //updates the amount of days of the rental
				   if ("rentalperiodindays".equals(node.getNodeName())) {
					   node.setTextContent(daysofrental1);
				   }
				}
				//write the content into XML file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(filepath));
				transformer.transform(source, result);

				System.out.println("Done");

			   } catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			   } catch (TransformerException tfe) {
				tfe.printStackTrace();
			   } catch (IOException ioe) {
				ioe.printStackTrace();
			   } catch (SAXException sae) {
				sae.printStackTrace();
			   }
			
		}
		//######################################################################################################################################
		
		if (questiontype == "confirmation") { 
			answer = ("Your booking is confirmed ");
			//String customername = input;
			//System.out.println(customername);

			Answered = 1; // An answer was given
			
		}
		//######################################################################################################################################
		
		if (questiontype == "exit") { // if the questiontype is exit, the application will shutdown
			answer = ("Thanks ");
			System.out.println("System exited");
			Answered = 1; // An answer was given
			System.exit(0);
		}
		//######################################################################################################################################
		
		out.add(input);
		out.add(answer);

		return out;
	}
	//######################################################################################################################################
	//method to determine the availability of the hotel
	public String hotelAvailable(List thelist, String input) {
		boolean available = true; //hotel availability is determined on a true or false basis (boolean), default is true
		String rentduration = "0";
		String pleasework = "";
		String customerName = "";
		String answer = "";
		Hotel curhotel = new Hotel();
		Lending curLend = new Lending();
		Employee curEmp = new Employee();
		String hotelmake = "";

		if (thelist == thehotelList) { //checking whether the list is equal to the hotellist
			int counter = 0;
			//identify which hotel is asked for
			for (int i = 0; i < thelist.size(); i++) {
				curhotel = (Hotel) thelist.get(i); 
				if (input.contains(curhotel.getMake().toLowerCase()) 
						|| input.contains(curhotel.getMake().toLowerCase())) { 
					counter = i;
					Currentindex = counter;
					theRecentThing.clear(); 
					classtype = thehotelList; 
					theRecentThing.add(classtype.get(Currentindex));
					hotelmake = curhotel.getMake();

					if (input.contains(curhotel.getMake().toLowerCase())) {
						input = input.replace(curhotel.getMake().toLowerCase(),
								"<b>" + curhotel.getMake().toLowerCase() + "</b>");
					}
					i = thelist.size() + 1; // force break
				}
			}
		}
		//######################################################################################################################################
		
		//maybe other way round or double
		if (thelist == theRecentThing && theRecentThing.get(0) != null) {
			if (theRecentThing.get(0).getClass().getSimpleName().toLowerCase().equals("hotel")) { 
				curhotel = (Hotel) theRecentThing.get(0); 
				hotelmake = curhotel.getMake();
			}
		}
		//######################################################################################################################################
		
		// check all lendings if they contain the hotels ID this determines if the hotel has been lended out thus making it unavailable
		for (int i = 0; i < theLendingList.size(); i++) {
			Lending curlend = (Lending) theLendingList.get(i); 
			// If there is a lending with the hotels hotelID, the hotel is not available
			if (curhotel.gethotelID().toLowerCase().equals(curlend.gethotelID().toLowerCase())) { 
				input = input.replace(curlend.gethotelID().toLowerCase(),
						"<b>" + curlend.gethotelID().toLowerCase() + "</b>");
				rentduration = curlend.getPeriodOfRent();
				pleasework = curlend.getLendedcustomername();
				available = false; //if the hotel's hotelID is listed in the lending list, the availability is set to false
				System.out.println(rentduration);
				i = thelist.size() + 1; // force break
			}
		}
		//######################################################################################################################################
		
		for (int i = 0; i < theCustomerList.size(); i++) {
			Customer curCust = (Customer) theCustomerList.get(i); 
			if (curhotel.gethotelID().toLowerCase().equals(curCust.gethotelID().toLowerCase())) { 
				input = input.replace(curCust.gethotelID().toLowerCase(),
						"<b>" + curCust.gethotelID().toLowerCase() + "</b>");
				customerName = curCust.getFName();
				available = false;
				System.out.println(customerName);
				i = thelist.size() + 1; // force break
			}
		}
		//######################################################################################################################################
		//if the hotel is available the answer is set as follows
		if (available) {

			answer = " How many days would you like to book the " + curhotel.getMake() + " for?";
			
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(filepath);

				// Get the root element
				Node reservation = doc.getFirstChild();

				// Get the customer element by tag name directly
				Node customer = doc.getElementsByTagName("customer").item(0);

				// update customer attribute
				NamedNodeMap attr = customer.getAttributes();
	
				
				// loop the customer child node
				NodeList list = customer.getChildNodes();

				for (int i = 0; i < list.getLength(); i++) {

		                   Node node = list.item(i);

				   // updates the name attribute with the chosen hotel
				   if ("chosenmake".equals(node.getNodeName())) {
					node.setTextContent(curhotel.getMake());
				   }
				}

				// write the content into XML file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(filepath));
				transformer.transform(source, result);

			   } catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			   } catch (TransformerException tfe) {
				tfe.printStackTrace();
			   } catch (IOException ioe) {
				ioe.printStackTrace();
			   } catch (SAXException sae) {
				sae.printStackTrace();
			   }

		} else { //the user is let known that the hotel is unavailable and has been booked 
			answer = "Sorry, " + curhotel.getMake() + " is not Available. It has been lent by " + pleasework 
					+ " for "+ rentduration;
					//+ "\n" ; //TODO ADD RECOMMENDATION BY TYPE OF VEHICLE*******************
		}
		
		//this links to a basic html page we created which would show the user an image + description of the hotel they chose
		URL3 = "https://soc.uwl.ac.uk/~21226018/" + curhotel.getMake().toLowerCase() +".html"; 
		tooltipstring = readwebsite(URL3);
		String html = "<html>" + tooltipstring + "</html>";
		Myface.setmytooltip(html);
		Myface.setmyinfobox(URL3);
		return (answer);
	}
	//######################################################################################################################################
	
	// Answer a question of the "How many ...." kind

	public Integer Count(List thelist) { // returns the number of instances in the relevant list
		return thelist.size();
	} 
	
	//######################################################################################################################################
	
	// Answer a question of the "What kind of..." kind
	public String ListAll(List thelist) {
		String listemall = "<ul>";
		if (thelist == thehotelList) { 
			for (int i = 0; i < thelist.size(); i++) {
				Hotel curhotel = (Hotel) thelist.get(i); 
				listemall = listemall + "<li>" + (curhotel.getMake() + "</li>"); 
			}
		}

		if (thelist == theEmployeeList) { 
			for (int i = 0; i < thelist.size(); i++) {
				Customer curmem = (Customer) thelist.get(i);
				listemall = listemall + "<li>" 
						+ (curmem.getFName() + " " + curmem.getSName() + "</li>");
			}
		}

		if (thelist == theCatalogList) { 
			for (int i = 0; i < thelist.size(); i++) {
				Catalog curcat = (Catalog) thelist.get(i); 
				listemall = listemall + "<li>" + (curcat.getCatalogID() + "</li>");
			}
		}

		if (thelist == theLendingList) {
			for (int i = 0; i < thelist.size(); i++) {
				Lending curlend = (Lending) thelist.get(i); 
				listemall = listemall + "<li>" + (curlend.gethotelID() + "</li>"); 
			}
		}

		listemall += "</ul>";

		String html = "<html>" + tooltipstring + "</html>";
		Myface.setmytooltip(html);

		return listemall;
	}
	//######################################################################################################################################

	public String ListMake(List thelist) {
		String listmake = "<ul>";
		if (thelist == thehotelList) { 
			for (int i = 0; i < thelist.size(); i++) {
				Hotel curhotel = (Hotel) thelist.get(i); 
				if(curhotel.getMake().equals("BMW")){ //THIS IS THE LINE YOU ARE MISSING
					listmake = listmake + "<li>" + (curhotel.getModel() + "</li>"); 
				}
			}
		}
		listmake += "</ul>";
		String html = "<html>" + tooltipstring + "</html>";
		Myface.setmytooltip(html);

		return listmake;
	}
	//######################################################################################################################################
	
	// Answer a question of the "Do you have..." kind
	public Vector<String> CheckFor(List thelist, String input) {
		Vector<String> yesorno = new Vector<String>();
		if (classtype.isEmpty()) {
			yesorno.add("Sorry please specify what you are searching for.");
		} else {
			yesorno.add("No, we unfortunatly don't have such a " + classtype.get(0).getClass().getSimpleName().toLowerCase());
		}

		Integer counter = 0;
		//checks whether the selected hotel is available
		if (thelist == thehotelList) { 
			for (int i = 0; i < thelist.size(); i++) {
				Hotel curhotel = (Hotel) thelist.get(i); 
				if (input.contains(curhotel.getMake().toLowerCase()) 
						|| input.contains(curhotel.gethotelID().toLowerCase()) 
						|| input.contains(curhotel.getMake().toLowerCase())) { 
					counter = i;
					yesorno.set(0, "Yes we have " + curhotel.getMake());
					yesorno.add(counter.toString());
					i = thelist.size() + 1; // force break
				}
			}
		}
		//######################################################################################################################################
		
		if (thelist == theEmployeeList) { 
			for (int i = 0; i < thelist.size(); i++) {
				Employee curemp = (Employee) thelist.get(i); 
				if (input.contains(curemp.getEmployeeID().toLowerCase()) 
						|| input.contains(curemp.getFName().toLowerCase()) 
						|| input.contains(curemp.getSName().toLowerCase()) 
						|| input.contains(curemp.getEmail().toLowerCase())) { 
					counter = i;
					yesorno.set(0, "Yes we have such a Employee"); 
					yesorno.add(counter.toString());
					i = thelist.size() + 1;
				}
			}
		}
		//######################################################################################################################################
		
		if (thelist == theCatalogList) { 
			for (int i = 0; i < thelist.size(); i++) {
				Catalog curcat = (Catalog) thelist.get(i); 
				if (input.contains(curcat.getCatalogID().toLowerCase()) 
						|| input.contains(curcat.getModel().toLowerCase())) { 
					counter = i;
					yesorno.set(0, "Yes we have such a Catalog"); 
					yesorno.add(counter.toString());
					i = thelist.size() + 1;
				}
			}
		}
		//######################################################################################################################################
		
		if (thelist == theLendingList) {
			for (int i = 0; i < thelist.size(); i++) {
				Lending curlend = (Lending) thelist.get(i); 
				if (input.contains(curlend.gethotelID().toLowerCase()) 
						|| input.contains(curlend.getLendedcustomername().toLowerCase())) {

					counter = i;
					yesorno.set(0, "Yes we have such a Lending"); 
					yesorno.add(counter.toString());
					i = thelist.size() + 1;
				}
			}
		}
		//######################################################################################################################################
		
		if (classtype.isEmpty()) {
			System.out.println("No class type given.");
		} else {

			String html = "<html>" + tooltipstring + "</html>";
			Myface.setmytooltip(html);

		}

		return yesorno;
	}

	// Method to retrieve the location information of the object (Where is...) kind
	public String Location(List classtypelist, String input) {
		List thelist = classtypelist;
		String location = "";
		//this is a reference to IT
		if (thelist == theRecentThing && theRecentThing.get(0) != null) {
			if (theRecentThing.get(0).getClass().getSimpleName().toLowerCase().equals("hotel")) { 
				Hotel curhotel = (Hotel) theRecentThing.get(0);
				location = (curhotel.getLocation()); 
			}

			if (theRecentThing.get(0).getClass().getSimpleName().toLowerCase().equals("member")) { 

				Customer curmem = (Customer) theRecentThing.get(0); 
				location = (curmem.getAddress() + " " + curmem.getMobileNo() + " " + curmem 
						.getEmail()); 
			}

			if (theRecentThing.get(0).getClass().getSimpleName().toLowerCase().equals("hotel")) {

				location = (ourrental.getCity() + " " + ourrental.getAddress() + ourrental 
						.getPostcode()); 
			}

		}
		
		// if a direct noun was used (hotel, member, etc)
		else {

			if (thelist == thehotelList) { 

				int counter = 0;

				for (int i = 0; i < thelist.size(); i++) {

					Hotel curhotel = (Hotel) thelist.get(i); 

					if (input.contains(curhotel.getMake().toLowerCase())
							|| input.contains(curhotel.gethotelID().toLowerCase()) 
							|| input.contains(curhotel.getMake().toLowerCase())) { 
						counter = i;

						Currentindex = counter;
						theRecentThing.clear(); // Clear it before adding (changing) theRecentThing
						classtype = thehotelList; 
						theRecentThing.add(classtype.get(Currentindex));
						i = thelist.size() + 1; // force break
					}
				}
			}
			//######################################################################################################################################
			
			if (thelist == theEmployeeList) { 
				int counter = 0;
				for (int i = 0; i < thelist.size(); i++) {
					Customer curmember = (Customer) thelist.get(i);

					if (input.contains(curmember.getFName().toLowerCase()) 
							|| input.contains(curmember.getSName().toLowerCase()) 
							|| input.contains(curmember.getCustomerID().toLowerCase())) {
						counter = i;
						location = (curmember.getAddress() + " ");
						Currentindex = counter;
						theRecentThing.clear(); 
						classtype = theEmployeeList; 
						theRecentThing.add(classtype.get(Currentindex));
						i = thelist.size() + 1; // force break
					}
				}
			}
			//######################################################################################################################################
			

			//######################################################################################################################################
			
			if (thelist == ourrentalList) {

				location = (ourrental.getCity() + " " + ourrental.getAddress() + ourrental 
						.getPostcode());
			}
		}
		String html = "<html>" + tooltipstring + "</html>";
		Myface.setmytooltip(html);

		return location;
	}
	//######################################################################################################################################
	
	public String testit() { // test the loaded knowledge by querying for hotels written by dostoyjewski
		String answer = "";
		System.out.println("hotel List = " + thehotelList.size()); 

		for (int i = 0; i < thehotelList.size(); i++) { // check each hotel in the List,

			Hotel curhotel = (Hotel) thehotelList.get(i); // cast list element to hotel Class
			System.out.println("hotel: " + curhotel.getMake());

			if (curhotel.getMake().equalsIgnoreCase("dostoyjewski")) { // check for the author
				answer = "A hotel written by " + curhotel.getMake() + "\n" 
						+ " is for example the classic " + curhotel.getMake() 
						+ ".";
			}
		}
		return answer;
	}

	public String readwebsite(String url) {
		String webtext = "";
		try {
			BufferedReader readit = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
			String lineread = readit.readLine();
			System.out.println("Reader okay");
			while (lineread != null) {
				webtext = webtext + lineread;
				lineread = readit.readLine();
			}

			// Hard coded cut out from "wordnet website source text": Check if website still has this structure 

			webtext = webtext.substring(webtext.indexOf("<ul>"), webtext.indexOf("</ul>")); 

			webtext = "<table width=\"700\"><tr><td>" + webtext + "</ul></td></tr></table>";

		} catch (Exception e) {
			webtext = "Not yet";
			System.out.println("Error connecting to wordnet");

		}
		return webtext;
	}
}
