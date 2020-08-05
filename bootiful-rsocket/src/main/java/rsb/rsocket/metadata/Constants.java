package rsb.rsocket.metadata;

import org.springframework.util.MimeType;

public class Constants {

	public static String CLIENT_ID_HEADER = "client-id";

	public static String LANG_HEADER = "lang";

	public static String CLIENT_NAME_HEADER = "client-name";

	public static String CLIENT_ID_VALUE = "messaging/x.bootiful." + CLIENT_ID_HEADER;

	public static String CLIENT_NAME_VALUE = "messaging/x.bootiful." + CLIENT_NAME_HEADER;

	public static String LANG_VALUE = "messaging/x.bootiful." + LANG_HEADER;

	public static MimeType CLIENT_ID = MimeType.valueOf(CLIENT_ID_VALUE);

	public static MimeType CLIENT_NAME = MimeType.valueOf(CLIENT_NAME_VALUE);

	public static MimeType LANG = MimeType.valueOf(LANG_VALUE);

}
