package rsb.rsocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.SneakyThrows;

import java.util.Map;

public class EncodingUtils {

	private final ObjectMapper objectMapper;

	private final ObjectReader objectReader;

	private final TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
	};

	public EncodingUtils(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.objectReader = this.objectMapper.readerFor(typeReference);
	}

	@SneakyThrows
	public <T> T decode(String json, Class<T> clazz) {
		return this.objectMapper.readValue(json, clazz);
	}

	@SneakyThrows
	public <T> String encode(T object) {
		return this.objectMapper.writeValueAsString(object);
	}

	@SneakyThrows
	public String encodeMetadata(Map<String, Object> metadata) {
		return this.objectMapper.writeValueAsString(metadata);
	}

	@SneakyThrows
	public Map<String, Object> decodeMetadata(String json) {
		return this.objectReader.readValue(json);
	}

}
