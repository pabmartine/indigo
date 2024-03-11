package com.martinia.indigo.notification.infrastructure.api.mappers;

import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.domain.model.StatusEnum;
import com.martinia.indigo.notification.infrastructure.api.model.NotificationDto;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NotificationDtoMapperImplTest {

	private final NotificationDtoMapper notificationDtoMapper = new NotificationDtoMapperImpl();

	@Test
	public void testDomain2Dto_NullDomain_ReturnsNull() {
		// Given
		Notification domain = null;

		// When
		NotificationDto dto = notificationDtoMapper.domain2Dto(domain);

		// Then
		assertNull(dto);
	}

	@Test
	public void testDomain2Dto_NonNullDomain_ReturnsDtoWithMappedValues() throws ParseException {
		// Given
		Notification domain = new Notification();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date sendDate = dateFormat.parse("01/01/2022 12:00:00");
		domain.setSendDate(sendDate);
		domain.setId("1");
		domain.setType("KINDLE");
		domain.setUser("user123");
		domain.setBook("book123");
		domain.setStatus("NOT_SEND");
		domain.setError("Error message");
		domain.setReadUser(true);
		domain.setReadAdmin(false);

		// When
		NotificationDto dto = notificationDtoMapper.domain2Dto(domain);

		// Then
		assertEquals(dateFormat.format(domain.getSendDate()), dto.getSendDate());
		assertEquals(domain.getId(), dto.getId());
		assertEquals(NotificationEnum.KINDLE, dto.getType());
		assertEquals(domain.getUser(), dto.getUser());
		assertEquals(domain.getBook(), dto.getBook());
		assertEquals(StatusEnum.NOT_SEND, dto.getStatus());
		assertEquals(domain.getError(), dto.getError());
		assertEquals(domain.isReadUser(), dto.isReadUser());
		assertEquals(domain.isReadAdmin(), dto.isReadAdmin());
	}

	@Test
	public void testDto2Domain_NullDto_ReturnsNull() {
		// Given
		NotificationDto dto = null;

		// When
		Notification domain = notificationDtoMapper.dto2Domain(dto);

		// Then
		assertNull(domain);
	}

	@Test
	public void testDto2Domain_NonNullDto_ReturnsDomainWithMappedValues() throws ParseException {
		// Given
		NotificationDto dto = new NotificationDto();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date sendDate = dateFormat.parse("01/01/2022 12:00:00");
		dto.setSendDate(dateFormat.format(sendDate));
		dto.setId("1");
		dto.setType(NotificationEnum.KINDLE);
		dto.setUser("user123");
		dto.setBook("book123");
		dto.setStatus(StatusEnum.SEND);
		dto.setError("Error message");
		dto.setReadUser(true);
		dto.setReadAdmin(false);

		// When
		Notification domain = notificationDtoMapper.dto2Domain(dto);

		// Then
		assertEquals(dto.getId(), domain.getId());
		assertEquals(dto.getType().name(), domain.getType());
		assertEquals(dto.getUser(), domain.getUser());
		assertEquals(dto.getBook(), domain.getBook());
		assertEquals(dto.getStatus().name(), domain.getStatus());
		assertEquals(dto.getError(), domain.getError());
		assertEquals(dto.isReadUser(), domain.isReadUser());
		assertEquals(dto.isReadAdmin(), domain.isReadAdmin());
	}

	@Test
	public void testDomains2Dtos_NullDomains_ReturnsNull() {
		// Given
		List<Notification> domains = null;

		// When
		List<NotificationDto> dtos = notificationDtoMapper.domains2Dtos(domains);

		// Then
		assertNull(dtos);
	}

	@Test
	public void testDomains2Dtos_NonNullDomains_ReturnsListOfMappedDtos() throws ParseException {
		// Given
		List<Notification> domains = new ArrayList<>();
		Notification domain1 = new Notification();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date sendDate1 = dateFormat.parse("01/01/2022 12:00:00");
		domain1.setSendDate(sendDate1);
		domain1.setId("1");
		domain1.setType("KINDLE");
		domain1.setUser("user123");
		domain1.setBook("book123");
		domain1.setStatus("NOT_SEND");
		domain1.setError("Error message");
		domain1.setReadUser(true);
		domain1.setReadAdmin(false);
		Notification domain2 = new Notification();
		Date sendDate2 = dateFormat.parse("02/01/2022 12:00:00");
		domain2.setSendDate(sendDate2);
		domain2.setId("2");
		domain2.setType("KINDLE");
		domain2.setUser("user456");
		domain2.setBook("book456");
		domain2.setStatus("NOT_SEND");
		domain2.setError(null);
		domain2.setReadUser(false);
		domain2.setReadAdmin(true);
		domains.add(domain1);
		domains.add(domain2);

		// When
		List<NotificationDto> dtos = notificationDtoMapper.domains2Dtos(domains);

		// Then
		assertEquals(domains.size(), dtos.size());
		for (int i = 0; i < domains.size(); i++) {
			assertEquals(dateFormat.format(domains.get(i).getSendDate()), dtos.get(i).getSendDate());
			assertEquals(domains.get(i).getId(), dtos.get(i).getId());
			assertEquals(NotificationEnum.valueOf(domains.get(i).getType()), dtos.get(i).getType());
			assertEquals(domains.get(i).getUser(), dtos.get(i).getUser());
			assertEquals(domains.get(i).getBook(), dtos.get(i).getBook());
			assertEquals(StatusEnum.valueOf(domains.get(i).getStatus()), dtos.get(i).getStatus());
			assertEquals(domains.get(i).getError(), dtos.get(i).getError());
			assertEquals(domains.get(i).isReadUser(), dtos.get(i).isReadUser());
			assertEquals(domains.get(i).isReadAdmin(), dtos.get(i).isReadAdmin());
		}
	}
}
