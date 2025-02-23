package liquibase.ext.cassandra.database;

import com.ing.data.cassandra.jdbc.CassandraConnection;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.structure.core.Index;

import java.sql.Statement;

/**
 * Cassandra 1.2.0 NoSQL database support.
 * Javadocs for ING Cassandra JDBC Wrapper: https://javadoc.io/doc/com.ing.data/cassandra-jdbc-wrapper/latest/index.html
 *
 * Javadocs for DataStax OSS Driver: https://javadoc.io/doc/com.datastax.oss/java-driver-core/latest/index.html
 * Jar file for DataStax OSS Driver: https://search.maven.org/search?q=com.DataStax.oss
 */
public class CassandraDatabase extends AbstractJdbcDatabase {
	public static final String PRODUCT_NAME = "Cassandra";
	public static final String SHORT_PRODUCT_NAME = "cassandra";
	public static final Integer DEFAULT_PORT = 9160;
	public static final String DEFAULT_DRIVER = "com.ing.data.cassandra.jdbc.CassandraDriver";
	private String keyspace;

	@Override
	public String getShortName() {
		return SHORT_PRODUCT_NAME;
	}

	@Override
	public int getPriority() {
		return PRIORITY_DEFAULT;
	}

	@Override
	protected String getDefaultDatabaseProductName() {
		return PRODUCT_NAME;
	}

	@Override
	public Integer getDefaultPort() {
		return DEFAULT_PORT;
	}

	@Override
	public int getDatabaseMinorVersion() throws DatabaseException {
		return 0;
	}

	@Override
	public boolean supportsInitiallyDeferrableColumns() {
		return false;
	}

	@Override
	public boolean supportsSequences() {
		return false;
	}

	@Override
	public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
		String databaseProductName = conn.getDatabaseProductName();
		return PRODUCT_NAME.equalsIgnoreCase(databaseProductName);
	}

	@Override
	public String getDefaultDriver(String url) {
		if (String.valueOf(url).startsWith("jdbc:cassandra:")) {
			return DEFAULT_DRIVER;
		}
		return null;
	}

	@Override
	public boolean supportsTablespaces() {
		return false;
	}

	@Override
	public boolean supportsRestrictForeignKeys() {
		return false;
	}

	@Override
	public boolean supportsDropTableCascadeConstraints() {
		return false;
	}

	@Override
	public boolean isAutoCommit(){
		return true;
	}

	@Override
	public void setAutoCommit(boolean b){
	}

	@Override
	public boolean isCaseSensitive() {
		return true;
	}

	@Override
	public String getCurrentDateTimeFunction() {
		// no alternative in cassandra, using client time
		return String.valueOf(System.currentTimeMillis());
	}

	public String getKeyspace() {
		if (keyspace == null) {
			try {
				keyspace = ((CassandraConnection) (this).getConnection()).getSession().getKeyspace().toString();
			} catch (Exception e) {
				Scope.getCurrentScope().getLog(CassandraDatabase.class)
						.severe("Could not get keyspace from connection", e);

			}
		}
		return keyspace;

	}

	@Override
	public boolean supportsSchemas() {
		return false;
	}

	/**
	 * Cassandra actually doesn't support neither catalogs nor schemas, but keyspaces.
	 * As default liquibase classes don't know what is keyspace we gonna use keyspace instead of catalog
	 */
	@Override
	public String getDefaultCatalogName() {
		return getKeyspace();
	}

	public Statement getStatement() throws DatabaseException {
		return ((JdbcConnection) super.getConnection()).createStatement();
	}

	@Override
	public boolean jdbcCallsCatalogsSchemas() {
		return true;
	}

	@Override
	public boolean supportsNotNullConstraintNames() {
		return false;
	}

	@Override
	public boolean supportsPrimaryKeyNames() {
		return false;
	}

   /**
   * there shouldn't be keyspace name before the index name, queries fail otherwise
   */
	@Override
	public String escapeIndexName(String catalogName, String schemaName, String indexName) {
		return this.escapeObjectName(indexName, Index.class);
	}
}
