package prueba.pack.test;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.resource.bundle.locale.LocaleResolver;

import org.apache.log4j.Logger;

public class TestLocaleResolver implements LocaleResolver {
	private static final Logger log = Logger.getLogger(TestLocaleResolver.class);
	public String resolveLocaleCode(HttpServletRequest request) {
		log.info("Dentro LocaleResolver!!!!!");
		if(request.getLocale() != Locale.getDefault())
			return request.getLocale().toString();
		else return null;
	}

}
