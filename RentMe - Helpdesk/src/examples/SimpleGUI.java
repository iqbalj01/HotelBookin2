package examples;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.*;
import java.util.*;

public class SimpleGUI {

	public JPanel Inframe, Outframe, Buttonframe;
	public JTextField Input;
	public JEditorPane Output, Info;
	public JLabel Inputlabel;
	public JScrollPane Scroll, ScrollInfo;
	public String dialogout = "";
	public Vector<String> dialoghistory = new Vector<String>();
	public String displaytext = "";
	String question = "";
	String answer = "";
	Reasoner myReasoner;

	public SimpleGUI() { // Constructor for an Instance of SimpleGUI

		myReasoner = new Reasoner(this); // Instantiate a "brain", reference
											// this GUI to it

		myReasoner.initknowledge(); // fill "the brain" with knowledge

		Input = new JTextField(50);
		Inputlabel = new JLabel("How can we help you today:");

		Output = new JEditorPane("text/html", "<b>Initial text</b>");
		Output.setEditable(false); // no one should be able to write in the
									// display
		Output.setToolTipText("<html>Your dialog with the machine.</html>");

		Scroll = new JScrollPane(Output);
		Scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		Scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		Scroll.setBorder(BorderFactory.createTitledBorder("hotel Rental:"));
		Scroll.getViewport().setPreferredSize(new Dimension(550, 550));

		Info = new JEditorPane("text/html", "");
		Info.setEditable(false);
		Info.setText("Background information about the conversations topic will be displayed in this window.");

		ScrollInfo = new JScrollPane(Info);
		ScrollInfo
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		ScrollInfo
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		ScrollInfo.setBorder(BorderFactory
				.createTitledBorder("Background Info:"));
		ScrollInfo.getViewport().setPreferredSize(new Dimension(550, 550));

		Inframe = new JPanel(); // Frame for the Inputelements
		Outframe = new JPanel(); // Frame for the Outputelements

		Inframe.add(Inputlabel); // adding the elements to the JPanels
		Inframe.add(Input);
		Outframe.add(Scroll);
		Outframe.add(ScrollInfo);

		JFrame Main = new JFrame("Guidance for hotel booking "); // our main frame

		try {
			Main.setContentPane(new JLabel(new ImageIcon(ImageIO.read(new File(
					"bg.jpg")))));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Main.getContentPane().setLayout(new FlowLayout());
		Main.getContentPane().add(Outframe);
		Main.getContentPane().add(Inframe);

		Main.addWindowListener(new WindowAdapter() { // implementing listeners
														// we need
			public void windowClosing(WindowEvent e) { // What to do if Window
														// is closed
				System.exit(0); // Kill program
			}
		});

		Input.addActionListener(new ActionListener() { // Action if "Enter" is
														// pressed
			public void actionPerformed(ActionEvent e) { // while "Input" has
															// the Focus
				questionasked();
				Input.setText("");
			}
		});

		Main.pack();
		Main.setSize(1500, 900);
		Main.setVisible(true); // Don't forget

		dialoghistory
				.add("<H2>Welcome to Travelodge, you're virtual hotel boking helpdesk</H2> "
						+ "<H3>I can assist you in booking a room, to start off what type of room would you like to book?</H3><br>");

		Output.setText(dialoghistory.firstElement());
		Input.requestFocusInWindow();
	} // Constructor done

	public void checkbrain() {

		String yo = myReasoner.testit(); // Testing the reasoner's knowledge
		System.out.println(yo); // Testing the reasoner's knowledge
	}

	public void questionasked() { // log questions in a String vector

		question = Input.getText();
		
		if (Input.getText().equals("Hello")) {
			System.out.println("Hey mate");
		}
		
		generateanswer();
	}

	public void generateanswer() { // generate an answer

		Vector<String> answers = new Vector<String>();

		answers = myReasoner.generateAnswer(question);

		displaytext = "<font color=\"red\" face=\"Georgia, Arial\">You: "
				+ answers.get(0) + "</font>"
				+ "<br><font COLOR=\"green\" face=\"Georgia, Arial\">"
				+ answers.get(1) + "</font><br>";

		dialoghistory.add(displaytext); // write the string to show in the
										// dialoghistory

		String dialogdisplay = "";

		for (int i = 0; i < dialoghistory.size(); i++) {
			dialogdisplay = dialogdisplay + dialoghistory.get(i);
		}

		Output.setText(dialogdisplay); // write the dialoghistory to output
		
		
		
		displaytext = ""; // clear the "long string" for the next answer

	}

	public void setmytooltip(String inputstring) {

		Info.setToolTipText(inputstring);
		System.out.println("Done setting the Tooltiptext");

	}

	public void setmyinfobox(String inputstring) {

		try {
			Info.setPage(inputstring);
		} catch (IOException e) {
			Info.setContentType("text/html");
			Info.setText("<html>Could not load" + inputstring);
		}
		System.out.println("Done setting the Tooltiptext");

	}

	public static void main(String[] args) { 
		
		SimpleGUI mygui = new SimpleGUI();
		mygui.checkbrain(); 

	}
}
