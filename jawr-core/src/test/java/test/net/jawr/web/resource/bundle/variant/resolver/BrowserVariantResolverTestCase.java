/**
 * 
 */
package test.net.jawr.web.resource.bundle.variant.resolver;


import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.variant.resolver.BrowserResolver;
import static org.mockito.BDDMockito.*;
import org.mockito.*;
import static net.jawr.web.resource.bundle.variant.resolver.BrowserResolver.*;

/**
 * @author ibrahim
 */
public class BrowserVariantResolverTestCase extends TestCase {

	private BrowserResolver resolver;

	@Mock
	private HttpServletRequest request;

	public void setUp(){
		resolver = new BrowserResolver();
		MockitoAnnotations.initMocks(this);
	}

	public void testResolveVariantForFirefox(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("Firefox 6");
		String variant = resolver.resolveVariant(request);
		assertEquals("firefox", variant);
	}

	public void testResolveVariantForIE6(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
		String variant = resolver.resolveVariant(request);
		assertEquals("ie6", variant);
	}

	public void testResolveVariantForIE7(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.0)");
		String variant = resolver.resolveVariant(request);
		assertEquals("ie7", variant);
	}

	public void testResolveVariantForIE8(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0)");
		String variant = resolver.resolveVariant(request);
		assertEquals("ie8", variant);
	}

	public void testResolveVariantForIE9(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)");
		String variant = resolver.resolveVariant(request);
		assertEquals("ie9", variant);
	}

	public void testResolveVariantForIE10(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)");
		String variant = resolver.resolveVariant(request);
		assertEquals("ie10", variant);
	}

	public void testResolveVariantForIE11(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
		String variant = resolver.resolveVariant(request);
		assertEquals("other", variant);
	}

	public void testResolveVariantForWebkit(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13\"");
		String variant = resolver.resolveVariant(request);
		assertEquals("webkit", variant);
	}

	public void testResolveVariantForOpera(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("Opera/9.25 (X11; Linux i686; U; fr-ca)");
		String variant = resolver.resolveVariant(request);
		assertEquals("opera", variant);
	}

	public void testResolveVariantForGoogleBot(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("Googlebot/2.X (http://www.googlebot.com/bot.html)");
		String variant = resolver.resolveVariant(request);
		assertEquals("other", variant);
	}

	public void testResolveVariantForUnknownOrFutureBrowsers(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("Foo");
		String variant = resolver.resolveVariant(request);
		assertEquals("other", variant);
	}

	public void testForEmptyUserAgent(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn("");
		String variant = resolver.resolveVariant(request);
		assertEquals("other", variant);
	}

	public void testForBotsAndNPEs(){
		when(request.getHeader(USER_AGENT_HEADER)).thenReturn(null);
		String variant = resolver.resolveVariant(request);
		assertEquals("other", variant);
	}
}

