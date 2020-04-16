package geolocator;

import java.net.URL;

import java.io.IOException;

import com.google.gson.Gson;

import com.google.common.net.UrlEscapers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for obtaining geolocation information about an IP address or host
 * name. The class uses the <a href="http://ip-api.com/">IP-API.com</a>
 * service.
 */
public class GeoLocator {

    private static Logger logger = LoggerFactory.getLogger(GeoLocator.class);

    /**
     * URI of the geolocation service.
     */
    public static final String GEOLOCATOR_SERVICE_URI = "http://ip-api.com/json/";

    private static Gson GSON = new Gson();

    /**
     * Creates a <code>GeoLocator</code> object.
     */
    public GeoLocator() {}

    /**
     * Returns geolocation information about the JVM running the application.
     *
     * @return an object wrapping the geolocation information returned
     * @throws IOException if any I/O error occurs
     */
    public GeoLocation getGeoLocation() throws IOException {
        return getGeoLocation(null);
    }

    /**
     * Returns geolocation information about the IP address or host name
     * specified. If the argument is <code>null</code>, the method returns
     * geolocation information about the JVM running the application.
     *
     * @param ipAddrOrHost the IP address or host name, may be {@code null}
     * @return an object wrapping the geolocation information returned
     * @throws IOException if any I/O error occurs
     * @throws IllegalArgumentException if the IP address or host name is
     * invalid
     */
    public GeoLocation getGeoLocation(String ipAddrOrHost) throws IOException {
        logger.trace("ipAddrOrHost: {}", ipAddrOrHost);
        URL url;
        if (ipAddrOrHost != null) {
            ipAddrOrHost = UrlEscapers.urlPathSegmentEscaper().escape(ipAddrOrHost);
            url = new URL(GEOLOCATOR_SERVICE_URI + ipAddrOrHost);
            logger.info("Querying geolocation information about {}", ipAddrOrHost);
        } else {
            url = new URL(GEOLOCATOR_SERVICE_URI);
            logger.info("Querying geolocation information about the JVM");
        }
        logger.info("Retrieving geolocation data from {}", url);
        String s = IOUtils.toString(url, "UTF-8");
        logger.debug("JSON response: {}", s);
        try {
            JsonObject jsonObject = JsonParser.parseString(s).getAsJsonObject();
            String status = jsonObject.getAsJsonPrimitive("status").getAsString();
            logger.debug("Response status: {}", status);
            if (status.equals("success")) {
                return GSON.fromJson(s, GeoLocation.class);
            } else if (status.equals("fail")) {
                String message = jsonObject.getAsJsonPrimitive("message").getAsString();
                throw new IllegalArgumentException(message);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
        }
        throw new AssertionError("Invalid response");
    }

    public static void main(String[] args) throws IOException {
        try {
            logger.trace("Command line arguments: {}", (Object) args);
            String arg = args.length > 0 ? args[0] : null;
            logger.info("Geolocation: {}", new GeoLocator().getGeoLocation(arg));
        } catch (IOException e) {
            logger.error("Exception caught:", e);
        }
    }

}
