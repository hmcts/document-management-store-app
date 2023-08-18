package uk.gov.hmcts.dm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Blob;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Make checkstyles happy with a javadoc summary.
 */
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DocumentContent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;

    @JsonIgnore
    @Getter
    @Basic(fetch = FetchType.LAZY)
    private Blob data;

    @OneToOne
    @Getter
    @Setter
    private DocumentContentVersion documentContentVersion;

    @Getter
    @Setter
    @CreatedBy
    private String createdBy;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;


    public Date getCreatedOn() {
        return (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
    }

}
