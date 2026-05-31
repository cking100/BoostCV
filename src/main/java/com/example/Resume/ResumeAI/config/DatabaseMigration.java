package com.example.Resume.ResumeAI.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time database migration to convert the 'keywords' and 'missing_keywords'
 * columns from PostgreSQL JSON type to TEXT.
 *
 * Hibernate ddl-auto=update never alters existing column types, so this runs
 * after startup to fix the type mismatch that causes:
 *   "column X is of type json but expression is of type character varying"
 *
 * The ALTER TYPE ... USING expression safely casts existing json data to text.
 * Running it again on an already-text column is a harmless no-op.
 */
@Component
public class DatabaseMigration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrateJsonColumnsToText() {
        alterColumnIfJson("resumes", "keywords");
        alterColumnIfJson("resumes", "missing_keywords");
    }

    private void alterColumnIfJson(String table, String column) {
        try {
            // Check the current column data type
            String currentType = jdbcTemplate.queryForObject(
                "SELECT data_type FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                String.class,
                table, column
            );

            if ("json".equalsIgnoreCase(currentType) || "jsonb".equalsIgnoreCase(currentType)) {
                logger.info("Migrating column {}.{} from {} to TEXT", table, column, currentType);
                jdbcTemplate.execute(
                    String.format("ALTER TABLE %s ALTER COLUMN %s TYPE TEXT USING %s::TEXT", table, column, column)
                );
                logger.info("Successfully migrated {}.{} to TEXT", table, column);
            } else {
                logger.debug("Column {}.{} is already type '{}', no migration needed", table, column, currentType);
            }
        } catch (Exception e) {
            logger.warn("Could not migrate column {}.{}: {}", table, column, e.getMessage());
        }
    }
}
