package rsb.orchestration.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.nio.charset.Charset;

@Slf4j
@Component
@Profile("chrome-on-macos")
class BrowserLauncher {

	@EventListener
	public void webServerInitializedEvent(WebServerInitializedEvent event) throws Exception {
		var apps = new File("/Applications");
		var googleChromeAppInstallations = apps.listFiles((dir, name) -> name.contains("Google Chrome.app"));
		Assert.state(apps.exists() && googleChromeAppInstallations != null && googleChromeAppInstallations.length > 0,
				"""
						Disable this class if you're running on some other OS besides macOS and if you
						don't have Google Chrome installed on macOS!
						""");
		var port = event.getWebServer().getPort();
		var url = "http://localhost:" + port + "/";
		log.info("trying to open " + url);
		var exec = Runtime.getRuntime().exec(
				new String[] { "open", "-n", googleChromeAppInstallations[0].getName(), url }, new String[] {}, apps);
		var error = exec.getErrorStream();
		var statusCode = exec.waitFor();
		var errorString = StreamUtils.copyToString(error, Charset.defaultCharset());
		log.info("the status code is " + statusCode + " and the process output is " + errorString);
	}

}
