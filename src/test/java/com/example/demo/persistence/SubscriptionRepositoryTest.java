package com.example.demo.persistence;

import static com.example.demo.model.SubscriptionFixture.hanSolo;
import static com.example.demo.model.SubscriptionFixture.subscription;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;

import com.example.demo.model.Subscription;
import com.example.demo.model.SubscriptionFixture;
import com.example.demo.shared.MongoDBConfiguration;
import com.example.demo.shared.ObjectMapperConfiguration;
import com.example.demo.shared.conf.TestMongoDBConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

@DataMongoTest
@Import({ MongoDBConfiguration.class, ObjectMapperConfiguration.class, TestMongoDBConfiguration.class })
class SubscriptionRepositoryTest {

	private Logger logger = LoggerFactory.getLogger(getClass());
	public final static String[] AUDIT_FIELD_NAMES = { "version", "createdOn", "modifiedOn" };

	@Autowired
	SubscriptionRepository cut;

	@Autowired
	ObjectMapper mapper;

	@Test
	void shouldFindAllCorrectly() throws Exception {
		List<Subscription> all = cut.findAll();
		assertThat(all).hasSize(2);
		logger.info(mapper.writeValueAsString(all));
	}

	@Test
	void sholdFailOnUniqueID() throws Exception {
		// populated from subscriptons.json
		assertThat(cut.findById(hanSolo.id())).isPresent();
		assertThatExceptionOfType(DuplicateKeyException.class).isThrownBy(() -> cut.save(hanSolo));
	}

	@Test
	void shouldFindCorrectly() throws Exception {
		Optional<Subscription> found = cut.findById(SubscriptionFixture.hanSolo.id());

		assertThat(found.get()).usingRecursiveComparison().ignoringFields(AUDIT_FIELD_NAMES)
				.isEqualTo(SubscriptionFixture.hanSolo);
	}

	@Test
	void shouldAuditCorrectly() {
		Subscription subscription = subscription();
		Subscription stored = cut.save(subscription);
		assertThat(stored).usingRecursiveComparison()
				// ignore audit fields
				.ignoringFields(AUDIT_FIELD_NAMES).isEqualTo(subscription);

		assertThat(stored.id()).isNotNull();
		assertThat(stored.version()).isEqualTo(0);
		assertThat(stored.createdOn()).isNotNull();
		// NOTE ! modifiedOn and createdOn should equal
		assertThat(stored.modifiedOn()).isEqualTo(stored.createdOn());

		String emailUpdate = "han.solo.updated@example.com";
		Subscription modified = stored.updateWith(stored.firstName(), stored.lastName(), emailUpdate);

		Subscription updated = cut.save(modified);
		assertThat(updated).usingRecursiveComparison()
				// ignore audit fields and email which was modified
				.ignoringFields(AUDIT_FIELD_NAMES).ignoringFields("email").isEqualTo(stored);

		assertThat(updated.email()).isEqualTo(emailUpdate);
		// createdOn was not modified
		assertThat(updated.createdOn()).isEqualTo(stored.createdOn());
		// incremented by one ?
		assertThat(updated.version()).isEqualTo(stored.version() + 1);
		// NOTE ! modifiedOn should be after
		assertThat(updated.modifiedOn()).isAfter(stored.modifiedOn());
	}

}
