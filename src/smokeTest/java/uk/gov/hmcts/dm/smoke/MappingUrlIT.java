package uk.gov.hmcts.dm.smoke;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags(@WithTag("testType:Smoke"))
public class MappingUrlIT extends BaseIT {


    @Value("${toggle.metadatasearchendpoint}")
    private boolean metadatasearchendpoint;
    @Value("${toggle.folderendpoint}")
    private boolean folderendpoint;
    @Value("${toggle.documentandmetadatauploadendpoint}")
    private boolean documentandmetadatauploadendpoint;
    @Value("${toggle.deleteenabled}")
    private boolean deleteenabled;
    @Value("${toggle.ttl}")
    private boolean ttl;
    @Value("${toggle.thumbnail}")
    private boolean thumbnail;
    @Value("${toggle.testing}")
    private boolean testing;
    private List<String> allEndpoints;

    @Before
    public void setup() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(givenUnauthenticatedRequest().get("/mappings").print());
        allEndpoints = jsonNode.findValues("predicate").stream().map(JsonNode::asText).collect(Collectors.toList());
    }

    @Test
    public void toggle_metadatasearchendpoint_toggle_Mappings() {
        Assert.assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.contains("owned")), metadatasearchendpoint);
        Assert.assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.contains("filter")), metadatasearchendpoint);
    }

    @Test
    public void toggle_folderendpoint_toggle_Mappings() {
        Assert.assertEquals(allEndpoints.stream().anyMatch(endpoint -> endpoint.contains("folders")), folderendpoint);
    }

    @Test
    public void toggle_deleteenabled_toggle_Mappings() {
        Assert.assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.equals("{DELETE [/documents/{documentId}]}")), deleteenabled);
    }

    @Test
    public void toggle_thumbnail_toggle_Mappings() {
        Assert.assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.equals("{GET [/documents/{documentId}/thumbnail]}")), thumbnail);
        Assert.assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.equals("{GET [/documents/{documentId}/versions/{versionId}/thumbnail]}")), thumbnail);
    }

    @Test
    public void toggle_testing_toggle_Testing() {
        Assert.assertEquals(allEndpoints.stream().anyMatch(endpoint ->
            endpoint.equals("{GET [/testing/azure-storage-binary-exists/{id}]}")), testing);

    }

    public boolean getMetadatasearchendpoint() {
        return metadatasearchendpoint;
    }

    public boolean isMetadatasearchendpoint() {
        return metadatasearchendpoint;
    }

    public void setMetadatasearchendpoint(boolean metadatasearchendpoint) {
        this.metadatasearchendpoint = metadatasearchendpoint;
    }

    public boolean getFolderendpoint() {
        return folderendpoint;
    }

    public boolean isFolderendpoint() {
        return folderendpoint;
    }

    public void setFolderendpoint(boolean folderendpoint) {
        this.folderendpoint = folderendpoint;
    }

    public boolean getDocumentandmetadatauploadendpoint() {
        return documentandmetadatauploadendpoint;
    }

    public boolean isDocumentandmetadatauploadendpoint() {
        return documentandmetadatauploadendpoint;
    }

    public void setDocumentandmetadatauploadendpoint(boolean documentandmetadatauploadendpoint) {
        this.documentandmetadatauploadendpoint = documentandmetadatauploadendpoint;
    }

    public boolean getDeleteenabled() {
        return deleteenabled;
    }

    public boolean isDeleteenabled() {
        return deleteenabled;
    }

    public void setDeleteenabled(boolean deleteenabled) {
        this.deleteenabled = deleteenabled;
    }

    public boolean getTtl() {
        return ttl;
    }

    public boolean isTtl() {
        return ttl;
    }

    public void setTtl(boolean ttl) {
        this.ttl = ttl;
    }

    public boolean getThumbnail() {
        return thumbnail;
    }

    public boolean isThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean getTesting() {
        return testing;
    }

    public boolean isTesting() {
        return testing;
    }

    public void setTesting(boolean testing) {
        this.testing = testing;
    }

    public List getAllEndpoints() {
        return allEndpoints;
    }

    public void setAllEndpoints(List allEndpoints) {
        this.allEndpoints = allEndpoints;
    }

}
