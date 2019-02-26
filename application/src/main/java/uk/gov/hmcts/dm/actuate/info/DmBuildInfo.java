package uk.gov.hmcts.dm.actuate.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class DmBuildInfo implements InfoContributor {

    private final String environment;
    private final String project;
    private final String name;
    private final String version;
    private final String build;
    private final String commit;
    private final String date;


    private static final String BUILD_INFO = "META-INF/build-info.properties";
    private static final String UNKNOWN = "unknown";
    private static final String EMPTY = "";

    @Autowired
    public DmBuildInfo(
            @Value("${info.app.name}") String name,
            @Value("${info.app.environment}") String environment,
            @Value("${info.app.project}") String project
    ) throws IOException {
        this(name,environment,project,BUILD_INFO);
    }

    DmBuildInfo(String name, String environment, String project, String versionPath)throws IOException {

        Properties prop = new Properties();
        URL buildInfoUrl = (versionPath == null) ? null : this.getClass().getClassLoader().getResource(versionPath);
        if (buildInfoUrl != null) {
            prop.load(this.getClass().getClassLoader().getResourceAsStream(versionPath));
        }

        this.environment = environment;
        this.project = project;
        this.name = name;
        this.version = prop.getProperty("build.version",UNKNOWN);
        this.build = prop.getProperty("build.number",EMPTY);
        this.commit = prop.getProperty("build.commit",UNKNOWN);
        this.date = prop.getProperty("build.date",UNKNOWN);
    }


    @Override
    public void contribute(Info.Builder builder) {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("environment",environment);
        map.put("project", project);
        map.put("name", name);
        map.put("version", version + (EMPTY.equals(build) ? "" : "-" + build));
        map.put("commit", commit);
        map.put("date", date);
        map.put("extra",new HashMap<>());
        builder.withDetail("buildInfo",map);
    }
}
