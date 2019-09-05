/*
 * Copyright 2019-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.main;

import java.sql.ResultSet;
import java.sql.SQLException;

import app.main.model.Foo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * @author Dave Syer
 */
@Component
public class Runner implements CommandLineRunner, Finder<Foo> {

	private static final String GET_FOO = "SELECT VALUE from FOOS where ID=?";

	private static final String ADD_FOO = "INSERT into FOOS (ID, VALUE) values (?, ?)";

	private final JdbcTemplate entities;

	private final FooMapper mapper = new FooMapper();

	public Runner(JdbcTemplate entities) {
		this.entities = entities;
	}

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "Expected transaction");
		try {
			find(1L);
		}
		catch (EmptyResultDataAccessException e) {
			entities.update(ADD_FOO, 1L, "Hello");
		}
	}

	class FooMapper implements RowMapper<Foo> {

		@Override
		public Foo mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new Foo(rs.getString(1));
		}

	}

	@Override
	public Foo find(long id) {
		return entities.queryForObject(GET_FOO, mapper, id);
	}

}