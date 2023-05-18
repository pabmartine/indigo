package com.martinia.indigo.common.configuration.application;

import com.martinia.indigo.common.configuration.domain.service.FindConfigurationByKeyUseCase;
import com.martinia.indigo.domain.model.Configuration;
import com.martinia.indigo.ports.out.mongo.ConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class FindConfigurationByKeyUseCaseImplTest {

	@Resource
	private FindConfigurationByKeyUseCase useCase;
	@MockBean
	private ConfigurationRepository configurationRepository;

	@Test
	public void testFindByKey() {
		// Arrange
		String key = "testKey";
		Configuration configuration = new Configuration();
		when(configurationRepository.findByKey(key)).thenReturn(Optional.of(configuration));

		// Act
		Optional<Configuration> result = useCase.findByKey(key);

		// Assert
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(configuration);
	}
}