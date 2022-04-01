/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.server.configuration;

import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.util.SubnetUtils;

public class ServerConfiguration implements ConfigurationProperties {

    private final static Logger LOGGER = LoggerUtils.getLogger(ServerConfiguration.class);

    private final Map<String, String> properties;

    public final Path dataDirectory;
    public final Path tempDirectory;

    public final String publicAddress;
    public final String listenAddress;

    public final WebConnector webAppConnector;
    public final WebConnector webServiceConnector;
    public final WebConnector multicastConnector;

    public final Set<String> masters;
    public final Set<String> groups;

    private ServerConfiguration(final Map<String, String> props) throws IOException {

        // Merge the maps.
        properties = new HashMap<>(props);

        //Set the data directory
        dataDirectory = getDataDirectory(getStringProperty(QWAZR_DATA, null));
        if (dataDirectory == null)
            throw new IOException("The data directory has not been set.");
        if (!Files.exists(dataDirectory))
            throw new IOException("The data directory does not exists: " + dataDirectory.toAbsolutePath());
        if (!Files.isDirectory(dataDirectory))
            throw new IOException("The data directory is not a directory: " + dataDirectory.toAbsolutePath());

        //Set the temp directory
        tempDirectory = getTempDirectory(dataDirectory, getStringProperty(QWAZR_TEMP, null));
        if (!Files.exists(tempDirectory))
            Files.createDirectories(tempDirectory);
        if (!Files.exists(tempDirectory))
            throw new IOException("The temp directory does not exists: " + tempDirectory.toAbsolutePath());
        if (!Files.isDirectory(tempDirectory))
            throw new IOException("The temp directory is not a directory: " + tempDirectory.toAbsolutePath());

        //Set the listen address
        listenAddress = findListenAddress(getStringProperty(LISTEN_ADDR, null));

        //Set the public address
        publicAddress = findPublicAddress(getStringProperty(PUBLIC_ADDR, null), this.listenAddress);

        //Set the connectors
        webAppConnector = new WebConnector(publicAddress, getIntegerProperty(WEBAPP_PORT, null), 9090,
                getStringProperty(WEBAPP_AUTHENTICATION, null), getStringProperty(WEBAPP_REALM, null));
        webServiceConnector = new WebConnector(publicAddress, getIntegerProperty(WEBSERVICE_PORT, null), 9091,
                getStringProperty(WEBSERVICE_AUTHENTICATION, null), getStringProperty(WEBSERVICE_REALM, null));
        multicastConnector =
                new WebConnector(getStringProperty(MULTICAST_ADDR, null), getIntegerProperty(MULTICAST_PORT, null),
                        9091, null, null);

        // Collect the master address.
        final LinkedHashSet<String> set = new LinkedHashSet<>();
        try {
            findMatchingAddress(getStringProperty(QWAZR_MASTERS, null), set);
        } catch (SocketException e) {
            LOGGER.warning("Failed in extracting IP information. No master server is configured.");
        }
        this.masters = set.isEmpty() ? null : Collections.unmodifiableSet(set);

        this.groups = buildSet(getStringProperty(QWAZR_GROUPS, null), ",; \t", true);
    }

    public String getStringProperty(final String propName, final String defaultValue) {
        final Object o = properties.get(propName);
        return o == null ? defaultValue : o.toString();
    }

    public Integer getIntegerProperty(final String propName, final Integer defaultValue) {
        final String value = properties.get(propName);
        if (value == null)
            return defaultValue;
        return Integer.parseInt(value);
    }

    protected static void fillStringListProperty(final String value, final String separatorChars, final boolean trim,
                                                 final Consumer<String> consumer) {
        if (value == null)
            return;
        final String[] parts = StringUtils.split(value, separatorChars);
        for (String part : parts)
            if (part != null)
                consumer.accept(trim ? part.trim() : part);
    }

    protected static Set<String> buildSet(final String value, final String separatorChars, final boolean trim) {
        if (value == null || value.isEmpty())
            return null;
        final HashSet<String> set = new HashSet<>();
        fillStringListProperty(value, separatorChars, trim, set::add);
        return Collections.unmodifiableSet(set);
    }

    private static Path getDataDirectory(final String dataDir) {
        //Set the data directory
        return StringUtils.isEmpty(dataDir) ? Paths.get(System.getProperty("user.dir")) : Paths.get(dataDir);
    }

    private static Path getTempDirectory(final Path dataDir, final String value) {
        return StringUtils.isEmpty(value) ? dataDir.resolve("tmp") : Paths.get(value);
    }

    private static Set<Path> getEtcDirectories(final String value) {
        final Set<Path> set = new LinkedHashSet<>();
        fillStringListProperty(value == null ? "etc" : value, File.pathSeparator, true, part -> {
            // By design relative path are relative to the working directory
            final Path etcPath = Paths.get(part);
            set.add(etcPath);
            LOGGER.info("Configuration (ETC) directory: " + etcPath.toAbsolutePath());
        });
        return Collections.unmodifiableSet(set);
    }

