package com.qwazr.utils;

import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class ConfigServiceTest {

    interface MyConfig {

        String getserverName();

        Boolean isProduction();

        URI getWebServiceUri();

        Path getLogPath();

        Integer getMaxConnection();

        Long getMaxSize();

        class Impl extends ConfigService.PropertiesConfig implements MyConfig {

            private final String serverName;
            private final Boolean isProduction;
            private final URI webServiceUri;
            private final Path logPath;
            private final Integer maxConnection;
            private final Long maxSize;

            protected Impl(final Properties properties, final Instant creationTime) {
                super(properties, creationTime);
                serverName = getStringProperty("SERVER_NAME", () -> "localhost");
                isProduction = getBooleanProperty("IS_PRODUCTION", () -> Boolean.TRUE);
                webServiceUri = getUriProperty("WEB_SERVICE", () -> URI.create("http://localhost"));
                logPath = getPathProperty("LOG_PATH", () -> Path.of("/var/log"));
                maxConnection = getIntegerProperty("MAX_CONNECTION", () -> 150);
                maxSize = getLongProperty("MAX_SIZE", () -> 123456789L);
            }

            @Override
            public String getserverName() {
                return serverName;
            }

            @Override
            public Boolean isProduction() {
                return isProduction;
            }

            @Override
            public URI getWebServiceUri() {
                return webServiceUri;
            }

            @Override
            public Path getLogPath() {
                return logPath;
            }

            @Override
            public Integer getMaxConnection() {
                return maxConnection;
            }

            @Override
            public Long getMaxSize() {
                return maxSize;
            }
        }
    }

    @Test
    public void testDefaultConfig() throws IOException {
        final Instant start = Instant.now();
        final Path configPath = Path.of("src", "test", "resources", "test", "doesnotexist.properties");
        final ConfigService<MyConfig.Impl> configService = new ConfigService.FileConfigService<>(configPath, MyConfig.Impl::new);
        final MyConfig myConfig1 = configService.getCurrent();
        final MyConfig.Impl myConfig2 = configService.getCurrent();
        assertThat(myConfig1, equalTo(myConfig2));
        assertThat(myConfig1, instanceOf(MyConfig.Impl.class));
        assertThat(myConfig1, equalTo(myConfig2));
        assertThat(myConfig1.getLogPath(), is(Path.of("/var/log")));
        assertThat(myConfig2.getCreationTime(), greaterThanOrEqualTo(start));
        assertThat(myConfig1.getMaxConnection(), is(150));
        assertThat(myConfig1.getMaxSize(), is(123456789L));
        assertThat(myConfig1.isProduction(), is(Boolean.TRUE));
        assertThat(myConfig1.getWebServiceUri(), is(URI.create("http://localhost")));
        assertThat(myConfig1.getserverName(), is("localhost"));
        assertThat(myConfig1, equalTo(configService.reload()));

    }

    @Test
    public void testFileConfig() throws IOException {
        final Path configPath = Path.of("src", "test", "config.properties");
        final Instant lastModified = Files.getLastModifiedTime(configPath).toInstant();
        final ConfigService<MyConfig.Impl> configService = new ConfigService.FileConfigService<>(configPath, MyConfig.Impl::new);
        final MyConfig myConfig1 = configService.getCurrent();
        final MyConfig.Impl myConfig2 = configService.reload();
        assertThat(myConfig1, equalTo(myConfig2));
        assertThat(myConfig1.getLogPath(), is(Path.of("/var/log/qwazr")));
        assertThat(myConfig2.getCreationTime(), is(lastModified));
        assertThat(myConfig1.getMaxConnection(), is(200));
        assertThat(myConfig1.getMaxSize(), is(10000L));
        assertThat(myConfig1.isProduction(), is(Boolean.FALSE));
        assertThat(myConfig1.getWebServiceUri(), is(URI.create("https://api.qwazr.com")));
        assertThat(myConfig1.getserverName(), is("qwazr.com"));
    }
}
