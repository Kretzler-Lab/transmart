import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.DefaultApi20
import org.scribe.exceptions.OAuthException
import org.scribe.extractors.AccessTokenExtractor
import org.scribe.model.*
import org.scribe.oauth.OAuthService
import org.scribe.utils.OAuthEncoder
import org.scribe.utils.Preconditions

@Grab(group='org.scribe', module='scribe', version='1.3.5')

class GrailsOAuth20Api extends DefaultApi20 {
	@Override
	public String getAccessTokenEndpoint() {
		return 'http://localhost:8080/transmart/oauth/token?grant_type=authorization_code&redirect_uri=http://localhost:8080/transmart/oauth/verify'
	}

	@Override
	public String getAuthorizationUrl(OAuthConfig oAuthConfig) {
		return 'http://localhost:8080/transmart/oauth/authorize?response_type=code&client_id=api-client&client_secret=api-client&redirect_uri=http://localhost:8080/transmart/oauth/verify'
	}

	@Override
	public AccessTokenExtractor getAccessTokenExtractor() {
		return new GrailsTokenExtractor()
	}
}

public class GrailsTokenExtractor implements AccessTokenExtractor {
	private static final TOKEN_REGEX = ~/"access_token":\s*"([^"]+)"/
	private static final String EMPTY_SECRET = ''

	/**
	 * {@inheritDoc}
	 */
	public Token extract(String response) {
		Preconditions.checkEmptyString(response, 'Response body is incorrect. Cannot extract a token from an empty string')

		def matcher = TOKEN_REGEX.matcher(response)
		if (matcher.find()) {
			String token = OAuthEncoder.decode(matcher.group(1))
			return new Token(token, EMPTY_SECRET, response)
		}
		else {
			throw new OAuthException('Response body is incorrect. Cannot extract a token from this: "' + response + '"', null)
		}
	}
}
final String PROTECTED_RESOURCE_URL = 'http://localhost:8080/transmart/studies'
final Token EMPTY_TOKEN = new Token('', '')

// If you choose to use a callback, 'oauth_verifier' will be the return value by Twitter (request param)
OAuthService service = new ServiceBuilder()
		.provider(GrailsOAuth20Api.class)
		.apiKey('api-client')
		.apiSecret('api-client')
		.build()
Scanner in2 = new Scanner(System.in)

System.out.println('=== Grails OAuth2 Provider Workflow ===')
System.out.println()

System.out.println('Now go and authorize Scribe here:')
System.out.println(service.getAuthorizationUrl(EMPTY_TOKEN))
System.out.println('And paste the verifier here')
System.out.print('>>')
Verifier verifier = new Verifier(in2.nextLine())
System.out.println()

// Trade the Verifier for the Access Token
System.out.println('Trading the Verifier for an Access Token...')
Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier)
System.out.println('Got the Access Token!')
System.out.println('(if you are curious it looks like this: ' + accessToken + ' )')
System.out.println()

// Now let's go and ask for a protected resource!
System.out.println('Now we are going to access a protected resource... $PROTECTED_RESOURCE_URL')
OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL)
service.signRequest(accessToken, request)
Response response = request.send()
System.out.println('Got it! Lets see what we found...')
System.out.println()
System.out.println(response.getBody())

System.out.println()
System.out.println('Thats it! We are happy now.')
