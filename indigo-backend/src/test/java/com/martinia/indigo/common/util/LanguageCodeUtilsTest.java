package com.martinia.indigo.common.util;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class LanguageCodeUtilsTest {
	@Test
	void normalizesRegionalScriptAndPosixCodesWithoutChangingBareIsoCodes() {
		assertThat(LanguageCodeUtils.normalizeAll(Arrays.asList(" es-ES ", "ES_ar", "en-EN", "en_US.UTF-8",
				"pt-BR", "zh-Hant-TW", "de_DE@euro", "spa", null, "")))
				.containsExactly("es", "en", "pt", "zh", "de", "spa");
		assertThat(LanguageCodeUtils.variants("es-AR")).containsExactlyInAnyOrderElementsOf(LanguageCodeUtils.variants("es"));
		assertThat(LanguageCodeUtils.expand(List.of("en-US", "eng"))).containsExactlyInAnyOrder("en", "eng");
	}
}
