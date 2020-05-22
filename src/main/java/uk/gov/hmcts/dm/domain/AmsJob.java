package uk.gov.hmcts.dm.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
public class AmsJob {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    private String inputAssetName;

    private String outputAssetName;

    private String locatorName;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @OneToOne
    private DocumentContentVersion documentContentVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_state")
    private TaskState taskState;

}
