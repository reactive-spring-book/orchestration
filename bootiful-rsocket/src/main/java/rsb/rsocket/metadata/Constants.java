package rsb.rsocket.metadata;

import org.springframework.util.MimeType;

public class Constants {

	// <1>
	public static final String CLIENT_ID_HEADER = "client-id";

	public static final String CLIENT_ID_VALUE = "messaging/x.bootiful."
			+ CLIENT_ID_HEADER;

	public static final MimeType CLIENT_ID = MimeType.valueOf(CLIENT_ID_VALUE);

	// <2>
	public static final String LANG_HEADER = "lang";

	public static final String LANG_VALUE = "messaging/x.bootiful." + LANG_HEADER;

	public static final MimeType LANG = MimeType.valueOf(LANG_VALUE);

}
