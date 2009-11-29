/**
 * 
 */
package net.jawr.web.wicket;

import org.apache.wicket.Application;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.markup.MarkupParserFactory;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.parser.XmlPullParser;

/**
 * Utility class to initialize wicket application for Jawr
 * 
 * @author Ibrahim Chaehoi
 */
public final class JawrWicketApplicationInitializer {

	/**
	 * Initialize the wicket application
	 * 
	 * @param app the aplpication to initialize
	 */
	public static void initApplication(Application app){
		
		// Add the Jawr tag handler to the MarkupParserFactory 
		MarkupParserFactory factory = new MarkupParserFactory(){
			
			public MarkupParser newMarkupParser(final MarkupResourceStream resource)
		    {
		       MarkupParser parser = new MarkupParser(new XmlPullParser(), resource);
		       parser.appendMarkupFilter(new JawrWicketLinkTagHandler());
		       return parser;
		    }
		};
		
		app.getMarkupSettings().setMarkupParserFactory(factory);
		
		// Add the Jawr link resolver
		app.getPageSettings().addComponentResolver(new JawrWicketLinkResolver());
	}
}
