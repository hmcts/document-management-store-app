package uk.gov.hmcts.dm.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamingUrlComponent {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "streaming_protocol_type")
    private StreamingProtocolType streamingProtocolType;

    private String streamingUrl;

    @ManyToOne
    @JsonIgnoreProperties("streamingUris")
    private DocumentContentVersion documentContentVersion;

}
