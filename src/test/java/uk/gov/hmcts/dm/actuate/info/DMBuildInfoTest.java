package uk.gov.hmcts.dm.actuate.info;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.boot.actuate.info.Info;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;

public class DMBuildInfoTest {

    private static final String BUILD_INFO_WITH_BUILD_NO = "META-INF/build-info-with-build-no.properties";


    @Test
    public void shouldAddBuildInfoToBuilder() throws Exception {
        DMBuildInfo dmBuildInfo = new DMBuildInfo("name","env","project");

        Info.Builder builder = new Info.Builder();
        dmBuildInfo.contribute(builder);

        Info info = builder.build();

        Map<String,Object> map = new HashMap<>();
        Map<String,Object> buildInfo = new HashMap<>();
        buildInfo.put("environment", "env");
        buildInfo.put("project", "project");
        buildInfo.put("name", "name");
        buildInfo.put("version", "unknown");
        buildInfo.put("date", "unknown");
        buildInfo.put("commit", "unknown");
        buildInfo.put("extra", Collections.EMPTY_MAP);

        map.put("buildInfo",buildInfo);

        assertThat(info.getDetails(), CoreMatchers.equalTo(map));
    }

    @Test
    public void shouldAddBuildInfoToBuilderNullBuildInfo() throws Exception {
        DMBuildInfo dmBuildInfo = new DMBuildInfo("name","env","project",null);

        Info.Builder builder = new Info.Builder();
        dmBuildInfo.contribute(builder);

        Info info = builder.build();

        Map<String,Object> map = new HashMap<>();
        Map<String,Object> buildInfo = new HashMap<>();
        buildInfo.put("environment", "env");
        buildInfo.put("project", "project");
        buildInfo.put("name", "name");
        buildInfo.put("version", "unknown");
        buildInfo.put("date", "unknown");
        buildInfo.put("commit", "unknown");
        buildInfo.put("extra", Collections.EMPTY_MAP);

        map.put("buildInfo",buildInfo);

        assertThat(info.getDetails(), CoreMatchers.equalTo(map));
    }

    @Test
    public void shouldAddBuildInfoToBuilderIncludesBuildNumber() throws Exception {
        DMBuildInfo dmBuildInfo = new DMBuildInfo("name","env","project",BUILD_INFO_WITH_BUILD_NO);

        Info.Builder builder = new Info.Builder();
        dmBuildInfo.contribute(builder);

        Info info = builder.build();

        Map<String,Object> map = new HashMap<>();
        Map<String,Object> buildInfo = new HashMap<>();
        buildInfo.put("environment", "env");
        buildInfo.put("project", "project");
        buildInfo.put("name", "name");
        buildInfo.put("version", "1.0-42");
        buildInfo.put("date", "today");
        buildInfo.put("commit", "aaaaaaa");
        buildInfo.put("extra", Collections.EMPTY_MAP);

        map.put("buildInfo",buildInfo);

        assertThat(info.getDetails(), CoreMatchers.equalTo(map));
    }


}
