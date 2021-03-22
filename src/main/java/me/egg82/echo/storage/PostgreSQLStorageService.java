package me.egg82.echo.storage;

import com.zaxxer.hikari.HikariConfig;
import io.ebean.config.dbplatform.postgres.Postgres9Platform;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class PostgreSQLStorageService extends AbstractJDBCStorageService {
    private PostgreSQLStorageService(@NotNull String name) {
        super(name);
    }

    public static Builder builder(@NotNull String name) { return new Builder(name); }

    public static class Builder {
        private final PostgreSQLStorageService service;
        private final HikariConfig config = new HikariConfig();

        private Builder(@NotNull String name) {
            service = new PostgreSQLStorageService(name);

            // Baseline
            config.setPoolName("ECHO-PostgreSQL");
            config.setDriverClassName("org.postgresql.Driver");
            config.setConnectionTestQuery("SELECT 1;");
            config.addDataSourceProperty("useLegacyDatetimeCode", "false");
            config.addDataSourceProperty("serverTimezone", "UTC");
        }

        public Builder url(@NotNull String address, int port, @NotNull String database) {
            config.setJdbcUrl("jdbc:postgresql://" + address + ":" + port + "/" + database);
            return this;
        }

        public Builder credentials(@NotNull String user, @NotNull String pass) {
            config.setUsername(user);
            config.setPassword(pass);
            return this;
        }

        public Builder options(@NotNull String options) throws IOException {
            options = !options.isEmpty() && options.charAt(0) == '?' ? options.substring(1) : options;
            Properties p = new Properties();
            p.load(new StringReader(options.replace("&", "\n")));
            config.setDataSourceProperties(p);
            return this;
        }

        public Builder poolSize(int min, int max) {
            config.setMaximumPoolSize(max);
            config.setMinimumIdle(min);
            return this;
        }

        public Builder life(long lifetime, long timeout) {
            config.setMaxLifetime(lifetime);
            config.setConnectionTimeout(timeout);
            return this;
        }

        public @NotNull PostgreSQLStorageService build() {
            service.createSource(config, new Postgres9Platform(), false, "postgresql");
            return service;
        }
    }
}
