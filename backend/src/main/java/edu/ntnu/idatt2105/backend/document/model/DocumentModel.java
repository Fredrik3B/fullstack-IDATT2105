package edu.ntnu.idatt2105.backend.document.model;

import edu.ntnu.idatt2105.backend.document.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentModule;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a compliance document attached to an organisation.
 *
 * <p>A document is either a file stored on disk ({@code storagePath} is set) or a
 * link to an external URL ({@code externalUrl} is set). The {@code uploadedAt} timestamp defaults
 * to {@link java.time.LocalDateTime#now()} via a {@link jakarta.persistence.PrePersist} callback if
 * not supplied by the caller.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documents")
public class DocumentModel {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doc_seq")
  @SequenceGenerator(name = "doc_seq", sequenceName = "doc_seq", allocationSize = 20)
  private Long id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private DocumentCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DocumentModule module;

  @Column(name = "external_url", length = 2048)
  private String externalUrl;

  @Column(name = "original_file_name", length = 255)
  private String originalFileName;

  @Column(name = "file_type", length = 100)
  private String fileType;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "storage_path", length = 500)
  private String storagePath;

  @Column(name = "expiry_date")
  private LocalDate expiryDate;

  @Column(name = "uploaded_at", nullable = false)
  private LocalDateTime uploadedAt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "uploaded_by_user_id", nullable = false)
  private UserModel uploadedBy;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id", nullable = false)
  private OrganizationModel organization;

  /**
   * Sets {@code uploadedAt} to the current timestamp before insert if not already set.
   */
  @PrePersist
  public void setDefaults() {
    if (uploadedAt == null) {
      uploadedAt = LocalDateTime.now();
    }
  }
}