    private static Predicate<Path> buildEtcFileFilter(final String etcFilter) {
        if (StringUtils.isEmpty(etcFilter))
            return path -> Files.isRegularFile(path);
        final String[] array = StringUtils.split(etcFilter, ',');
        if (array == null || array.length == 0)
            return path -> Files.isRegularFile(path);
        return new ConfigurationFileFilter(array);
    }

    public static class WebConnector {

        public final String authentication;
        public final String address;
        public final String realm;
        public final int port;
        public final String addressPort;

        private WebConnector(final String address, final Integer port, final int defaulPort,
                             final String authentication, final String realm) {
            this.address = address;
            this.authentication = authentication;
            this.realm = realm;
            this.port = port == null ? defaulPort : port;
            this.addressPort = this.address == null ? null : this.address + ":" + this.port;
        }

    }

    /**
     * Manage that kind of pattern:
     * 192.168.0.0/16,172.168.0.0/16
     * 192.168.0.0/16
     * 10.3.12.12
     *
     * @param addressPattern a mask or an ip address
     * @param collect        a collection filled with the matching addresses
     * @throws SocketException
     */
    private static void findMatchingAddress(final String addressPattern, final Collection<String> collect)
            throws SocketException {
        final String[] patterns = StringUtils.split(addressPattern, ",; ");
        if (patterns == null)
            return;
        for (String pattern : patterns) {
            if (pattern == null)
                continue;
            pattern = pattern.trim();
            if (!pattern.contains("/")) {
                collect.add(pattern);
                continue;
            }
            final SubnetUtils.SubnetInfo subnet = pattern.contains("/") ? new SubnetUtils(pattern).getInfo() : null;
            final Enumeration<NetworkInterface> enumInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumInterfaces != null && enumInterfaces.hasMoreElements()) {
                final NetworkInterface ifc = enumInterfaces.nextElement();
                if (!ifc.isUp())
                    continue;
                final Enumeration<InetAddress> enumAddresses = ifc.getInetAddresses();
                while (enumAddresses != null && enumAddresses.hasMoreElements()) {
                    final InetAddress inetAddress = enumAddresses.nextElement();
                    if (!(inetAddress instanceof Inet4Address))
                        continue;
                    final String address = inetAddress.getHostAddress();
                    if (subnet != null && subnet.isInRange(address) || address.equals(pattern))
                        collect.add(address);
                }
            }
        }
    }

    private final static String DEFAULT_LISTEN_ADDRESS = "0.0.0.0";

    private static String findListenAddress(final String addressPattern) {
        if (StringUtils.isEmpty(addressPattern))
            return DEFAULT_LISTEN_ADDRESS;
        try {
            final ArrayList<String> list = new ArrayList<>();
            findMatchingAddress(addressPattern, list);
            return list.isEmpty() ? DEFAULT_LISTEN_ADDRESS : list.get(0);
        } catch (SocketException e) {
            LOGGER.log(Level.WARNING, e, () -> "Failed in extracting IP informations. Listen address set to default (" +
                    DEFAULT_LISTEN_ADDRESS + ")");
            return DEFAULT_LISTEN_ADDRESS;
        }
    }

    private final static String DEFAULT_PUBLIC_ADDRESS = "localhost";

    private static String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.log(Level.WARNING, "Cannot extract the address of the localhost.", e);
            return DEFAULT_PUBLIC_ADDRESS;
        }
    }

    private static String findPublicAddress(final String addressPattern, final String listenAddress)
            throws SocketException {
        if (StringUtils.isEmpty(addressPattern))
            return StringUtils.isEmpty(listenAddress) || DEFAULT_LISTEN_ADDRESS.equals(listenAddress) ?
                    getLocalHostAddress() :
                    listenAddress;
        final ArrayList<String> list = new ArrayList<>();
        findMatchingAddress(addressPattern, list);
        if (list.isEmpty())
            throw new SocketException("Failed in finding a matching public IP address. Pattern: " + addressPattern);
        if (list.size() > 1)
            LOGGER.warning(() -> "Several matching IP adresses where found (" + list.size() + ')');
        return list.get(0);
    }

    private static Map<String, String> argsToMapPrefix(final String prefix, final String... args) {
        final HashMap<String, String> props = new HashMap<>();
        if (args == null || args.length == 0)
            return props;
        final Integer prefixLength = prefix == null || prefix.isEmpty() ? null : prefix.length();
        for (String arg : args) {
            if (arg == null || arg.isEmpty())
                continue;
            if (prefixLength != null && !arg.startsWith(prefix))
                continue;
            final String[] split = StringUtils.split(arg, "=");
            final int l = split.length - 1;
            if (l < 1)
                continue;
            final String value = split[l];
            for (int i = 0; i < l; i++) {
                final String key = prefixLength == null || i > 0 ? split[i] : split[i].substring(prefixLength);
                props.put(key, value);
            }
        }
        return props;
    }

    protected static Map<String, String> argsToMap(final String... args) throws IOException {
        final Map<String, String> props = argsToMapPrefix("--", args);

        // Load the QWAZR_PROPERTIES
        String propertyFile = props.get(QWAZR_PROPERTIES);
        if (propertyFile == null)
            propertyFile = System.getProperty(QWAZR_PROPERTIES, System.getenv(QWAZR_PROPERTIES));
        if (propertyFile != null) {
            final Path propFile = Paths.get(propertyFile);
            LOGGER.info(() -> "Load QWAZR_PROPERTIES file: " + propFile.toAbsolutePath());
            final Properties properties = new Properties();
            try (final BufferedReader reader = Files.newBufferedReader(propFile, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            // Priority to program argument, we only put the value if the key is not present
            properties.forEach((key, value) -> props.putIfAbsent(key.toString(), value.toString()));
        }

        return props;
    }

    public static Builder of() {
        return new Builder();
    }

    public static class Builder {

        private final Map<String, String> map;
        private final Set<String> masters;
        private final Set<String> groups;

        protected Builder() {
            this.map = new HashMap<>();
            this.masters = new LinkedHashSet<>();
            this.groups = new LinkedHashSet<>();
        }

        public Builder applyEnvironmentVariables() {
            map.putAll(System.getenv());
            return this;
        }

        public Builder applySystemProperties() {
            System.getProperties().forEach((key, value) -> {
                if (key != null && value != null) map.put(key.toString(), value.toString());
            });
            return this;
        }

        public Builder applyCommandLineArgs(final String... args) throws IOException {
            map.putAll(argsToMap(args));
            return this;
        }

        public Builder data(final Path path) {
            if (path != null)
                map.put(QWAZR_DATA, path.toString());
            return this;
        }

        public Builder temp(final Path path) {
            if (path != null)
                map.put(QWAZR_TEMP, path.toString());
            return this;
        }

        public Builder publicAddress(final String address) {
            if (address != null)
                map.put(PUBLIC_ADDR, address);
            return this;
        }

        public Builder listenAddress(final String address) {
            if (address != null)
                map.put(LISTEN_ADDR, address);
            return this;
        }

        public Builder master(final String... masters) {
            if (masters != null)
                Collections.addAll(this.masters, masters);
            return this;
        }

        public Builder master(final Collection<String> masters) {
            if (masters != null)
                this.masters.addAll(masters);
            return this;
        }

        public Builder group(final String... groups) {
            if (groups != null)
                Collections.addAll(this.groups, groups);
            return this;
        }

        public Builder group(final Collection<String> groups) {
            if (groups != null)
                this.groups.addAll(groups);
            return this;
        }

        public Builder webAppPort(Integer webappPort) {
            if (webappPort != null)
                map.put(WEBAPP_PORT, webappPort.toString());
            return this;
        }

        public Builder webServicePort(Integer webServicePort) {
            if (webServicePort != null)
                map.put(WEBSERVICE_PORT, webServicePort.toString());
            return this;
        }

        public Builder webAppAuthentication(String authentication) {
            if (authentication != null)
                map.put(WEBAPP_AUTHENTICATION, authentication);
            return this;
        }

        public Builder webAppRealm(String webAppRealm) {
            if (webAppRealm != null)
                map.put(WEBAPP_REALM, webAppRealm);
            return this;
        }

        public Builder webServiceAuthentication(String authentication) {
            if (authentication != null)
                map.put(WEBSERVICE_AUTHENTICATION, authentication);
            return this;
        }

        public Builder webServiceRealm(String webServiceRealm) {
            if (webServiceRealm != null)
                map.put(WEBSERVICE_REALM, webServiceRealm);
            return this;
        }

        public Builder multicastAddress(String multicastAddress) {
            if (multicastAddress != null)
                map.put(MULTICAST_ADDR, multicastAddress);
            return this;
        }

        public Builder multicastPort(Integer multicastPort) {
            if (multicastPort != null)
                map.put(MULTICAST_PORT, multicastPort.toString());
            return this;
        }

        private Map<String, String> finalizeMap() {
            if (!masters.isEmpty())
                map.put(QWAZR_MASTERS, StringUtils.join(masters, ','));
            if (!groups.isEmpty())
                map.put(QWAZR_GROUPS, StringUtils.join(groups, ','));
            return map;
        }

        public ServerConfiguration build() throws IOException {
            return new ServerConfiguration(finalizeMap());
        }

    }
}


