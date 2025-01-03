package uk.gov.hmcts.dm.actuate.info;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

class DmBuildInfoTest {

    @Test
    void shouldAddBuildInfoToBuilder() {
        DmBuildInfo dmBuildInfo = new DmBuildInfo("name","env","project");

        Info.Builder builder = new Info.Builder();
        dmBuildInfo.contribute(builder);


        Map<String,Object> buildInfo = new HashMap<>();
        buildInfo.put("environment", "env");
        buildInfo.put("project", "project");
        buildInfo.put("name", "name");
        buildInfo.put("version", "unknown");
        buildInfo.put("date", "unknown");
        buildInfo.put("commit", "unknown");
        buildInfo.put("extra", Collections.EMPTY_MAP);

        Map<String,Object> map = new HashMap<>();
        map.put("buildInfo",buildInfo);

        Info info = builder.build();

        assertThat(info.getDetails(), CoreMatchers.equalTo(map));
    }

}
